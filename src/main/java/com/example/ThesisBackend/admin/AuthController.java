package com.example.ThesisBackend.admin;

import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.security.JWTService;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.service.AdminService;
import com.example.ThesisBackend.service.StudentService;
import com.example.ThesisBackend.studentUtils.StudentNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private StudentService studentService;

    // ✅ Register a new student
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody StudentModel student) {
        if (studentRepository.findByStudentNumber(student.getStudentNumber()).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Student already exists");
        }

        student.setStudentPassword(passwordEncoder.encode(student.getStudentPassword()));
        studentRepository.save(student);
        return ResponseEntity.ok("✅ Student registered successfully");
    }

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



    @GetMapping("/admin/allStudentNotificationIds")
    public ResponseEntity<?> getAllStudentNotificationIds(@RequestHeader("Authorization") String authHeader) {
        // Validate auth header
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

    // POST
    // admin and officer student access
    @PostMapping("/admin/addStudentNotification")
    public ResponseEntity<?> addStudentNotificationToAll(
            @RequestBody StudentNotification event,
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔒 Step 1: Validate token presence and format
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);

        // 🔒 Step 2: Validate token
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body("❌ Invalid or expired token");
        }

        try {
            // ✅ Step 3: Add the notification to all students
            List<StudentModel> updatedStudents = studentService.addAllStudentNotification(event, token);

            return ResponseEntity.ok("✅ Notification added to " + updatedStudents.size() + " students.");
        }
        catch (RuntimeException e) {
            // 🚫 Unauthorized (OFFICER/ADMIN check)
            return ResponseEntity.status(403).body(e.getMessage());
        }
        catch (Exception e) {
            // ❌ Other server errors
            return ResponseEntity.status(500).body("❌ Error adding notification: " + e.getMessage());
        }
    }



}
