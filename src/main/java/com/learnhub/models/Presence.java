package com.learnhub.models;

public class Presence {
    private int id;
    private String statut;
    private int seanceId;
    private int etudiantId;
    private String dateSeance;
    private String moduleNom;
    private String etudiantNom;

    public Presence() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getSeanceId() { return seanceId; }
    public void setSeanceId(int seanceId) { this.seanceId = seanceId; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public String getDateSeance() { return dateSeance; }
    public void setDateSeance(String dateSeance) { this.dateSeance = dateSeance; }

    public String getModuleNom() { return moduleNom; }
    public void setModuleNom(String moduleNom) { this.moduleNom = moduleNom; }

    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }
}
