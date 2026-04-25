package com.learnhub.models;

public class Filiere {
    private int id;
    private String code;
    private String nom;
    private String description;
    private String niveau;
    private int duree;
    private int dureeAnnees;
    private String responsable;
    private int responsableId;
    private int capaciteMax;
    private int universiteId;
    private String videoUrl;


    public Filiere() {}

    public Filiere(int id, String code, String nom, String niveau, int dureeAnnees, int capaciteMax, int universiteId, int responsableId) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.niveau = niveau;
        this.dureeAnnees = dureeAnnees;
        this.capaciteMax = capaciteMax;
        this.universiteId = universiteId;
        this.responsableId = responsableId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public int getDureeAnnees() {
        return dureeAnnees;
    }

    public void setDureeAnnees(int dureeAnnees) {
        this.dureeAnnees = dureeAnnees;
        this.duree = dureeAnnees; // Synchronisation
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public int getResponsableId() {
        return responsableId;
    }

    public void setResponsableId(int responsableId) {
        this.responsableId = responsableId;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public int getUniversiteId() {
        return universiteId;
    }

    public void setUniversiteId(int universiteId) {
        this.universiteId = universiteId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public String toString() {
        return nom; // Pour l'affichage dans les ComboBox
    }
}
