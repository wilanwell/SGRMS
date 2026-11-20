// Firestore Security Rules tests for SGRMS.
// Run from this folder:  npm test
// (starts the Firestore emulator with a demo project; no real data is touched)

import {readFileSync} from "node:fs";
import {after, before, beforeEach, describe, test} from "node:test";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  query,
  setDoc,
  updateDoc,
  where,
} from "firebase/firestore";

let env;

const ADMIN = "uid-admin";
const TEACHER = "uid-teacher";
const STUDENT = "uid-student";
const OTHER_STUDENT = "uid-student-2";

before(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-sgrms",
    firestore: {
      rules: readFileSync(new URL("../firestore.rules", import.meta.url), "utf8"),
    },
  });
});

after(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();

  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const now = Date.now();

    await setDoc(doc(db, "users", ADMIN), {
      userId: ADMIN, fullName: "Admin", role: "Admin",
      studentId: "", teacherId: "", email: "admin@sgrms.test",
    });
    await setDoc(doc(db, "users", TEACHER), {
      userId: TEACHER, fullName: "Cikgu", role: "Teacher",
      studentId: "", teacherId: "t1", email: "teacher@sgrms.test",
    });
    await setDoc(doc(db, "users", STUDENT), {
      userId: STUDENT, fullName: "Ali", role: "Student",
      studentId: "s1", teacherId: "", email: "ali@sgrms.test",
    });
    await setDoc(doc(db, "users", OTHER_STUDENT), {
      userId: OTHER_STUDENT, fullName: "Abu", role: "Student",
      studentId: "s2", teacherId: "", email: "abu@sgrms.test",
    });

    await setDoc(doc(db, "teachers", "t1"), {
      teacherId: "t1", userId: TEACHER, teacherName: "Cikgu", phoneNumber: "",
    });
    await setDoc(doc(db, "students", "s1"), {
      studentId: "s1", userId: STUDENT, studentName: "Ali", phoneNumber: "",
      classId: "c1",
    });
    await setDoc(doc(db, "students", "s2"), {
      studentId: "s2", userId: OTHER_STUDENT, studentName: "Abu",
      phoneNumber: "", classId: "c1",
    });

    await setDoc(doc(db, "classes", "c1"), {classId: "c1", className: "5A"});
    await setDoc(doc(db, "subjects", "sub1"), {subjectId: "sub1", teacherId: "t1"});

    await setDoc(doc(db, "grades", "g1"), {gradeId: "g1", studentId: "s1", marks: 70});
    await setDoc(doc(db, "grades", "g2"), {gradeId: "g2", studentId: "s2", marks: 90});

    await setDoc(doc(db, "notifications", "n1"), {
      notificationId: "n1", studentId: "s1", title: "Grade", isRead: false,
      read: false, createdAt: now,
    });
    await setDoc(doc(db, "notifications", "n2"), {
      notificationId: "n2", studentId: "s2", title: "Grade", isRead: false,
      read: false, createdAt: now,
    });

    await setDoc(doc(db, "reportRemarks", "s1"), {studentId: "s1", remarks: "Bagus"});
    await setDoc(doc(db, "reportRemarks", "s2"), {studentId: "s2", remarks: "Baik"});
  });
});

const as = (uid) => env.authenticatedContext(uid).firestore();
const anon = () => env.unauthenticatedContext().firestore();

// ---------------------------------------------------------------------------

describe("not signed in", () => {
  test("cannot read anything", async () => {
    await assertFails(getDoc(doc(anon(), "users", ADMIN)));
    await assertFails(getDoc(doc(anon(), "subjects", "sub1")));
    await assertFails(getDoc(doc(anon(), "grades", "g1")));
  });

  test("signed in without a users profile cannot read staff data", async () => {
    const db = as("uid-stranger");
    await assertFails(getDoc(doc(db, "students", "s1")));
    await assertFails(getDoc(doc(db, "grades", "g1")));
    await assertFails(setDoc(doc(db, "users", "uid-stranger"), {role: "Admin"}));
  });
});

describe("users", () => {
  test("nobody can create a users profile from the app", async () => {
    await assertFails(setDoc(doc(as(ADMIN), "users", "new"), {role: "Student"}));
  });

  test("a student cannot make themselves Admin", async () => {
    await assertFails(updateDoc(doc(as(STUDENT), "users", STUDENT), {role: "Admin"}));
  });

  test("a student cannot relink their studentId", async () => {
    await assertFails(updateDoc(doc(as(STUDENT), "users", STUDENT), {studentId: "s2"}));
  });

  test("a user can edit their own personal fields", async () => {
    await assertSucceeds(updateDoc(doc(as(STUDENT), "users", STUDENT), {
      fullName: "Ali bin Abu", phoneNumber: "0123", updatedAt: 1,
    }));
  });

  test("a student cannot read another user's profile", async () => {
    await assertFails(getDoc(doc(as(STUDENT), "users", OTHER_STUDENT)));
  });

  test("staff can list users", async () => {
    await assertSucceeds(getDocs(collection(as(ADMIN), "users")));
    await assertSucceeds(getDocs(collection(as(TEACHER), "users")));
  });

  test("admin can edit a profile but not change its role", async () => {
    await assertSucceeds(updateDoc(doc(as(ADMIN), "users", TEACHER), {fullName: "X"}));
    await assertFails(updateDoc(doc(as(ADMIN), "users", TEACHER), {role: "Admin"}));
  });

  test("a teacher can sync a student's name/email but not a teacher's", async () => {
    await assertSucceeds(updateDoc(doc(as(TEACHER), "users", STUDENT), {
      fullName: "Ali", email: "ali2@sgrms.test", updatedAt: 1,
    }));
    await assertFails(updateDoc(doc(as(TEACHER), "users", ADMIN), {fullName: "X"}));
    await assertFails(updateDoc(doc(as(TEACHER), "users", STUDENT), {role: "Teacher"}));
  });

  test("nobody can delete a users profile from the app", async () => {
    await assertFails(deleteDoc(doc(as(ADMIN), "users", STUDENT)));
  });
});

describe("teachers", () => {
  test("a teacher can edit their own profile fields", async () => {
    await assertSucceeds(updateDoc(doc(as(TEACHER), "teachers", "t1"), {
      phoneNumber: "0123", qualification: "Ijazah", updatedAt: 1,
    }));
  });

  test("a teacher cannot change their own userId", async () => {
    await assertFails(updateDoc(doc(as(TEACHER), "teachers", "t1"), {userId: "x"}));
  });

  test("a student cannot edit a teacher", async () => {
    await assertFails(updateDoc(doc(as(STUDENT), "teachers", "t1"), {phoneNumber: "1"}));
  });

  test("teacher documents can only be created by Cloud Functions", async () => {
    await assertFails(setDoc(doc(as(ADMIN), "teachers", "t9"), {teacherId: "t9"}));
  });
});

describe("students", () => {
  test("a student reads only their own student record", async () => {
    await assertSucceeds(getDoc(doc(as(STUDENT), "students", "s1")));
    await assertFails(getDoc(doc(as(STUDENT), "students", "s2")));
    await assertFails(getDocs(collection(as(STUDENT), "students")));
  });

  test("a student can edit personal fields and save an FCM token", async () => {
    await assertSucceeds(updateDoc(doc(as(STUDENT), "students", "s1"), {
      phoneNumber: "0123", guardianName: "Abu", fcmToken: "tok", updatedAt: 1,
    }));
  });

  test("a student cannot move themselves to another class", async () => {
    await assertFails(updateDoc(doc(as(STUDENT), "students", "s1"), {classId: "c2"}));
  });

  test("staff can edit a student but not change its userId", async () => {
    await assertSucceeds(updateDoc(doc(as(TEACHER), "students", "s1"), {classId: "c2"}));
    await assertFails(updateDoc(doc(as(TEACHER), "students", "s1"), {userId: "x"}));
  });

  test("merge-set by staff (updateStudent) is allowed", async () => {
    await assertSucceeds(setDoc(doc(as(ADMIN), "students", "s1"), {
      studentId: "s1", userId: STUDENT, studentName: "Ali B",
    }, {merge: true}));
  });

  test("students cannot be created or deleted from the app", async () => {
    await assertFails(setDoc(doc(as(ADMIN), "students", "s9"), {studentId: "s9"}));
    await assertFails(deleteDoc(doc(as(ADMIN), "students", "s1")));
  });
});

describe("classes and subjects", () => {
  test("everyone signed in can read", async () => {
    await assertSucceeds(getDocs(collection(as(STUDENT), "classes")));
    await assertSucceeds(getDocs(collection(as(STUDENT), "subjects")));
  });

  test("only admin writes classes", async () => {
    await assertSucceeds(setDoc(doc(as(ADMIN), "classes", "c2"), {className: "5B"}));
    await assertFails(setDoc(doc(as(TEACHER), "classes", "c3"), {className: "5C"}));
  });

  test("staff write subjects, students cannot", async () => {
    await assertSucceeds(setDoc(doc(as(TEACHER), "subjects", "sub2"), {subjectName: "BM"}));
    await assertFails(setDoc(doc(as(STUDENT), "subjects", "sub3"), {subjectName: "BI"}));
  });
});

describe("grades", () => {
  test("a student can query only their own grades", async () => {
    const db = as(STUDENT);
    await assertSucceeds(getDocs(query(collection(db, "grades"), where("studentId", "==", "s1"))));
    await assertFails(getDocs(query(collection(db, "grades"), where("studentId", "==", "s2"))));
    await assertFails(getDocs(collection(db, "grades")));
    await assertFails(getDoc(doc(db, "grades", "g2")));
  });

  test("a student cannot change their own marks", async () => {
    await assertFails(updateDoc(doc(as(STUDENT), "grades", "g1"), {marks: 100}));
  });

  test("a teacher can add and edit grades", async () => {
    await assertSucceeds(setDoc(doc(as(TEACHER), "grades", "g3"), {studentId: "s1", marks: 50}));
    await assertSucceeds(updateDoc(doc(as(TEACHER), "grades", "g1"), {marks: 75}));
  });
});

describe("notifications", () => {
  test("a student queries only their own notifications", async () => {
    const db = as(STUDENT);
    await assertSucceeds(getDocs(query(collection(db, "notifications"), where("studentId", "==", "s1"))));
    await assertFails(getDocs(query(collection(db, "notifications"), where("studentId", "==", "s2"))));
  });

  test("a student can only mark their own notification as read", async () => {
    await assertSucceeds(updateDoc(doc(as(STUDENT), "notifications", "n1"), {isRead: true, read: true}));
    await assertFails(updateDoc(doc(as(STUDENT), "notifications", "n1"), {title: "Hacked"}));
    await assertFails(updateDoc(doc(as(STUDENT), "notifications", "n2"), {isRead: true}));
  });

  test("only staff create notifications", async () => {
    await assertSucceeds(setDoc(doc(as(TEACHER), "notifications", "n3"), {studentId: "s1"}));
    await assertFails(setDoc(doc(as(STUDENT), "notifications", "n4"), {studentId: "s2"}));
  });

  test("a student can delete only their own notification", async () => {
    await assertSucceeds(deleteDoc(doc(as(STUDENT), "notifications", "n1")));
    await assertFails(deleteDoc(doc(as(STUDENT), "notifications", "n2")));
  });
});

describe("report remarks", () => {
  test("a student reads only their own remarks", async () => {
    await assertSucceeds(getDoc(doc(as(STUDENT), "reportRemarks", "s1")));
    await assertFails(getDoc(doc(as(STUDENT), "reportRemarks", "s2")));
  });

  test("only staff write remarks", async () => {
    await assertSucceeds(setDoc(doc(as(TEACHER), "reportRemarks", "s1"), {remarks: "Cemerlang"}));
    await assertFails(setDoc(doc(as(STUDENT), "reportRemarks", "s1"), {remarks: "Cemerlang"}));
  });
});

describe("unknown collections", () => {
  test("are denied by default", async () => {
    await assertFails(setDoc(doc(as(ADMIN), "anything", "x"), {a: 1}));
    await assertFails(getDoc(doc(as(ADMIN), "anything", "x")));
  });
});
