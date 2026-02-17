package org.example.model.fitness;

import java.time.LocalDate;

/**
 * Tracks a user's completion of a workout on a given date (habit tracking).
 * Used for streak calculation and weekly completion percentage.
 */
public class WorkoutProgress {
    private int id;
    private int userId;
    private int workoutId;
    private LocalDate completedDate;
    private int streakCount;
    private String notes;
    private int rating; // 0=None, 1=Dislike, 2=Like

    public WorkoutProgress() {
    }

    public WorkoutProgress(int userId, int workoutId, LocalDate completedDate, int streakCount) {
        this.userId = userId;
        this.workoutId = workoutId;
        this.completedDate = completedDate;
        this.streakCount = streakCount;
    }

    public WorkoutProgress(int userId, int workoutId, LocalDate completedDate, int streakCount, String notes,
            int rating) {
        this(userId, workoutId, completedDate, streakCount);
        this.notes = notes;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "WorkoutProgress{id=" + id + ", userId=" + userId + ", workoutId=" + workoutId + ", date="
                + completedDate + ", streak=" + streakCount + ", rating=" + rating + "}";
    }
}
