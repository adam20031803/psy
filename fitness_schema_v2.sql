-- Fitness Module v2 - Workout Plan table
-- Run this in your MySQL database (phpMyAdmin or MySQL CLI)

USE psy;

-- workout_plan: Coach assigns workouts to clients for specific days of the week
CREATE TABLE IF NOT EXISTS workout_plan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT 'The client this plan is for',
    workout_id INT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL COMMENT 'Monday, Tuesday, etc.',
    coach_id INT NOT NULL COMMENT 'The coach who assigned this',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workout(id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_workout_day (user_id, workout_id, day_of_week)
);

-- Add notes column to workout_progress if not exists
ALTER TABLE workout_progress ADD COLUMN IF NOT EXISTS notes TEXT AFTER streak_count;
ALTER TABLE workout_progress ADD COLUMN IF NOT EXISTS rating INT DEFAULT 0 COMMENT '0=None, 1=Dislike, 2=Like' AFTER notes;

-- Indexes
CREATE INDEX idx_plan_user ON workout_plan(user_id);
CREATE INDEX idx_plan_coach ON workout_plan(coach_id);
CREATE INDEX idx_plan_day ON workout_plan(day_of_week);
