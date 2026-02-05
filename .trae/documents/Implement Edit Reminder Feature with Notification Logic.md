I will implement the "Edit Reminder" feature with the following steps:

1. **Navigation Update (`NavHost.kt`)**:

   * Modify `Screen.CreateReminder` to support an optional `reminderId` argument (`create_reminder?reminderId={reminderId}`).

   * Update `AppNavHost` to pass this argument to `CreateReminderScreen`.

2. **Data Layer Update (`MockDataStore.kt`)**:

   * Add an `updateReminder` function to `MockDataStore`.

   * This function will:

     * Update the reminder details (title, time, etc.).

     * Update `updatedAt` timestamp.

     * Set `isRead` to `false` for all participants (ensuring they see the notification).

3. **UI Model Update (`HomeScreen.kt`)**:

   * Update `MockEvent` data class to include `isModified` (derived from `updatedAt > createdAt`).

   * Update `toMockEvent` mapper to populate this field.

   * Modify `EventCardContent` to show a **Yellow Dot** if the item is unread AND modified, and keep the **Red Dot** for new unread items.

4. **Edit Screen Implementation (`CreateReminderScreen.kt`)**:

   * Update `CreateReminderScreen` to accept `reminderId`.

   * If `reminderId` is present, load the reminder data into the form fields.

   * Modify the "Complete" button logic:

     * If editing, show a **Confirmation Dialog**: "Saving will change the reminder for everyone."

     * On confirmation, call `MockDataStore.updateReminder`.

5. **Detail Screen Entry Point (`ReminderDetailScreen.kt`)**:

   * Add an **Edit Button** in the `DetailBottomBar` (or TopBar) that is only visible if `isCreator` is true.

   * The button will navigate to `CreateReminderScreen` with the current `reminderId`.

