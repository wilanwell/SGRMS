# SGRMS Cloud Functions (optional, Blaze plan only)

SGRMS runs on the free **Spark** plan without these functions:

| Feature | On Spark (current) | With these functions (Blaze) |
|---|---|---|
| Create accounts | `AccountService` on a second `FirebaseApp` | `createTeacherAccount`, `createStudentAccount` |
| Delete accounts | Profile + records deleted; Auth login stays but has no access | `deleteUserAccount` also deletes the Auth login |
| Phone notifications | `NotificationCheckWorker`, about every 15 minutes | `sendNotificationPush`, event-driven FCM push |

The code is kept so the project can switch later. To use it:

1. Upgrade the Firebase project to Blaze.
2. Add the `functions` block back to `firebase.json`.
3. Point `TeacherFirebaseService`, `StudentFirebaseService` and `UserFirebaseRepository` back at a callable-functions client.
4. Run `firebase deploy --only functions`.
