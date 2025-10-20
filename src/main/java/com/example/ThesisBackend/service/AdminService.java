package com.example.ThesisBackend.service;

import com.example.ThesisBackend.Model.StudentModel;

import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JWTService jwtService;

    // 🟢 OFFICER or ADMIN can get all notification IDs
    public List<Map<String, Object>> getAllStudentNotificationIds(String token) {
        // Validate token
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("❌ Invalid or expired token");
        }

        String role = jwtService.getRoleFromToken(token);

        // 🔒 Only OFFICER or ADMIN allowed
        if (!"OFFICER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: Only officer or admin can access this data.");
        }

        // Fetch all students
        List<StudentModel> students = studentRepository.findAll();

        // Collect notification IDs
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
    
}
