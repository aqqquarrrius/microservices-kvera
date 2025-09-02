package com.example.userservice.event;

import java.time.Instant;


public class UserEvent {
    public enum Operation {CREATE, DELETE}

    private Operation operation;
    private Long userId;
    private String email;
    private Instant timestamp;

    public UserEvent() {}

    public UserEvent(Operation operation, Long userId, String email, Instant timestamp) {
        this.operation = operation;
        this.userId = userId;
        this.email = email;
        this.timestamp = timestamp;
    }

    public Operation getOperation() { return operation; }
    public void setOperation(Operation operation) { this.operation = operation; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
