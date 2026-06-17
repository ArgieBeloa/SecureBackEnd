package com.example.ThesisBackend.adminUtils;


public class currentOfficer {

    private String studentId;
    private String studentName;
    private String studentNumber;
    private Boolean canEditEvent;
    private Boolean canAddEvent;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Boolean getCanEditEvent() {
        return canEditEvent;
    }

    public void setCanEditEvent(Boolean canEditEvent) {
        this.canEditEvent = canEditEvent;
    }

    public Boolean getCanAddEvent() {
        return canAddEvent;
    }

    public void setCanAddEvent(Boolean canAddEvent) {
        this.canAddEvent = canAddEvent;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
}
