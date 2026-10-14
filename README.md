# My Workout Journey

A complete, mobile-first personal home workout tracking Android application built with Kotlin and Jetpack Compose.

## Key Features

1. **User Profile & Safety-First Approach**:
   - Initial profile: Age 19, 182 cm height, 45 kg baseline weight, 65 kg target weight.
   - Non-alarming, clear medical assessment health notice for low baseline BMI (< 16.5) recommending medical assessment before starting an intense regimen.
   - All profile values (weight, goals, equipment, days/week) are editable anytime.

2. **Home Dashboard**:
   - Today's date and scheduled workout.
   - Prominent **Start Workout** button.
   - Exercise preview with target sets and rep ranges.
   - Weight tracking progress toward target.
   - Accurate streak tracking and weekly completion consistency.
   - Rest Day recovery cards for scheduled recovery days (Wednesday, Saturday, Sunday).
   - Last workout summary.

3. **Editable 4-Day Home Workout Plan**:
   - **Monday — Upper Body A**: Normal Push-ups, Pike Push-ups, Diamond Push-ups, School-bag Rows.
   - **Tuesday — Lower Body A**: Bodyweight Squats, Reverse Lunges, Glute Bridges, Calf Raises.
   - **Wednesday — Recovery Day**: Joint mobility, walking, hydration, nourishment.
   - **Thursday — Upper Body B**: Normal Push-ups, Pull-ups, School-bag Rows, Light School-bag Biceps Curls.
   - **Friday — Lower Body B**: Bodyweight Squats, Reverse Lunges, Glute Bridges, Calf Raises, Bird-dog.
   - **Saturday — Recovery Day**: Rest and gentle mobility.
   - **Sunday — Full Rest**: Muscle protein synthesis & total rest.
   - Every exercise target, set count, rep range, and rest timer is fully editable, with 1-tap swaps for safer regression alternatives (e.g., Chair-assisted lunges, incline push-ups, knee diamonds).

4. **Dedicated Active Workout Session**:
   - Focuses on **one exercise at a time** in an easy-to-read mobile viewport.
   - Custom dynamic movement illustration cards showing start and finish postures with auto-animation and full-screen view.
   - Step-by-step instructions, posture, breathing cues, and common mistakes to avoid.
   - Previous session's performance display.
   - Integrated live rest countdown timer with MM:SS countdown, +/-15s adjustments, and completion vibration.
   - Controls: Log Set, Skip Exercise, Pause Workout, Finish Early.

5. **Accurate Set-by-Set Logging Modal**:
   - Records actual reps completed (fewer, target, or more).
   - Independent left and right leg rep tracking for unilateral movements (Reverse Lunges, Bird-dog).
   - Optional difficulty rating (Easy, Moderate, Hard).
   - Exercises are only completed when sets are explicitly logged.

6. **Conservative Progressive Overload Advisor**:
   - Analyzes recent completed sets for each exercise.
   - Suggests a safe +1 rep increase only after target ranges are consistently hit with pain-free form and Easy/Moderate rating.
   - Never rewards training to failure or max-rep testing.
   - Provides options to keep same target, increase slightly, make easier, or reduce workload.

7. **Progress & Analytics**:
   - Native Canvas body weight progress line chart with 65 kg target line.
   - Weekly workout consistency grid.
   - Full workout history breakdown with set-by-set details.
   - Genuine personal records tracker based on recorded data.
   - Edit or delete historical entries with confirmation.

8. **Optional Nutrition Log**:
   - Meal logging (Breakfast, Lunch, Dinner, Snack) with item, quantity, estimated calories, and protein.
   - Clear disclaimer that values are approximate estimates.
   - The workout tracker remains 100% functional without nutrition logging.

9. **Data Storage & Privacy**:
   - Persistent offline storage using Android Room (SQLite).
   - No login, account creation, or paid services required.
   - Export Data and Import Data via standard JSON backup.
   - Clear All Data with confirmation dialog.

## Technical Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material 3)
- **Local Persistence**: Android Room Database with Coroutines and Flow
- **Architecture**: MVVM (Model-View-ViewModel with Repository pattern)
- **Theme**: Dark Navy / Charcoal background (`#0A0F1D`, `#131D31`) with restrained vibrant lime-green accents (`#A3E635`) and sky blue highlights (`#38BDF8`).

## Build & Run

Run the Gradle task to assemble the debug APK:
```bash
gradle assembleDebug
```
To run local tests:
```bash
gradle :app:testDebugUnitTest
```
