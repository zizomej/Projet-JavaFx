package com.learnhub.models;

public class Ressource {
    private int id;
    private String titre;
    private String type;
    private String url;
    private boolean estPublic;
    private int moduleId;
    
    // Jointures
    private String moduleIntitule;

    public Ressource() {
    }

    public Ressource(int id, String titre, String type, String url, boolean estPublic, int moduleId) {
        this.id = id;
        this.titre = titre;
        this.type = type;
        this.url = url;
        this.estPublic = estPublic;
        this.moduleId = moduleId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public boolean isEstPublic() { return estPublic; }
    public void setEstPublic(boolean estPublic) { this.estPublic = estPublic; }
    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }
    
    public String getModuleIntitule() { return moduleIntitule; }
    public void setModuleIntitule(String moduleIntitule) { this.moduleIntitule = moduleIntitule; }

    @Override
    public String toString() {
        return titre + " (" + type + ")";
    }
}
