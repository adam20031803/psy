-- Fitness Module Database Schema
-- Run this in your MySQL database (phpMyAdmin or MySQL CLI)

USE psy;

-- Create workout table
CREATE TABLE IF NOT EXISTS workout (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    duration_minutes INT NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create workout_progress table for habit tracking
CREATE TABLE IF NOT EXISTS workout_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    workout_id INT NOT NULL,
    completed_date DATE NOT NULL,
    streak_count INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workout(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_workout_date (user_id, workout_id, completed_date)
);

-- Insert sample workouts
INSERT INTO workout (title, description, category, duration_minutes, difficulty_level) VALUES
('Morning Stretch Routine', 'Gentle stretching to wake up your body and improve flexibility', 'mobility', 10, 'easy'),
('Push-up Challenge', 'Classic bodyweight exercise for upper body strength', 'strength', 15, 'medium'),
('30-minute Run', 'Cardiovascular endurance training', 'cardio', 30, 'medium'),
('Yoga Flow', 'Mindful movement combining strength and flexibility', 'flexibility', 25, 'medium'),
('Core Plank Series', 'Strengthen your core with various plank variations', 'strength', 12, 'hard'),
('Evening Mobility', 'Gentle movements to maintain joint health', 'mobility', 8, 'easy');

-- Create indexes for better performance
CREATE INDEX idx_workout_category ON workout(category);
CREATE INDEX idx_workout_created_at ON workout(created_at DESC);
CREATE INDEX idx_progress_user_date ON workout_progress(user_id, completed_date DESC);
CREATE INDEX idx_progress_streak ON workout_progress(user_id, streak_count DESC);