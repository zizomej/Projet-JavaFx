package com.learnhub.medical.entity;

import java.sql.Time;
import java.time.LocalTime;

public class Creneau {
    private int id;
    private String jour; // Format AAAA-MM-JJ or "Lundi", etc.
    private LocalTime heure;
    private String recurrence;
    private boolean disponibilite;
    private String medecinName; // Pour affichage (Dr. Khalil par défaut)

    public Creneau() {}

    public Creneau(int id, String jour, LocalTime heure, String recurrence, boolean disponibilite) {
        this.id = id;
        this.jour = jour;
        this.heure = heure;
        this.recurrence = recurrence;
        this.disponibilite = disponibilite;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }

    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }

    public boolean isDisponibilite() { return disponibilite; }
    public void setDisponibilite(boolean disponibilite) { this.disponibilite = disponibilite; }

    public String getMedecinName() { return medecinName; }
    public void setMedecinName(String medecinName) { this.medecinName = medecinName; }

    @Override
    public String toString() {
        return jour + " à " + heure + " (" + recurrence + ")";
    }
}
