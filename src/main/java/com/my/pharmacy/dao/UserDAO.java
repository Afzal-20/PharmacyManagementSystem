package com.my.pharmacy.dao;

import com.my.pharmacy.model.User;

import java.util.List;

public interface UserDAO {
    User authenticate(String username, String password);
    List<User> getAllUsers();
    boolean addUser(User user, String plainTextPassword);
    boolean updatePassword(int userId, String newPlainTextPassword);
    boolean toggleUserStatus(int userId, boolean isActive);
}