package com.example.ThesisBackend.admin;

import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import com.example.ThesisBackend.service.AdminService;
import com.example.ThesisBackend.service.ExpoNotificationService;
import com.example.ThesisBackend.service.StudentService;
import com.example.ThesisBackend.studentUtils.StudentEventAttended;
import com.example.ThesisBackend.studentUtils.StudentNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🎓 AuthController — handles authentication and admin-level operations.
 * Includes: Student registration, login, notifications, and attendance management.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // ==========================================================================================
    // 🧩 DEPENDENCY INJECTIONS
    // ==========================================================================================

    @Autowired private StudentRepository studentRepository;
    @Autowired private AdminService adminService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JWTService jwtService;
    @Autowired private StudentService studentService;
    @Autowired private ExpoNotificationService expoNotificationService;

    // ==========================================================================================
    // 🧾 AUTHENTICATION
    // ==========================================================================================

    /**
     * ✅ Register a new student.
     * Checks if student number already exists and encrypts password before saving.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody StudentModel student) {
        if (studentRepository.findByStudentNumber(student.getStudentNumber()).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Student already exists");
        }

        student.setStudentPassword(passwordEncoder.encode(student.getStudentPassword()));
        studentRepository.save(student);
        return ResponseEntity.ok("✅ Student registered successfully");
    }

    /**
     * 🔑 Login endpoint for students.
     * Verifies credentials and returns JWT token with user role.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody StudentModel loginRequest) {
        if (loginRequest.getStudentNumber() == null || loginRequest.getStudentPassword() == null) {
            return ResponseEntity.badRequest().body("❌ Missing student number or password");
        }

        var studentOpt = studentRepository.findByStudentNumber(loginRequest.getStudentNumber());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(401).body("❌ Invalid student number or password");
        }

        var student = studentOpt.get();
        if (!passwordEncoder.matches(loginRequest.getStudentPassword(), student.getStudentPassword())) {
            return ResponseEntity.status(401).body("❌ Invalid student number or password");
        }

        String token = jwtService.generateToken(student.getStudentNumber(), student.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("_id", student.getId());
        response.put("role", student.getRole());
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    // ==========================================================================================
    // 📚 ADMIN FEATURES
    // ==========================================================================================

    /**
     * 👀 Get all students (ADMIN/OFFICER ONLY)
     */
    @GetMapping("/admin/allStudents")
    public ResponseEntity<?> getAllStudents(@RequestHeader("Authorization") String token) {
        try {
            String cleanToken = token.replace("Bearer ", "").trim();
            List<StudentModel> students = studentService.getAllStudents(cleanToken);
            return ResponseEntity.ok(students);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("⚠️ Server error: " + e.getMessage());
        }
    }

    /**
     * 📱 Get all student Expo notification IDs.
     * Used when sending push notifications.
     */
    @GetMapping("/admin/allStudentNotificationIds")
    public ResponseEntity<?> getAllStudentNotificationIds(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Missing or invalid token"));
        }

        String token = authHeader.substring(7);

        try {
            List<Map<String, Object>> data = adminService.getAllStudentNotificationIds(token);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Successfully fetched all student notification IDs",
                    "total", data.size(),
                    "data", data
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // ==========================================================================================
    // 🔔 NOTIFICATIONS
    // ==========================================================================================

    /**
     * 📨 Add a new notification to all students’ records.
     * ADMIN or OFFICER only.
     */
    @PostMapping("/admin/addStudentNotification")
    public ResponseEntity<?> addStudentNotificationToAll(
            @RequestBody StudentNotification event,
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        try {
            List<StudentModel> updatedStudents = studentService.addAllStudentNotification(event, token);
            return ResponseEntity.ok("✅ Notification added to " + updatedStudents.size() + " students.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error adding notification: " + e.getMessage());
        }
    }

    /**
     * 📲 Send Expo push notifications to student devices.
     * ADMIN or OFFICER only.
     */
    @PostMapping("/admin/sendExpoNotification")
    public ResponseEntity<?> sendExpoNotification(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            String token = authHeader.substring(7);
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("❌ Invalid or expired token");
            }

            String role = jwtService.getRoleFromToken(token);
            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("🚫 Only ADMIN or OFFICER can send notifications.");
            }

            List<String> expoTokens = (List<String>) payload.get("tokens");
            String title = (String) payload.get("title");
            String body = (String) payload.get("body");

            expoNotificationService.sendPushNotification(expoTokens, title, body);
            return ResponseEntity.ok("✅ Notifications sent successfully to " + expoTokens.size() + " users.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Failed to send notification: " + e.getMessage());
        }
    }

    // ==========================================================================================
    // 🧾 STUDENT EVENT ATTENDANCE
    // ==========================================================================================

    /**
     * ✅ Add an event attendance record for a student.
     * Only ADMIN and OFFICER roles can perform this action.
     */
    @PostMapping("/admin/addAttendance/{studentId}")
    public ResponseEntity<?> addEventAttendance(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String studentId,
            @RequestBody StudentEventAttended event
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            String token = authHeader.substring(7);
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body("❌ Invalid or expired token");
            }

            String role = jwtService.getRoleFromToken(token);
            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                return ResponseEntity.status(403).body("🚫 Only ADMIN or OFFICER can add attendance.");
            }

            StudentModel updatedStudent = studentService.addEventAttendance(studentId, event, authHeader);
            if (updatedStudent == null) {
                return ResponseEntity.status(404).body("❌ Student not found with ID: " + studentId);
            }

            return ResponseEntity.ok("✅ Event added for student: " + updatedStudent.getStudentName());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error adding attendance: " + e.getMessage());
        }
    }

    /**
     * 🧭 Promote a student to OFFICER role.
     * 🔒 Only ADMIN can perform this action.
     *
     * Example request:
     *   PATCH /api/admin/promote/68f66dca05840f3caa5cfa72
     *   Authorization: Bearer <ADMIN_TOKEN>
     */
    @PatchMapping("/promote/{studentId}")
    public ResponseEntity<?> promoteStudentToOfficer(
            @RequestHeader("Authorization") String token,
            @PathVariable String studentId
    ) {
        try {
            StudentModel updatedStudent = adminService.promoteStudentToOfficer(token, studentId);
            return ResponseEntity.ok(updatedStudent);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 🧭 Demote a OFFICER to STUDENT role.
     * 🔒 Only ADMIN can perform this action.
     *
     * Example request:
     *   PATCH /api/admin/demote/68f66dca05840f3caa5cfa72
     *   Authorization: Bearer <ADMIN_TOKEN>
     */
    @PatchMapping("/demote/{studentId}")
    public ResponseEntity<?> demoteOfficer(
            @RequestHeader("Authorization") String token,
            @PathVariable String studentId
    ) {
        try {
            StudentModel updatedStudent = adminService.demoteOfficer(studentId, token);
            return ResponseEntity.ok(updatedStudent);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteStudent/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing token");
            }

            adminService.deleteStudent(id, authHeader);
            return ResponseEntity.ok("✅ student deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }



}
