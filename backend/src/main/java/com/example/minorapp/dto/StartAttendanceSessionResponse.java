package com.example.minorapp.dto;

public class StartAttendanceSessionResponse {

    private Long courseId;
    private String sessionId;
    private long timestamp;
    private long expiresAtEpochSec;
    private String qrPayload;

    public StartAttendanceSessionResponse(Long courseId, String sessionId, long timestamp, long expiresAtEpochSec, String qrPayload) {
        this.courseId = courseId;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.expiresAtEpochSec = expiresAtEpochSec;
        this.qrPayload = qrPayload;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getExpiresAtEpochSec() {
        return expiresAtEpochSec;
    }

    public String getQrPayload() {
        return qrPayload;
    }
}
