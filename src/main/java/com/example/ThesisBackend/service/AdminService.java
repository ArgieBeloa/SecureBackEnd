package com.example.ThesisBackend.service;

import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 🧑‍💼 AdminService
 * ----------------------------------------------------------
 * Handles all ADMIN-only actions such as:
 *  - Promoting students to OFFICER (without changing their password)
 *  - Viewing all student notification IDs (for ADMIN / OFFICER)
 */
@Service
public class AdminService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JWTService jwtService;

    /**
     * 🔔 Retrieve all students’ notification IDs.
     * Only OFFICER or ADMIN can access.
     */
    public List<Map<String, Object>> getAllStudentNotificationIds(String token) {
        // ✅ Validate token
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("❌ Invalid or expired token");
        }

        String role = jwtService.getRoleFromToken(token);

        // 🔒 Only OFFICER or ADMIN allowed
        if (!"OFFICER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: Only OFFICER or ADMIN can access this data.");
        }

        // ✅ Fetch all students
        List<StudentModel> students = studentRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (StudentModel student : students) {
            if (student.getNotificationId() != null && !student.getNotificationId().isEmpty()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("studentId", student.getId());
                entry.put("studentName", student.getStudentName());
                entry.put("notificationId", student.getNotificationId());
                result.add(entry);
            }
        }

        return result;
    }

    /**
     * 🧭 Promote a student to OFFICER role.
     * Only ADMIN can perform this operation.
     * Keeps the existing encrypted password (no reset).
     */
    public StudentModel promoteStudentToOfficer(String token, String studentId) {
        // ✅ Remove "Bearer " prefix if included
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // ✅ Check if token belongs to ADMIN
        String role = jwtService.getRoleFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Access Denied: Only ADMIN can promote students.");
        }

        // ✅ Find student to promote
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("❌ Student not found with ID: " + studentId);
        }

        StudentModel student = studentOpt.get();

        // ✅ Promote role, but keep same encrypted password
        student.setRole("OFFICER");

        // ✅ Save changes
        return studentRepository.save(student);
    }

    public StudentModel demoteOfficer(String studentId, String token) {
        // ✅ Remove "Bearer " prefix if included
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // ✅ Check if token belongs to ADMIN
        String role = jwtService.getRoleFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Access Denied: Only ADMIN can promote students.");
        }

        // ✅ Find student to promote
        Optional<StudentModel> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("❌ Student not found with ID: " + studentId);
        }

        StudentModel student = studentOpt.get();

        // ✅ Promote role, but keep same encrypted password
        student.setRole("STUDENT");

        System.out.println("Successfully demoted "+student.getStudentName());

        // ✅ Save changes
        return studentRepository.save(student);
    }

    public void deleteStudent(String id, String token) {
        try {
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim(); // remove "Bearer "
            }

            String adminRole = jwtService.getRoleFromToken(cleanToken);

            if("ADMIN".equalsIgnoreCase(adminRole)){
                studentRepository.deleteById(id);
                System.out.println("🗑️ Student deleted with ID: " + id);
            }else{
                throw new RuntimeException("🚫 Unauthorized: ONLY admin can delete event");
            }


        } catch (Exception e) {
            System.out.println("❌ Error deleting event: " + e.getMessage());
            throw e;
        }
    }
}
