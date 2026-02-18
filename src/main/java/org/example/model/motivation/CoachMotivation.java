package org.example.model;

public class CoachMotivation {
    private int idCoach;
    private String nomCoach;
    private String email;
    private String style;
    private String description;
    private boolean actif;

    // Constructeurs
    public CoachMotivation() {}

    public CoachMotivation(String nomCoach,String email, String style, String description) {
        this.nomCoach = nomCoach;
        this.email = email;
        this.style = style;
        this.description = description;
        this.actif = true;
    }

    // Getters & Setters
    public int getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(int idCoach) {
        this.idCoach = idCoach;
    }

    public String getNomCoach() {
        return nomCoach;
    }

    public void setNomCoach(String nomCoach) {
        this.nomCoach = nomCoach;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "CoachMotivation{" +
                "idCoach=" + idCoach +
                ", nomCoach='" + nomCoach + '\'' +
                ", email='" + email + '\'' +
                ", style='" + style + '\'' +
                ", description='" + description + '\'' +
                ", actif=" + actif +
                '}';
    }
}
