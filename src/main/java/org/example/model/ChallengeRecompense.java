package org.example.model;

import java.sql.Timestamp;

public class ChallengeRecompense {

    private int idChallenge;
    private int idRecompense;

    private String titreChallenge;
    private String titreRecompense;
    private String typeRecompense;

    private Timestamp dateAttribution;

    /* ================= CONSTRUCTEURS ================= */

    // Constructeur vide (OBLIGATOIRE pour JavaFX / JDBC)
    public ChallengeRecompense() {
    }

    // Constructeur minimal (ids seulement)
    public ChallengeRecompense(int idChallenge, int idRecompense) {
        this.idChallenge = idChallenge;
        this.idRecompense = idRecompense;
    }

    // Constructeur complet (pour affichage)
    public ChallengeRecompense(int idChallenge, int idRecompense,
                               String titreChallenge,
                               String titreRecompense,
                               String typeRecompense,
                               Timestamp dateAttribution) {
        this.idChallenge = idChallenge;
        this.idRecompense = idRecompense;
        this.titreChallenge = titreChallenge;
        this.titreRecompense = titreRecompense;
        this.typeRecompense = typeRecompense;
        this.dateAttribution = dateAttribution;
    }

    /* ================= GETTERS ================= */

    public int getIdChallenge() {
        return idChallenge;
    }

    public int getIdRecompense() {
        return idRecompense;
    }

    public String getTitreChallenge() {
        return titreChallenge;
    }

    public String getTitreRecompense() {
        return titreRecompense;
    }

    public String getTypeRecompense() {
        return typeRecompense;
    }

    public Timestamp getDateAttribution() {
        return dateAttribution;
    }

    /* ================= SETTERS ================= */

    public void setIdChallenge(int idChallenge) {
        this.idChallenge = idChallenge;
    }

    public void setIdRecompense(int idRecompense) {
        this.idRecompense = idRecompense;
    }

    public void setTitreChallenge(String titreChallenge) {
        this.titreChallenge = titreChallenge;
    }

    public void setTitreRecompense(String titreRecompense) {
        this.titreRecompense = titreRecompense;
    }

    public void setTypeRecompense(String typeRecompense) {
        this.typeRecompense = typeRecompense;
    }

    public void setDateAttribution(Timestamp dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    /* ================= UTILE ================= */

    @Override
    public String toString() {
        return "ChallengeRecompense{" +
                "idChallenge=" + idChallenge +
                ", idRecompense=" + idRecompense +
                ", titreChallenge='" + titreChallenge + '\'' +
                ", titreRecompense='" + titreRecompense + '\'' +
                ", typeRecompense='" + typeRecompense + '\'' +
                ", dateAttribution=" + dateAttribution +
                '}';
    }
}
