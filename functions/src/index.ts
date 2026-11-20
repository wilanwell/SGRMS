/**
 * SGRMS Cloud Functions.
 *
 * Account creation and deletion run here (Admin SDK) instead of in the
 * Android app, so that:
 *  - the admin is not signed out when a new account is created,
 *  - only an Admin can create or delete accounts,
 *  - the Firebase Auth account and the Firestore documents are created
 *    and deleted together.
 *
 * The notification trigger sends an FCM push whenever a document is added
 * to the `notifications` collection.
 */

import {setGlobalOptions} from "firebase-functions/v2";
import {CallableRequest, HttpsError, onCall} from "firebase-functions/v2/https";
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import {initializeApp} from "firebase-admin/app";
import {getAuth} from "firebase-admin/auth";
import {
  DocumentReference,
  FieldValue,
  getFirestore,
} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";
import {getStorage} from "firebase-admin/storage";

initializeApp();
setGlobalOptions({maxInstances: 10});

const db = getFirestore();
const auth = getAuth();

const ROLE_ADMIN = "Admin";
const ROLE_TEACHER = "Teacher";
const ROLE_STUDENT = "Student";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 6;

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Ensures the caller is signed in and has the Admin role.
 * @param {CallableRequest} request The callable request.
 * @return {Promise<string>} The admin's uid.
 */
async function requireAdmin(request: CallableRequest): Promise<string> {
  const uid = request.auth?.uid;

  if (!uid) {
    throw new HttpsError("unauthenticated", "Please log in again.");
  }

  const userSnapshot = await db.collection("users").doc(uid).get();

  if (userSnapshot.get("role") !== ROLE_ADMIN) {
    throw new HttpsError(
      "permission-denied",
      "Only an Admin can manage accounts."
    );
  }

  return uid;
}

/**
 * Reads a string field from the request payload.
 * @param {Record<string, unknown>} data The request payload.
 * @param {string} field The field name.
 * @param {boolean} required Whether a blank value is rejected.
 * @return {string} The trimmed value.
 */
function readString(
  data: Record<string, unknown>,
  field: string,
  required = false
): string {
  const value = data[field];
  const text = typeof value === "string" ? value.trim() : "";

  if (required && text.length === 0) {
    throw new HttpsError("invalid-argument", `${field} is required.`);
  }

  return text;
}

/**
 * Validates the email and password used for a new login account.
 * @param {string} email The email address.
 * @param {string} password The password.
 */
function validateCredentials(email: string, password: string): void {
  if (!EMAIL_PATTERN.test(email)) {
    throw new HttpsError("invalid-argument", "Email address is not valid.");
  }

  if (password.length < MIN_PASSWORD_LENGTH) {
    throw new HttpsError(
      "invalid-argument",
      `Password must be at least ${MIN_PASSWORD_LENGTH} characters.`
    );
  }
}

/**
 * Creates a Firebase Auth user and maps Auth errors to readable messages.
 * @param {string} email The email address.
 * @param {string} password The password.
 * @param {string} displayName The display name.
 * @return {Promise<string>} The new uid.
 */
async function createAuthUser(
  email: string,
  password: string,
  displayName: string
): Promise<string> {
  try {
    const userRecord = await auth.createUser({email, password, displayName});
    return userRecord.uid;
  } catch (error) {
    const code = (error as {code?: string}).code ?? "";

    if (code === "auth/email-already-exists") {
      throw new HttpsError(
        "already-exists",
        "This email is already used by another account."
      );
    }

    if (code === "auth/invalid-password") {
      throw new HttpsError("invalid-argument", "Password is not valid.");
    }

    logger.error("createUser failed", error);
    throw new HttpsError("internal", "Failed to create the login account.");
  }
}

/**
 * Deletes a Firebase Auth user created in this call when a later step
 * fails, so no orphan login account is left behind.
 * @param {string} uid The uid to remove.
 */
async function rollbackAuthUser(uid: string): Promise<void> {
  try {
    await auth.deleteUser(uid);
  } catch (error) {
    logger.error("Rollback of auth user failed", {uid, error});
  }
}

/**
 * Deletes every document returned by a query, in batches of 400.
 * @param {FirebaseFirestore.Query} query The query to delete.
 */
async function deleteQuery(query: FirebaseFirestore.Query): Promise<void> {
  let snapshot = await query.limit(400).get();

  while (!snapshot.empty) {
    const batch = db.batch();
    snapshot.docs.forEach((doc) => batch.delete(doc.ref));
    await batch.commit();
    snapshot = await query.limit(400).get();
  }
}

/**
 * Updates every document returned by a query, in batches of 400.
 * @param {FirebaseFirestore.Query} query The query to update.
 * @param {Record<string, unknown>} changes The fields to set.
 */
async function updateQuery(
  query: FirebaseFirestore.Query,
  changes: Record<string, unknown>
): Promise<void> {
  const snapshot = await query.get();
  const docs = snapshot.docs;

  for (let start = 0; start < docs.length; start += 400) {
    const batch = db.batch();
    docs.slice(start, start + 400).forEach((doc) => {
      batch.update(doc.ref, changes);
    });
    await batch.commit();
  }
}

// ---------------------------------------------------------------------------
// Account creation
// ---------------------------------------------------------------------------

export const createTeacherAccount = onCall(async (request) => {
  await requireAdmin(request);

  const data = (request.data ?? {}) as Record<string, unknown>;
  const staffId = readString(data, "staffId", true);
  const teacherName = readString(data, "teacherName", true);
  const email = readString(data, "email", true).toLowerCase();
  const password = typeof data.password === "string" ? data.password : "";
  const phoneNumber = readString(data, "phoneNumber");
  const department = readString(data, "department");
  const position = readString(data, "position") || ROLE_TEACHER;

  validateCredentials(email, password);

  const uid = await createAuthUser(email, password, teacherName);
  const teacherRef = db.collection("teachers").doc();
  const now = Date.now();

  const teacher = {
    teacherId: teacherRef.id,
    staffId,
    teacherName,
    email,
    phoneNumber,
    department,
    position,
    userId: uid,
    profileImageBase64: "",
    createdAt: now,
    updatedAt: now,
  };

  try {
    const batch = db.batch();
    batch.set(teacherRef, teacher);
    batch.set(db.collection("users").doc(uid), {
      userId: uid,
      fullName: teacherName,
      email,
      role: ROLE_TEACHER,
      studentId: "",
      teacherId: teacherRef.id,
      status: "Active",
      createdAt: now,
      updatedAt: now,
    });
    await batch.commit();
  } catch (error) {
    logger.error("Writing teacher documents failed", error);
    await rollbackAuthUser(uid);
    throw new HttpsError("internal", "Failed to save the teacher profile.");
  }

  return teacher;
});

export const createStudentAccount = onCall(async (request) => {
  await requireAdmin(request);

  const data = (request.data ?? {}) as Record<string, unknown>;
  const studentCode = readString(data, "studentCode", true);
  const studentName = readString(data, "studentName", true);
  const email = readString(data, "email", true).toLowerCase();
  const password = typeof data.password === "string" ? data.password : "";
  const phoneNumber = readString(data, "phoneNumber");
  const classId = readString(data, "classId");
  const className = readString(data, "className");
  const gender = readString(data, "gender");
  const status = readString(data, "status") || "Active";

  validateCredentials(email, password);

  const uid = await createAuthUser(email, password, studentName);
  const studentRef = db.collection("students").doc();
  const now = Date.now();

  const student = {
    studentId: studentRef.id,
    studentCode,
    studentName,
    email,
    phoneNumber,
    classId,
    className,
    userId: uid,
    gender,
    status,
    profileImageBase64: "",
    createdAt: now,
    updatedAt: now,
  };

  try {
    const batch = db.batch();
    batch.set(studentRef, student);
    batch.set(db.collection("users").doc(uid), {
      userId: uid,
      fullName: studentName,
      email,
      role: ROLE_STUDENT,
      studentId: studentRef.id,
      teacherId: "",
      status: "Active",
      createdAt: now,
      updatedAt: now,
    });
    await batch.commit();
  } catch (error) {
    logger.error("Writing student documents failed", error);
    await rollbackAuthUser(uid);
    throw new HttpsError("internal", "Failed to save the student profile.");
  }

  return student;
});

// ---------------------------------------------------------------------------
// Account deletion
// ---------------------------------------------------------------------------

/**
 * Deletes an account completely: the Firebase Auth login, the `users`
 * profile, the `teachers` / `students` profile and the records that belong
 * to it. Pass exactly one of `userId`, `teacherId` or `studentId`.
 */
export const deleteUserAccount = onCall(async (request) => {
  const callerUid = await requireAdmin(request);
  const data = (request.data ?? {}) as Record<string, unknown>;

  let userId = readString(data, "userId");
  let teacherId = readString(data, "teacherId");
  let studentId = readString(data, "studentId");

  if (!userId && !teacherId && !studentId) {
    throw new HttpsError(
      "invalid-argument",
      "userId, teacherId or studentId is required."
    );
  }

  // Resolve every id from whichever one was given.
  if (teacherId && !userId) {
    const teacher = await db.collection("teachers").doc(teacherId).get();
    userId = teacher.get("userId") ?? "";
  }

  if (studentId && !userId) {
    const student = await db.collection("students").doc(studentId).get();
    userId = student.get("userId") ?? "";
  }

  if (userId) {
    const user = await db.collection("users").doc(userId).get();
    teacherId = teacherId || (user.get("teacherId") ?? "");
    studentId = studentId || (user.get("studentId") ?? "");
  }

  if (userId === callerUid) {
    throw new HttpsError(
      "failed-precondition",
      "You cannot delete your own account."
    );
  }

  // 1. Login account first, so the person can no longer sign in.
  if (userId) {
    try {
      await auth.deleteUser(userId);
    } catch (error) {
      const code = (error as {code?: string}).code ?? "";
      if (code !== "auth/user-not-found") {
        logger.error("deleteUser failed", {userId, error});
        throw new HttpsError("internal", "Failed to delete login account.");
      }
    }
  }

  // 2. Profile photo in Cloud Storage.
  if (userId) {
    try {
      await getStorage()
        .bucket()
        .file(`profile_images/${userId}.jpg`)
        .delete({ignoreNotFound: true});
    } catch (error) {
      logger.warn("Deleting profile photo failed", {userId, error});
    }
  }

  // 3. Profile documents.
  const refs: DocumentReference[] = [];
  if (userId) refs.push(db.collection("users").doc(userId));
  if (teacherId) refs.push(db.collection("teachers").doc(teacherId));
  if (studentId) {
    refs.push(db.collection("students").doc(studentId));
    refs.push(db.collection("reportRemarks").doc(studentId));
  }

  const batch = db.batch();
  refs.forEach((ref) => batch.delete(ref));
  await batch.commit();

  // 4. Records that belong to the account.
  if (studentId) {
    await deleteQuery(
      db.collection("grades").where("studentId", "==", studentId)
    );
    await deleteQuery(
      db.collection("notifications").where("studentId", "==", studentId)
    );
    await deleteQuery(
      db.collection("reports").where("studentId", "==", studentId)
    );
  }

  if (teacherId) {
    await updateQuery(
      db.collection("subjects").where("teacherId", "==", teacherId),
      {teacherId: "", teacherName: "", updatedAt: Date.now()}
    );
    await updateQuery(
      db.collection("classes").where("classTeacherId", "==", teacherId),
      {classTeacherId: "", classTeacherName: "", updatedAt: Date.now()}
    );
  }

  logger.info("Account deleted", {by: callerUid, userId, teacherId, studentId});

  return {userId, teacherId, studentId};
});

// ---------------------------------------------------------------------------
// Push notifications
// ---------------------------------------------------------------------------

/**
 * Sends an FCM push to the student when a notification document is added.
 */
export const sendNotificationPush = onDocumentCreated(
  "notifications/{notificationId}",
  async (event) => {
    const notification = event.data?.data();
    const studentId = notification?.studentId as string | undefined;

    if (!notification || !studentId) {
      return;
    }

    const studentRef = db.collection("students").doc(studentId);
    const student = await studentRef.get();
    const token = student.get("fcmToken") as string | undefined;

    if (!token) {
      logger.info("Student has no FCM token; push skipped", {studentId});
      return;
    }

    const title = (notification.title as string) || "SGRMS";
    const body =
      (notification.message as string) || "You have a new notification.";

    try {
      await getMessaging().send({
        token,
        notification: {title, body},
        data: {
          title,
          body,
          type: String(notification.type ?? ""),
          notificationId: event.params.notificationId,
          relatedGradeId: String(notification.relatedGradeId ?? ""),
          relatedSubjectId: String(notification.relatedSubjectId ?? ""),
        },
        android: {
          priority: "high",
          notification: {channelId: "sgrms_grade_notifications"},
        },
      });
    } catch (error) {
      const code = (error as {code?: string}).code ?? "";

      // The app was uninstalled or the token expired: forget it.
      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token"
      ) {
        await studentRef.update({fcmToken: FieldValue.delete()});
        return;
      }

      logger.error("FCM send failed", {studentId, error});
    }
  }
);
