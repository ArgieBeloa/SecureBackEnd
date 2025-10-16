package com.example.ThesisBackend.studentUtils;

public class StudentRecentEvaluation {

    private String eventId;
    private String eventTitle;
    private double studentRatingsGive;
    private String studentDateRated;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public double  getStudentRatingsGive() {
        return studentRatingsGive;
    }

    public void setStudentRatingsGive(int studentRatingsGive) {
        this.studentRatingsGive = studentRatingsGive;
    }

    public String getStudentDateRated() {
        return studentDateRated;
    }

    public void setStudentDateRated(String studentDateRated) {
        this.studentDateRated = studentDateRated;
    }
}