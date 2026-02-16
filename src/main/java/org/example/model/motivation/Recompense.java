package org.example.model.motivation;

public class Recompense {
    private int idRecompense;
    private String titre;
    private String description;
    private String typeRecompense;
    private String conditionObtention;
    private boolean actif;

    // Constructeurs
    public Recompense() {
    }

    public Recompense(String titre, String description, String typeRecompense, String conditionObtention) {
        this.titre = titre;
        this.description = description;
        this.typeRecompense = typeRecompense;
        this.conditionObtention = conditionObtention;
        this.actif = true;
    }

    // Getters & Setters
    public int getIdRecompense() {
        return idRecompense;
    }

    public void setIdRecompense(int idRecompense) {
        this.idRecompense = idRecompense;
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

    public String getTypeRecompense() {
        return typeRecompense;
    }

    public void setTypeRecompense(String typeRecompense) {
        this.typeRecompense = typeRecompense;
    }

    public String getConditionObtention() {
        return conditionObtention;
    }

    public void setConditionObtention(String conditionObtention) {
        this.conditionObtention = conditionObtention;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "Recompense{" +
                "idRecompense=" + idRecompense +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", typeRecompense='" + typeRecompense + '\'' +
                ", conditionObtention='" + conditionObtention + '\'' +
                ", actif=" + actif +
                '}';
    }
}
