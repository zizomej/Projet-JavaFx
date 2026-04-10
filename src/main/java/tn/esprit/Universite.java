package tn.esprit;

public class Universite {
    private int id;
    private String nom;
    private String type;
    private String ville;
    private String adresse;
    private String telephone;
    private String email;
    private int filieresCount; // Logical column for UI

    public Universite(int id, String nom, String type, String ville, String adresse, String telephone, String email, int filieresCount) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.ville = ville;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.filieresCount = filieresCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getFilieresCount() { return filieresCount; }
    public void setFilieresCount(int filieresCount) { this.filieresCount = filieresCount; }
}
