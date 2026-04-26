package com.learnhub.medical.util;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.medical.repository.RDVRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Service de compatibilité redirigeant vers PenaltyService.
 */
public class MedicalService {

    private final PenaltyService penaltyService = new PenaltyService();

    public String cancelAppointment(int rdvId) throws Exception {
        return penaltyService.processCancellation(rdvId);
    }

    public String rescheduleAppointment(int rdvId, int newCreneauId) throws Exception {
        return penaltyService.rescheduleAppointment(rdvId, newCreneauId);
    }
}
