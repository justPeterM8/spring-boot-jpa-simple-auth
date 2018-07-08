package pl.asap.asapbe.entities;

public enum Status {
    OPEN("OPEN"),
    IN_PROGRESS("IN_PROGRESS"),
    IN_TESTS("IN_TESTS"),
    DONE("DONE");

    private String statusName;

    Status(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }
}
