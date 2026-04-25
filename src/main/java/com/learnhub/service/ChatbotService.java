package com.learnhub.service;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.UniversiteDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Universite;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ChatbotService {

    private final UniversiteDAO universiteDAO = new UniversiteDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();

    public String generateResponse(String query) {
        if (query == null || query.isBlank()) return "Comment puis-je vous aider aujourd'hui ?";

        // Pre-processing for fuzzy matching (remove accents, lowercase)
        String input = normalize(query);

        try {
            List<Universite> allUnis = universiteDAO.findAll();
            
            // --- 0. OFF-TOPIC FILTERING ---
            if (isOffTopic(input, allUnis)) {
                return "🤖 **LearnBot** : Désolé, mais cette question est en dehors du domaine de l'application **LearnHub**.\n\n" +
                       "Je suis spécialisé dans la gestion des universités, des filières et de l'orientation académique. " +
                       "Puis-je vous aider à trouver une formation ou un établissement ?";
            }

            // --- 1. DATA EXTRACTION ---
            Universite mentionedUni = null;
            for (Universite u : allUnis) {
                if (input.contains(normalize(u.getNom()))) {
                    mentionedUni = u;
                    break;
                }
            }

            // --- 2. INTELLIGENT RESPONSES ---

            // Intent: Greeting
            if (input.matches(".*(bonjour|salut|hello|hey|bjr).*")) {
                return "👋 Bonjour ! Je suis **LearnBot**, votre conseiller expert **LearnHub**. \n\n" +
                        "Je connais parfaitement nos " + allUnis.size() + " établissements partenaires. " +
                        "Posez-moi une question sur une université ou une filière spécifique !";
            }

            // Intent: How to Apply (General or Specific)
            if (input.matches(".*(postuler|inscr|regist|admiss|comment faire|comment joindre).*")) {
                if (mentionedUni != null) {
                    return "📝 **Postuler à " + mentionedUni.getNom() + "** :\n\n" +
                           "C'est très simple ! Allez sur la fiche de l'établissement, cliquez sur **'Voir les détails'**, puis sur le bouton vert **'Postuler en ligne'**.\n\n" +
                           "Vous devrez remplir votre nom, email et téléphone pour recevoir une confirmation.";
                } else {
                    return "📝 **Comment postuler sur LearnHub ?**\n\n" +
                           "1. Choisissez une université dans la liste.\n" +
                           "2. Cliquez sur **'Détails'**.\n" +
                           "3. Dans la liste des filières, cliquez sur **'Postuler en ligne'**.\n\n" +
                           "Une fois soumis, vous recevrez un email de confirmation automatique !";
                }
            }

            // Detailed University Info
            if (mentionedUni != null) {
                if (input.matches(".*(filiere|programm|format|enseigne|specialit).*")) {
                    List<Filiere> filieres = filiereDAO.findByUniversite(mentionedUni.getId());
                    if (filieres.isEmpty()) return "🎓 **" + mentionedUni.getNom() + "** n'a pas encore de filières enregistrées.";
                    String list = filieres.stream().map(f -> "• " + f.getNom()).collect(Collectors.joining("\n"));
                    return "🎓 **Filières à " + mentionedUni.getNom() + "** :\n\n" +
                           "Cet établissement propose les formations suivantes :\n" + list + "\n\n" +
                           "L'une de ces filières vous intéresse ?";
                }
                if (input.matches(".*(ou|localisa|adress|ville|situe).*")) {
                    return "📍 **Localisation de " + mentionedUni.getNom() + "** :\n\n" +
                           "L'établissement est situé à **" + mentionedUni.getVille() + "**.\n" +
                           "Adresse : " + mentionedUni.getAdresse() + "\n\n" +
                           "Vous pouvez cliquer sur 'Afficher la carte' pour voir la position précise Google Maps.";
                }
                if (input.matches(".*(contact|appel|joindre|mail|telephone).*")) {
                    return "📞 **Contacter " + mentionedUni.getNom() + "** :\n\n" +
                           "• Email : " + mentionedUni.getEmail() + "\n" +
                           "• Tél : " + mentionedUni.getTelephone() + "\n\n" +
                           "N'hésitez pas à les solliciter pour plus d'informations.";
                }
                if (input.matches(".*(avis|qualite|recommand|bien|top|meilleur).*")) {
                    return "🌟 **Mon avis sur " + mentionedUni.getNom() + "** :\n\n" +
                           "C'est un établissement " + mentionedUni.getType() + " de haut niveau. " +
                           "Il est particulièrement apprécié pour son infrastructure à " + mentionedUni.getVille() + ".";
                }
            }

            // General Search by City
            if (input.matches(".*( a | dans | sur ).*")) {
                String potentialCity = input.split("( a | dans | sur )")[1].trim().replace("?", "");
                List<Universite> unisInCity = allUnis.stream()
                        .filter(u -> normalize(u.getVille()).contains(normalize(potentialCity)))
                        .collect(Collectors.toList());
                
                if (!unisInCity.isEmpty()) {
                    return "📍 Voici les établissements à **" + potentialCity + "** :\n\n" + 
                           unisInCity.stream().map(u -> "• " + u.getNom()).collect(Collectors.joining("\n")) + 
                           "\n\nL'un d'eux vous tente ?";
                }
            }

            // General Information about Universities
            if (input.matches(".*(universit|ecole|facult).*")) {
                return "🏢 LearnHub regroupe **" + allUnis.size() + "** universités partenaires.\n\n" +
                       "Les plus populaires sont : " + allUnis.stream().limit(3).map(u -> "**" + u.getNom() + "**").collect(Collectors.joining(", ")) + ".\n\n" +
                       "Voulez-vous voir les détails de l'une d'entre elles ?";
            }

            // General Information about Programs (Intelligent summary)
            if (input.matches(".*(filiere|master|licence|ingenieur|etudier|propos|dispo|liste|offre|formation).*")) {
                List<Filiere> allFilieres = filiereDAO.findAll();
                if (!allFilieres.isEmpty()) {
                    String domains = allFilieres.stream()
                        .map(Filiere::getNom)
                        .distinct()
                        .limit(8)
                        .collect(Collectors.joining("\n• "));
                    return "🎓 **Formations disponibles sur LearnHub** :\n\n" +
                           "Nous proposons un large choix de filières d'excellence :\n• " + domains + "\n\n" +
                           "Souhaitez-vous des détails sur l'une de ces formations ou sur un établissement en particulier ?";
                }
                return "📚 LearnHub regroupe des formations d'excellence en Informatique, Management et Ingénierie.\n\n" +
                       "Dites-moi quel domaine vous passionne !";
            }

            // Intent: Events
            if (input.matches(".*(evenement|event|actu|conference|webinaire|salon|portes ouvertes).*")) {
                return "📅 **Actualités & Événements** :\n\n" +
                       "LearnHub organise régulièrement des salons d'orientation et des webinaires avec nos universités partenaires.\n\n" +
                       "Consultez la section **'Événements'** dans le menu principal pour voir les prochaines dates !";
            }

            // Intent: Partners
            if (input.matches(".*(partenaire|entreprise|reseau|alliance|avec qui).*")) {
                return "🤝 **Notre Réseau** :\n\n" +
                       "Nous collaborons avec plus de **" + allUnis.size() + "** établissements prestigieux et de nombreuses entreprises pour faciliter vos stages.\n\n" +
                       "Voulez-vous voir la liste de nos entreprises partenaires ?";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "⚠️ Je rencontre une petite difficulté technique pour accéder aux données.";
        }

        return "🤔 Je comprends votre intérêt pour LearnHub, mais je n'ai pas assez de détails. \n\n" +
               "Dites-moi si vous voulez **postuler**, trouver une **université** ou connaître les **filières** disponibles !";
    }

    private String normalize(String str) {
        if (str == null) return "";
        return str.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[ç]", "c")
                .replaceAll("[?.,!]", "") // Remove punctuation
                .trim();
    }

    private boolean isOffTopic(String input, List<Universite> allUnis) {
        String[] domainKeywords = {
            "universite", "filiere", "etude", "formation", "inscri", "postuler", 
            "admission", "contact", "telephon", "email", "ville", "etablissement", 
            "learnhub", "cours", "programme", "diplome", "master", "licence", 
            "ingenieur", "informatique", "management", "business", "sante", "ecole", 
            "appren", "etudiant", "bac", "fac", "scolaire", "academique", "dispo", "mieux",
            "evenement", "event", "partenaire", "salon"
        };
        
        for (String key : domainKeywords) {
            if (input.contains(key)) return false;
        }
        
        for (Universite u : allUnis) {
            if (input.contains(normalize(u.getNom()))) return false;
        }

        return !input.matches(".*(bonjour|salut|hello|hey|bjr|merci|thanks|aurevoir|ciao|revoir).*");
    }
}
