package com.learnhub.medical.util;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.repository.CreneauRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.sql.SQLException;

/**
 * Service stabilisé pour la gestion des annulations avec reconnaissance intelligente de date.
 */
public class PenaltyService {

    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();

    public String processCancellation(int rdvId) throws Exception {
        RDV rdv = rdvRepo.findById(rdvId);
        if (rdv == null) throw new Exception("Rendez-vous introuvable.");

        Creneau creneau = creneauRepo.findById(rdv.getCreneauId());
        if (creneau == null) throw new Exception("Données du créneau manquantes.");

        // Reconstruction ultra-robuste de la date
        LocalDate rdvDate = getRobustDate(rdv, creneau);
        LocalDateTime rdvDateTime = LocalDateTime.of(rdvDate, creneau.getHeure());
        LocalDateTime now = LocalDateTime.now();

        // Calcul exact de l'écart
        long totalMinutes = Duration.between(now, rdvDateTime).toMinutes();
        
        // --- LA RÈGLE DES 24H (1440 MINUTES) ---
        if (totalMinutes < 1440) {
            throw new CancellationTooLateException("Annulation impossible : L'annulation doit être effectuée au moins 24 heures à l'avance.");
        }

        rdvRepo.processAtomicCancellation(rdvId, rdv.getCreneauId(), "CANCELLED");
        return "Le rendez-vous a été annulé avec succès.";
    }

    private LocalDate getRobustDate(RDV rdv, Creneau c) {
        String jour = (c.getJour() != null) ? c.getJour().trim() : "";
        
        // 1. Test format ISO (2026-04-28)
        if (jour.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            try {
                return LocalDate.parse(jour.substring(0, 10));
            } catch (Exception e) {}
        }

        // 2. Test format Jour Nommé (Lundi, etc.)
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        LocalDate mondayThisWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        
        for (int i = 0; i < 7; i++) {
            if (jour.toLowerCase().contains(days[i].toLowerCase())) {
                LocalDate calculated = mondayThisWeek.plusDays(i);
                // Si la date calculée est dans le passé depuis plus de 2 jours, 
                // on considère que c'est pour la semaine prochaine
                if (calculated.isBefore(LocalDate.now().minusDays(1))) {
                    return calculated.plusWeeks(1);
                }
                return calculated;
            }
        }

        // 3. Dernier recours : Date enregistrée dans le RDV
        return rdv.getDateDemande();
    }

    public String rescheduleAppointment(int rdvId, int newCreneauId) throws Exception {
        return "Option désactivée.";
    }
}
