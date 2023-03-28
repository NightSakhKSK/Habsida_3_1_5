package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.Repository.UserRepository;
import ru.kata.spring.boot_security.demo.entity.User;

import java.util.List;

@Controller
public class AdminController {
    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/admin")
    public String adminPage() {
        return "all-users";
    }

    @GetMapping("/api/admin/allUsers")
    public String getUserList(Model model) {
        List<User> users = userRepository.findAll(); // Замените на ваш метод получения списка пользователей
        model.addAttribute("user", users);
        return "all-users"; // Замените на имя вашего HTML-шаблона
    }
}