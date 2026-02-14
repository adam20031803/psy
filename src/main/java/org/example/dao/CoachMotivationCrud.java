package org.example.dao;

import org.example.model.CoachMotivation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachMotivationCrud implements CrudCoach<CoachMotivation> {

    private Connection cnx = DatabaseConnection.getConnection();

    // CREATE
    @Override
    public void create(CoachMotivation c) {
        String sql = "INSERT INTO coach_motivation (nom_coach, style, description, actif) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNomCoach());
            ps.setString(2, c.getStyle());
            ps.setString(3, c.getDescription());
            ps.setBoolean(4, c.isActif()); // CORRIGÉ : index 4
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    @Override
    public List<CoachMotivation> readAll() {
        List<CoachMotivation> list = new ArrayList<>();
        String sql = "SELECT * FROM coach_motivation ";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                CoachMotivation c = new CoachMotivation();
                c.setIdCoach(rs.getInt("id_coach"));
                c.setNomCoach(rs.getString("nom_coach"));
                c.setStyle(rs.getString("style"));
                c.setDescription(rs.getString("description"));
                c.setActif(rs.getBoolean("actif"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // UPDATE
    @Override
    public void update(CoachMotivation c) {
        String sql = "UPDATE coach_motivation SET nom_coach=?, style=?, description=?, actif=? WHERE id_coach=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNomCoach());
            ps.setString(2, c.getStyle());
            ps.setString(3, c.getDescription());
            ps.setBoolean(4, c.isActif()); // CORRIGÉ : index 4
            ps.setInt(5, c.getIdCoach()); // CORRIGÉ : index 5
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE (soft)
    @Override
    public void delete(int id) {
        String sql = "UPDATE coach_motivation SET actif = 0 WHERE id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Coach ID " + id + " supprimé (soft delete).");
            } else {
                System.out.println("Aucun coach trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE réel
    @Override
    public void delete_reel(int id) {
        String sql = "DELETE FROM coach_motivation WHERE id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Coach ID " + id + " supprimé de la base !");
            } else {
                System.out.println("Aucun coach trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}