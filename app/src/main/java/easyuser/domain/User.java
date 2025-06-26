package easyuser.domain;

public class User {

    private String firstName;
    private String lastName;
    private String email;

    public User() {
        this.firstName = "";
        this.lastName = "";
        this.email = "";
    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public String toString() {
        return "firstName: " + firstName + " lastName: " + lastName + " email: " + email;
    }
}
