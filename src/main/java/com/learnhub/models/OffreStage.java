package com.learnhub.models;

public class OffreStage {
    private int id;
    private String titre;
    private String description;
    private int partenaireId;
    private String partenaireNom;
    private int filiereId;
    private String filiereNom;
    private String typeStage;
    private int dureeMois;
    private String datePublication;
    private String dateFin;
    private String statut;
    private double remuneration;
    private int candidatureCount;

    public OffreStage() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPartenaireId() { return partenaireId; }
    public void setPartenaireId(int partenaireId) { this.partenaireId = partenaireId; }

    public String getPartenaireNom() { return partenaireNom; }
    public void setPartenaireNom(String partenaireNom) { this.partenaireNom = partenaireNom; }

    public int getFiliereId() { return filiereId; }
    public void setFiliereId(int filiereId) { this.filiereId = filiereId; }

    public String getFiliereNom() { return filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }

    public String getTypeStage() { return typeStage; }
    public void setTypeStage(String typeStage) { this.typeStage = typeStage; }

    public String getType() { return typeStage; }
    public void setType(String type) { this.typeStage = type; }

    public int getDureeMois() { return dureeMois; }
    public void setDureeMois(int dureeMois) { this.dureeMois = dureeMois; }

    public int getDuree() { return dureeMois; }
    public void setDuree(int duree) { this.dureeMois = duree; }

    public String getDatePublication() { return datePublication; }
    public void setDatePublication(String datePublication) { this.datePublication = datePublication; }

    public String getDateDebut() { return datePublication; }
    public void setDateDebut(String dateDebut) { this.datePublication = dateDebut; }

    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }

    public String getStatut() {
        return (statut == null || statut.isBlank()) ? "publiee" : statut;
    }
    public void setStatut(String statut) { this.statut = statut; }

    public double getRemuneration() { return remuneration; }
    public void setRemuneration(double remuneration) { this.remuneration = remuneration; }

    public int getCandidatureCount() { return candidatureCount; }
    public void setCandidatureCount(int candidatureCount) { this.candidatureCount = candidatureCount; }

    @Override
    public String toString() {
        return titre + " (" + (partenaireNom != null ? partenaireNom : "Partenaire") + ")";
    }
}
