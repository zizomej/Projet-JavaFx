package com.learnhub.util;

import com.learnhub.models.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private Utilisateur currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public Utilisateur getCurrentUser() { return currentUser; }
    public void setCurrentUser(Utilisateur user) { this.currentUser = user; }
    public void logout() { this.currentUser = null; }
    public boolean isLoggedIn() { return currentUser != null; }
    public String getRole() { return currentUser != null ? currentUser.getRoles() : ""; }
}
