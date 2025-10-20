package com.example.ThesisBackend.service;

import com.example.ThesisBackend.Model.EventModel;
import com.example.ThesisBackend.eventUtils.EventAttendance;
import com.example.ThesisBackend.eventUtils.EventEvaluationDetails;
import com.example.ThesisBackend.repository.EventRepository;
import com.example.ThesisBackend.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JWTService jwtService;

    // =====================================================
    // 🟢 CREATE
    // =====================================================

    /**
     * ✅ Create a new event (ADMIN or OFFICER only)
     */
    public EventModel createEvent(EventModel event, String token) {
        try {
            String role = jwtService.getRoleFromToken(token);

            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: only ADMIN or OFFICER can create events.");
            }

            EventModel saved = eventRepository.save(event);
            System.out.println("✅ Event created successfully: " + saved.getEventTitle());
            return saved;
        } catch (Exception e) {
            System.out.println("❌ Error creating event: " + e.getMessage());
            throw e;
        }
    }

    // =====================================================
    // 🟡 READ
    // =====================================================

    /**
     * ✅ Get all events (Public)
     */
    public List<EventModel> getAllEvents() {
        try {
            List<EventModel> events = eventRepository.findAll();
//            System.out.println("✅ Retrieved " + events.size() + " events.");
            return events;
        } catch (Exception e) {
            System.out.println("❌ Error fetching events: " + e.getMessage());
            throw e;
        }
    }

    /**
     * ✅ Get a single event by ID
     */
    public Optional<EventModel> getEventById(String id) {
        try {
            Optional<EventModel> event = eventRepository.findById(id);
            if (event.isPresent()) {
                System.out.println("✅ Found event: " + event.get().getEventTitle());
            } else {
                System.out.println("❌ Event not found with ID: " + id);
            }
            return event;
        } catch (Exception e) {
            System.out.println("❌ Error getting event by ID: " + e.getMessage());
            throw e;
        }
    }

    // =====================================================
    // 🟠 UPDATE
    // =====================================================

    /**
     * ✅ Update an existing event (ADMIN or OFFICER only)
     */
    public EventModel updateEvent(String id, EventModel newEvent, String token) {
        try {
            String role = jwtService.getRoleFromToken(token);
            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: only ADMIN or OFFICER can update events.");
            }

            EventModel existing = eventRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("❌ Event not found with ID: " + id));

            updateEventFields(existing, newEvent);
            EventModel updated = eventRepository.save(existing);

            System.out.println("✅ Event updated: " + updated.getEventTitle());
            return updated;
        } catch (Exception e) {
            System.out.println("❌ Error updating event: " + e.getMessage());
            throw e;
        }
    }

    /**
     * ✅ Update "allStudentAttending" count
     * Allowed for STUDENT / OFFICER / ADMIN
     */
    public EventModel updateAllStudentAttending(String eventId, int newCount, String requester, String role) {
        try {
            Optional<EventModel> eventOpt = eventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                throw new RuntimeException("❌ Event not found with ID: " + eventId);
            }

            if (!"STUDENT".equalsIgnoreCase(role)
                    && !"OFFICER".equalsIgnoreCase(role)
                    && !"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only student, officer, or admin can update this field.");
            }

            EventModel event = eventOpt.get();
            event.setAllStudentAttending(newCount);
            eventRepository.save(event);

            System.out.println("✅ Updated allStudentAttending to " + newCount + " by " + role + " (" + requester + ")");
            return event;
        } catch (Exception e) {
            System.out.println("❌ Error updating allStudentAttending: " + e.getMessage());
            throw e;
        }
    }

    // =====================================================
    // 🟣 ADD ATTENDANCE & EVALUATION
    // =====================================================

    /**
     * ✅ Add a student's attendance to an event (OFFICER or ADMIN only)
     */
    public EventModel addEventAttendance(String eventId, EventAttendance attendance, String token) {
        try {
            // 🧹 1️⃣ Clean up the token — remove "Bearer " prefix if it exists
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim(); // remove "Bearer "
            }

            // 🧠 2️⃣ Extract role from the cleaned token
            String role = jwtService.getRoleFromToken(cleanToken);

            // 🔒 3️⃣ Only OFFICER or ADMIN can add attendance
            if (!"OFFICER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only OFFICER or ADMIN can add attendance.");
            }

            // 🔍 4️⃣ Fetch event by ID
            Optional<EventModel> eventOpt = eventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                System.out.println("❌ Event not found with ID: " + eventId);
                throw new RuntimeException("❌ Event not found with ID: " + eventId);
            }

            EventModel event = eventOpt.get();

            // 🧾 5️⃣ Initialize attendance list if null
            if (event.getEventAttendances() == null) {
                event.setEventAttendances(new ArrayList<>());
            }

            // ➕ 6️⃣ Add attendance record
            event.getEventAttendances().add(attendance);

            // 💾 7️⃣ Save updated event
            eventRepository.save(event);

            System.out.println("✅ Attendance added for event: " + event.getEventTitle());
            return event;

        } catch (Exception e) {
            // 🧨 8️⃣ Detailed logging
            System.out.println("❌ Error adding attendance: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("❌ Error adding attendance: " + e.getMessage(), e);
        }
    }


    /**
     * ✅ Add evaluation for an event (STUDENT, OFFICER, or ADMIN)
     */
    public EventModel addEventEvaluation(String eventId, EventEvaluationDetails evaluation, String role) {
        try {
            Optional<EventModel> eventOpt = eventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                System.out.println("❌ Event not found with ID: " + eventId);
                return null;
            }

            if (!"STUDENT".equalsIgnoreCase(role)
                    && !"OFFICER".equalsIgnoreCase(role)
                    && !"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only student, officer, or admin can add evaluations.");
            }

            EventModel event = eventOpt.get();
            if (event.getEventEvaluationDetails() == null) {
                event.setEventEvaluationDetails(new ArrayList<>());
            }

            boolean alreadyExists = event.getEventEvaluationDetails().stream()
                    .anyMatch(detail -> detail.getStudentName().equalsIgnoreCase(evaluation.getStudentName()));

            if (alreadyExists) {
                System.out.println("⚠️ Evaluation already exists for student: " + evaluation.getStudentName());
                return event;
            }

            event.getEventEvaluationDetails().add(evaluation);
            eventRepository.save(event);

            System.out.println("✅ Evaluation added by " + role + " for event: " + event.getEventTitle());
            return event;
        } catch (Exception e) {
            System.out.println("❌ Error adding evaluation: " + e.getMessage());
            throw e;
        }
    }

    // =====================================================
    // 🔴 DELETE
    // =====================================================

    /**
     * ✅ Delete event (ADMIN only)
     */
    public void deleteEvent(String id, String token) {
        try {
            String role = jwtService.getRoleFromToken(token);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: only ADMIN can delete events.");
            }

            eventRepository.deleteById(id);
            System.out.println("🗑️ Event deleted with ID: " + id);
        } catch (Exception e) {
            System.out.println("❌ Error deleting event: " + e.getMessage());
            throw e;
        }
    }

    // =====================================================
    // ⚙️ HELPER
    // =====================================================

    /**
     * ✅ Helper method to update event fields safely
     */
    private void updateEventFields(EventModel event, EventModel newEvent) {
        if (newEvent.getEventTitle() != null) event.setEventTitle(newEvent.getEventTitle());
        if (newEvent.getEventShortDescription() != null) event.setEventShortDescription(newEvent.getEventShortDescription());
        if (newEvent.getEventBody() != null) event.setEventBody(newEvent.getEventBody());
        if (newEvent.getEventDate() != null) event.setEventDate(newEvent.getEventDate());
        if (newEvent.getEventTime() != null) event.setEventTime(newEvent.getEventTime());
        if (newEvent.getEventTimeLength() != null) event.setEventTimeLength(newEvent.getEventTimeLength());
        if (newEvent.getEventLocation() != null) event.setEventLocation(newEvent.getEventLocation());
        if (newEvent.getEventCategory() != null) event.setEventCategory(newEvent.getEventCategory());
        event.setAllStudentAttending(newEvent.getAllStudentAttending());
    }
}
