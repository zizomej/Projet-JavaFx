package com.learnhub.models;

import java.time.LocalDate;

public class OffreStage {
    private int id;
    private String titre;
    private String description;
    private String typeStage; // "pfe", "observation", "initiation"
    private int dureeMois;
    private LocalDate datePublication;
    private int partenaireId;
    private String partenaireNom;
    private int filiereId;
    private String filiereNom;
    private int placesDisponibles;
    private String statut; // "active", "fermee"

    public OffreStage() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeStage() { return typeStage; }
    public void setTypeStage(String typeStage) { this.typeStage = typeStage; }

    public int getDureeMois() { return dureeMois; }
    public void setDureeMois(int dureeMois) { this.dureeMois = dureeMois; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    public int getPartenaireId() { return partenaireId; }
    public void setPartenaireId(int partenaireId) { this.partenaireId = partenaireId; }

    public String getPartenaireNom() { return partenaireNom; }
    public void setPartenaireNom(String partenaireNom) { this.partenaireNom = partenaireNom; }

    public int getFiliereId() { return filiereId; }
    public void setFiliereId(int filiereId) { this.filiereId = filiereId; }

    public String getFiliereNom() { return filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() { return titre; }
}
