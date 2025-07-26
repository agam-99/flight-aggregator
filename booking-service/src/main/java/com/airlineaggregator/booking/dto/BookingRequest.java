package com.airlineaggregator.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class BookingRequest {

    @NotNull(message = "Flight routine ID is required")
    private UUID flightRoutineId;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerInfo> passengers;

    @NotNull(message = "Contact information is required")
    @Valid
    private ContactInfo contactInfo;

    private List<String> specialRequests;

    // Constructors
    public BookingRequest() {}

    // Nested classes
    public static class PassengerInfo {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Date of birth is required")
        private String dateOfBirth; // YYYY-MM-DD format

        private String passportNumber;
        private String nationality;
        private String seatPreference; // window, aisle, middle
        private String mealPreference;

        // Constructors
        public PassengerInfo() {}

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getPassportNumber() { return passportNumber; }
        public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }
        public String getSeatPreference() { return seatPreference; }
        public void setSeatPreference(String seatPreference) { this.seatPreference = seatPreference; }
        public String getMealPreference() { return mealPreference; }
        public void setMealPreference(String mealPreference) { this.mealPreference = mealPreference; }
    }

    public static class ContactInfo {
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email is required")
        private String email;

        @NotBlank(message = "Phone number is required")
        private String phone;

        // Constructors
        public ContactInfo() {}

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    // Main class getters and setters
    public UUID getFlightRoutineId() { return flightRoutineId; }
    public void setFlightRoutineId(UUID flightRoutineId) { this.flightRoutineId = flightRoutineId; }
    public List<PassengerInfo> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerInfo> passengers) { this.passengers = passengers; }
    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }
    public List<String> getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(List<String> specialRequests) { this.specialRequests = specialRequests; }

    @Override
    public String toString() {
        return "BookingRequest{" +
                "flightRoutineId=" + flightRoutineId +
                ", passengers=" + passengers.size() +
                ", contactInfo=" + contactInfo.getEmail() +
                '}';
    }
} 