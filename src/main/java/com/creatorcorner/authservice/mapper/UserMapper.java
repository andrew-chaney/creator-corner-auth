package com.creatorcorner.authservice.mapper;

import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class UserMapper {

    private PasswordEncoder passwordEncoder;

    public User userToMap(UserDto user) {
        return User.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .hashedPassword(passwordEncoder.encode(user.getPassword()))
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
}
