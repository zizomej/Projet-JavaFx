package com.learnhub.models;

public class Cours {
    private int id;
    private String titre;
    private String description;
    private String matiere;
    private int professeurId;
    private String professeurNom;
    private String niveau;
    private String filiere;
    private boolean actif;

    public Cours() { this.actif = true; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public int getProfesseurId() { return professeurId; }
    public void setProfesseurId(int professeurId) { this.professeurId = professeurId; }
    public String getProfesseurNom() { return professeurNom; }
    public void setProfesseurNom(String professeurNom) { this.professeurNom = professeurNom; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    @Override public String toString() { return titre; }
}
