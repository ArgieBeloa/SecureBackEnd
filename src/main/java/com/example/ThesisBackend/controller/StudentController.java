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

import java.util.*;

/**
 * 🎓 StudentController
 * Handles all student-related endpoints:
 * - Student data retrieval
 * - Attendance and evaluation tracking
 * - Upcoming and recent events
 * - Notifications
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    // 🔧 Dependencies
    @Autowired private StudentService studentService;
    @Autowired private EventService eventService;
    @Autowired private JWTService jwtService;

    /* --------------------------------------------------------------------------
     * 📘  GET: Fetch student data
     * -------------------------------------------------------------------------- */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(
            @PathVariable String id,
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

        String studentNumberFromToken = jwtService.getUsernameFromToken(token);
        Optional<StudentModel> studentOpt = studentService.getStudentById(id);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();
        if (!student.getStudentNumber().equals(studentNumberFromToken)) {
            return ResponseEntity.status(403)
                    .body("🚫 You are not authorized to access this student’s data");
        }

        return ResponseEntity.ok(student);
    }
    /* --------------------------------------------------------------------------
     * 🧾  POST: Add event attendance and evaluation record
     * -------------------------------------------------------------------------- */
    @PostMapping("/{studentId}/addAttendedEvaluation")
    public ResponseEntity<?> addAttendedEvaluation(
            @PathVariable String studentId,
            @RequestBody StudentEventAttendedAndEvaluationDetails event,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Validate token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Missing or invalid token"));
        }

        String token = authHeader.substring(7).trim();
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Invalid or expired token"));
        }

        String requesterNumber = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "❌ Student not found"));
        }

        StudentModel student = studentOpt.get();

        boolean isStudentSelf = student.getStudentNumber().equals(requesterNumber);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "🚫 Unauthorized: Only the student or an officer/admin can add attendance and evaluation."
            ));
        }

        // ✅ Add event attendance + evaluation
        StudentModel updatedStudent = studentService.addEventAttendedAndEvaluation(studentId, event);

        return ResponseEntity.ok(Map.of(
                "message", "✅ Event attendance and evaluation added successfully",
                "updatedStudent", updatedStudent
        ));
    }

    /* --------------------------------------------------------------------------
     * 📝  POST: Add recent evaluation
     * -------------------------------------------------------------------------- */
    @PostMapping("/{studentId}/addRecentEvaluation")
    public ResponseEntity<?> addRecentEvaluation(
            @PathVariable String studentId,
            @RequestBody StudentRecentEvaluation event,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Validate
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String studentNumber = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();

        if (!student.getStudentNumber().equals(studentNumber)
                && !"OFFICER".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body("🚫 Unauthorized: Only student or officer/admin can modify");
        }

        if (student.getStudentRecentEvaluations() == null) {
            student.setStudentRecentEvaluations(new ArrayList<>());
        }

        boolean alreadyExists = student.getStudentRecentEvaluations()
                .stream()
                .anyMatch(e -> e.getEventId().equals(event.getEventId()));

        if (alreadyExists) {
            return ResponseEntity.badRequest()
                    .body("⚠️ Event already in student's Recent Evaluation list: "
                            + event.getEventTitle());
        }

        StudentModel updatedStudent = studentService.addRecentEvaluation(studentId, event);
        return ResponseEntity.ok(updatedStudent);
    }

    /* --------------------------------------------------------------------------
     * 📅  POST: Add upcoming event
     * -------------------------------------------------------------------------- */
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

        String studentNumber = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        Optional<StudentModel> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Student not found");
        }

        StudentModel student = studentOpt.get();
        boolean isStudentSelf = student.getStudentNumber().equals(studentNumber);
        boolean isOfficerOrAdmin = "OFFICER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

        if (!isStudentSelf && !isOfficerOrAdmin) {
            return ResponseEntity.status(403)
                    .body("🚫 Unauthorized: Only student/officer/admin can add upcoming events");
        }

        if (student.getStudentUpcomingEvents() == null) {
            student.setStudentUpcomingEvents(new ArrayList<>());
        }

        boolean already = student.getStudentUpcomingEvents()
                .stream()
                .anyMatch(e -> e.getEventId().equals(event.getEventId()));

        if (already) {
            return ResponseEntity.badRequest()
                    .body("⚠️ Event already in student's upcoming list: " + event.getEventTitle());
        }

        StudentModel updatedStudent = studentService.addUpcomingEvent(studentId, event);

        String message = isOfficerOrAdmin
                ? "✅ Officer added upcoming event for " + student.getStudentName()
                : "✅ Upcoming event added successfully";

        return ResponseEntity.ok(Map.of(
                "message", message,
                "updatedStudent", updatedStudent
        ));
    }




    /* --------------------------------------------------------------------------
     * 🧾  PUT: Mark attendance / evaluation (separate endpoints)
     * -------------------------------------------------------------------------- */
    @PutMapping("/mark-attended/{studentId}/{eventId}")
    public ResponseEntity<?> markAttended(
            @PathVariable String studentId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            StudentModel updated = studentService.markStudentAttended(studentId, eventId, token);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Attendance marked successfully",
                    "updatedStudent", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ " + e.getMessage()));
        }
    }

    @PutMapping("/mark-evaluated/{studentId}/{eventId}")
    public ResponseEntity<?> markEvaluated(
            @PathVariable String studentId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            StudentModel updated = studentService.markStudentEvaluated(studentId, eventId, token);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Event marked as evaluated successfully",
                    "updatedStudent", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ " + e.getMessage()));
        }
    }

    @PostMapping("/{studentId}/events/{eventId}/markEvaluated")
    public ResponseEntity<?> markEventAttendanceEvaluated(
            @PathVariable String studentId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            String token = authHeader.substring(7).trim();

            // Call service to update evaluated status
            StudentModel updatedStudent = studentService.markEventEvaluation(studentId, eventId, token);

            return ResponseEntity.ok(updatedStudent);
        } catch (RuntimeException e) {
            // Return 403 for authorization issues, 404 for missing student/event, or other errors
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }


    /* --------------------------------------------------------------------------
     * 🗑️  DELETE: Remove student notification
     * -------------------------------------------------------------------------- */
    @DeleteMapping("/{studentId}/notifications/{notificationId}")
    public ResponseEntity<?> deleteStudentNotification(
            @PathVariable String studentId,
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        String requesterStudentNumber = jwtService.getUsernameFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        try {
            StudentModel updatedStudent = studentService.deleteStudentNotificationById(
                    studentId, notificationId, requesterStudentNumber, role);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Notification deleted successfully",
                    "updatedStudent", updatedStudent
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "⚠️ " + e.getMessage()));
        }
    }

    /* --------------------------------------------------------------------------
     * ⭐  POST: Add event evaluation
     * -------------------------------------------------------------------------- */
    @PostMapping("/{eventId}/addEvaluation")
    public ResponseEntity<?> addEventEvaluation(
            @PathVariable String eventId,
            @RequestBody EventEvaluationDetails evaluation,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Missing or invalid token"));
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Invalid or expired token"));
        }

        String role = jwtService.getRoleFromToken(token);
        if (!List.of("STUDENT", "OFFICER", "ADMIN").contains(role.toUpperCase())) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "🚫 Unauthorized: Only student/officer/admin can add evaluations"
            ));
        }

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

    // ✅ DELETE: Remove a student's upcoming event
    @DeleteMapping("/{studentId}/upcomingEvents/{eventId}")
    public ResponseEntity<?> deleteStudentUpcomingEvent(
            @PathVariable String studentId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            String token = authHeader.substring(7).trim();
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("❌ Invalid or expired token");
            }

            String requesterStudentNumber = jwtService.getUsernameFromToken(token);
            String role = jwtService.getRoleFromToken(token);

            StudentModel updated = studentService.deleteStudentUpcomingEventById(
                    studentId, eventId, requesterStudentNumber, role
            );

            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

}
