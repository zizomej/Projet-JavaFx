package com.learnhub.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evenement {
    private int id;
    private String titre;
    private String description;
    private String typeEvenement;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String statut;
    private int lieuId;
    private String lieuNom;
    private String organisateurNom;
    private int organisateurId;
    private String categorie;
    private int capacite;
    private String image;

    // Constructeurs
    public Evenement() {}

    public Evenement(int id, String titre, String description, String typeEvenement,
                     LocalDate dateDebut, LocalDate dateFin, LocalTime heureDebut,
                     LocalTime heureFin, String statut, int lieuId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.typeEvenement = typeEvenement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = statut;
        this.lieuId = lieuId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeEvenement() { return typeEvenement; }
    public void setTypeEvenement(String typeEvenement) { this.typeEvenement = typeEvenement; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getLieuId() { return lieuId; }
    public void setLieuId(int lieuId) { this.lieuId = lieuId; }

    public String getLieuNom() { return lieuNom; }
    public void setLieuNom(String lieuNom) { this.lieuNom = lieuNom; }

    public String getOrganisateurNom() { return organisateurNom; }
    public void setOrganisateurNom(String organisateurNom) { this.organisateurNom = organisateurNom; }

    public int getOrganisateurId() { return organisateurId; }
    public void setOrganisateurId(int organisateurId) { this.organisateurId = organisateurId; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    // Méthodes compatibilité avec l'ancien code
    public String getDate() { return dateDebut != null ? dateDebut.toString() : ""; }
    public void setDate(String date) { this.dateDebut = LocalDate.parse(date); }

    public String getLieu() { return lieuNom; }
    public void setLieu(String lieu) { this.lieuNom = lieu; }

    @Override
    public String toString() {
        return titre;
    }
}
