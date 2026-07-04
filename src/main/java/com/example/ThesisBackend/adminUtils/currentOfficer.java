package com.example.ThesisBackend.adminUtils;
import  com.example.ThesisBackend.studentUtils.OfficerCredentials;

public class currentOfficer extends OfficerCredentials{

    private String studentId;
    private String studentName;
    private String studentNumber;

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

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
}
