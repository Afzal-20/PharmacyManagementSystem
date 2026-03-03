package com.my.pharmacy.dao;

import com.my.pharmacy.model.User;

public interface UserDAO {
    User authenticate(String username, String password);
}