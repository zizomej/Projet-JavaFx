package com.learnhub.medical.entity;

import java.time.LocalDateTime;

public class Utilisateur {
    private int id;
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private String cin;
    private String role; // 'admin', 'medecin', 'etudiant', 'professeur'
    private String telephone;
    private String statut; // 'actif', 'inactif', etc.
    private LocalDateTime dateInscription;

    public Utilisateur() {}

    public Utilisateur(int id, String email, String nom, String prenom, String role, String statut) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getFullName() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    public String getRoles() {
        // Pour compatibilité avec SessionManager qui attend un format Symfony style (ROLE_...)
        if (role == null) return "ROLE_USER";
        return switch (role.toLowerCase()) {
            case "admin" -> "ROLE_ADMIN";
            case "medecin" -> "ROLE_MEDECIN";
            case "etudiant" -> "ROLE_STUDENT";
            case "professeur" -> "ROLE_PROFESSOR";
            default -> "ROLE_USER";
        };
    }
    
    public void setRoles(String roles) {
        // Inverse mapping pour compatibilité
        if (roles == null) return;
        this.role = switch (roles.toUpperCase()) {
            case "ROLE_ADMIN" -> "admin";
            case "ROLE_MEDECIN" -> "medecin";
            case "ROLE_STUDENT" -> "etudiant";
            case "ROLE_PROFESSOR" -> "professeur";
            default -> "utilisateur";
        };
    }
}
