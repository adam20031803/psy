package org.example.dao.motivation;

import org.example.model.motivation.CoachMotivation;
import org.example.model.motivation.Challenge;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengeCoachCrud {
    private Connection cnx = DatabaseConnection.getConnection();

    // Obtenir tous les coaches d'un challenge
    public List<CoachMotivation> getCoachesForChallenge(int challengeId) {
        List<CoachMotivation> coaches = new ArrayList<>();
        String sql = "SELECT cm.* FROM coach_motivation cm " +
                "INNER JOIN challenge_coach cc ON cm.id_coach = cc.id_coach " +
                "WHERE cc.id_challenge = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CoachMotivation coach = new CoachMotivation();
                coach.setIdCoach(rs.getInt("id_coach"));
                coach.setNomCoach(rs.getString("nom_coach"));
                coach.setStyle(rs.getString("style"));
                coach.setDescription(rs.getString("description"));
                coach.setActif(rs.getBoolean("actif"));
                coaches.add(coach);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coaches;
    }

    // Obtenir tous les challenges d'un coach (UNE SEULE VERSION)
    public List<Challenge> getChallengesForCoach(int coachId) {
        List<Challenge> challenges = new ArrayList<>();
        String sql = "SELECT c.* FROM challenge c " +
                "INNER JOIN challenge_coach cc ON c.id_challenge = cc.id_challenge " +
                "WHERE cc.id_coach = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Challenge challenge = new Challenge();
                challenge.setIdChallenge(rs.getInt("id_challenge"));
                challenge.setTitre(rs.getString("titre"));
                challenge.setDescription(rs.getString("description"));
                challenge.setDureeJours(rs.getInt("duree_jours"));
                challenge.setNiveauDifficulte(rs.getString("niveau_difficulte"));
                challenge.setTypeChallenge(rs.getString("type_challenge"));
                challenge.setActif(rs.getBoolean("actif"));
                challenges.add(challenge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challenges;
    }

    // Associer un coach à un challenge - CORRIGÉ avec vérification
    public boolean associateCoachToChallenge(int challengeId, int coachId) {
        // Vérifier d'abord si l'association existe déjà
        if (isCoachAssociated(challengeId, coachId)) {
            System.out.println("L'association entre le challenge " + challengeId +
                    " et le coach " + coachId + " existe déjà.");
            return false; // Ou return true selon votre logique
        }

        String sql = "INSERT INTO challenge_coach (id_challenge, id_coach) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ps.setInt(2, coachId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Coach " + coachId + " associé au challenge " + challengeId + " avec succès.");
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Si l'association existe déjà (clé dupliquée)
            System.out.println("Erreur d'intégrité: l'association existe déjà.");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Dissocier un coach d'un challenge
    public boolean dissociateCoachFromChallenge(int challengeId, int coachId) {
        String sql = "DELETE FROM challenge_coach WHERE id_challenge = ? AND id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ps.setInt(2, coachId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Coach " + coachId + " dissocié du challenge " + challengeId + " avec succès.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si un coach est déjà associé à un challenge - AMÉLIORÉ
    public boolean isCoachAssociated(int challengeId, int coachId) {
        String sql = "SELECT COUNT(*) FROM challenge_coach WHERE id_challenge = ? AND id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            ps.setInt(2, coachId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtenir le nombre de coaches par challenge
    public Map<Integer, Integer> getCoachCountByChallenge() {
        Map<Integer, Integer> coachCountMap = new HashMap<>();
        String sql = "SELECT id_challenge, COUNT(*) as coach_count FROM challenge_coach GROUP BY id_challenge";

        try (Statement stmt = cnx.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int challengeId = rs.getInt("id_challenge");
                int coachCount = rs.getInt("coach_count");
                coachCountMap.put(challengeId, coachCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coachCountMap;
    }

    // NOUVELLE MÉTHODE: Dissocier tous les coaches d'un challenge
    public boolean dissociateAllCoachesFromChallenge(int challengeId) {
        String sql = "DELETE FROM challenge_coach WHERE id_challenge = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, challengeId);
            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected + " coaches dissociés du challenge " + challengeId);
            return rowsAffected >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NOUVELLE MÉTHODE: Compter le nombre de challenges pour un coach
    public int getChallengeCountForCoach(int coachId) {
        String sql = "SELECT COUNT(*) FROM challenge_coach WHERE id_coach = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // NOUVELLE MÉTHODE: Obtenir les coaches avec leur nombre de challenges
    public Map<Integer, Integer> getChallengeCountByCoach() {
        Map<Integer, Integer> challengeCountMap = new HashMap<>();
        String sql = "SELECT id_coach, COUNT(*) as challenge_count FROM challenge_coach GROUP BY id_coach";

        try (Statement stmt = cnx.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int coachId = rs.getInt("id_coach");
                int challengeCount = rs.getInt("challenge_count");
                challengeCountMap.put(coachId, challengeCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return challengeCountMap;
    }
}