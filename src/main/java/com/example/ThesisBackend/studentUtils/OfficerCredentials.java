package com.example.ThesisBackend.studentUtils;

public class OfficerCredentials {

    private boolean canEditEvent;
    private boolean canAddEvent;
    private boolean canAddStudent;
    private boolean canScanStudent;

    public boolean isCanScanStudent() {
        return canScanStudent;
    }

    public void setCanScanStudent(boolean canScanStudent) {
        this.canScanStudent = canScanStudent;
    }

    public boolean isCanAddStudent() {
        return canAddStudent;
    }

    public void setCanAddStudent(boolean canAddStudent) {
        this.canAddStudent = canAddStudent;
    }

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
