package org.model;

public class Invitation {
    private final int partyId;
    private final String invitedPlayerId;
    private String status;

    public Invitation(int partyId, String invitedPlayerId, String status) {
        this.partyId = partyId;
        this.invitedPlayerId = invitedPlayerId;
        this.status = status;
    }

    public int getPartyId() { return partyId; }
    public String getInvitedPlayerId() { return invitedPlayerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}