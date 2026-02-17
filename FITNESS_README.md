# Fitness Module - ATOMIC HABITS

## Overview
The fitness module is part of the ATOMIC YOU psychological wellness platform. It implements a habit-tracking system based on the principles of Atomic Habits by James Clear, focusing on small daily improvements to build consistency.

## Features
- **Workout Management**: Create, edit, and delete workout templates
- **Habit Tracking**: Mark workouts as completed to build streaks
- **Progress Visualization**: 
  - Current streak counter (consecutive days with workout completion)
  - Weekly completion percentage
  - Progress bar visualization
- **Dashboard**: Clean, modern interface with workout cards
- **Database Integration**: MySQL backend with proper relationships

## Database Setup

### 1. Run the Schema
Execute the `fitness_schema.sql` file in your MySQL database:

```sql
-- In phpMyAdmin or MySQL CLI
SOURCE c:/pys/psy/fitness_schema.sql;
```

This will create:
- `workout` table - stores workout templates
- `workout_progress` table - tracks user completions and streaks
- Sample workout data for testing

### 2. Verify Tables
```sql
USE psy;
SHOW TABLES LIKE 'workout%';
```

## Module Structure

### Controllers
- `FitnessDashboardController.java` - Main dashboard with streak tracking
- `WorkoutController.java` - CRUD operations for workout management

### Models
- `Workout.java` - Workout template entity
- `WorkoutProgress.java` - Progress tracking entity

### DAOs
- `WorkoutDAO.java` - Workout data access operations
- `WorkoutProgressDAO.java` - Progress tracking operations

### UI Files
- `fitness_dashboard.fxml` - Main dashboard interface
- `workout_crud.fxml` - Workout management interface
- `styles.css` - Fitness module styling

## Key Functionality

### Streak Calculation
The system automatically calculates streaks based on consecutive days of workout completion:
- If you complete a workout today and had a completion yesterday, streak increases
- If you miss a day, streak resets to 1
- Each workout completion is tracked separately per user per day

### Weekly Progress
- Counts distinct days in the current week (Monday-Sunday) with at least one workout
- Displays as percentage of 7 days
- Visual progress bar updates in real-time

### Navigation
- Dashboard → Workout Management (and back)
- Integrated with main application navigation
- Consistent styling with rest of the platform

## Usage Instructions

### For Users
1. Log into the application
2. Navigate to Fitness module
3. View your current streak and weekly progress
4. Browse available workouts
5. Click "✓ Mark done today" on any workout to complete it
6. View updated statistics immediately

### For Administrators
1. Use "MANAGE WORKOUTS" button to access CRUD interface
2. Add new workout templates with:
   - Title and description
   - Category (strength, cardio, mobility, etc.)
   - Duration in minutes
   - Difficulty level (easy, medium, hard)
3. Edit or delete existing workouts
4. Return to dashboard to see changes

## Technical Notes

### Package Structure
```
org.example.controller.fitness
org.example.model.fitness
org.example.dao.fitness
```

### Dependencies
- JavaFX for UI components
- MySQL for data persistence
- Standard Java 17+ features

### Error Handling
- Database connection errors show user-friendly messages
- Form validation prevents invalid data entry
- Graceful handling of missing data

## Troubleshooting

### Common Issues
1. **"Fitness data error" message**: Run `fitness_schema.sql` to create tables
2. **Empty workout list**: Check if sample data was inserted
3. **Streak not updating**: Ensure user is logged in and date is correct

### Development Notes
- All timestamps use LocalDateTime for consistency
- Foreign key constraints ensure data integrity
- Prepared statements prevent SQL injection
- Proper session management for user context

## Future Enhancements
- Workout categories filtering
- Personal best tracking
- Social features (friends, challenges)
- Mobile app integration
- Advanced analytics and insights