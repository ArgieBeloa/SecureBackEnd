package com.example.ThesisBackend.service;

import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import com.example.ThesisBackend.studentUtils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;


    @Autowired
    private JWTService jwtService;

    // ✅ GET
    public Optional<StudentModel> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    // POST
    public StudentModel addUpcomingEvent(String studentId, StudentUpcomingEvents event) {
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            System.out.println("❌ Student not found with ID: " + studentId);
            return null;
        }

        StudentModel student = studentOpt.get();

        // Initialize list if null
        if (student.getStudentUpcomingEvents() == null) {
            student.setStudentUpcomingEvents(new ArrayList<>());
        }
        boolean alreadyInUpcoming = student.getStudentUpcomingEvents().stream()
                .anyMatch(upcoming -> upcoming.getEventId().equals(event.getEventId()));

        if (alreadyInUpcoming) {
            System.out.println("⚠️ Event already in student's upcoming list: " + event.getEventId());
            return student;
        }

        // Add the new event
        student.getStudentUpcomingEvents().add(event);

        // Save updated student
        studentRepository.save(student);

        System.out.println("✅ Upcoming event added for student: " + student.getStudentName());
        return student;
    }

    public StudentModel addEventAttendance(String studentId, StudentEventAttended event) {
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            System.out.println("❌ Student not found with ID: " + studentId);
            return null;
        }

        StudentModel student = studentOpt.get();

        // Initialize list if null
        if (student.getStudentEventAttended() == null) {
            student.setStudentEventAttended(new ArrayList<>());
        }
        boolean already = student.getStudentEventAttended().stream()
                .anyMatch(eventData-> eventData.getEventId().equals(event.getEventId()));

        if (already) {
            System.out.println("⚠️ Event already in student's Attendance list: " + event.getEventId());
            return student;
        }

        // Add the new event
        student.getStudentEventAttended().add(event);

        // Save updated student
        studentRepository.save(student);

        System.out.println("✅ Event added for student: " + student.getStudentName());
        return student;
    }

    public StudentModel addEventAttendedAndEvaluation(String studentId, StudentEventAttendedAndEvaluationDetails event) {
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            System.out.println("❌ Student not found with ID: " + studentId);
            return null;
        }

        StudentModel student = studentOpt.get();

        // Initialize list if null
        if (student.getStudentEventAttendedAndEvaluationDetails() == null) {
            student.setStudentEventAttendedAndEvaluationDetails(new ArrayList<>());
        }

        boolean already = student.getStudentEventAttendedAndEvaluationDetails().stream()
                .anyMatch(eventData-> eventData.getEventId().equals(event.getEventId()));

        if (already) {
            System.out.println("⚠️ Event already in student's Profile list: " + event.getEventId());
            return student;
        }

        // Add the new event
        student.getStudentEventAttendedAndEvaluationDetails().add(event);

        // Save updated student
        studentRepository.save(student);

        System.out.println("✅ Event Attendance and Evaluation added for student: " + student.getStudentName());
        return student;
    }

    public List<StudentModel> addAllStudentNotification(StudentNotification event, String token) {
        // ✅ Get role from JWT token
        String role = jwtService.getRoleFromToken(token);

        // ✅ Only OFFICER or ADMIN can add notifications
        if (!"OFFICER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: Only OFFICER or ADMIN can add notifications.");
        }

        // ✅ Get all students
        List<StudentModel> allStudents = studentRepository.findAll();

        if (allStudents.isEmpty()) {
            System.out.println("⚠️ No students found in the database.");
            return new ArrayList<>();
        }

        // ✅ Add notification to each student
        for (StudentModel student : allStudents) {
            if (student.getStudentNotifications() == null) {
                student.setStudentNotifications(new ArrayList<>());
            }

            student.getStudentNotifications().add(event);
            studentRepository.save(student);

            System.out.println("✅ Notification added for student: " + student.getStudentName());
        }

        return allStudents; // optional: return updated students
    }


    public StudentModel addRecentEvaluation(String studentId, StudentRecentEvaluation event) {
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            System.out.println("❌ Student not found with ID: " + studentId);
            return null;
        }

        StudentModel student = studentOpt.get();

        // Initialize list if null
        if (student.getStudentRecentEvaluations() == null) {
            student.setStudentRecentEvaluations(new ArrayList<>());
        }
        boolean already = student.getStudentRecentEvaluations().stream()
                .anyMatch(eventData -> eventData.getEventId().equals(event.getEventId()));

        if (already) {
            System.out.println("⚠️ Event already in student's Recent Evaluation list: " + event.getEventId());
            return student;
        }
        // Add the new event
        student.getStudentRecentEvaluations().add(event);

        // Save updated student
        studentRepository.save(student);

        System.out.println("✅ Event Evaluation added for student: " + student.getStudentName());
        return student;
    }

    // PATCH
    public StudentModel updateStudentAttendedEvaluated(String studentId, String eventId) {
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isEmpty()) {
            throw new RuntimeException("❌ Student not found with ID: " + studentId);
        }

        StudentModel student = studentOpt.get();

        // 🧩 Check if attended list exists
        if (student.getStudentEventAttended() == null || student.getStudentEventAttended().isEmpty()) {
            throw new RuntimeException("⚠️ No attended events found for this student.");
        }

        boolean updated = false;

        for (StudentEventAttended detail : student.getStudentEventAttended()) {
            if (detail.getEventId().equals(eventId)) {
                if (Boolean.TRUE.equals(detail.isEvaluated())) {
                    throw new RuntimeException("⚠️ Event already marked as evaluated.");
                }

                detail.setEvaluated(true);
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new RuntimeException("❌ Event not found in student's attended list: " + eventId);
        }

        studentRepository.save(student);
        System.out.println("✅ Updated evaluated=true for eventId: " + eventId + " in student: " + student.getStudentName());
        return student;
    }


    public StudentModel updateStudentAttendedEvaluated(String studentId, String eventId, String token) {
        // 🔒 Validate token
        if (token == null || !jwtService.validateToken(token)) {
            throw new RuntimeException("❌ Invalid or expired token");
        }

        // Extract info
        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        // Find student
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("❌ Student not found with ID: " + studentId);
        }

        StudentModel student = studentOpt.get();

        // Authorization check
        boolean isStudentSelf = student.getStudentNumber().equals(studentNumberFromToken);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            throw new RuntimeException("🚫 Unauthorized: You cannot update this student's event.");
        }

        // Check attended events list
        if (student.getStudentEventAttendedAndEvaluationDetails() == null ||
                student.getStudentEventAttendedAndEvaluationDetails().isEmpty()) {
            throw new RuntimeException("⚠️ No attended events found for this student.");
        }

        boolean updated = false;

        // 🔄 Loop and update the matching event
        for (StudentEventAttendedAndEvaluationDetails detail :
                student.getStudentEventAttendedAndEvaluationDetails()) {

            if (detail.getEventId().equals(eventId)) {
                if (Boolean.TRUE.equals(detail.getEvaluated())) {
                    throw new RuntimeException("⚠️ Event already marked as evaluated.");
                }

                detail.setEvaluated(true);
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new RuntimeException("❌ Event not found in student's attended list: " + eventId);
        }

        // 💾 Save to DB
        studentRepository.save(student);
        System.out.println("✅ Updated evaluated=true for eventId: " + eventId + " in student: " + student.getStudentName());
        return student;
    }

   // DELETE
   public StudentModel deleteStudentNotificationById(String studentId, String notificationId, String requesterStudentNumber, String role) {
       // 🔍 Find student
       Optional<StudentModel> studentOpt = studentRepository.findById(studentId);

       if (studentOpt.isEmpty()) {
           throw new RuntimeException("❌ Student not found with ID: " + studentId);
       }

       StudentModel student = studentOpt.get();

       // 🔒 Check role permissions
       boolean isStudentSelf = student.getStudentNumber().equals(requesterStudentNumber);
       boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

       if (!isStudentSelf && !isOfficerOrAdmin) {
           throw new RuntimeException("🚫 Unauthorized: Only the student, officer, or admin can delete notifications.");
       }

       // 🧾 Check if list exists or is empty
       if (student.getStudentNotifications() == null || student.getStudentNotifications().isEmpty()) {
           throw new RuntimeException("⚠️ No notifications found for this student.");
       }

       // 🗑️ Remove the notification
       boolean removed = student.getStudentNotifications().removeIf(
               notif -> notif.getEventId().equals(notificationId)
       );

       if (!removed) {
           throw new RuntimeException("❌ Notification not found with ID: " + notificationId);
       }

       // 💾 Save updated student
       studentRepository.save(student);

       System.out.println("✅ Notification deleted by " + role + " for student: " + student.getStudentName());
       return student;
   }






}
