package com.learnhub.models;

public class Module {
    private int id;
    private String code;
    private String intitule;
    private int semestre;
    private int credits;
    private int filiere_id;
    private int responsable_id;

    public Module() {
    }

    public Module(int id, String code, String intitule, int semestre, int credits, int filiere_id, int responsable_id) {
        this.id = id;
        this.code = code;
        this.intitule = intitule;
        this.semestre = semestre;
        this.credits = credits;
        this.filiere_id = filiere_id;
        this.responsable_id = responsable_id;
    }

    // Getters et setters
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getIntitule() { return intitule; }
    public int getSemestre() { return semestre; }
    public int getCredits() { return credits; }
    public int getFiliere_id() { return filiere_id; }
    public int getResponsable_id() { return responsable_id; }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setFiliere_id(int filiere_id) {
        this.filiere_id = filiere_id;
    }

    public void setResponsable_id(int responsable_id) {
        this.responsable_id = responsable_id;
    }

    @Override
    public String toString() {
        return intitule != null ? intitule : "Module " + code;
    }
}