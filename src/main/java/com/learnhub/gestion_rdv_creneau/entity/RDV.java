package com.learnhub.gestion_rdv_creneau.entity;

import java.time.LocalDate;

public class RDV {
    private int id;
    private String motif;
    private String description;
    private LocalDate dateDemande;
    private String statut;
    private String compteRendu;
    private String ordonnanceUrl;
    private int etudiantId;
    private int creneauId;

    public RDV() {}

    public RDV(int id, String motif, String description, LocalDate dateDemande, String statut, String compteRendu, String ordonnanceUrl, int etudiantId, int creneauId) {
        this.id = id;
        this.motif = motif;
        this.description = description;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.compteRendu = compteRendu;
        this.ordonnanceUrl = ordonnanceUrl;
        this.etudiantId = etudiantId;
        this.creneauId = creneauId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }

    public String getOrdonnanceUrl() { return ordonnanceUrl; }
    public void setOrdonnanceUrl(String ordonnanceUrl) { this.ordonnanceUrl = ordonnanceUrl; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public int getCreneauId() { return creneauId; }
    public void setCreneauId(int creneauId) { this.creneauId = creneauId; }
}


