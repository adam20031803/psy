package org.example.dao.fitness;

import org.example.config.DatabaseConnection;
import org.example.model.fitness.WorkoutPlan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for workout_plan CRUD.
 */
public class WorkoutPlanDAO {

    // CREATE
    public void create(WorkoutPlan wp) {
        String sql = "INSERT INTO workout_plan (user_id, workout_id, day_of_week, coach_id, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, wp.getUserId());
            ps.setInt(2, wp.getWorkoutId());
            ps.setString(3, wp.getDayOfWeek());
            ps.setInt(4, wp.getCoachId());
            ps.setString(5, wp.getNotes());
            ps.setObject(6, wp.getCreatedAt() != null ? wp.getCreatedAt() : LocalDateTime.now());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ all plans assigned by a coach (with workout title and client name)
    public List<WorkoutPlan> findByCoachId(int coachId) {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT wp.*, w.title AS workout_title, CONCAT(u.prenom, ' ', u.nom) AS client_name " +
                "FROM workout_plan wp " +
                "JOIN workout w ON wp.workout_id = w.id " +
                "JOIN user u ON wp.user_id = u.id " +
                "WHERE wp.coach_id = ? ORDER BY wp.day_of_week, u.nom";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // READ plans for a specific client
    public List<WorkoutPlan> findByUserId(int userId) {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT wp.*, w.title AS workout_title, '' AS client_name " +
                "FROM workout_plan wp " +
                "JOIN workout w ON wp.workout_id = w.id " +
                "WHERE wp.user_id = ? ORDER BY FIELD(wp.day_of_week, 'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')";
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

    // UPDATE
    public void update(WorkoutPlan wp) {
        String sql = "UPDATE workout_plan SET user_id=?, workout_id=?, day_of_week=?, notes=? WHERE id=?";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, wp.getUserId());
            ps.setInt(2, wp.getWorkoutId());
            ps.setString(3, wp.getDayOfWeek());
            ps.setString(4, wp.getNotes());
            ps.setInt(5, wp.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM workout_plan WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
                PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all clients (users who are not admin/coach)
    public List<int[]> getAllClients() {
        List<int[]> clients = new ArrayList<>(); // [id]
        return clients; // handled via UserDAO
    }

    private static WorkoutPlan mapResultSet(ResultSet rs) throws SQLException {
        WorkoutPlan wp = new WorkoutPlan();
        wp.setId(rs.getInt("id"));
        wp.setUserId(rs.getInt("user_id"));
        wp.setWorkoutId(rs.getInt("workout_id"));
        wp.setDayOfWeek(rs.getString("day_of_week"));
        wp.setCoachId(rs.getInt("coach_id"));
        wp.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        wp.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        try {
            wp.setWorkoutTitle(rs.getString("workout_title"));
        } catch (SQLException ignored) {
        }
        try {
            wp.setClientName(rs.getString("client_name"));
        } catch (SQLException ignored) {
        }
        return wp;
    }
}
