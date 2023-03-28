package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.DTO.UserUpdateDTO;
import ru.kata.spring.boot_security.demo.Repository.RoleRepository;
import ru.kata.spring.boot_security.demo.Repository.UserRepository;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.entity.UserResponse;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/admin/")
public class AdminRestController {

    private UserRepository userRepository;
    private UserService userService;
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AdminRestController(UserRepository userRepository, UserService userService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping("/addNewUser")
    public ResponseEntity<?> addNewUser(@RequestBody Map<String, Object> userData) {
        String username = (String) userData.get("username");
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setFirstName((String) userData.get("firstName"));
        user.setLastName((String) userData.get("lastName"));
        user.setSalary((Integer) userData.get("salary"));
        user.setDepartment((String) userData.get("department"));
        user.setPassword(passwordEncoder.encode((String) userData.get("password")));

        List<Map<String, Object>> roleDataList = (List<Map<String, Object>>) userData.get("roles");
        if (roleDataList == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Roles are required");
        }

        Set<Role> roles = new HashSet<>();
        for (Map<String, Object> roleData : roleDataList) {
            Integer roleId = (Integer) roleData.get("id");
            Role role = roleRepository.findById((long) roleId).orElse(null);
            if (role != null) {
                roles.add(role);
            }
        }
        user.setRoles(roles);

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/saveUser")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/editUser/{id}")
    public ResponseEntity<?> saveUpdatedUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = userRepository.findByUsername(userUpdateDTO.getUser().getUsername());
        if (existingUser != null && !existingUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        User updatedUser = optionalUser.get();
        updatedUser.setUsername(userUpdateDTO.getUser().getUsername());
        updatedUser.setFirstName(userUpdateDTO.getUser().getFirstName());
        updatedUser.setLastName(userUpdateDTO.getUser().getLastName());
        updatedUser.setSalary(userUpdateDTO.getUser().getSalary());
        updatedUser.setDepartment(userUpdateDTO.getUser().getDepartment());
        //Проверка, был ли изменён пароль пользователя или нет
        String oldPassword = userUpdateDTO.getOldPassword();
        String newPassword = userUpdateDTO.getUser().getPassword();
        if (oldPassword == null || !oldPassword.matches(newPassword)) {
            updatedUser.setPassword(newPassword);
        }

        // Загрузка ролей из репозитория и установка их для обновленного пользователя
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(userUpdateDTO.getRoleIds()));
        updatedUser.setRoles(roles);

        userService.update(updatedUser, oldPassword);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUserById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        UserResponse userResponse = new UserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/getRoleByName/{name}")
    public ResponseEntity<Long> getRoleIdByName(@PathVariable String name) {
        Role role = roleRepository.findByName(name);
        if (role != null) {
            return ResponseEntity.ok(role.getId());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/allRoles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> allRoles = roleRepository.findAll();
        return ResponseEntity.ok(allRoles);
    }

    @PostMapping("/addNewRole")
    public ResponseEntity<?> addNewRole(@RequestBody Role role) {
        Role existingRole = roleRepository.findByName(role.getName());
        if (existingRole != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Role already exists");
        }
        roleRepository.save(role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/editRole/{id}")
    public ResponseEntity<?> editRole(@PathVariable Long id, @RequestBody Role updatedRole) {
        Role existingRole = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid role Id:" + id));

        if (!existingRole.getName().equals(updatedRole.getName())) {
            Role roleWithName = roleRepository.findByName(updatedRole.getName());
            if (roleWithName != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Role with the same name already exists");
            }
        }
        existingRole.setName(updatedRole.getName());
        roleRepository.save(existingRole);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/deleteRole/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid role Id:" + id));
        roleRepository.delete(role);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/currentUser")
    public ResponseEntity<UserUpdateDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsernameWithRoles(auth.getName());

        // Создайте объект UserDto из текущего пользователя
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