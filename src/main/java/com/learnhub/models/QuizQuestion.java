package com.learnhub.models;

public class QuizQuestion {
    private int id;
    private int quiz_id;
    private String texte_question;
    private String type_question;
    private int points;

    public QuizQuestion() {}

    public QuizQuestion(int id, int quiz_id, String texte_question, String type_question, int points) {
        this.id = id;
        this.quiz_id = quiz_id;
        this.texte_question = texte_question;
        this.type_question = type_question;
        this.points = points;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuiz_id() { return quiz_id; }
    public void setQuiz_id(int quiz_id) { this.quiz_id = quiz_id; }
    public String getTexte_question() { return texte_question; }
    public void setTexte_question(String texte_question) { this.texte_question = texte_question; }
    public String getType_question() { return type_question; }
    public void setType_question(String type_question) { this.type_question = type_question; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
