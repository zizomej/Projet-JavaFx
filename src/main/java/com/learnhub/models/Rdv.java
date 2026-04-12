package com.learnhub.models;

/**
 * Maps to the `rdv` table in gestion_universitaire DB.
 * Columns: id, motif, description, date_demande, statut,
 *          compte_rendu, ordonnance_url, etudiant_id, creneau_id
 */
public class Rdv {
    private int id;
    private String motif;
    private String description;
    private String dateDemande;
    private String statut;
    private String compteRendu;
    private String ordonnanceUrl;
    private int etudiantId;
    private int creneauId;
    private String etudiantNom; // computed via JOIN
    private String medecinNom;  // computed via JOIN (creneau -> utilisateur)

    public Rdv() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String dateDemande) { this.dateDemande = dateDemande; }
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
    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }
    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    // Aliases for backward compat with old RdvDAO/controllers
    public int getPatientId() { return etudiantId; }
    public void setPatientId(int id) { this.etudiantId = id; }
    public String getPatientNom() { return etudiantNom; }
    public void setPatientNom(String nom) { this.etudiantNom = nom; }
    public String getDateHeure() { return dateDemande; }
    public void setDateHeure(String dh) { this.dateDemande = dh; }
    public String getNotes() { return compteRendu; }
    public void setNotes(String notes) { this.compteRendu = notes; }
}
