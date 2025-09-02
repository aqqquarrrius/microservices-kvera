package com.example.userservice.service;

import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.example.userservice.event.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Instant;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public UserServiceImpl(UserRepository userRepository,
                           KafkaTemplate<String, UserEvent> kafkaTemplate,
                           @Value("${app.kafka.user-events-topic}") String topic) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto);

    }

    @Override
    public UserDto createUser(UserCreateRequest request) {
        User user = UserMapper.toEntity(request);
        User saved = userRepository.save(user);

        UserEvent event = new UserEvent(
                UserEvent.Operation.CREATE,
                saved.getId(),
                saved.getEmail(),
                Instant.now()
        );
        kafkaTemplate.send(topic, String.valueOf(saved.getId()), event);

        return UserMapper.toDto(saved);
    }


    @Override
    public Optional<UserDto> updateUser(Long id, UserUpdateRequest request) {
        return userRepository.findById(id)
                .map(existing -> {
                    UserMapper.updateEntity(existing, request);
                    return UserMapper.toDto(userRepository.save(existing));
                });
    }

    @Override
    public void deleteUser(Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id" + id));
        userRepository.delete(existing);

        UserEvent event = new UserEvent(
                UserEvent.Operation.DELETE,
                existing.getId(),
                existing.getEmail(),
                Instant.now()
            );
        kafkaTemplate.send(topic, String.valueOf(existing.getId()), event);
        }

}