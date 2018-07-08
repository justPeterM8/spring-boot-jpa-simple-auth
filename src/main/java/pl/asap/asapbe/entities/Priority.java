package pl.asap.asapbe.entities;

public enum Priority {
    LOW("LOW"),
    NORMAL("NORMAL"),
    HIGH("HIGH");

    private String priorityName;

    Priority(String priorityName) {
        this.priorityName = priorityName;
    }

    public String getPriorityName() {
        return priorityName;
    }
}