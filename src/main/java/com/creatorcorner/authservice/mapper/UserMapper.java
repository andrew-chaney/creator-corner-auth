package com.creatorcorner.authservice.mapper;

import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.entity.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User userToMap(UserDto user) {
        return User.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .hashedPassword(hashPassword(user.getPassword()))
                .build();
    }

    public UserDto mapToUser(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .createdTsEpoch(user.getCreatedTsEpoch())
                .updatedTsEpoch(user.getUpdatedTsEpoch())
                .build();
    }

    private String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }
}
