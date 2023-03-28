package ru.kata.spring.boot_security.demo.DTO;

import ru.kata.spring.boot_security.demo.entity.User;

import java.util.List;
import java.util.Set;

public class UserUpdateDTO {
    private User user;
    private String oldPassword;
    private boolean isPasswordChanged;
    private Set<Long> roleIds;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public boolean isPasswordChanged() {
        return isPasswordChanged;
    }

    public void setPasswordChanged(boolean passwordChanged) {
        isPasswordChanged = passwordChanged;
    }
}
