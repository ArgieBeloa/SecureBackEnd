package com.example.ThesisBackend.eventUtils;

import java.util.List;

public class EventEvaluationDetails {

    private String studentName;
    private double studentAverageRate;
    private String studentSuggestion;

    private List<StudentEvaluationInfo> studentEvaluationInfos;

    public List<StudentEvaluationInfo> getStudentEvaluationInfos() {
        return studentEvaluationInfos;
    }

    public void setStudentEvaluationInfos(List<StudentEvaluationInfo> studentEvaluationInfos) {
        this.studentEvaluationInfos = studentEvaluationInfos;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public double getStudentAverageRate() {
        return studentAverageRate;
    }

    public void setStudentAverageRate(double studentAverageRate) {
        this.studentAverageRate = studentAverageRate;
    }

    public String getStudentSuggestion() {
        return studentSuggestion;
    }

    public void setStudentSuggestion(String studentSuggestion) {
        this.studentSuggestion = studentSuggestion;
    }
}
