package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRecord findByName(String name) {
        try {
            return userRepository.findByName(name);
        } catch (Exception e) {
            // Fallback to manual iteration if Spring Data JPA method fails
            Iterable<UserRecord> allUsers = userRepository.findAll();
            for (UserRecord user : allUsers) {
                if (user.getName() != null && user.getName().equals(name)) {
                    return user;
                }
            }
            return null;
        }
    }

    public UserRecord findById(Long id) {
        return userRepository.findById(id.longValue());
    }

    public Iterable<UserRecord> findAll() {
        return userRepository.findAll();
    }
}