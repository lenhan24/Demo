package model;

import java.sql.Timestamp;

public class ActivityLog {
    private int logId;
    private String actorName;
    private String actorRole;
    private String actionText;
    private Timestamp actedAt;

    public ActivityLog() {}

    public ActivityLog(int logId, String actorName, String actorRole,
                        String actionText, Timestamp actedAt) {
        this.logId = logId;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.actionText = actionText;
        this.actedAt = actedAt;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }

    public Timestamp getActedAt() { return actedAt; }
    public void setActedAt(Timestamp actedAt) { this.actedAt = actedAt; }
}
