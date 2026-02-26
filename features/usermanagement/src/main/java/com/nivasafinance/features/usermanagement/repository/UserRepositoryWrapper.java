package com.nivasafinance.features.usermanagement.repository;

import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserRepositoryWrapper {

    private final UserRepository userRepository;

    public User findByIdWithException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundById(userId));
    }

    public User findByUsernameWithException(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundByUsername(username));
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> findByPersonPhoneNumber(String phoneNumber) {
        return userRepository.findByPersonPhoneNumber(phoneNumber);
    }
    
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }
}
