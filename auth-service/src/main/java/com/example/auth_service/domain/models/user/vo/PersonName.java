package com.example.auth_service.domain.models.user.vo;

/**
 * Value Object representing a person's name, consisting of a first name and a last name.
 * Immutable and encapsulates validation logic to ensure that both first and last names are provided and not
 * empty. Provides utility methods to access the full name and override toString for easy representation.
 * Designed to be used within the User Aggregate to represent the user's name and ensure that only valid names are created and stored.
 */

public record PersonName(String firstName, String lastName) {

    public  PersonName(String firstName, String lastName) {
        if(firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if(lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public static PersonName of(String firstName, String lastName) {
        return new PersonName(firstName, lastName);
    }
    public String getFullName() {
        return firstName + " " + lastName;
    }
    @Override
    public String toString() {
        return getFullName();
    }
}
