package com.learnhub.models;

public class Creneau {
    private int id;
    private int medecinId;
    private String medecinNom;
    private String date;
    private String heureDebut;
    private String heureFin;
    private boolean disponible;

    public Creneau() { this.disponible = true; }
    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public int getMedecinId() { return medecinId; } public void setMedecinId(int v) { medecinId = v; }
    public String getMedecinNom() { return medecinNom; } public void setMedecinNom(String v) { medecinNom = v; }
    public String getDate() { return date; } public void setDate(String v) { date = v; }
    public String getHeureDebut() { return heureDebut; } public void setHeureDebut(String v) { heureDebut = v; }
    public String getHeureFin() { return heureFin; } public void setHeureFin(String v) { heureFin = v; }
    public boolean isDisponible() { return disponible; } public void setDisponible(boolean v) { disponible = v; }
    @Override public String toString() { return date + " " + heureDebut + " - " + heureFin; }
}
