package com.example.ThesisBackend.controller;

import com.example.ThesisBackend.Model.EventModel;
import com.example.ThesisBackend.eventUtils.EventAttendance;
import com.example.ThesisBackend.eventUtils.EventEvaluationDetails;
import com.example.ThesisBackend.service.EventService;
import com.example.ThesisBackend.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private JWTService jwtService;

    // ✅ PUBLIC: Get all events (no authentication needed)
    @GetMapping
    public ResponseEntity<List<EventModel>> getAllEvents() {
        List<EventModel> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable String id) {
        var eventOpt = eventService.getEventById(id);

        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Event not found");
        }

        return ResponseEntity.ok(eventOpt.get());
    }

    // 🔐 PROTECTED: Create event (only ADMIN or OFFICER)
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody EventModel event, @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            String token = authHeader.substring(7).trim();
            EventModel created = eventService.createEvent(event, token);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }


    @PostMapping("/{eventId}/addAttendance")
    public ResponseEntity<?> addAttendance(
            @PathVariable String eventId,
            @RequestBody EventAttendance attendance,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }

            // 🧹 Remove "Bearer " prefix
            String token = authHeader.substring(7).trim();

            EventModel event = eventService.addEventAttendance(eventId, attendance, token);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("❌ " + e.getMessage());
        }
    }


    // 🔐 PROTECTED: Add event evaluation (STUDENT, OFFICER, ADMIN)
    @PostMapping("/{eventId}/addEvaluation")
    public ResponseEntity<?> addEvaluation(
            @PathVariable String eventId,
            @RequestBody EventEvaluationDetails eventEvaluationDetails,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing token");
            }

            String token = authHeader.substring(7).trim();
            String role = jwtService.getRoleFromToken(token);

            if (role == null || role.isEmpty()) {
                return ResponseEntity.status(403).body("❌ Role not found in token");
            }

            EventModel updated = eventService.addEventEvaluation(eventId, eventEvaluationDetails, role);

            if (updated == null) {
                return ResponseEntity.status(404).body("❌ Event not found");
            }

            // ✅ If everything went fine, return a clean success message
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "✅ Evaluation submitted successfully",
                    "eventTitle", updated.getEventTitle()
            ));

        } catch (IllegalStateException e) {
            // For cases like "already evaluated"
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "warning",
                    "message", e.getMessage()
            ));

        } catch (RuntimeException e) {
            // For unexpected issues (role, token, etc.)
            return ResponseEntity.status(403).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            // Fallback for unhandled errors
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "❌ Server error: " + e.getMessage()
            ));
        }
    }



    // 🔐 PROTECTED: Update event (only ADMIN or OFFICER)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable String id,
            @RequestBody EventModel newEvent,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("❌ Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String role = jwtService.getRoleFromToken(token);

        try {
            // Let service handle role validation
            EventModel updated = eventService.updateEvent(id, newEvent, role);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            // 🔒 Token valid but role not allowed (OFFICER trying to modify restricted data)
            return ResponseEntity.status(401).body("🚫 Unauthorized: you are not allowed to modify this event");
        } catch (RuntimeException e) {
            // ⚠️ Other app errors (like event not found)
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ✅ PATCH: Update "allStudentAttending" publicly (STUDENT / OFFICER / ADMIN)
    @PatchMapping("/updateAllStudentAttending/{eventId}")
    public ResponseEntity<?> updateAllStudentAttending(
            @PathVariable String eventId,
            @RequestParam int newCount,
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

            // ✅ Anyone (STUDENT / OFFICER / ADMIN) can access this endpoint
            String role = jwtService.getRoleFromToken(token);
            String requester = jwtService.getUsernameFromToken(token);

            EventModel updated = eventService.updateAllStudentAttending(eventId, newCount, requester, role);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // 🔐 PROTECTED: Delete event (only ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing token");
            }

            String token = authHeader.substring(7);
            String role = jwtService.getRoleFromToken(token);

            eventService.deleteEvent(id, role);
            return ResponseEntity.ok("✅ Event deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
