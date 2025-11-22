package models;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private List<Question> questions;
    private int passingPercentage; // e.g. 60 -> need >=60% to pass
    private int maxAttempts; // 0 or negative -> unlimited

    public Quiz() {
        this.questions = new ArrayList<>();
        this.passingPercentage = 60;
        this.maxAttempts = 0;
    }

    public Quiz(List<Question> questions, int passingPercentage, int maxAttempts) {
        this.questions = questions;
        this.passingPercentage = passingPercentage;
        this.maxAttempts = maxAttempts;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getPassingPercentage() {
        return passingPercentage;
    }

    public void setPassingPercentage(int passingPercentage) {
        this.passingPercentage = passingPercentage;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int totalQuestions() { 
        return questions == null ? 0 : questions.size(); 
    }
}

