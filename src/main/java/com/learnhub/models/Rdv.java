package com.learnhub.models;

public class Rdv {
    private int id;
    private int patientId;
    private String patientNom;
    private int medecinId;
    private String medecinNom;
    private int creneauId;
    private String dateHeure;
    private String motif;
    private String statut; // EN_ATTENTE, CONFIRME, ANNULE, TERMINE
    private String notes;

    public Rdv() {}
    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; } public void setPatientId(int v) { patientId = v; }
    public String getPatientNom() { return patientNom; } public void setPatientNom(String v) { patientNom = v; }
    public int getMedecinId() { return medecinId; } public void setMedecinId(int v) { medecinId = v; }
    public String getMedecinNom() { return medecinNom; } public void setMedecinNom(String v) { medecinNom = v; }
    public int getCreneauId() { return creneauId; } public void setCreneauId(int v) { creneauId = v; }
    public String getDateHeure() { return dateHeure; } public void setDateHeure(String v) { dateHeure = v; }
    public String getMotif() { return motif; } public void setMotif(String v) { motif = v; }
    public String getStatut() { return statut; } public void setStatut(String v) { statut = v; }
    public String getNotes() { return notes; } public void setNotes(String v) { notes = v; }
}
