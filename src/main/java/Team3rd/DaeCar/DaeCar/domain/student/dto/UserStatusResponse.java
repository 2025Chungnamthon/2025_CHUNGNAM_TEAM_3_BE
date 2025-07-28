package Team3rd.DaeCar.DaeCar.domain.student.dto;

public class UserStatusResponse {
    private boolean userExists;
    private String message;
    private String email;
    
    public UserStatusResponse(boolean userExists, String message, String email) {
        this.userExists = userExists;
        this.message = message;
        this.email = email;
    }
    
    // Getters and Setters
    public boolean isUserExists() { return userExists; }
    public void setUserExists(boolean userExists) { this.userExists = userExists; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}