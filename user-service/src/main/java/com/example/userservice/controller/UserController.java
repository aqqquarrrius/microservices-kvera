package com.example.userservice.controller;

import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public CollectionModel<EntityModel<UserDto>> getAllUsers() {
        List<EntityModel<UserDto>> users = userService.getAllUsers().stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"),
                        linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete")))
                .collect(Collectors.toList());

        return CollectionModel.of(users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).createUser(null)).withRel("create"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserDto>> getUserById(@PathVariable Long id) {
        Optional<UserDto> user = userService.getUserById(id);

        return user.map(u -> ResponseEntity.ok(
                        EntityModel.of(u,
                                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"),
                                linkTo(methodOn(UserController.class).updateUser(id, null)).withRel("update"),
                                linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EntityModel<UserDto>> createUser(@RequestBody @Valid UserCreateRequest request) {
        UserDto created = userService.createUser(request);

        return ResponseEntity.ok(
                EntityModel.of(created,
                        linkTo(methodOn(UserController.class).getUserById(created.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserDto>> updateUser(@PathVariable Long id,
                                                           @RequestBody @Valid UserUpdateRequest request) {
        Optional<UserDto> updated = userService.updateUser(id, request);

        return updated.map(u -> ResponseEntity.ok(
                        EntityModel.of(u,
                                linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel(),
                                linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"),
                                linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"))))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}