package com.learnhub.medical.util;

import com.learnhub.medical.entity.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private Utilisateur currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(Utilisateur user) { this.currentUser = user; }
    public Utilisateur getCurrentUser() { return currentUser; }

    public int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : 1;
    }

    public String getCurrentRole() {
        return (currentUser != null && currentUser.getRoles() != null)
                ? currentUser.getRoles() : "ROLE_ADMIN";
    }
}
