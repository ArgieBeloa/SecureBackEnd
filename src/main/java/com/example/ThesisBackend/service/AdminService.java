package com.example.ThesisBackend.service;

import com.example.ThesisBackend.Model.AdminModel;
import com.example.ThesisBackend.Model.EventModel;
import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.eventUtils.EventEvaluationDetails;
import com.example.ThesisBackend.repository.AdminRepository;
import com.example.ThesisBackend.repository.EventRepository;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import  com.example.ThesisBackend.adminUtils.*;
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
    private EventRepository eventRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JWTService jwtService;

    //get ADMIN
    public  Optional<AdminModel> getAdminById(String adminId,String token) {
        try {
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }

            // 🔍 Validate role from cleaned token
            String role = jwtService.getRoleFromToken(cleanToken);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only ADMIN can access this endpoint");
            }
            Optional<AdminModel> admin = adminRepository.findById(adminId);
            if (admin.isPresent()) {
                System.out.println("✅ Found admin: ");
            } else {
                System.out.println("❌ Admin not found with ID: " + adminId);
            }
            return  admin;

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    //Add Admin
    public AdminModel createAdmin(AdminModel adminModel, String token){

        try {
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }

            // 🔍 Validate role from cleaned token
            String role = jwtService.getRoleFromToken(cleanToken);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only ADMIN can access this endpoint");
            }
            AdminModel saveAdminData = adminRepository.save(adminModel);
            System.out.println("✅ Admin data created successfully: ");
            return saveAdminData ;


        } catch (Exception e) {
            System.out.println("❌ Error creating admin data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //Add current officer to admin data
    public AdminModel addOfficer(String adminId, currentOfficer officer, String token){
        try {
            Optional<AdminModel> adminOpt = adminRepository.findById(adminId);
            if (adminOpt.isEmpty()) {
                System.out.println("❌ Admin not found with ID: " + adminId);
                return null;
            }

            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }

            // 🔍 Validate role from cleaned token
            String role = jwtService.getRoleFromToken(cleanToken);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only ADMIN can access this endpoint");
            }
            AdminModel adminModel = adminOpt.get();
            if (adminModel.getCurrentOfficer() == null) {
                adminModel.setCurrentOfficer(new ArrayList<>());
            }

            boolean alreadyExists = adminModel.getCurrentOfficer().stream()
                    .anyMatch(detail -> detail.getStudentName().equalsIgnoreCase(officer.getStudentName()));

            if (alreadyExists) {
                System.out.println("⚠️ Student already exists in current officer: " + officer.getStudentName());
                return adminModel;
            }

            adminModel.getCurrentOfficer().add(officer);
            adminRepository.save(adminModel);

            System.out.println("✅ Student added by successfully");
            return adminModel;

        } catch (RuntimeException e) {
            System.out.println("❌ Error adding officer: ");
            throw new RuntimeException(e);
        }
    }

    //Add admin approval update event data
    public AdminModel addEventApproval(String adminId,  approvalUpdateEvent approveEvent, String token){
        try {
            Optional<AdminModel> adminOpt = adminRepository.findById(adminId);
            if (adminOpt.isEmpty()) {
                System.out.println("❌ Admin not found with ID: " + adminId);
                return null;
            }

            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }

            // 🔍 Validate role from cleaned token
            String role = jwtService.getRoleFromToken(cleanToken);
            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only ADMIN can access this endpoint");
            }
            AdminModel adminModel = adminOpt.get();
            if (adminModel.getApprovalUpdateEvents() == null) {
                adminModel.setApprovalUpdateEvents(new ArrayList<>());
            }

            boolean alreadyExists = adminModel.getApprovalUpdateEvents().stream()
                    .anyMatch(detail -> detail.getId().equalsIgnoreCase(approveEvent.getId()));

            if (alreadyExists) {
                System.out.println("⚠️ Event already exists in waiting for approval: " + approveEvent.getEventTitle());
                return adminModel;
            }

            adminModel.getApprovalUpdateEvents().add(approveEvent);
            adminRepository.save(adminModel);

            System.out.println("✅ Event approval added by successfully");
            return adminModel;

        } catch (RuntimeException e) {
            System.out.println("❌ Error adding approval event: ");
            throw new RuntimeException(e);
        }
    }
// DELETE current officer

// DELETE approval event




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

//  public event evaluation (event id and user data )
public EventModel addEventEvaluation(
        String eventId,
        EventEvaluationDetails evaluation
) {
    try {
        Optional<EventModel> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            System.out.println("❌ Event not found with ID: " + eventId);
            return null;
        }

        EventModel event = eventOpt.get();

        // ✅ ADD EVALUATION
        event.getEventEvaluationDetails().add(evaluation);

        eventRepository.save(event);

        System.out.println("✅ Evaluation added by "+ event.getEventTitle());

        return event;

    } catch (Exception e) {
        System.out.println("❌ Error adding evaluation: " + e.getMessage());
        throw e;
    }
}
}
