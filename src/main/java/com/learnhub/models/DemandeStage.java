package com.learnhub.models;

public class DemandeStage {
    private int id;
    private int etudiantId;
    private String etudiantNom;
    private String etudiantEmail;
    private int offreStageId;
    private String offreTitre;
    private String partenaireNom;
    private String pieceJointe;
    private String motivation;
    private String dateDemande;
    private String statut;
    private String commentaire;

    public DemandeStage() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }

    public String getEtudiantEmail() { return etudiantEmail; }
    public void setEtudiantEmail(String etudiantEmail) { this.etudiantEmail = etudiantEmail; }

    public int getOffreStageId() { return offreStageId; }
    public void setOffreStageId(int offreStageId) { this.offreStageId = offreStageId; }

    public int getOffreId() { return offreStageId; }
    public void setOffreId(int offreId) { this.offreStageId = offreId; }

    public String getOffreTitre() { return offreTitre; }
    public void setOffreTitre(String offreTitre) { this.offreTitre = offreTitre; }

    public String getPartenaireNom() { return partenaireNom; }
    public void setPartenaireNom(String partenaireNom) { this.partenaireNom = partenaireNom; }

    public String getPieceJointe() { return pieceJointe; }
    public void setPieceJointe(String pieceJointe) { this.pieceJointe = pieceJointe; }

    public String getCvPath() { return pieceJointe; }
    public void setCvPath(String cvPath) { this.pieceJointe = cvPath; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public String getLettreMotivation() { return motivation; }
    public void setLettreMotivation(String lettreMotivation) { this.motivation = lettreMotivation; }

    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
