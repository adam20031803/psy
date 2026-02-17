package org.example.model.motivation;

import java.util.ArrayList;
import java.util.List;

public class Challenge {
    private int idChallenge;
    private String titre;
    private String description;
    private int dureeJours;
    private String niveauDifficulte;
    private String typeChallenge;
    private boolean actif;

    private List<CoachMotivation> coaches = new ArrayList<>();

    // Constructeurs
    public Challenge() {
    }

    public Challenge(String titre, String description, int dureeJours, String niveauDifficulte, String typeChallenge) {
        this.titre = titre;
        this.description = description;
        this.dureeJours = dureeJours;
        this.niveauDifficulte = niveauDifficulte;
        this.typeChallenge = typeChallenge;
        this.actif = true;
    }

    public List<CoachMotivation> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<CoachMotivation> coaches) {
        this.coaches = coaches;
    }

    // Méthode pour obtenir les noms des coaches formatés
    public String getCoachesNames() {
        if (coaches == null || coaches.isEmpty()) {
            return "—";
        }
        StringBuilder names = new StringBuilder();
        for (CoachMotivation coach : coaches) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(coach.getNomCoach());
        }
        return names.toString();
    }

    // Getters et Setters
    public int getIdChallenge() {
        return idChallenge;
    }

    public void setIdChallenge(int idChallenge) {
        this.idChallenge = idChallenge;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDureeJours() {
        return dureeJours;
    }

    public void setDureeJours(int dureeJours) {
        this.dureeJours = dureeJours;
    }

    public String getNiveauDifficulte() {
        return niveauDifficulte;
    }

    public void setNiveauDifficulte(String niveauDifficulte) {
        this.niveauDifficulte = niveauDifficulte;
    }

    public String getTypeChallenge() {
        return typeChallenge;
    }

    public void setTypeChallenge(String typeChallenge) {
        this.typeChallenge = typeChallenge;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "idChallenge=" + idChallenge +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dureeJours=" + dureeJours +
                ", niveauDifficulte='" + niveauDifficulte + '\'' +
                ", typeChallenge='" + typeChallenge + '\'' +
                ", actif=" + actif +
                '}';
    }
}
