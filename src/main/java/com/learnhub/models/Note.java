package com.learnhub.models;

public class Note {
    private int id;
    private int etudiantId;
    private String etudiantNom;
    private int moduleId;
    private String moduleTitre;
    private double valeur;
    private String typeNote;
    private double coefficient = 1.0;
    private String dateSaisie;
    private int enseignantId;
    private String enseignantNom;
    private String commentaire;

    public Note() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public String getEtudiantNom() { return etudiantNom; }
    public void setEtudiantNom(String etudiantNom) { this.etudiantNom = etudiantNom; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public String getModuleTitre() { return moduleTitre; }
    public void setModuleTitre(String moduleTitre) { this.moduleTitre = moduleTitre; }

    public double getValeur() { return valeur; }
    public void setValeur(double valeur) { this.valeur = valeur; }

    public String getTypeNote() { return typeNote; }
    public void setTypeNote(String typeNote) { this.typeNote = typeNote; }

    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }

    public String getDateSaisie() { return dateSaisie; }
    public void setDateSaisie(String dateSaisie) { this.dateSaisie = dateSaisie; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getEnseignantNom() { return enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    // Backward-compatible aliases used by existing controllers/FXML tables.
    public String getType() { return typeNote; }
    public void setType(String type) { this.typeNote = type; }
    public String getModuleIntitule() { return moduleTitre; }
    public void setModuleIntitule(String v) { this.moduleTitre = v; }

    public String getDateEvaluation() { return dateSaisie; }
    public void setDateEvaluation(String dateEvaluation) { this.dateSaisie = dateEvaluation; }

    public String getMention() {
        if (valeur >= 16) return "Tres bien";
        if (valeur >= 14) return "Bien";
        if (valeur >= 12) return "Assez bien";
        if (valeur >= 10) return "Passable";
        return "Insuffisant";
    }
}
