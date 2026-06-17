package com.example.ThesisBackend.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import  com.example.ThesisBackend.adminUtils.*;
import java.util.List;

//collection Name
@Document(collection = "adminData")
public class AdminModel {

      //id
      @Id
      private  String id;

      private  String adminName;

     //Utils
     private List<currentOfficer> currentOfficer;
     private List<approvalUpdateEvent> approvalUpdateEvents;
     private List<evaluationTemplate> evaluationTemplates;

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public List<evaluationTemplate> getEvaluationTemplates() {
        return evaluationTemplates;
    }

    public void setEvaluationTemplates(List<evaluationTemplate> evaluationTemplates) {
        this.evaluationTemplates = evaluationTemplates;
    }

    public List<approvalUpdateEvent> getApprovalUpdateEvents() {
        return approvalUpdateEvents;
    }

    public void setApprovalUpdateEvents(List<approvalUpdateEvent> approvalUpdateEvents) {
        this.approvalUpdateEvents = approvalUpdateEvents;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<currentOfficer> getCurrentOfficer() {
        return currentOfficer;
    }

    public void setCurrentOfficer(List<currentOfficer> currentOfficer) {
        this.currentOfficer = currentOfficer;
    }
}
