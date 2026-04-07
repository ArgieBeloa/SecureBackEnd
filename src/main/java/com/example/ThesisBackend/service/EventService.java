package com.example.ThesisBackend.service;

import com.example.ThesisBackend.Model.EventModel;
import com.example.ThesisBackend.Model.StudentModel;
import com.example.ThesisBackend.eventUtils.EventAttendance;
import com.example.ThesisBackend.eventUtils.EventEvaluationDetails;
import com.example.ThesisBackend.repository.EventRepository;
import com.example.ThesisBackend.repository.StudentRepository;
import com.example.ThesisBackend.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JWTService jwtService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EventImageService eventImageService;

    // =====================================================
    // 🟢 CREATE
    // =====================================================

    /**
     * ✅ Create a new event (ADMIN or OFFICER only)
     */
    public EventModel createEvent(EventModel event, String token) {
        try {
            // 🧹 Clean up the token (remove "Bearer " prefix if it exists)
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }

            // 🔍 Validate role from cleaned token
            String role = jwtService.getRoleFromToken(cleanToken);
            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only ADMIN or OFFICER can create events.");
            }

            // 💾 Save to MongoDB
            EventModel saved = eventRepository.save(event);
            System.out.println("✅ Event created successfully: " + saved.getEventTitle());
            return saved;

        } catch (Exception e) {
            System.out.println("❌ Error creating event: " + e.getMessage());
            throw e;
        }
    }


    /**
     * ✅ Upload and link an image to an event
     */
    public EventModel uploadEventImage(String eventId, MultipartFile file, String token) {
        try {
            // 🧹 Clean token (remove "Bearer " prefix if present)
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim();
            }

            // 🔐 Validate user role
            String role = jwtService.getRoleFromToken(cleanToken);
            if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                throw new RuntimeException("🚫 Unauthorized: Only ADMIN or OFFICER can upload event images.");
            }

            // 🔍 Find event
            EventModel event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("❌ Event not found with ID: " + eventId));

            // 💾 Store image in GridFS using same ID as event
            String fileId = eventImageService.storeImageWithEventId(eventId, file);

            // 🔗 Save image ID (same as event ID)
            event.setEventImageId(fileId);
            eventRepository.save(event);

            System.out.println("✅ Event image uploaded and linked: " + event.getEventTitle());
            return event;

        } catch (Exception e) {
            System.out.println("❌ Error uploading event image: " + e.getMessage());
            throw new RuntimeException(e);
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
            System.out.println("✅ Retrieved " + events.size() + " events.");
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
    public EventModel updateEvent(String id, EventModel newEvent, String role) {

        System.out.println("ID: " + id);
        System.out.println("Role: " + role);

        EventModel existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Event not found"));

        System.out.println("Event Found: " + existingEvent.getEventTitle());
        // 1️⃣ Find existing event
//        EventModel existingEvent = eventRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("❌ Event not found"));

        if (role == null) {
            throw new SecurityException("No role found");
        }

    /* =========================================================
       🟢 ADMIN - Can update almost everything
    ========================================================= */
        if (role.equalsIgnoreCase("ADMIN")) {

            existingEvent.setWhoPostedName(newEvent.getWhoPostedName());
            existingEvent.setEventTitle(newEvent.getEventTitle());
            existingEvent.setEventShortDescription(newEvent.getEventShortDescription());
            existingEvent.setEventBody(newEvent.getEventBody());
            existingEvent.setEventDate(newEvent.getEventDate());
            existingEvent.setEventTime(newEvent.getEventTime());
            existingEvent.setEventTimeLength(newEvent.getEventTimeLength());
            existingEvent.setEventLocation(newEvent.getEventLocation());
            existingEvent.setEventCategory(newEvent.getEventCategory());
            existingEvent.setEventOrganizer(newEvent.getEventOrganizer());
            existingEvent.setEventImageId(newEvent.getEventImageId());
            existingEvent.setEventAgendas(newEvent.getEventAgendas());
            existingEvent.setEvaluationQuestions(newEvent.getEvaluationQuestions());

            // ❌ Do NOT update:
            // eventAttendances
            // eventEvaluationDetails
            // allStudentAttending
        }

    /* =========================================================
       🔵 OFFICER - Limited update
    ========================================================= */
        else if (role.equalsIgnoreCase("OFFICER")) {

            existingEvent.setEventTitle(newEvent.getEventTitle());
            existingEvent.setEventShortDescription(newEvent.getEventShortDescription());
            existingEvent.setEventBody(newEvent.getEventBody());
            existingEvent.setEventDate(newEvent.getEventDate());
            existingEvent.setEventTime(newEvent.getEventTime());
            existingEvent.setEventTimeLength(newEvent.getEventTimeLength());
            existingEvent.setEventLocation(newEvent.getEventLocation());
            existingEvent.setEventCategory(newEvent.getEventCategory());
            existingEvent.setEventImageId(newEvent.getEventImageId());
            existingEvent.setEventAgendas(newEvent.getEventAgendas());

            // 🚫 Officer CANNOT modify:
            // whoPostedName
            // eventOrganizer
            // evaluationQuestions
            // eventAttendances
            // eventEvaluationDetails
            // allStudentAttending
        }

        else {
            throw new SecurityException("🚫 Unauthorized role");
        }

        return eventRepository.save(existingEvent);
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
 delete event data related


*/

public void removeEventFromAllStudents(String eventId) {

    Query query = new Query();

    Update update = new Update()
            .pull("studentUpcomingEvents", Query.query(Criteria.where("eventId").is(eventId)))
            .pull("studentEventAttended", Query.query(Criteria.where("eventId").is(eventId)))
            .pull("studentRecentEvaluations", Query.query(Criteria.where("eventId").is(eventId)))
            .pull("studentNotifications", Query.query(Criteria.where("eventId").is(eventId)))
            .pull("studentEventAttendedAndEvaluationDetails", Query.query(Criteria.where("eventId").is(eventId)));

    mongoTemplate.updateMulti(query, update, StudentModel.class);
}

    /**
     * ✅ Delete event (ADMIN only)
     */
    public void deleteEvent(String id, String token) {
        try {
            String cleanToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                cleanToken = token.substring(7).trim(); // remove "Bearer "
            }

            String adminRole = jwtService.getRoleFromToken(cleanToken);

            if("ADMIN".equalsIgnoreCase(adminRole)){
                //student related data in delete
                removeEventFromAllStudents(id);

                //Admin access (delete event by id)
                eventRepository.deleteById(id);


                System.out.println("🗑️ Event deleted with ID: " + id);
            }else{
                throw new RuntimeException("🚫 Unauthorized: ONLY admin can delete event");
            }

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
