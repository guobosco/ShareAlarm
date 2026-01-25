## Issue Analysis

The current implementation has two main issues:

1. **Date selection doesn't switch to time tab**: After selecting a date, the dialog remains on the date tab instead of automatically switching to the time selection tab.

2. **No proper time limit enforcement**: While there's some validation when confirming the selection, the pickers themselves don't properly limit selection to times after the current date and time.

## Solution Plan

### 1. Auto-switch to time tab after date selection

**Changes needed:**
- Modify the `TimePickerDialog` composable to pass the `selectedTabIndex` state to the `CalendarTab` composable
- Update the `CalendarTab` composable signature to accept a `selectedTabIndex` state parameter
- Modify the `onDateSelected` lambda in `CalendarTab` to switch the tab index to 1 (time tab) after date selection

### 2. Enforce time limit for current date/time

**Changes needed:**
- Ensure the `minDate` parameter is consistently set to the current date and time
- In `CalendarTab`, make sure dates before `minDate` are properly disabled
- In `ClockTab`, add validation to ensure the selected time doesn't result in a datetime before `minDate`
- Update the time selection logic to respect the minimum datetime constraint

## Expected Behavior

After implementing these changes:
1. When a user selects a date, the dialog will automatically switch to the time selection tab
2. Both date and time pickers will only allow selection of times after the current date and time
3. The validation at confirmation will continue to ensure the selected time is within valid bounds

## Implementation Steps

1. Update `CalendarTab` composable signature to accept `selectedTabIndex` state
2. Modify `TimePickerDialog` to pass `selectedTabIndex` to `CalendarTab`
3. Update `onDateSelected` lambda in `CalendarTab` to switch to time tab
4. Review and enhance time limit enforcement in both pickers
5. Test the implementation to ensure it works as expected