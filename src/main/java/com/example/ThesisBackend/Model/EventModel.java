package com.example.ThesisBackend.Model;

import com.example.ThesisBackend.eventUtils.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * 📄 EventModel - Represents a single event document in MongoDB (collection: eventData)
 * Includes event information, attendance, agenda, evaluations, and optional Base64 image.
 */
@Document(collection = "eventData")
public class EventModel {

    @Id
    private String id;

    /* =========================================================
       🟩 BASIC EVENT INFORMATION
    ========================================================= */
    private String eventTitle;
    private String eventShortDescription;
    private String eventBody;
    private String eventDate;
    private String eventTime;
    private String eventTimeLength;
    private String eventLocation;
    private String eventCategory;  // ✅ renamed field (was 'category')

    // Total number of students attending (can be updated dynamically)
    private int allStudentAttending;

    // Organizer information (embedded object)
    private EventOrganizer eventOrganizer;

    // 🖼️ Optional event poster or banner image (Base64-encoded)
    private String eventImage;


    /* =========================================================
       🟦 RELATED OBJECT COLLECTIONS
    ========================================================= */
    private List<EventAttendance> eventAttendances;                // Attendance records
    private List<EventAgenda> eventAgendas;                        // Event agendas/schedules
    private List<EvaluationQuestion> evaluationQuestions;          // Evaluation form questions
    private List<EventEvaluationDetails> eventEvaluationDetails;   // Submitted evaluations


    /* =========================================================
       🟨 GETTERS AND SETTERS
    ========================================================= */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }


    public String getEventShortDescription() {
        return eventShortDescription;
    }

    public void setEventShortDescription(String eventShortDescription) {
        this.eventShortDescription = eventShortDescription;
    }


    public String getEventBody() {
        return eventBody;
    }

    public void setEventBody(String eventBody) {
        this.eventBody = eventBody;
    }


    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }


    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }


    public String getEventTimeLength() {
        return eventTimeLength;
    }

    public void setEventTimeLength(String eventTimeLength) {
        this.eventTimeLength = eventTimeLength;
    }


    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }


    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }


    public int getAllStudentAttending() {
        return allStudentAttending;
    }

    public void setAllStudentAttending(int allStudentAttending) {
        this.allStudentAttending = allStudentAttending;
    }


    public EventOrganizer getEventOrganizer() {
        return eventOrganizer;
    }

    public void setEventOrganizer(EventOrganizer eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }


    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }


    public List<EventAttendance> getEventAttendances() {
        return eventAttendances;
    }

    public void setEventAttendances(List<EventAttendance> eventAttendances) {
        this.eventAttendances = eventAttendances;
    }


    public List<EventAgenda> getEventAgendas() {
        return eventAgendas;
    }

    public void setEventAgendas(List<EventAgenda> eventAgendas) {
        this.eventAgendas = eventAgendas;
    }


    public List<EvaluationQuestion> getEvaluationQuestions() {
        return evaluationQuestions;
    }

    public void setEvaluationQuestions(List<EvaluationQuestion> evaluationQuestions) {
        this.evaluationQuestions = evaluationQuestions;
    }


    public List<EventEvaluationDetails> getEventEvaluationDetails() {
        return eventEvaluationDetails;
    }

    public void setEventEvaluationDetails(List<EventEvaluationDetails> eventEvaluationDetails) {
        this.eventEvaluationDetails = eventEvaluationDetails;
    }
}
