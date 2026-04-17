package com.learnhub.models;

import java.time.LocalDateTime;

public class Quiz {
    private int id;
    private int module_id;
    private int professeur_id;
    private String titre;
    private String description;
    private LocalDateTime date_creation;
    private LocalDateTime deadline;
    private boolean is_visible;

    public Quiz() {}

    public Quiz(int id, int module_id, int professeur_id, String titre, String description, LocalDateTime date_creation, LocalDateTime deadline, boolean is_visible) {
        this.id = id;
        this.module_id = module_id;
        this.professeur_id = professeur_id;
        this.titre = titre;
        this.description = description;
        this.date_creation = date_creation;
        this.deadline = deadline;
        this.is_visible = is_visible;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getModule_id() { return module_id; }
    public void setModule_id(int module_id) { this.module_id = module_id; }
    public int getProfesseur_id() { return professeur_id; }
    public void setProfesseur_id(int professeur_id) { this.professeur_id = professeur_id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDate_creation() { return date_creation; }
    public void setDate_creation(LocalDateTime date_creation) { this.date_creation = date_creation; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public boolean isIs_visible() { return is_visible; }
    public void setIs_visible(boolean is_visible) { this.is_visible = is_visible; }
}
