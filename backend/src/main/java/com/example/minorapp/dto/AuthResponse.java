package com.example.minorapp.dto;

public class AuthResponse {

    private String status;
    private String message;
    private AuthData data;

    public AuthResponse() {
    }

    public AuthResponse(String status, String message, AuthData data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuthData getData() {
        return data;
    }

    public void setData(AuthData data) {
        this.data = data;
    }

    public static class AuthData {
        private String accessToken;
        private String refreshToken;
        private UserData user;

        public AuthData() {
        }

        public AuthData(String accessToken, String refreshToken, UserData user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public UserData getUser() {
            return user;
        }

        public void setUser(UserData user) {
            this.user = user;
        }
    }

    public static class UserData {
        private Long id;
        private String email;
        private String role;
        private String name;
        private String classSection;

        public UserData() {
        }

        public UserData(Long id, String email, String role, String name, String classSection) {
            this.id = id;
            this.email = email;
            this.role = role;
            this.name = name;
            this.classSection = classSection;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClassSection() {
            return classSection;
        }

        public void setClassSection(String classSection) {
            this.classSection = classSection;
        }
    }
}


