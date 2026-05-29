package com.example.auth_service.domain.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.example.auth_service.domain.models.user.User;
import com.example.auth_service.domain.models.user.vo.Email;
import com.example.auth_service.domain.models.user.vo.GoogleSub;
import com.example.auth_service.domain.models.user.vo.UserId;
import com.example.auth_service.domain.models.user.vo.Username;

/**
 * Repository interface for managing User entities. This defines the contract for how users are stored, retrieved, and queried in the system.
 * By abstracting the data access layer behind this interface, we can easily swap out different implementations
 * (e.g. in-memory, JPA, MongoDB) without affecting the rest of the application. The methods provided allow for basic CRUD operations, as well as more specific queries based on
 * user attributes like username and email. This repository is a key part of the domain layer, as users are central to authentication and authorization logic throughout the application.
 * Transactional annotations can be added to the implementation of this interface to ensure that operations that modify the database are executed within a transaction, providing atomicity and consistency guarantees.
 * Example usage:
 * User user = new User(new UserId(UUID.randomUUID()), new Username("johndoe"), new Email("example@gmail.com"), "hashedpassword", Set.of(role1, role2));
 * userRepository.save(user);
 * Optional<User> foundUser = userRepository.findByUsername("johndoe");
 * if (foundUser.isPresent()) {
 *     User user = foundUser.get();
 *     // Use user for authentication and authorization checks
 * }
 */

public interface UserRepository {
    /**
     * Save a user (insert or update)
     */
    User save(User user);

    /**
     * Find user by ID
     */
    Optional<User> findById(UserId userId);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(Username username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(Email email);

    Optional<User> findByGoogleSub(GoogleSub sub);

    
    default boolean isUsernameTaken(Username username) {
        return findByUsername(username).isPresent();
    }

    /**
     * Check if username exists
     */
    boolean existsByUsername(Username username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(Email email);

    /**
     * Delete user
     */
    void delete(User user);

    /**
     * Find user by ID with roles loaded (for authorization)
     */
    Optional<User> findByIdWithRoles(UserId userId);

    /**
     * Find user by username with roles loaded (for authentication)
     */
    Optional<User> findByUsernameWithRoles(Username username);

    /**
     *Keyset pagination on {@code (createdAt DESC, id DESC)}.
     First page: pass {@code null} for both cursor fields.

     * @param createdAt last seen createdAt; use null for the first page
     * @param cursor last seen id; use 0 for the first page
     * @param limit max items to return
     * @return list of up to {@code limit + 1} users
     */
    List<User> findAfterCursor(Instant createdAt, String cursor, int limit);


}
