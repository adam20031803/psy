package org.example.model.fitness;

import java.time.LocalDateTime;

/**
 * Entity representing a workout template (Atomic Habits: small daily habit).
 * Used for CRUD and as reference when tracking progress.
 */
public class Workout {
    private int id;
    private String title;
    private String description;
    private String category;       // strength, cardio, mobility, etc.
    private int durationMinutes;
    private String difficultyLevel; // easy, medium, hard
    private LocalDateTime createdAt;

    public Workout() {}

    public Workout(String title, String description, String category,
                   int durationMinutes, String difficultyLevel) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.difficultyLevel = difficultyLevel;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Workout{id=" + id + ", title='" + title + "', category='" + category + "', duration=" + durationMinutes + "}";
    }
}
