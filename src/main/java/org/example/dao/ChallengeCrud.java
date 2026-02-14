package org.example.dao;

import org.example.model.Challenge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChallengeCrud implements Crud_challenge<Challenge> {

    private Connection cnx = DatabaseConnection.getConnection();

    // CREATE
    @Override

    public void create(Challenge challenge) {
        String query = "INSERT INTO challenge (titre, description, duree_jours, niveau_difficulte, type_challenge, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, challenge.getTitre());
            ps.setString(2, challenge.getDescription());
            ps.setInt(3, challenge.getDureeJours());
            ps.setString(4, challenge.getNiveauDifficulte());
            ps.setString(5, challenge.getTypeChallenge());
            ps.setBoolean(6, challenge.isActif());

            System.out.println("SQL INSERT - actif = " + challenge.isActif());
            ps.executeUpdate();
            System.out.println("Challenge inséré avec succès!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // READ
    @Override
    public List<Challenge> readAll() {
        List<Challenge> list = new ArrayList<>();
        String sql = "SELECT * FROM challenge ";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Challenge c = new Challenge();
                c.setIdChallenge(rs.getInt("id_challenge"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setDureeJours(rs.getInt("duree_jours"));
                c.setNiveauDifficulte(rs.getString("niveau_difficulte"));
                c.setTypeChallenge(rs.getString("type_challenge"));
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
    public void update(Challenge c) {
        String sql = "UPDATE challenge SET titre=?, description=?, duree_jours=?, " +
                "niveau_difficulte=?, type_challenge=? WHERE id_challenge=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setObject(3, c.getDureeJours());
            ps.setString(4, c.getNiveauDifficulte());
            ps.setString(5, c.getTypeChallenge());
            ps.setInt(6, c.getIdChallenge());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE (soft delete)
    @Override
    public void delete(int id) {
        String sql = "UPDATE challenge SET actif = 0 WHERE id_challenge = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();  // executeUpdate renvoie le nombre de lignes modifiées
            if (rowsAffected > 0) {
                System.out.println("Challenge avec ID " + id + " supprimé (soft delete).");
            } else {
                System.out.println("Aucun challenge trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void delete_reel(int id) {
        String sql = "DELETE FROM challenge WHERE id_challenge = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Challenge ID " + id + " supprimé de la base !");
            } else {
                System.out.println("Aucun challenge trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
