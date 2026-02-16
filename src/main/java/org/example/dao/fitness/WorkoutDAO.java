package org.example.dao.fitness;

import org.example.config.DatabaseConnection;
import org.example.model.fitness.Workout;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Workout CRUD using prepared statements and config DatabaseConnection.
 */
public class WorkoutDAO {

    // CREATE
    public void create(Workout w) {
        String sql = "INSERT INTO workout (title, description, category, duration_minutes, difficulty_level, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, w.getTitle());
            ps.setString(2, w.getDescription());
            ps.setString(3, w.getCategory());
            ps.setInt(4, w.getDurationMinutes());
            ps.setString(5, w.getDifficultyLevel());
            ps.setObject(6, w.getCreatedAt() != null ? w.getCreatedAt() : LocalDateTime.now());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ all
    public List<Workout> readAll() {
        List<Workout> list = new ArrayList<>();
        String sql = "SELECT * FROM workout ORDER BY created_at DESC";
        try (Connection cnx = DatabaseConnection.getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Workout w = mapResultSetToWorkout(rs);
                list.add(w);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // READ by id
    public Workout readById(int id) {
        String sql = "SELECT * FROM workout WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToWorkout(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE
    public void update(Workout w) {
        String sql = "UPDATE workout SET title=?, description=?, category=?, duration_minutes=?, difficulty_level=? WHERE id=?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, w.getTitle());
            ps.setString(2, w.getDescription());
            ps.setString(3, w.getCategory());
            ps.setInt(4, w.getDurationMinutes());
            ps.setString(5, w.getDifficultyLevel());
            ps.setInt(6, w.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM workout WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Workout mapResultSetToWorkout(ResultSet rs) throws SQLException {
        Workout w = new Workout();
        w.setId(rs.getInt("id"));
        w.setTitle(rs.getString("title"));
        w.setDescription(rs.getString("description"));
        w.setCategory(rs.getString("category"));
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setDifficultyLevel(rs.getString("difficulty_level"));
        Timestamp ts = rs.getTimestamp("created_at");
        w.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return w;
    }
}
