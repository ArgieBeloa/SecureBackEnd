package com.example.ThesisBackend.adminUtils;
import  com.example.ThesisBackend.eventUtils.EvaluationQuestion;

import java.util.List;

public class evaluationTemplate {

    private String templateName;

    private List<EvaluationQuestion> evaluationQuestions;

    public List<EvaluationQuestion> getEvaluationQuestions() {
        return evaluationQuestions;
    }

    public void setEvaluationQuestions(List<EvaluationQuestion> evaluationQuestions) {
        this.evaluationQuestions = evaluationQuestions;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
