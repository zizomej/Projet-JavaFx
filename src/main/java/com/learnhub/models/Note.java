package com.learnhub.models;

public class Note {
    private int id;
    private double valeur;
    private String typeNote;
    private double coefficient;
    private String dateSaisie;
    private int etudiantId;
    private String etudiantNom;
    private int enseignantId;
    private int moduleId;
    private String moduleIntitule;

    public Note() {}

    // Getters
    public int getId() { return id; }
    public double getValeur() { return valeur; }
    public String getTypeNote() { return typeNote; }
    public double getCoefficient() { return coefficient; }
    public String getDateSaisie() { return dateSaisie; }
    public int getEtudiantId() { return etudiantId; }
    public String getEtudiantNom() { return etudiantNom; }
    public int getEnseignantId() { return enseignantId; }
    public int getModuleId() { return moduleId; }
    public String getModuleIntitule() { return moduleIntitule; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setValeur(double valeur) { this.valeur = valeur; }
    public void setTypeNote(String typeNote) { this.typeNote = typeNote; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
    public void setDateSaisie(String dateSaisie) { this.dateSaisie = dateSaisie; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }
    public void setModuleIntitule(String moduleIntitule) { this.moduleIntitule = moduleIntitule; }
}
