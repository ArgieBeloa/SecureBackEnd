package com.example.ThesisBackend.controller;

import com.example.ThesisBackend.Model.EventModel;
import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.eventUtils.EventEvaluationDetails;
import com.example.ThesisBackend.security.JWTService;
import com.example.ThesisBackend.service.EventService;
import com.example.ThesisBackend.service.StudentService;
import com.example.ThesisBackend.studentUtils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    EventService eventService;


    @Autowired
    private JWTService jwtService;

    // ✅ GET student by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        Optional<StudentModel> studentOpt = studentService.getStudentById(id);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        if (!student.getStudentNumber().equals(studentNumberFromToken)) {
            return ResponseEntity.status(403).body("🚫 You are not authorized to access this student’s data");
        }

        return ResponseEntity.ok(student);
    }

    // ✅ Add Recent Evaluation
    @PostMapping("/{studentId}/addRecentEvaluation")
    public ResponseEntity<?> addRecentEvaluation(
            @PathVariable String studentId,
            @RequestBody StudentRecentEvaluation event,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        // Only student themself can add evaluation
        if (!student.getStudentNumber().equals(studentNumberFromToken) &&
                !"OFFICER".equalsIgnoreCase(role) &&
                !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("🚫 Unauthorized: Only student or officer/admin can modify");
        }

        if (student.getStudentRecentEvaluations() == null) {
            student.setStudentRecentEvaluations(new ArrayList<>());
        }

        boolean already = student.getStudentRecentEvaluations().stream()
                .anyMatch(e -> e.getEventId().equals(event.getEventId()));

        if (already) {
            return ResponseEntity.badRequest()
                    .body("⚠️ Event already in student's Recent Evaluation list: " + event.getEventTitle());
        }

        StudentModel updatedStudent = studentService.addRecentEvaluation(studentId, event);
        return ResponseEntity.ok(updatedStudent);
    }

    // ✅ Add Attendance and Evaluation (Student or Officer/Admin)
    @PostMapping("/{studentId}/addEventAttendanceAndEvaluation")
    public ResponseEntity<?> addEventAttendanceAndEvaluation(
            @PathVariable String studentId,
            @RequestBody StudentEventAttendedAndEvaluationDetails event,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        boolean isStudentSelf = student.getStudentNumber().equals(studentNumberFromToken);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403)
                    .body("🚫 Unauthorized: Only the student or an officer/admin can add attendance");
        }

        if (student.getStudentEventAttendedAndEvaluationDetails() == null) {
            student.setStudentEventAttendedAndEvaluationDetails(new ArrayList<>());
        }

        boolean already = student.getStudentEventAttendedAndEvaluationDetails().stream()
                .anyMatch(e -> e.getEventId().equals(event.getEventId()));

        if (already) {
            return ResponseEntity.badRequest()
                    .body("⚠️ Event already exists in student's attended list: " + event.getEventTitle());
        }

        StudentModel updatedStudent = studentService.addEventAttendedAndEvaluation(studentId, event);

        String message = isOfficerOrAdmin
                ? "✅ Officer successfully added attendance for student: " + student.getStudentName()
                : "✅ Attendance recorded successfully for: " + student.getStudentName();

        return ResponseEntity.ok(Map.of(
                "message", message,
                "updatedStudent", updatedStudent
        ));
    }

    // ✅ Add Attended Event (Student or Officer/Admin)
    @PostMapping("/{studentId}/addEventAttendance")
    public ResponseEntity<?> addEventAttendance(
            @PathVariable String studentId,
            @RequestBody StudentEventAttended event,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        boolean isStudentSelf = student.getStudentNumber().equals(studentNumberFromToken);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403)
                    .body("🚫 Unauthorized: Only the student or an officer/admin can add attendance");
        }

        if (student.getStudentEventAttended() == null) {
            student.setStudentEventAttended(new ArrayList<>());
        }

        boolean already = student.getStudentEventAttended().stream()
                .anyMatch(e -> e.getEventId().equals(event.getEventId()));

        if (already) {
            return ResponseEntity.badRequest()
                    .body("⚠️ Event already in student's Attended list: " + event.getEventTitle());
        }

        StudentModel updatedStudent = studentService.addEventAttendance(studentId, event);

        String message = isOfficerOrAdmin
                ? "✅ Officer successfully added attendance for student: " + student.getStudentName()
                : "✅ Attendance recorded successfully for: " + student.getStudentName();

        return ResponseEntity.ok(Map.of(
                "message", message,
                "updatedStudent", updatedStudent
        ));
    }

    // ✅ Add Upcoming Event
    @PostMapping("/{studentId}/addUpcomingEvent")
    public ResponseEntity<?> addUpcomingEvent(
            @PathVariable String studentId,
            @RequestBody StudentUpcomingEvents event,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        boolean isStudentSelf = student.getStudentNumber().equals(studentNumberFromToken);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403)
                    .body("🚫 Unauthorized: Only the student or an officer/admin can add upcoming events");
        }

        if (student.getStudentUpcomingEvents() == null) {
            student.setStudentUpcomingEvents(new ArrayList<>());
        }

        boolean already = student.getStudentUpcomingEvents().stream()
                .anyMatch(e -> e.getEventId().equals(event.getEventId()));

        if (already) {
            return ResponseEntity.badRequest()
                    .body("⚠️ Event already in student's upcoming list: " + event.getEventTitle());
        }

        StudentModel updatedStudent = studentService.addUpcomingEvent(studentId, event);

        String message = isOfficerOrAdmin
                ? "✅ Officer added upcoming event for student: " + student.getStudentName()
                : "✅ Upcoming event added successfully";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "updatedStudent", updatedStudent
        ));
    }
    @PostMapping("/{eventId}/addEvaluation")
    public ResponseEntity<?> addEventEvaluation(
            @PathVariable String eventId,
            @RequestBody EventEvaluationDetails evaluation,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Step 1: Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Missing or invalid token"));
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Invalid or expired token"));
        }

        // 🔒 Step 2: Extract role from token
        String role = jwtService.getRoleFromToken(token);

        // ✅ Step 3: Allow STUDENT, OFFICER, and ADMIN
        if (!"STUDENT".equalsIgnoreCase(role)
                && !"OFFICER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "🚫 Unauthorized: Only student, officer, or admin can add evaluations"
            ));
        }

        // ✅ Step 4: Perform add evaluation
        try {
            EventModel updatedEvent = eventService.addEventEvaluation(eventId, evaluation, role);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Evaluation added successfully by " + role,
                    "updatedEvent", updatedEvent
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ " + e.getMessage()));
        }
    }

    // PATCH
    @PatchMapping("/{studentId}/events/{eventId}/markEvaluated")
    public ResponseEntity<?> markEventAsEvaluated(
            @PathVariable String studentId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Step 1: Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        // 🔒 Step 2: Extract info from token
        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        // 🔍 Step 3: Check student existence
        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        // 🔒 Step 4: Security — Only student, OFFICER, or ADMIN can update
        boolean isStudentSelf = student.getStudentNumber().equals(studentNumberFromToken);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403).body("🚫 Unauthorized: Only the student, officer, or admin can update this record.");
        }

        // ✅ Step 5: Perform update
        try {
            StudentModel updatedStudent = studentService.updateStudentAttendedEvaluated(studentId, eventId);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Event marked as evaluated successfully",
                    "updatedStudent", updatedStudent
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("⚠️ " + e.getMessage());
        }
    }

    @PatchMapping("/{studentId}/events/{eventId}/profileEvaluated")
    public ResponseEntity<?> updateProfileData(
            @PathVariable String studentId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Step 1: Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        // 🔒 Step 2: Extract info from token
        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        // 🔍 Step 3: Check student existence
        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        // 🔒 Step 4: Security — Only student, OFFICER, or ADMIN can update
        boolean isStudentSelf = student.getStudentNumber().equals(studentNumberFromToken);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403)
                    .body("🚫 Unauthorized: Only the student, officer, or admin can update this record.");
        }

        // ✅ Step 5: Perform update
        try {
            StudentModel updatedStudent = studentService.updateStudentAttendedEvaluated(studentId, eventId, token);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Event marked as evaluated successfully",
                    "updatedStudent", updatedStudent
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("⚠️ " + e.getMessage());
        }
    }

    @DeleteMapping("/{studentId}/notifications/{notificationId}")
    public ResponseEntity<?> deleteStudentNotification(
            @PathVariable String studentId,
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        // 🧩 Extract user info
        String requesterStudentNumber = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        try {
            StudentModel updatedStudent = studentService.deleteStudentNotificationById(
                    studentId,
                    notificationId,
                    requesterStudentNumber,
                    role
            );

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Notification deleted successfully",
                    "updatedStudent", updatedStudent
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ " + e.getMessage()));
        }
    }





}
