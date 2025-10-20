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
    public ResponseEntity<?> createEvent(
            @RequestBody EventModel event,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing token");
            }

            String token = authHeader.substring(7);
            String role = jwtService.getRoleFromToken(token);

            EventModel createdEvent = eventService.createEvent(event, role);
            return ResponseEntity.ok(createdEvent);
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
                return ResponseEntity.status(401).body("❌ Missing token");
            }

            String token = authHeader.substring(7);
            String role = jwtService.getRoleFromToken(token);

            EventModel updated = eventService.addEventAttendance(eventId, attendance, role);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
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

            String token = authHeader.substring(7);
            String role = jwtService.getRoleFromToken(token);

            if (role == null || role.isEmpty()) {
                return ResponseEntity.status(403).body("❌ Role not found in token");
            }

            EventModel updated = eventService.addEventEvaluation(eventId, eventEvaluationDetails, role);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
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
