package com.example.ThesisBackend.Model;

import com.example.ThesisBackend.studentUtils.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "studentData")
public class StudentModel {

    @Id
    private  String id;

    // credentials
    private  String studentNumber;
    private String studentPassword;

   // Access control
   private String role = "STUDENT";

    // info
    private  String studentName;
    private String course;
    private String department;
    public String notificationId;

    // Array of classes
    private List<StudentUpcomingEvents> studentUpcomingEvents;
    private List<StudentEventAttended> studentEventAttended;
    private List<StudentRecentEvaluation> studentRecentEvaluations;
    private List<StudentNotification> studentNotifications;
    private List<StudentEventAttendedAndEvaluationDetails> studentEventAttendedAndEvaluationDetails;





    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStudentPassword() {
        return studentPassword;
    }

    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public List<StudentUpcomingEvents> getStudentUpcomingEvents() {
        return studentUpcomingEvents;
    }

    public void setStudentUpcomingEvents(List<StudentUpcomingEvents> studentUpcomingEvents) {
        this.studentUpcomingEvents = studentUpcomingEvents;
    }

    public List<StudentEventAttendedAndEvaluationDetails> getStudentEventAttendedAndEvaluationDetails() {
        return studentEventAttendedAndEvaluationDetails;
    }

    public void setStudentEventAttendedAndEvaluationDetails(List<StudentEventAttendedAndEvaluationDetails> studentEventAttendedAndEvaluationDetails) {
        this.studentEventAttendedAndEvaluationDetails = studentEventAttendedAndEvaluationDetails;
    }

    public List<StudentEventAttended> getStudentEventAttended() {
        return studentEventAttended;
    }

    public void setStudentEventAttended(List<StudentEventAttended> studentEventAttended) {
        this.studentEventAttended = studentEventAttended;
    }

    public List<StudentNotification> getStudentNotifications() {
        return studentNotifications;
    }

    public void setStudentNotifications(List<StudentNotification> studentNotifications) {
        this.studentNotifications = studentNotifications;
    }

    public List<StudentRecentEvaluation> getStudentRecentEvaluations() {
        return studentRecentEvaluations;
    }

    public void setStudentRecentEvaluations(List<StudentRecentEvaluation> studentRecentEvaluations) {
        this.studentRecentEvaluations = studentRecentEvaluations;
    }
}
