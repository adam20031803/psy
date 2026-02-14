package org.example.dao;

import org.example.model.Challenge;
import org.example.model.ChallengeRecompense;
import org.example.model.Recompense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public interface CrudCoach<T> {
    void create(T t);
    List<T> readAll();
    void update(T t);
    void delete(int id);       // soft delete
    void delete_reel(int id);  // vrai delete

    class ChallengeRecompenseCrud {

        private Connection cnx = DatabaseConnection.getConnection();

        // Associer une récompense à un challenge
        public void associateRecompense(int idChallenge, int idRecompense) {
            String sql = "INSERT INTO challenge_recompense (id_challenge, id_recompense) VALUES (?, ?)";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, idChallenge);
                ps.setInt(2, idRecompense);
                ps.executeUpdate();
                System.out.println("Association créée !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Dissocier une récompense d'un challenge
        public void dissociateRecompense(int idChallenge, int idRecompense) {
            String sql = "DELETE FROM challenge_recompense WHERE id_challenge = ? AND id_recompense = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, idChallenge);
                ps.setInt(2, idRecompense);
                ps.executeUpdate();
                System.out.println("Association supprimée !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Récupérer toutes les récompenses d'un challenge
        public List<Recompense> getRecompensesByChallenge(int idChallenge) {
            List<Recompense> recompenses = new ArrayList<>();
            String sql = """
                SELECT r.* FROM recompense r
                INNER JOIN challenge_recompense cr ON r.id_recompense = cr.id_recompense
                WHERE cr.id_challenge = ? AND r.actif = 1
                ORDER BY r.titre
                """;

            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, idChallenge);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Recompense r = new Recompense();
                    r.setIdRecompense(rs.getInt("id_recompense"));
                    r.setTitre(rs.getString("titre"));
                    r.setDescription(rs.getString("description"));
                    r.setTypeRecompense(rs.getString("type_recompense"));
                    r.setConditionObtention(rs.getString("condition_obtention"));
                    r.setActif(rs.getBoolean("actif"));
                    recompenses.add(r);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return recompenses;
        }

        // Récupérer tous les challenges associés à une récompense
        public List<Challenge> getChallengesByRecompense(int idRecompense) {
            List<Challenge> challenges = new ArrayList<>();
            String sql = """
                SELECT c.* FROM challenge c
                INNER JOIN challenge_recompense cr ON c.id_challenge = cr.id_challenge
                WHERE cr.id_recompense = ? AND c.actif = 1
                ORDER BY c.titre
                """;

            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, idRecompense);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Challenge c = new Challenge();
                    c.setIdChallenge(rs.getInt("id_challenge"));
                    c.setTitre(rs.getString("titre"));
                    c.setDescription(rs.getString("description"));
                    c.setDureeJours(rs.getInt("duree_jours"));
                    c.setNiveauDifficulte(rs.getString("niveau_difficulte"));
                    c.setTypeChallenge(rs.getString("type_challenge"));
                    c.setActif(rs.getBoolean("actif"));
                    challenges.add(c);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return challenges;
        }

        // Récupérer toutes les associations
        public List<ChallengeRecompense> getAllAssociations() {
            List<ChallengeRecompense> associations = new ArrayList<>();
            String sql = """
                SELECT 
                    cr.id_challenge,
                    cr.id_recompense,
                    c.titre as titre_challenge,
                    r.titre as titre_recompense,
                    r.type_recompense,
                    cr.date_attribution
                FROM challenge_recompense cr
                INNER JOIN challenge c ON cr.id_challenge = c.id_challenge
                INNER JOIN recompense r ON cr.id_recompense = r.id_recompense
                WHERE c.actif = 1 AND r.actif = 1
                ORDER BY cr.date_attribution DESC
                """;

            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    ChallengeRecompense cr = new ChallengeRecompense();
                    cr.setIdChallenge(rs.getInt("id_challenge"));
                    cr.setIdRecompense(rs.getInt("id_recompense"));
                    cr.setTitreChallenge(rs.getString("titre_challenge"));
                    cr.setTitreRecompense(rs.getString("titre_recompense"));
                    cr.setTypeRecompense(rs.getString("type_recompense"));
                    associations.add(cr);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return associations;
        }
    }
}
