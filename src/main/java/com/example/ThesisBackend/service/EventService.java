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

    // POST
    // ✅ Create new event (only allowed for ADMIN or OFFICER roles)
    public EventModel createEvent(EventModel event, String token) {
        String role = jwtService.getRoleFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: only ADMIN or OFFICER can create events.");
        }

        return eventRepository.save(event);
    }

    // GET
    // ✅ Get all events
    public List<EventModel> getAllEvents() {
        return eventRepository.findAll();
    }

    // ✅ Get event by ID
    public Optional<EventModel> getEventById(String id) {
        return eventRepository.findById(id);
    }

    // ✅ Update event (only ADMIN/OFFICER)
    public EventModel updateEvent(String id, EventModel newEvent, String token) {
        String role = jwtService.getRoleFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: only ADMIN or OFFICER can update events.");
        }

        EventModel existing = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Event not found"));

        // ✅ Update fields using helper
        updateEventFields(existing, newEvent);

        return eventRepository.save(existing);
    }

    // POST
    public EventModel addEventAttendance(String eventId, EventAttendance attendance, String token) {
        // ✅ Extract role from token
        String role = jwtService.getRoleFromToken(token);

        // ✅ Only OFFICER (or ADMIN if you want) can add attendance
        if (!"OFFICER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: Only OFFICER can add attendance.");
        }

        Optional<EventModel> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            System.out.println("❌ Event not found with ID: " + eventId);
            return null;
        }

        EventModel event = eventOpt.get();

        // Initialize list if null
        if (event.getEventAttendances() == null) {
            event.setEventAttendances(new ArrayList<>());
        }

        // Add the new attendance record
        event.getEventAttendances().add(attendance);

        // Save updated event
        eventRepository.save(event);

        System.out.println("✅ Attendance added for event: " + event.getEventTitle());
        return event;
    }

      // Student event Evaluation
      public EventModel addEventEvaluation(String eventId, EventEvaluationDetails evaluation, String role) {
          Optional<EventModel> eventOpt = eventRepository.findById(eventId);

          if (eventOpt.isEmpty()) {
              System.out.println("❌ Event not found with ID: " + eventId);
              return null;
          }

          // 🔒 Allow STUDENT, OFFICER, and ADMIN
          if (!"STUDENT".equalsIgnoreCase(role)
                  && !"OFFICER".equalsIgnoreCase(role)
                  && !"ADMIN".equalsIgnoreCase(role)) {
              throw new RuntimeException("🚫 Unauthorized: Only student, officer, or admin can add evaluations.");
          }

          EventModel event = eventOpt.get();

          // Initialize list if null
          if (event.getEventEvaluationDetails() == null) {
              event.setEventEvaluationDetails(new ArrayList<>());
          }

          // ✅ Prevent duplicate evaluations by same student name
          boolean alreadyExists = event.getEventEvaluationDetails().stream()
                  .anyMatch(detail -> detail.getStudentName().equalsIgnoreCase(evaluation.getStudentName()));

          if (alreadyExists) {
              System.out.println("⚠️ Evaluation already exists for student: " + evaluation.getStudentName());
              return event;
          }

          // ✅ Add the new evaluation
          event.getEventEvaluationDetails().add(evaluation);

          // 💾 Save updated event
          eventRepository.save(event);

          System.out.println("✅ Evaluation added by " + role + " for event: " + event.getEventTitle());
          return event;
      }







    // ✅ Delete event (only ADMIN)
    public void deleteEvent(String id, String token) {
        String role = jwtService.getRoleFromToken(token);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("🚫 Unauthorized: only ADMIN can delete events.");
        }

        eventRepository.deleteById(id);
    }

//    helper fot update
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

