package com.my.pharmacy.util;

import com.my.pharmacy.model.User;

public class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession(User user) {
        this.currentUser = user;
    }

    public static void login(User user) {
        instance = new UserSession(user);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getUser() {
        return currentUser;
    }

    public static void logout() {
        instance = null;
    }

    public static boolean isLoggedIn() {
        return instance != null;
    }
}