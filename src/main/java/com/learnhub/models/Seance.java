package com.learnhub.models;

public class Seance {
    private int id;
    private int moduleId;
    private String moduleTitre;
    private String date;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private String type;
    private String description;

    private int enseignantId;

    public Seance() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }
    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public String getModuleTitre() { return moduleTitre; }
    public void setModuleTitre(String moduleTitre) { this.moduleTitre = moduleTitre; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    @Override public String toString() { return moduleTitre + " - " + date + " " + heureDebut; }
}

