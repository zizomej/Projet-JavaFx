package com.learnhub.models;

public class Presence {
    private int id;
    private int etudiantId;
    private String etudiantNom;
    private int seanceId;
    private String seanceInfo;
    private String statut;
    private String dateSeance;
    private String commentaire;

    public Presence() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }

    public int getSeanceId() { return seanceId; }
    public void setSeanceId(int seanceId) { this.seanceId = seanceId; }

    public String getSeanceInfo() { return seanceInfo; }
    public void setSeanceInfo(String seanceInfo) { this.seanceInfo = seanceInfo; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateSeance() { return dateSeance; }
    public void setDateSeance(String dateSeance) { this.dateSeance = dateSeance; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    // Backward-compatible aliases.
    public String getDate() { return dateSeance; }
    public void setDate(String date) { this.dateSeance = date; }
}
