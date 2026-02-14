package org.example.model;

public class Reclamation {

    private int id;
    private int userId;
    private String sujet;
    private String description;
    private String priorite;
    private String statut;

    // ✅ Constructeur pour INSERT (création)
    public Reclamation(int userId, String sujet, String description, String priorite) {
        this.userId = userId;
        this.sujet = sujet;
        this.description = description;
        this.priorite = priorite;
        this.statut = "EN_ATTENTE";
    }

    // ✅ Constructeur pour lecture depuis DB (liste)
    public Reclamation(int id, int userId, String sujet,
                       String description, String priorite, String statut) {
        this.id = id;
        this.userId = userId;
        this.sujet = sujet;
        this.description = description;
        this.priorite = priorite;
        this.statut = statut;
    }

    // ✅ Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getSujet() { return sujet; }
    public String getDescription() { return description; }
    public String getPriorite() { return priorite; }
    public String getStatut() { return statut; }

    // ✅ Setters (IMPORTANT si tu veux modifier dans TableView)
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    public void setDescription(String description) { this.description = description; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", userId=" + userId +
                ", sujet='" + sujet + '\'' +
                ", description='" + description + '\'' +
                ", priorite='" + priorite + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
