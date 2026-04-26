package com.learnhub.medical.util;

/**
 * Exception lancée lorsque l'annulation d'un rendez-vous est demandée
 * trop tardivement (moins de 2 heures avant l'échéance).
 */
public class CancellationTooLateException extends Exception {
    public CancellationTooLateException(String message) {
        super(message);
    }
}
