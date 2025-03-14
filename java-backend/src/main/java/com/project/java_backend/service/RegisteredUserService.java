package com.project.java_backend.service;

import com.project.java_backend.model.RegisteredUser;
import com.project.java_backend.repository.RegisteredUserRepository;
import com.project.java_backend.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegisteredUserService {

    @Autowired
    private RegisteredUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PaymentService paymentService;

    // Get all users
    public List<RegisteredUser> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public RegisteredUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    // Create new user
    public RegisteredUser createUser(RegisteredUser user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Test user input fields
    public void testUser(RegisteredUser user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Missing email");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        if (user.getCardNumber() == null ||
            user.getExpiryDate() == null ||
            user.getCvc() == null) {
                throw new IllegalArgumentException("Missing card details");
        }
        if (user.getCardNumber().isBlank() || 
			user.getCardNumber().length() != 16 ||
			user.getExpiryDate().isBlank() || 
			user.getExpiryDate().length() != 4 ||
			user.getCvc().isBlank() || 
			user.getCvc().length() != 3
			) {
				throw new IllegalArgumentException("Invalid card details");
		}
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Missing password");
        }
        
    }

    // Update existing user
    public RegisteredUser updateUser(Long id, RegisteredUser userDetails) {
        RegisteredUser user = getUserById(id);

        if(userDetails.getName() != null && !userDetails.getName().isBlank()){user.setName(userDetails.getName());}
        if(userDetails.getAddress() != null && !userDetails.getAddress().isBlank()){user.setAddress(userDetails.getAddress());}
        if(userDetails.getCardNumber() != null && !userDetails.getCardNumber().isBlank()){user.setCardNumber(userDetails.getCardNumber());}
        if(userDetails.getExpiryDate() != null && !userDetails.getExpiryDate().isBlank()){user.setExpiryDate(userDetails.getExpiryDate());}
        if(userDetails.getCvc() != null && !userDetails.getCvc().isBlank()){user.setCvc(userDetails.getCvc());}
        // If updating password
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    // Retrieve a Registered User by email (useful for login/authentication)
    public Optional<RegisteredUser> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Delete user by ID
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    // Authenticate user
    public RegisteredUser authenticate(String email, String password) {
        RegisteredUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (passwordEncoder.matches(password, user.getPassword())) {
            // Check if the last account charge was more than a year ago
            if (user.getLastAccountCharge().isBefore(LocalDateTime.now().minusYears(1))) {
                // Renew the account
                paymentService.renewAccount(user.getCardNumber(), user.getEmail());
                // Update the lastAccountCharge to today's date
                user.setLastAccountCharge(LocalDateTime.now());
                userRepository.save(user); // Save the updated user
            }
            return user;
        } else {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }
}
