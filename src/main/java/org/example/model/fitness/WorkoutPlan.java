package org.example.model.fitness;

import java.time.LocalDateTime;

/**
 * A coach-assigned workout for a client on a specific day of the week.
 */
public class WorkoutPlan {
    private int id;
    private int userId; // client
    private int workoutId;
    private String dayOfWeek; // Monday, Tuesday, …
    private int coachId;
    private String notes;
    private LocalDateTime createdAt;

    // transient helpers (populated by DAO joins)
    private String workoutTitle;
    private String clientName;

    public WorkoutPlan() {
    }

    public WorkoutPlan(int userId, int workoutId, String dayOfWeek, int coachId, String notes) {
        this.userId = userId;
        this.workoutId = workoutId;
        this.dayOfWeek = dayOfWeek;
        this.coachId = coachId;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
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

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getCoachId() {
        return coachId;
    }

    public void setCoachId(int coachId) {
        this.coachId = coachId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getWorkoutTitle() {
        return workoutTitle;
    }

    public void setWorkoutTitle(String workoutTitle) {
        this.workoutTitle = workoutTitle;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "WorkoutPlan{id=" + id + ", userId=" + userId + ", workoutId=" + workoutId +
                ", day=" + dayOfWeek + ", coachId=" + coachId + "}";
    }
}
