package com.learnhub.models;

public class Seance {
    private int id;
    private int moduleId;
    private String moduleTitre;
    private String dateSeance;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private String type;
    private int enseignantId;
    private String enseignantNom;
    private String description;

    public Seance() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public String getModuleTitre() { return moduleTitre; }
    public void setModuleTitre(String moduleTitre) { this.moduleTitre = moduleTitre; }

    public String getDateSeance() { return dateSeance; }
    public void setDateSeance(String dateSeance) { this.dateSeance = dateSeance; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getEnseignantNom() { return enseignantNom; }
    public void setEnseignantNom(String enseignantNom) { this.enseignantNom = enseignantNom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Backward-compatible aliases.
    public String getDate() { return dateSeance; }
    public void setDate(String date) { this.dateSeance = date; }

    @Override
    public String toString() {
        String module = moduleTitre != null ? moduleTitre : "Seance";
        String date = dateSeance != null ? dateSeance : "";
        String start = heureDebut != null ? heureDebut : "";
        return (module + " - " + date + " " + start).trim();
    }
}
