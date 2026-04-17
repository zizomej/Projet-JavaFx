package com.learnhub.models;

public class QuizOption {
    private int id;
    private int question_id;
    private String texte_option;
    private boolean is_correct;

    public QuizOption() {}

    public QuizOption(int id, int question_id, String texte_option, boolean is_correct) {
        this.id = id;
        this.question_id = question_id;
        this.texte_option = texte_option;
        this.is_correct = is_correct;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuestion_id() { return question_id; }
    public void setQuestion_id(int question_id) { this.question_id = question_id; }
    public String getTexte_option() { return texte_option; }
    public void setTexte_option(String texte_option) { this.texte_option = texte_option; }
    public boolean isIs_correct() { return is_correct; }
    public void setIs_correct(boolean is_correct) { this.is_correct = is_correct; }
}
