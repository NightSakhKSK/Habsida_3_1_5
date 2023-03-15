package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.kata.spring.boot_security.demo.Repository.RoleRepository;
import ru.kata.spring.boot_security.demo.Repository.UserRepository;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public UserService(RoleRepository roleRepository,
                       UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setName("USER");
        roleRepository.save(userRole);

//Создаю админа в таблице Юзер, проблема, в том, что если апдейтить, то юзернейм "умирает" и зайти обратно нельзя
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setFirstName("ADMIN");
        adminUser.setLastName("Don't Touch it please");
        adminUser.setSalary(-555);
        adminUser.setDepartment("SECURITY");
        adminUser.setUsername("admin");
        adminUser.setPassword("admin");
        adminUser.setRoles(Collections.singleton(adminRole));

        User baka = new User();
        baka.setId(2L);
        baka.setFirstName("USER");
        baka.setLastName("Test_name");
        baka.setSalary(0);
        baka.setDepartment("TEST_DEPARTMENT");
        baka.setUsername("baka");
        baka.setPassword(new BCryptPasswordEncoder().encode("user"));
        baka.setRoles(Collections.singleton(userRole));

        setRolesForUsers();
        saveAllUserNames();
        userRepository.save(adminUser);
        userRepository.save(baka);

    }

    public void setRolesForUsers() {
        Role userRole = roleRepository.findByName("USER");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            Set<Role> roles = user.getRoles();
            if (roles == null) {
                roles = new HashSet<>();
            }
            if (!roles.contains(userRole) && roles.size() == 0) {
                roles.add(userRole);
                user.setRoles(roles);
                userRepository.save(user);
            }
        }
    }

    public void saveAllUserNames() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setUsername(user.generateUsername());
            userRepository.save(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities);
    }
}