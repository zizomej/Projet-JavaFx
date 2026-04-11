package com.learnhub.models;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String roles; // ROLE_ADMIN, ROLE_PROFESSEUR, ROLE_ETUDIANT, ROLE_PARENT, ROLE_MEDECIN
    private String telephone;
    private String adresse;
    private String dateNaissance;
    private boolean actif;
    private String photo;

    public Utilisateur() { this.actif = true; }

    public Utilisateur(String nom, String prenom, String email, String password, String roles) {
        this.nom = nom; this.prenom = prenom; this.email = email;
        this.password = password; this.roles = roles; this.actif = true;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getNomComplet() { return (prenom != null ? prenom : "") + " " + (nom != null ? nom : ""); }
    public String getRoleLabel() {
        if (roles == null) return "Inconnu";
        return switch (roles) {
            case "ROLE_ADMIN" -> "Administrateur";
            case "ROLE_PROFESSEUR" -> "Professeur";
            case "ROLE_ETUDIANT" -> "Étudiant";
            case "ROLE_PARENT" -> "Parent";
            case "ROLE_MEDECIN" -> "Médecin";
            default -> roles;
        };
    }

    @Override
    public String toString() { return getNomComplet() + " (" + getRoleLabel() + ")"; }
}
