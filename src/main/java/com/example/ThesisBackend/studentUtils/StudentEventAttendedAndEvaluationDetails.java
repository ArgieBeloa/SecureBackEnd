package com.example.ThesisBackend.studentUtils;


public class StudentEventAttendedAndEvaluationDetails {
    private String eventId;
    private String eventTitle;
    private String eventDateAndTime;
    private Boolean attended;
    private Boolean evaluated;

    public String getEventDateAndTime() {
        return eventDateAndTime;
    }

    public void setEventDateAndTime(String eventDateAndTime) {
        this.eventDateAndTime = eventDateAndTime;
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

    public Boolean getAttended() {
        return attended;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended;
    }

    public Boolean getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(Boolean evaluated) {
        this.evaluated = evaluated;
    }
}

