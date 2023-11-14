package com.creatorcorner.authservice.service;

import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.entity.User;
import com.creatorcorner.authservice.mapper.UserMapper;
import com.creatorcorner.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public Mono<UserDto> createUser(UserDto userDto) {
        return userRepository.existsByEmail(userDto.getEmail())
                .flatMap(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        return Mono.empty();
                    }

                    User user = userMapper.userToMap(userDto);
                    user.setCreatedTsEpoch(LocalDateTime.now());
                    user.setUpdatedTsEpoch(LocalDateTime.now());
                    return userRepository.save(user)
                            .map(userMapper::mapToUser);
                });
    }

    public Mono<UserDto> getUser(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::mapToUser);
    }
}
