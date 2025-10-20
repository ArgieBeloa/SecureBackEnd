package com.example.ThesisBackend.Model;

import com.example.ThesisBackend.eventUtils.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "eventData")
public class EventModel {

    @Id
    private String id;

    // Info
    private String eventTitle;
    private String eventShortDescription;
    private String eventBody;
    private int allStudentAttending;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private String category;
    private String eventTimeLength;
    private EventOrganizer eventOrganizer;

    // Array of class
    private List<EventAttendance> eventAttendances;
    private  List<EventAgenda> eventAgendas;
    private List<EvaluationQuestion> evaluationQuestions;
    private List<EventEvaluationDetails> eventEvaluationDetails;



    // Getters and Setters
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

    public int getAllStudentAttending() {
        return allStudentAttending;
    }

    public void setAllStudentAttending(int allStudentAttending) {
        this.allStudentAttending = allStudentAttending;
    }

    public String getEventBody() {
        return eventBody;
    }

    public void setEventBody(String eventBody) {
        this.eventBody = eventBody;
    }

    public String getEventCategory() {
        return category;
    }

    public void setEventCategory(String category) {
        this.category = category;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventShortDescription() {
        return eventShortDescription;
    }

    public void setEventShortDescription(String eventShortDescription) {
        this.eventShortDescription = eventShortDescription;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
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

    public List<EventAttendance> getEventAttendances() {
        return eventAttendances;
    }

    public void setEventAttendances(List<EventAttendance> eventAttendances) {
        this.eventAttendances = eventAttendances;
    }

    public List<EvaluationQuestion> getEvaluationQuestions() {
        return evaluationQuestions;
    }

    public void setEvaluationQuestions(List<EvaluationQuestion> evaluationQuestions) {
        this.evaluationQuestions = evaluationQuestions;
    }

    public List<EventAgenda> getEventAgendas() {
        return eventAgendas;
    }

    public void setEventAgendas(List<EventAgenda> eventAgendas) {
        this.eventAgendas = eventAgendas;
    }

    public List<EventEvaluationDetails> getEventEvaluationDetails() {
        return eventEvaluationDetails;
    }

    public void setEventEvaluationDetails(List<EventEvaluationDetails> eventEvaluationDetails) {
        this.eventEvaluationDetails = eventEvaluationDetails;
    }

    public EventOrganizer getEventOrganizer() {
        return eventOrganizer;
    }

    public void setEventOrganizer(EventOrganizer eventOrganizer) {
        this.eventOrganizer = eventOrganizer;
    }
}
