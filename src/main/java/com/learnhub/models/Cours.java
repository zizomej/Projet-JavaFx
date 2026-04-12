package com.learnhub.models;

/**
 * Maps to the `module` table in gestion_universitaire DB.
 * Columns: id, code, intitule, semestre, credits, filiere_id, responsable_id
 */
public class Cours {
    private int id;
    private String code;
    private String intitule;   // was "titre" - now matches DB column
    private int semestre;
    private int credits;
    private int filiereId;
    private int responsableId;
    private String responsableNom; // computed via JOIN

    public Cours() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    // Alias for backward compat
    public String getTitre() { return intitule; }
    public void setTitre(String titre) { this.intitule = titre; }
    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }
    public String getNiveau() { return "Semestre " + semestre; }
    public void setNiveau(String niveau) { /* alias kept for compatibility */ }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getFiliereId() { return filiereId; }
    public void setFiliereId(int filiereId) { this.filiereId = filiereId; }
    public int getResponsableId() { return responsableId; }
    public void setResponsableId(int responsableId) { this.responsableId = responsableId; }
    public String getResponsableNom() { return responsableNom; }
    public void setResponsableNom(String responsableNom) { this.responsableNom = responsableNom; }
    // Aliases used by existing controllers
    public String getProfesseurNom() { return responsableNom; }
    public void setProfesseurNom(String nom) { this.responsableNom = nom; }
    public int getProfesseurId() { return responsableId; }
    public void setProfesseurId(int id) { this.responsableId = id; }

    @Override
    public String toString() { return intitule != null ? intitule : ""; }
}
