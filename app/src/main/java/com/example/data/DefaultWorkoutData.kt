package com.example.data

import com.example.data.model.ExerciseDefinition
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutPlan

object DefaultWorkoutData {
    val defaultProfile = UserProfile(
        id = 1,
        name = "",
        age = 19,
        heightCm = 182f,
        currentWeightKg = 45f,
        targetWeightKg = 65f,
        experience = "Some exercise experience, new to structured sets",
        equipment = "Pull-up bar, chair, bodyweight, school bag",
        daysPerWeek = 4,
        primaryGoal = "Gain weight and muscle at home without a gym",
        updatedAt = System.currentTimeMillis()
    )

    val defaultInitialWeight = WeightLog(
        weightKg = 45f,
        dateEpochMillis = System.currentTimeMillis(),
        notes = "Baseline starting weight"
    )

    val defaultPlans = listOf(
        WorkoutPlan(
            dayOfWeek = 1,
            name = "Upper Body A",
            isRestDay = false,
            description = "Push and horizontal pull focus targeting chest, shoulders, triceps, and upper back.",
            focusArea = "Chest, Shoulders & Back"
        ),
        WorkoutPlan(
            dayOfWeek = 2,
            name = "Lower Body A",
            isRestDay = false,
            description = "Foundational leg strength, balance, glute activation, and ankle stability.",
            focusArea = "Quads, Glutes & Calves"
        ),
        WorkoutPlan(
            dayOfWeek = 3,
            name = "Recovery Day",
            isRestDay = true,
            description = "Active recovery, gentle mobility, joint decompression, and nourishing meals.",
            focusArea = "Mobility & Rest"
        ),
        WorkoutPlan(
            dayOfWeek = 4,
            name = "Upper Body B",
            isRestDay = false,
            description = "Vertical pull emphasis with pull-ups, rows, chest press, and biceps curls.",
            focusArea = "Lats, Arms & Chest"
        ),
        WorkoutPlan(
            dayOfWeek = 5,
            name = "Lower Body B",
            isRestDay = false,
            description = "Lower body endurance, core stability with Bird-dog, and unilateral strength.",
            focusArea = "Full Lower & Core"
        ),
        WorkoutPlan(
            dayOfWeek = 6,
            name = "Recovery Day",
            isRestDay = true,
            description = "Gentle bodyweight movement, light walking, and deep recovery.",
            focusArea = "Active Recovery"
        ),
        WorkoutPlan(
            dayOfWeek = 7,
            name = "Full Rest",
            isRestDay = true,
            description = "Complete rest day to allow muscle protein synthesis, tissue repair, and recharge.",
            focusArea = "Rest & Nourishment"
        )
    )

    val defaultExercises = listOf(
        // Monday - Upper Body A
        ExerciseDefinition(
            id = "mon_pushups",
            dayOfWeek = 1,
            orderIndex = 1,
            name = "Normal Push-ups",
            targetSets = 2,
            targetRepsMin = 8,
            targetRepsMax = 12,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Place hands slightly wider than shoulder-width flat on the floor.\n2. Form a rigid straight line from heels to head.\n3. Lower your chest until elbows bend to 90 degrees or chest nears floor.\n4. Press floor away firmly back to top lockout.",
            postureAndBreathing = "Inhale slowly during controlled lowering. Exhale smoothly as you press up. Keep core tight and avoid sagging hips.",
            commonMistakes = "Flaring elbows straight out at 90°; sagging lower back; craning neck to touch ground early.",
            alternativeName = "Incline Push-ups (Hands on Chair)",
            alternativeDescription = "Hands elevated on a sturdy chair or bench reduces load by ~30%, preserving clean push mechanics without shoulder pain.",
            illustrationKey = "pushups"
        ),
        ExerciseDefinition(
            id = "mon_pike_pushups",
            dayOfWeek = 1,
            orderIndex = 2,
            name = "Pike Push-ups",
            targetSets = 2,
            targetRepsMin = 6,
            targetRepsMax = 10,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Form an inverted 'V' shape with hips pushed high into the air and arms extended.\n2. Gaze toward your feet.\n3. Lower the crown of your head forward in a tripod angle in front of your hands.\n4. Push up and back diagonally into the starting pike position.",
            postureAndBreathing = "Inhale lowering head forward; exhale pressing back up. Keep hips elevated throughout.",
            commonMistakes = "Dropping head straight between hands instead of forward; bending knees too much; rushing reps.",
            alternativeName = "Elevated Hands Pike Push-ups",
            alternativeDescription = "Place hands on an elevated surface like a low step or chair to reduce vertical shoulder load.",
            illustrationKey = "pike_pushups"
        ),
        ExerciseDefinition(
            id = "mon_diamond_pushups",
            dayOfWeek = 1,
            orderIndex = 3,
            name = "Diamond Push-ups",
            targetSets = 2,
            targetRepsMin = 5,
            targetRepsMax = 8,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Bring hands under center of chest with thumbs and index fingers touching in a diamond shape.\n2. Keep elbows tucked close to ribcage.\n3. Lower chest toward the diamond.\n4. Press back up focusing on triceps engagement.",
            postureAndBreathing = "Inhale down, exhale up. Maintain rigid core and glute tension.",
            commonMistakes = "Flaring elbows outward putting stress on wrists; letting hips drop.",
            alternativeName = "Knee Diamond Push-ups",
            alternativeDescription = "Perform with knees on the floor or use a close-grip push-up with hands 6 inches apart for wrist comfort.",
            illustrationKey = "diamond_pushups"
        ),
        ExerciseDefinition(
            id = "mon_schoolbag_rows",
            dayOfWeek = 1,
            orderIndex = 4,
            name = "School-bag Rows",
            targetSets = 2,
            targetRepsMin = 10,
            targetRepsMax = 15,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Pack school bag to a moderate, safe weight (books/clothes).\n2. Stand with feet hip-width, push hips backward with a flat spine tilted at ~45 degrees.\n3. Grip the bag straps with both hands.\n4. Pull the bag up toward your lower ribs, squeezing shoulder blades together.\n5. Lower under 2-second control.",
            postureAndBreathing = "Exhale as you pull and squeeze back; inhale as you lower slowly. Keep lower back firmly locked in neutral.",
            commonMistakes = "Rounding the lower spine; using momentum/jerking torso; shrugging shoulders up.",
            alternativeName = "Single-arm Supported Bag Row",
            alternativeDescription = "Place one hand on a chair for spine support while rowing the bag with the opposite arm.",
            illustrationKey = "rows"
        ),

        // Tuesday - Lower Body A
        ExerciseDefinition(
            id = "tue_squats",
            dayOfWeek = 2,
            orderIndex = 1,
            name = "Bodyweight Squats",
            targetSets = 2,
            targetRepsMin = 12,
            targetRepsMax = 15,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Stand with feet slightly wider than shoulder-width, toes angled slightly out.\n2. Initiate movement by sending hips back as if sitting into an invisible chair.\n3. Lower until thighs are parallel to the floor, keeping chest proud.\n4. Drive through mid-foot and heel to stand tall.",
            postureAndBreathing = "Inhale on descent, exhale standing up. Keep knees tracking in line with your toes.",
            commonMistakes = "Knees caving inwards (valgus); heels lifting off ground; collapsing torso forward.",
            alternativeName = "Chair Box Squats",
            alternativeDescription = "Sit down onto a chair and stand back up without bouncing, building pristine hip mechanics safely.",
            illustrationKey = "squats"
        ),
        ExerciseDefinition(
            id = "tue_reverse_lunges",
            dayOfWeek = 2,
            orderIndex = 2,
            name = "Reverse Lunges",
            targetSets = 2,
            targetRepsMin = 8,
            targetRepsMax = 10,
            restDurationSeconds = 60,
            isUnilateral = true,
            unit = "reps/leg",
            instructions = "1. Stand upright with hands on hips or in front of chest.\n2. Step one leg straight back, landing softly on the ball of the foot.\n3. Lower until front thigh is parallel to floor and rear knee hovers just above ground.\n4. Push off the front foot to return to standing. Alternate or complete all reps.",
            postureAndBreathing = "Inhale as you step back and lower; exhale as you push up to standing. Maintain upright posture.",
            commonMistakes = "Banging rear knee on floor; front knee drifting excessively past toes; leaning forward.",
            alternativeName = "Chair-Assisted Reverse Lunges",
            alternativeDescription = "Hold the back of a sturdy chair for balance to relieve knee shear and stabilize hips.",
            illustrationKey = "lunges"
        ),
        ExerciseDefinition(
            id = "tue_glute_bridges",
            dayOfWeek = 2,
            orderIndex = 3,
            name = "Glute Bridges",
            targetSets = 2,
            targetRepsMin = 12,
            targetRepsMax = 15,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Lie flat on back with knees bent and feet flat on floor, about hip-width apart.\n2. Arms resting flat along your sides.\n3. Drive through your heels to raise your hips until knees, hips, and shoulders align.\n4. Squeeze glutes hard at top for 2 seconds, then lower gently.",
            postureAndBreathing = "Exhale as you bridge hips up, pause at peak contraction; inhale as you lower.",
            commonMistakes = "Arching lower back instead of driving from hips; pushing up onto toes.",
            alternativeName = "Single-Leg Glute Bridge",
            alternativeDescription = "Raise one foot slightly off the floor if standard bridges become too effortless, or place feet on a low cushion.",
            illustrationKey = "glute_bridge"
        ),
        ExerciseDefinition(
            id = "tue_calf_raises",
            dayOfWeek = 2,
            orderIndex = 4,
            name = "Calf Raises",
            targetSets = 2,
            targetRepsMin = 15,
            targetRepsMax = 20,
            restDurationSeconds = 45,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Stand upright with balls of feet on floor or the edge of a sturdy book/board.\n2. Lightly touch a wall for balance.\n3. Press up onto the balls of your feet as high as possible.\n4. Pause for 1 second at the top, then lower heels down under control.",
            postureAndBreathing = "Exhale rising onto toes; inhale lowering slowly over 2 full seconds.",
            commonMistakes = "Bouncing reps quickly using Achilles tendon elasticity; bending knees.",
            alternativeName = "Seated Calf Raises with School-bag",
            alternativeDescription = "Sit on a chair, rest loaded school-bag on knees, and raise heels against resistance.",
            illustrationKey = "calf_raises"
        ),

        // Thursday - Upper Body B
        ExerciseDefinition(
            id = "thu_pushups",
            dayOfWeek = 4,
            orderIndex = 1,
            name = "Normal Push-ups",
            targetSets = 2,
            targetRepsMin = 8,
            targetRepsMax = 12,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Clean push-up tempo: 2 seconds lowering, 1 second pause at bottom, strong press.\n2. Keep your full body locked straight as a plank.\n3. Focus on quality muscle activation rather than speed.",
            postureAndBreathing = "Inhale down, exhale up. Maintain neutral neck looking at floor slightly ahead.",
            commonMistakes = "Dipping head instead of lowering torso; flared elbows.",
            alternativeName = "Hands-Elevated Push-ups",
            alternativeDescription = "Elevate hands 12-18 inches to refine scapular control and chest pump.",
            illustrationKey = "pushups"
        ),
        ExerciseDefinition(
            id = "thu_pullups",
            dayOfWeek = 4,
            orderIndex = 2,
            name = "Pull-ups",
            targetSets = 2,
            targetRepsMin = 3,
            targetRepsMax = 6,
            restDurationSeconds = 90,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Grip pull-up bar with overhand grip slightly wider than shoulders.\n2. Start from a dead hang with active, engaged shoulders.\n3. Pull elbows down and back toward your ribs until chin clears bar.\n4. Lower under strict control back to active hang.",
            postureAndBreathing = "Exhale on the pull, inhale on controlled lowering. Keep body steady without swinging legs.",
            commonMistakes = "Kicking legs or kipping; partial reps; letting shoulders crash into ears at bottom.",
            alternativeName = "Chair-Assisted Pull-ups / Negative Pull-ups",
            alternativeDescription = "Rest one foot lightly on a chair beneath the bar to take off 20-30% of bodyweight, or jump to top and lower slowly over 5 seconds.",
            illustrationKey = "pullups"
        ),
        ExerciseDefinition(
            id = "thu_schoolbag_rows",
            dayOfWeek = 4,
            orderIndex = 3,
            name = "School-bag Rows",
            targetSets = 2,
            targetRepsMin = 10,
            targetRepsMax = 15,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Hinge at hips with straight back, knees soft.\n2. Hold bag handles firmly.\n3. Pull bag into stomach, emphasizing squeezing shoulder blades together.\n4. Return smoothly to full arm extension.",
            postureAndBreathing = "Exhale pulling up, pause, inhale down. Protect lower back with tight abs.",
            commonMistakes = "Jerking with body momentum; rounded thoracic spine.",
            alternativeName = "Doorframe / Towel Rows",
            alternativeDescription = "Anchor a towel around a sturdy door handle or post and lean back, rowing body toward door.",
            illustrationKey = "rows"
        ),
        ExerciseDefinition(
            id = "thu_biceps_curls",
            dayOfWeek = 4,
            orderIndex = 4,
            name = "Light School-bag Biceps Curls",
            targetSets = 2,
            targetRepsMin = 10,
            targetRepsMax = 15,
            restDurationSeconds = 45,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Stand upright holding bag straps with underhand grip (palms up).\n2. Pin elbows firmly against your ribcage.\n3. Curl bag upward by flexing biceps, stopping near upper chest.\n4. Lower slowly under 3-second tension.",
            postureAndBreathing = "Exhale curling up, inhale lowering down. Torso remains completely still.",
            commonMistakes = "Swinging torso to cheat weight; letting elbows flare forward away from body.",
            alternativeName = "Hammer-Grip Bag Curls",
            alternativeDescription = "Grip straps neutral (palms facing each other) for brachialis and forearm comfort.",
            illustrationKey = "curls"
        ),

        // Friday - Lower Body B
        ExerciseDefinition(
            id = "fri_squats",
            dayOfWeek = 5,
            orderIndex = 1,
            name = "Bodyweight Squats",
            targetSets = 2,
            targetRepsMin = 12,
            targetRepsMax = 15,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Solid stance, feet shoulder-width, toes turned 15° outward.\n2. Lower into a smooth deep squat, chest up, arms outstretched for balance.\n3. Stand up forcefully by pressing through midfoot.",
            postureAndBreathing = "Inhale down, exhale up. Drive knees out over pinky toes.",
            commonMistakes = "Heels rising; rounded lower spine.",
            alternativeName = "Tempo Squats (3s down, 2s pause)",
            alternativeDescription = "Slow down cadence with no added weight to deeply train leg fibers safely.",
            illustrationKey = "squats"
        ),
        ExerciseDefinition(
            id = "fri_reverse_lunges",
            dayOfWeek = 5,
            orderIndex = 2,
            name = "Reverse Lunges",
            targetSets = 2,
            targetRepsMin = 8,
            targetRepsMax = 10,
            restDurationSeconds = 60,
            isUnilateral = true,
            unit = "reps/leg",
            instructions = "1. Controlled step back onto the ball of your foot.\n2. Lower until both knees form 90° angles.\n3. Push through front heel to return to standing position.",
            postureAndBreathing = "Inhale down, exhale returning. Keep torso tall.",
            commonMistakes = "Front knee wobbling; leaning sideways.",
            alternativeName = "Static Split Squats",
            alternativeDescription = "Keep feet in staggered lunge stance and simply lower and raise without stepping each rep.",
            illustrationKey = "lunges"
        ),
        ExerciseDefinition(
            id = "fri_glute_bridges",
            dayOfWeek = 5,
            orderIndex = 3,
            name = "Glute Bridges",
            targetSets = 2,
            targetRepsMin = 12,
            targetRepsMax = 15,
            restDurationSeconds = 60,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Lie on back, feet flat and close to hips.\n2. Drive through heels to raise hips into full bridge.\n3. Hold contraction at top for 2 full seconds before lowering.",
            postureAndBreathing = "Exhale up, inhale down. Consciously contract glutes.",
            commonMistakes = "Over-arching lumbar spine.",
            alternativeName = "Frog Pumps / Butterfly Glute Bridge",
            alternativeDescription = "Place soles of feet together with knees spread to target glute medius and prevent back strain.",
            illustrationKey = "glute_bridge"
        ),
        ExerciseDefinition(
            id = "fri_calf_raises",
            dayOfWeek = 5,
            orderIndex = 4,
            name = "Calf Raises",
            targetSets = 2,
            targetRepsMin = 15,
            targetRepsMax = 20,
            restDurationSeconds = 45,
            isUnilateral = false,
            unit = "reps",
            instructions = "1. Stand on edge of step with heels hanging slightly.\n2. Lower heels for a gentle calf stretch.\n3. Rise onto toes into a peak calf squeeze for 1 second.",
            postureAndBreathing = "Exhale rising, inhale lowering slowly.",
            commonMistakes = "Rushing repetitions.",
            alternativeName = "Single-Leg Calf Raises",
            alternativeDescription = "Perform one leg at a time while holding wall for increased isolated challenge.",
            illustrationKey = "calf_raises"
        ),
        ExerciseDefinition(
            id = "fri_bird_dog",
            dayOfWeek = 5,
            orderIndex = 5,
            name = "Bird-dog",
            targetSets = 2,
            targetRepsMin = 8,
            targetRepsMax = 8,
            restDurationSeconds = 45,
            isUnilateral = true,
            unit = "reps/side",
            instructions = "1. Begin on hands and knees with wrists under shoulders and knees under hips.\n2. Brace core as if anticipating a light punch.\n3. Simultaneously reach your right arm straight forward and left leg straight back.\n4. Hold for 2 seconds with limbs parallel to floor.\n5. Return smoothly and repeat on opposite side.",
            postureAndBreathing = "Inhale preparing, exhale reaching out with braced core. Breathe steadily throughout.",
            commonMistakes = "Arching lower back; tilting pelvis to one side; hyperextending neck.",
            alternativeName = "Bird-dog Arms Only / Legs Only",
            alternativeDescription = "Move only the arm or only the leg until rotational core stability is mastered.",
            illustrationKey = "bird_dog"
        )
    )
}
