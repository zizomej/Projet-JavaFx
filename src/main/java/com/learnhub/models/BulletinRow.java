package com.learnhub.models;

public class BulletinRow {
    private String moduleCode;
    private String moduleIntitule;
    private int credits;

    private double noteCC = -1;
    private double noteTP = -1;
    private double noteExamen = -1;

    private double moyenneFinal = -1;
    private String mention;
    private String resultat; // Admise / Non Admis pour le dashboard
    private String appreciation;

    public BulletinRow(String moduleCode, String moduleIntitule, int credits) {
        this.moduleCode = moduleCode;
        this.moduleIntitule = moduleIntitule;
        this.credits = credits;
    }

    public void addNote(Note note) {
        if (note.getTypeNote() == null) return;
        
        String type = note.getTypeNote().toLowerCase();
        if (type.contains("cc") || type.contains("controle") || type.contains("ds")) {
            this.noteCC = note.getValeur();
        } else if (type.contains("tp") || type.contains("pratique") || type.contains("projet")) {
            this.noteTP = note.getValeur();
        } else if (type.contains("examen") || type.contains("exam")) {
            this.noteExamen = note.getValeur();
        } else {
            // par defaut si on ne sais pas, on met exam
            if (this.noteExamen == -1) this.noteExamen = note.getValeur();
        }
    }

    public void calculerMoyenne() {
        // Logique simplifiée : si TP/CC existent on pourrait pondérer, sinon moyenne arithmétique de ce qui existe
        double sum = 0;
        int count = 0;
        
        if (noteCC >= 0) { sum += noteCC; count++; }
        if (noteTP >= 0) { sum += noteTP; count++; }
        if (noteExamen >= 0) { sum += noteExamen; count++; }
        
        if (count > 0) {
            moyenneFinal = sum / count;
            
            if (moyenneFinal >= 16) {
                mention = "Très Bien";
                appreciation = "Excellente maîtrise des concepts. Continuez ainsi !";
            } else if (moyenneFinal >= 14) {
                mention = "Bien";
                appreciation = "Bon travail, très bons résultats dans l'ensemble.";
            } else if (moyenneFinal >= 12) {
                mention = "Assez Bien";
                appreciation = "Des résultats satisfaisants, mais peut mieux faire.";
            } else if (moyenneFinal >= 10) {
                mention = "Passable";
                appreciation = "Juste suffisant, efforts nécessaires pour s'améliorer.";
            } else {
                mention = "Non Admis";
                appreciation = "Des lacunes importantes, attention à redoubler d'efforts.";
            }
            
            resultat = moyenneFinal >= 10 ? "✓ Admis" : "X Non Admis";
        } else {
            moyenneFinal = -1;
            mention = "N/A";
            resultat = "-";
            appreciation = "En attente d'évaluation.";
        }
    }
    
    // Formatters for table display
    public String getNoteCCStr() { return noteCC >= 0 ? String.format("%.1f", noteCC) : "-"; }
    public String getNoteTPStr() { return noteTP >= 0 ? String.format("%.1f", noteTP) : "-"; }
    public String getNoteExamenStr() { return noteExamen >= 0 ? String.format("%.1f", noteExamen) : "-"; }
    public String getMoyenneStr() { 
        if (moyenneFinal < 0) return "-";
        if (moyenneFinal == (long) moyenneFinal) return String.format("%d/20", (long)moyenneFinal);
        return String.format(java.util.Locale.US, "%.2f/20", moyenneFinal).replace(".00", ""); 
    }
    public String getMoyenneValue() { return moyenneFinal >= 0 ? String.format("%.2f", moyenneFinal) : "0"; }

    // Getters and Setters
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getModuleIntitule() { return moduleIntitule; }
    public void setModuleIntitule(String moduleIntitule) { this.moduleIntitule = moduleIntitule; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public double getNoteCC() { return noteCC; }
    public void setNoteCC(double noteCC) { this.noteCC = noteCC; }

    public double getNoteTP() { return noteTP; }
    public void setNoteTP(double noteTP) { this.noteTP = noteTP; }

    public double getNoteExamen() { return noteExamen; }
    public void setNoteExamen(double noteExamen) { this.noteExamen = noteExamen; }

    public double getMoyenneFinal() { return moyenneFinal; }
    public void setMoyenneFinal(double moyenneFinal) { this.moyenneFinal = moyenneFinal; }

    public String getMention() { return mention; }
    public void setMention(String mention) { this.mention = mention; }

    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }

    public String getAppreciation() { return appreciation; }
    public void setAppreciation(String appreciation) { this.appreciation = appreciation; }
}
