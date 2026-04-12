package com.learnhub.models;

/**
 * Maps to the `evenement` table in gestion_universitaire DB.
 * Columns: id, titre, description, type_evenement, date_debut, date_fin,
 *          heure_debut, heure_fin, statut, lieu_id
 */
public class Evenement {
    private int id;
    private String titre;
    private String description;
    private String typeEvenement;
    private String dateDebut;
    private String dateFin;
    private String heureDebut;
    private String heureFin;
    private String statut;
    private int lieuId;
    private String lieuNom; // computed via JOIN with `lieu` table

    public Evenement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTypeEvenement() { return typeEvenement; }
    public void setTypeEvenement(String typeEvenement) { this.typeEvenement = typeEvenement; }
    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }
    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getLieuId() { return lieuId; }
    public void setLieuId(int lieuId) { this.lieuId = lieuId; }
    public String getLieuNom() { return lieuNom; }
    public void setLieuNom(String lieuNom) { this.lieuNom = lieuNom; }

    // Aliases used by existing code
    public String getDate() { return dateDebut; }
    public void setDate(String date) { this.dateDebut = date; }
    public String getLieu() { return lieuNom; }
    public void setLieu(String lieu) { this.lieuNom = lieu; }
    public String getCategorie() { return typeEvenement; }
    public void setCategorie(String cat) { this.typeEvenement = cat; }

    @Override
    public String toString() { return titre != null ? titre : ""; }
}
