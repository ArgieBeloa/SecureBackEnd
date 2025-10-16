package com.example.ThesisBackend.eventUtils;

public class StudentEvaluationInfo {

    private String question;
    private double studentRate;


    public double getStudentRate() {
        return studentRate;
    }

    public void setStudentRate(double studentRate) {
        this.studentRate = studentRate;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
