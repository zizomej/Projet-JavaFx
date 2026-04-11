package com.learnhub.models;

import java.time.LocalDate;

public class DemandeStage {
    private int id;
    private LocalDate dateDemande;
    private String statut; // "en_attente", "acceptee", "refusee"
    private String pieceJointe;
    private String motivation;
    private int offreStageId;
    private String offreTitre;
    private int etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;

    public DemandeStage() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPieceJointe() { return pieceJointe; }
    public void setPieceJointe(String pieceJointe) { this.pieceJointe = pieceJointe; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public int getOffreStageId() { return offreStageId; }
    public void setOffreStageId(int offreStageId) { this.offreStageId = offreStageId; }

    public String getOffreTitre() { return offreTitre; }
    public void setOffreTitre(String offreTitre) { this.offreTitre = offreTitre; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }

    public String getEtudiantPrenom() { return etudiantPrenom; }
    public void setEtudiantPrenom(String etudiantPrenom) { this.etudiantPrenom = etudiantPrenom; }

    public String getEtudiantFullName() { return etudiantPrenom + " " + etudiantNom; }
}
