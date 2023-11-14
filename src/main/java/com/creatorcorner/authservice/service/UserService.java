package com.creatorcorner.authservice.service;

import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.entity.User;
import com.creatorcorner.authservice.mapper.UserMapper;
import com.creatorcorner.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public Mono<UserDto> createUser(UserDto userDto) {
        return userRepository.existsByEmail(userDto.getEmail())
                .flatMap(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        log.info(
                                "User with email {} already exists for creation request: {}",
                                userDto.getEmail(),
                                userDto.creationLogString()
                        );
                        return Mono.empty();
                    }

                    User user = userMapper.userToMap(userDto);
                    user.setCreatedTsEpoch(LocalDateTime.now());
                    user.setUpdatedTsEpoch(LocalDateTime.now());
                    return userRepository.save(user)
                            .map(userMapper::mapToUser)
                            .doOnNext(u -> log.info("Persisted user: {}", u.persistedLogString()));
                });
    }

    public Mono<UserDto> getUser(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::mapToUser);
    }
}
