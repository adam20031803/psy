package org.example.dao;

import org.example.model.Recompense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecompenseCrud implements CrudRecompense<Recompense> {

    private Connection cnx = DatabaseConnection.getConnection();

    // CREATE
    @Override
    public void create(Recompense r) {
        String sql = "INSERT INTO recompense (titre, description, type_recompense, condition_obtention, actif) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getTypeRecompense());
            ps.setString(4, r.getConditionObtention());
            ps.setBoolean(5, r.isActif());
            ps.executeUpdate();
            System.out.println("Récompense créée !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ

    // READ
    @Override
    public List<Recompense> readAll() {
        List<Recompense> list = new ArrayList<>();
        String sql = "SELECT * FROM recompense"; // ENLEVEZ "WHERE actif = 1"
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Recompense r = new Recompense();
                r.setIdRecompense(rs.getInt("id_recompense"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setTypeRecompense(rs.getString("type_recompense"));
                r.setConditionObtention(rs.getString("condition_obtention"));
                r.setActif(rs.getBoolean("actif"));
                list.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // UPDATE

    // UPDATE
    @Override
    public void update(Recompense r) {
        String sql = "UPDATE recompense SET titre=?, description=?, type_recompense=?, condition_obtention=?, actif=? WHERE id_recompense=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getTypeRecompense());
            ps.setString(4, r.getConditionObtention());
            ps.setBoolean(5, r.isActif()); // AJOUTEZ CETTE LIGNE
            ps.setInt(6, r.getIdRecompense());
            ps.executeUpdate();
            System.out.println("Récompense modifiée !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE soft
    @Override
    public void delete(int id) {
        String sql = "UPDATE recompense SET actif = 0 WHERE id_recompense = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Récompense ID " + id + " supprimée (soft delete).");
            else System.out.println("Aucune récompense trouvée avec l'ID " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE réel
    @Override
    public void delete_reel(int id) {
        String sql = "DELETE FROM recompense WHERE id_recompense = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Récompense ID " + id + " supprimée de la base !");
            else System.out.println("Aucune récompense trouvée avec l'ID " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
