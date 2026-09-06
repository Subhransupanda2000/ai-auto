package com.healthcareai.service;

import java.util.Optional;

import com.healthcareai.entity.Role;
import com.healthcareai.entity.User;

public interface UserService {

    User createUser(String email, String rawPassword, String fullName, Role role);

    Optional<User> findByEmail(String email);

    long count();
}
