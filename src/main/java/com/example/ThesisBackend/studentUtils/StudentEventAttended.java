package com.example.ThesisBackend.studentUtils;


import java.time.LocalDateTime;

public class StudentEventAttended {

    private String eventId;
    private String eventTitle;
    private LocalDateTime evaluationTime;
    private String studentDateAttended;
    private boolean evaluated;

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

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

    public String getStudentDateAttended() {
        return studentDateAttended;
    }

    public LocalDateTime getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }

    public void setStudentDateAttended(String studentDateAttended) {
        this.studentDateAttended = studentDateAttended;
    }
}
