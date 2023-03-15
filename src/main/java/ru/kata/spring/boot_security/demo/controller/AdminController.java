package ru.kata.spring.boot_security.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.Repository.UserRepository;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @RequestMapping
    public String getAllUsers(Model model) {
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        return "all-users";
    }

    @RequestMapping("/addNewUser")
    public String addNewUser(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "addNewUser";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute("user") User user) {
        userRepository.save(user);
        return "redirect:/admin";
    }

    @GetMapping("/updateUser={id}")
    public String updateUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        model.addAttribute("user", user);
        userService.setRolesForUsers();
        return "addNewUser";
    }

    @PostMapping("/updateUser={id}")
    public String saveUpdatedUser(@PathVariable Long id, @ModelAttribute("user") User user) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        existingUser.setFirstName(existingUser.getFirstName());
        existingUser.setLastName(existingUser.getLastName());
        existingUser.setUsername(existingUser.getUsername());
        existingUser.setPassword(new BCryptPasswordEncoder().encode(existingUser.getPassword()));
        existingUser.setRoles(existingUser.getRoles());
        userRepository.save(existingUser);
        return "addNewUser";
    }

    @GetMapping("/deleteUser={id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin";
    }
}