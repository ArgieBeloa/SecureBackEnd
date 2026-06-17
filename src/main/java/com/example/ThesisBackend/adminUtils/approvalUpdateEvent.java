package com.example.ThesisBackend.adminUtils;

import com.example.ThesisBackend.Model.EventModel;

public class approvalUpdateEvent extends EventModel {

    private Boolean isApprove;

    public Boolean getApprove() {
        return isApprove;
    }

    public void setApprove(Boolean approve) {
        isApprove = approve;
    }
}
