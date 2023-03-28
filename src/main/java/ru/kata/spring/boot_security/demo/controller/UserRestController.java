package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import ru.kata.spring.boot_security.demo.DTO.UserUpdateDTO;
import ru.kata.spring.boot_security.demo.Repository.UserRepository;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserRestController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/currentUser")
    public ResponseEntity<UserUpdateDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsernameWithRoles(auth.getName());

        List<String> roleNames = currentUser.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        UserUpdateDTO userDto = new UserUpdateDTO(
                currentUser.getId()
                ,currentUser.getUsername()
                ,currentUser.getFirstName()
                ,currentUser.getLastName()
                ,currentUser.getDepartment()
                ,currentUser.getSalary()
                ,roleNames);

        return ResponseEntity.ok(userDto);
    }
}
