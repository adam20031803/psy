package org.example.dao.fitness;

import org.example.config.DatabaseConnection;
import org.example.model.fitness.WorkoutProgress;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for WorkoutProgress: mark workout completed, fetch by user, compute
 * streak and weekly stats.
 */
public class WorkoutProgressDAO {

    // Insert a completion record (mark workout as completed for a user on a date)
    public void create(WorkoutProgress wp) {
        String sql = "INSERT INTO workout_progress (user_id, workout_id, completed_date, streak_count, notes, rating) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, wp.getUserId());
            ps.setInt(2, wp.getWorkoutId());
            ps.setObject(3, wp.getCompletedDate());
            ps.setInt(4, wp.getStreakCount());
            ps.setString(5, wp.getNotes());
            ps.setInt(6, wp.getRating());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // UPDATE
    public void update(WorkoutProgress wp) {
        String sql = "UPDATE workout_progress SET completed_date=?, notes=?, rating=? WHERE id=?";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setObject(1, wp.getCompletedDate());
            ps.setString(2, wp.getNotes());
            ps.setInt(3, wp.getRating());
            ps.setInt(4, wp.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ all (for admin / coach view)
    public List<WorkoutProgress> readAll() {
        List<WorkoutProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM workout_progress ORDER BY completed_date DESC";
        try (Connection cnx = DatabaseConnection.getConnection();
                Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<WorkoutProgress> findByUserId(int userId) {
        List<WorkoutProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM workout_progress WHERE user_id = ? ORDER BY completed_date DESC";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<WorkoutProgress> findByUserIdAndDateRange(int userId, LocalDate start, LocalDate end) {
        List<WorkoutProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM workout_progress WHERE user_id = ? AND completed_date >= ? AND completed_date <= ? ORDER BY completed_date";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setObject(2, start);
            ps.setObject(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Check if user already logged this workout on this date (avoid duplicate) */
    public boolean existsByUserWorkoutAndDate(int userId, int workoutId, LocalDate date) {
        String sql = "SELECT 1 FROM workout_progress WHERE user_id = ? AND workout_id = ? AND completed_date = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, workoutId);
            ps.setObject(3, date);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get current streak: consecutive days up to today with at least one workout.
     */
    public int getCurrentStreak(int userId) {
        String sql = "SELECT streak_count FROM workout_progress WHERE user_id = ? ORDER BY completed_date DESC LIMIT 1";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("streak_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Count distinct days in the current week (Mon–Sun) where user completed at
     * least one workout.
     */
    public long getCompletedDaysInCurrentWeek(int userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        List<WorkoutProgress> list = findByUserIdAndDateRange(userId, startOfWeek, endOfWeek);
        return list.stream().map(WorkoutProgress::getCompletedDate).distinct().count();
    }

    /**
     * Mark a workout as completed for the current user today.
     * Computes streak: consecutive days with at least one completion.
     * Returns true if inserted, false if already completed today for this workout.
     */
    public boolean markCompleted(int userId, int workoutId) {
        LocalDate today = LocalDate.now();
        if (existsByUserWorkoutAndDate(userId, workoutId, today))
            return false;
        int newStreak = computeNextStreak(userId, today);
        WorkoutProgress wp = new WorkoutProgress(userId, workoutId, today, newStreak);
        create(wp);
        return true;
    }

    /**
     * Compute streak: if user had completion yesterday, streak = previous + 1; else
     * 1.
     */
    private int computeNextStreak(int userId, LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        List<WorkoutProgress> recent = findByUserIdAndDateRange(userId, yesterday, today);
        boolean hadYesterday = recent.stream().anyMatch(wp -> yesterday.equals(wp.getCompletedDate()));
        if (!hadYesterday)
            return 1;
        int maxStreakYesterday = recent.stream()
                .filter(wp -> yesterday.equals(wp.getCompletedDate()))
                .mapToInt(WorkoutProgress::getStreakCount)
                .max().orElse(0);
        return maxStreakYesterday + 1;
    }

    /** Delete a progress record (e.g. undo completion) */
    public void delete(int id) {
        String sql = "DELETE FROM workout_progress WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static WorkoutProgress mapResultSet(ResultSet rs) throws SQLException {
        WorkoutProgress wp = new WorkoutProgress();
        wp.setId(rs.getInt("id"));
        wp.setUserId(rs.getInt("user_id"));
        wp.setWorkoutId(rs.getInt("workout_id"));
        Date d = rs.getDate("completed_date");
        wp.setCompletedDate(d != null ? d.toLocalDate() : null);
        wp.setStreakCount(rs.getInt("streak_count"));
        try {
            wp.setNotes(rs.getString("notes"));
        } catch (SQLException ignored) {
        }
        try {
            wp.setRating(rs.getInt("rating"));
        } catch (SQLException ignored) {
        }
        return wp;
    }
}
