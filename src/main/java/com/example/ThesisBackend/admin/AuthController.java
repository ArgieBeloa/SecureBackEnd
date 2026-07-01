package com.example.ThesisBackend.admin;

import com.example.ThesisBackend.Model.AdminModel;
import com.example.ThesisBackend.Model.EventModel;
import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.adminUtils.*;
import com.example.ThesisBackend.eventUtils.EventEvaluationDetails;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import com.example.ThesisBackend.service.AdminService;
import com.example.ThesisBackend.service.ExpoNotificationService;
import com.example.ThesisBackend.service.StudentService;
import com.example.ThesisBackend.studentUtils.StudentEventAttended;
import com.example.ThesisBackend.studentUtils.StudentNotification;
import jakarta.servlet.http.HttpServletRequest;
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
    // 🧾 Admin Access
    // ==========================================================================================

   //GET admin data
   @GetMapping("/admin/{id}")
   public ResponseEntity<?> getEventById(@PathVariable String id, @RequestHeader("Authorization") String authHeader) {

       try {
           if (authHeader == null || !authHeader.startsWith("Bearer ")) {
               return ResponseEntity.status(401).body("❌ Missing or invalid token");
           }

           String token = authHeader.substring(7).trim();
           var adminOpt = adminService.getAdminById(id, token);

           if (adminOpt.isEmpty()) {
               return ResponseEntity.status(404).body("❌ Event not found");
           }

           return ResponseEntity.ok(adminOpt.get());
       }catch (RuntimeException e) {
           return ResponseEntity.status(403).body(e.getMessage());
       }
   }
    @GetMapping("/admin/evaluationTemplates/{adminId}")
    public ResponseEntity<?> getEvaluationTemplates(
            @RequestHeader("Authorization") String token,
            @PathVariable String adminId) {

        try {
            List<evaluationTemplate> templates =
                    adminService.getEvaluationTemplates(
                            adminId,
                            token);

            return ResponseEntity.ok(templates);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // ADD New Officer to admin data
    @PostMapping("/admin/addNewOfficer/{id}")
    public ResponseEntity<?> addNewOfficer(@PathVariable String id, @RequestHeader("Authorization") String authHeader, @RequestBody currentOfficer newOfficer){


        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            // 🧹 Remove "Bearer " prefix
            String token = authHeader.substring(7).trim();

            AdminModel admin = adminService.addOfficer(id, newOfficer, token);
            return ResponseEntity.ok(admin);
        } catch (Exception e) {

            return ResponseEntity.status(400).body("❌ " + e.getMessage());
        }
    }

  // ADD New approval event
  @PostMapping("/admin/addApprovalEvent/{id}")
  public ResponseEntity<?> addNewApprovalEvent(@PathVariable String id, @RequestHeader("Authorization") String authHeader, @RequestBody approvalUpdateEvent newApprovalEvent){


      try {
          if (authHeader == null || !authHeader.startsWith("Bearer ")) {
              return ResponseEntity.status(401).body("❌ Missing or invalid token");
          }

          // 🧹 Remove "Bearer " prefix
          String token = authHeader.substring(7).trim();

          AdminModel admin = adminService.addEventApproval(id, newApprovalEvent, token);
          return ResponseEntity.ok(admin);
      } catch (Exception e) {

          return ResponseEntity.status(400).body("❌ " + e.getMessage());
      }
  }

   //  Add new evaluation template
    @PostMapping("/admin/addEvaluationTemplate/{id}")
    public ResponseEntity<?> addNewEvaluationTemplate(@PathVariable String id, @RequestHeader("Authorization") String authHeader, @RequestBody evaluationTemplate newEvaluationTemplate){

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            // 🧹 Remove "Bearer " prefix
            String token = authHeader.substring(7).trim();

            AdminModel admin = adminService.addEvaluationTemplate(id, newEvaluationTemplate, token);
            return ResponseEntity.ok(admin);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("❌ " + e.getMessage());
        }
    }

//    DELETE MAPPING
    @DeleteMapping("/admin/{adminId}/currentOfficer/{studentId}")
    public ResponseEntity<AdminModel> deleteCurrentOfficer(
            @PathVariable String adminId,
            @PathVariable String studentId,
            @RequestHeader("Authorization") String token) {


        AdminModel result = adminService.deleteCurrentOfficer(adminId, studentId, token);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/admin/{adminId}/eventApproval/{eventId}")
    public ResponseEntity<AdminModel> deleteApprovalEvent(
            @PathVariable String adminId,
            @PathVariable String eventId,
            @RequestHeader("Authorization") String token) {

        AdminModel result = adminService.deleteApprovalEvent(adminId, eventId, token);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/admin/{adminId}/evaluationTemplate/{templateId}")
    public ResponseEntity<AdminModel> deleteEvaluationTemplate(
            @PathVariable String adminId,
            @PathVariable String templateId,
            @RequestHeader("Authorization") String token) {

        AdminModel result = adminService.deleteEvaluationTemplate(adminId, templateId, token);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }



    // ==========================================================================================
    // 🧾 AUTHENTICATION
    // ==========================================================================================

    /**
     * ✅ Register a new student.
     * Checks if student number already exists and encrypts password before saving.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody StudentModel student, HttpServletRequest request) {

        if (studentRepository.findByStudentNumber(student.getStudentNumber()).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Student already exists");
        }

        // Set default role
        student.setRole("STUDENT");

        // Encrypt password
        student.setStudentPassword(passwordEncoder.encode(student.getStudentPassword()));

        // Save student
        String clientIp = getClientIp(request);

        System.out.println("Client IP: " + clientIp+ " Student Name " +student.getStudentName());
        studentRepository.save(student);

        return ResponseEntity.ok("✅ Student registered successfully");
    }

    /**
     * 🔑 Login endpoint for students.
     * Verifies credentials and returns JWT token with user role.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody StudentModel loginRequest, HttpServletRequest request) {
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
        String clientIp = getClientIp(request);

        System.out.println("Client IP: " + clientIp+ "/n" +student.getStudentName());
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
            @PathVariable String studentId,
            @RequestParam boolean canEdit,
            @RequestParam boolean canAdd
    ) {
        try {
            StudentModel updatedStudent =
                    adminService.promoteStudentToOfficer(
                            token,
                            studentId,
                            canEdit,
                            canAdd
                    );

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

//    public evaluation
@PostMapping("/{eventId}/addEvaluation")
public ResponseEntity<?> addEvaluation(
        @PathVariable String eventId,
        @RequestBody EventEvaluationDetails eventEvaluationDetails
) {

    try {

        EventModel updated =
                adminService.addEventEvaluation(eventId, eventEvaluationDetails);

        if (updated == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "❌ Event not found"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "✅ Evaluation submitted successfully",
                "eventTitle", updated.getEventTitle()
        ));

    } catch (IllegalStateException e) {

        return ResponseEntity.badRequest().body(Map.of(
                "status", "warning",
                "message", e.getMessage()
        ));

    } catch (RuntimeException e) {

        return ResponseEntity.status(403).body(Map.of(
                "status", "error",
                "message", e.getMessage()
        ));

    } catch (Exception e) {
        e.printStackTrace();

        return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "❌ Server error: " + e.getMessage()
        ));
    }
}

    private String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For can contain multiple IPs
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

}
