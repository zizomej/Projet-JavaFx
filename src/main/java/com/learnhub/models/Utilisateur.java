package com.learnhub.models;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role; // admin, professeur, etudiant, parent, medecin
    private String telephone;
    private String cin;
    private String statut; // ACTIF, actif, inactif
    private String dateInscription;
    private Integer filiereId;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String role) {
        this.nom = nom; this.prenom = prenom; this.email = email;
        this.motDePasse = motDePasse; this.role = role; this.statut = "actif";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    // Alias kept for backward compat with login code
    public String getPassword() { return motDePasse; }
    public void setPassword(String p) { this.motDePasse = p; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    // Alias kept for backward compat
    public String getRoles() { return role; }
    public void setRoles(String role) { this.role = role; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public boolean isActif() { return statut != null && (statut.equalsIgnoreCase("actif") || statut.equalsIgnoreCase("ACTIF")); }
    public void setActif(boolean actif) { this.statut = actif ? "actif" : "inactif"; }
    public String getDateInscription() { return dateInscription; }
    public void setDateInscription(String dateInscription) { this.dateInscription = dateInscription; }
    public Integer getFiliereId() { return filiereId; }
    public void setFiliereId(Integer filiereId) { this.filiereId = filiereId; }

    public String getNomComplet() { return (prenom != null ? prenom : "") + " " + (nom != null ? nom : ""); }

    public String getRoleLabel() {
        if (role == null) return "Inconnu";
        return switch (role.toLowerCase()) {
            case "admin"      -> "Administrateur";
            case "professeur" -> "Professeur";
            case "etudiant"   -> "Étudiant";
            case "parent"     -> "Parent";
            case "medecin"    -> "Médecin";
            default -> role;
        };
    }

    @Override
    public String toString() { return getNomComplet() + " (" + getRoleLabel() + ")"; }
}
