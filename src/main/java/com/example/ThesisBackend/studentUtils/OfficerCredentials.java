package com.example.ThesisBackend.studentUtils;

public class OfficerCredentials {

    private boolean canEditEvent;
    private  boolean canAddEvent;

    public boolean isCanAddEvent() {
        return canAddEvent;
    }

    public void setCanAddEvent(boolean canAddEvent) {
        this.canAddEvent = canAddEvent;
    }

    public boolean isCanEditEvent() {
        return canEditEvent;
    }

    public void setCanEditEvent(boolean canEditEvent) {
        this.canEditEvent = canEditEvent;
    }
}
