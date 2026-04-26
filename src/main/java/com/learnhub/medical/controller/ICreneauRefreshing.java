package com.learnhub.medical.controller;

/**
 * Interface partagée permettant de rafraîchir l'affichage des créneaux,
 * implémentée par AdminCreneauManagementController et CreneauManagementController.
 */
public interface ICreneauRefreshing {
    void refreshPlanning();
}
