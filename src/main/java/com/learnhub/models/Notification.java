package com.learnhub.models;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private int utilisateurId;
    private String message;
    private String type;
    private Timestamp dateCreation;
    private boolean lu;

    public Notification() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
}
