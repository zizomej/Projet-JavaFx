package com.learnhub.models;

import java.time.LocalDateTime;

public class QuizSoumission {
    private int id;
    private int quiz_id;
    private int etudiant_id;
    private int note_obtenue;
    private LocalDateTime date_soumission;

    public QuizSoumission() {}

    public QuizSoumission(int id, int quiz_id, int etudiant_id, int note_obtenue, LocalDateTime date_soumission) {
        this.id = id;
        this.quiz_id = quiz_id;
        this.etudiant_id = etudiant_id;
        this.note_obtenue = note_obtenue;
        this.date_soumission = date_soumission;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuiz_id() { return quiz_id; }
    public void setQuiz_id(int quiz_id) { this.quiz_id = quiz_id; }
    public int getEtudiant_id() { return etudiant_id; }
    public void setEtudiant_id(int etudiant_id) { this.etudiant_id = etudiant_id; }
    public int getNote_obtenue() { return note_obtenue; }
    public void setNote_obtenue(int note_obtenue) { this.note_obtenue = note_obtenue; }
    public LocalDateTime getDate_soumission() { return date_soumission; }
    public void setDate_soumission(LocalDateTime date_soumission) { this.date_soumission = date_soumission; }
}
