package com.learnhub.models;

/**
 * Maps to the `filiere` table in gestion_universitaire DB.
 * Columns: id, code, nom, niveau, duree_annees, capacite_max, universite_id, responsable_id
 */
public class Filiere {
    private int id;
    private String code;
    private String nom;
    private String niveau;
    private int dureeAnnees;
    private int capaciteMax;
    private int universiteId;
    private int responsableId;
    private String responsableNom; // computed via JOIN

    public Filiere() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public int getDureeAnnees() { return dureeAnnees; }
    public void setDureeAnnees(int dureeAnnees) { this.dureeAnnees = dureeAnnees; }
    // Alias for backward compat
    public int getDuree() { return dureeAnnees; }
    public void setDuree(int duree) { this.dureeAnnees = duree; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }
    public int getUniversiteId() { return universiteId; }
    public void setUniversiteId(int universiteId) { this.universiteId = universiteId; }
    public int getResponsableId() { return responsableId; }
    public void setResponsableId(int responsableId) { this.responsableId = responsableId; }
    public String getResponsableNom() { return responsableNom; }
    public void setResponsableNom(String responsableNom) { this.responsableNom = responsableNom; }
    // Alias for backward compat
    public String getResponsable() { return responsableNom; }
    public void setResponsable(String r) { this.responsableNom = r; }
    // Alias for backward compat (old code used description)
    public String getDescription() { return niveau + " - " + dureeAnnees + " ans"; }

    @Override
    public String toString() { return nom != null ? nom : ""; }
}
