package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.Repository.RoleRepository;
import ru.kata.spring.boot_security.demo.Repository.UserRepository;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.entity.UserResponse;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/admin/")
public class RestController {

    private UserRepository userRepository;
    private UserService userService;
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public RestController(UserRepository userRepository, UserService userService, RoleRepository roleRepository) {
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

    @PostMapping("/editUser")
    public ResponseEntity<?> saveUpdatedUser(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        if (user.getNewPassword() != null && !user.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getNewPassword()));
        }

        userService.update(user);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, String roleName) {
        userService.deleteUserById(id, roleName);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName());
        return ResponseEntity.ok(currentUser);
    }
}