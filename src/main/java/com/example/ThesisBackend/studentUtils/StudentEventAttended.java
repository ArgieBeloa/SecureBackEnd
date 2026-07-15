package com.example.ThesisBackend.studentUtils;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class StudentEventAttended {

    private String eventId;
    private String eventTitle;
    private Instant evaluationTime;
    private String studentDateAttended;
    private boolean evaluated;

    public Instant getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(Instant evaluationTime) {
        this.evaluationTime = evaluationTime;
    }

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



    public void setStudentDateAttended(String studentDateAttended) {
        this.studentDateAttended = studentDateAttended;
    }
}
