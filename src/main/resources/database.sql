-- =============================================
-- LearnHub - Base de données MySQL
-- Compatible avec le projet Symfony de référence
-- =============================================

CREATE DATABASE IF NOT EXISTS learnhub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE learnhub_db;

-- =============================================
-- TABLE: utilisateur
-- =============================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    email           VARCHAR(180) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    roles           VARCHAR(50) NOT NULL DEFAULT 'ROLE_ETUDIANT',
    telephone       VARCHAR(20),
    adresse         VARCHAR(255),
    date_naissance  DATE,
    actif           TINYINT(1) NOT NULL DEFAULT 1,
    photo           VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_roles CHECK (roles IN ('ROLE_ADMIN','ROLE_PROFESSEUR','ROLE_ETUDIANT','ROLE_PARENT','ROLE_MEDECIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: filiere
-- =============================================
CREATE TABLE IF NOT EXISTS filiere (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    description TEXT,
    niveau      VARCHAR(50),
    duree       INT COMMENT 'Durée en années',
    responsable VARCHAR(150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: module
-- =============================================
CREATE TABLE IF NOT EXISTS module (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    titre           VARCHAR(200) NOT NULL,
    description     TEXT,
    matiere         VARCHAR(100),
    professeur_id   INT,
    niveau          VARCHAR(50),
    filiere         VARCHAR(100),
    actif           TINYINT(1) NOT NULL DEFAULT 1,
    FOREIGN KEY (professeur_id) REFERENCES utilisateur(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: seance
-- =============================================
CREATE TABLE IF NOT EXISTS seance (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    module_id   INT NOT NULL,
    date        DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin   TIME NOT NULL,
    salle       VARCHAR(50),
    type        VARCHAR(20) DEFAULT 'CM' COMMENT 'CM, TD, TP',
    description TEXT,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: note
-- =============================================
CREATE TABLE IF NOT EXISTS note (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id     INT NOT NULL,
    module_id       INT NOT NULL,
    valeur          DECIMAL(4,2) NOT NULL COMMENT 'Note sur 20',
    type            VARCHAR(30) DEFAULT 'Examen' COMMENT 'Examen, Controle, TP, Projet',
    date_evaluation DATE,
    commentaire     TEXT,
    FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: presence
-- =============================================
CREATE TABLE IF NOT EXISTS presence (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id INT NOT NULL,
    seance_id   INT NOT NULL,
    statut      VARCHAR(20) NOT NULL DEFAULT 'PRESENT' COMMENT 'PRESENT, ABSENT, JUSTIFIE',
    date        DATE NOT NULL,
    commentaire TEXT,
    FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (seance_id) REFERENCES seance(id) ON DELETE CASCADE,
    UNIQUE KEY uq_presence (etudiant_id, seance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: creneau (créneaux médecin)
-- =============================================
CREATE TABLE IF NOT EXISTS creneau (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    medecin_id  INT NOT NULL,
    date        DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin   TIME NOT NULL,
    disponible  TINYINT(1) NOT NULL DEFAULT 1,
    FOREIGN KEY (medecin_id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: rdv (rendez-vous médicaux)
-- =============================================
CREATE TABLE IF NOT EXISTS rdv (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    patient_id  INT NOT NULL,
    medecin_id  INT NOT NULL,
    creneau_id  INT,
    date_heure  DATETIME NOT NULL,
    motif       VARCHAR(255),
    statut      VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' COMMENT 'EN_ATTENTE, CONFIRME, ANNULE, TERMINE',
    notes       TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (medecin_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (creneau_id) REFERENCES creneau(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: evenement
-- =============================================
CREATE TABLE IF NOT EXISTS evenement (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    titre           VARCHAR(200) NOT NULL,
    description     TEXT,
    date            DATETIME NOT NULL,
    lieu            VARCHAR(200),
    organisateur_id INT,
    categorie       VARCHAR(100),
    capacite        INT DEFAULT 0,
    image           VARCHAR(255),
    FOREIGN KEY (organisateur_id) REFERENCES utilisateur(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLE: participation (étudiant <-> événement)
-- =============================================
CREATE TABLE IF NOT EXISTS participation (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT NOT NULL,
    evenement_id    INT NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (evenement_id) REFERENCES evenement(id) ON DELETE CASCADE,
    UNIQUE KEY uq_participation (utilisateur_id, evenement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- DONNÉES DE DÉMONSTRATION
-- =============================================

-- Admin
INSERT INTO utilisateur (nom, prenom, email, password, roles) VALUES
('Admin', 'Super', 'admin@learnhub.tn', 'admin123', 'ROLE_ADMIN');

-- Professeurs
INSERT INTO utilisateur (nom, prenom, email, password, roles, telephone) VALUES
('Ben Ali', 'Mohamed', 'prof1@learnhub.tn', 'prof123', 'ROLE_PROFESSEUR', '+216 71 000 001'),
('Trabelsi', 'Fatma', 'prof2@learnhub.tn', 'prof123', 'ROLE_PROFESSEUR', '+216 71 000 002');

-- Étudiants
INSERT INTO utilisateur (nom, prenom, email, password, roles, telephone) VALUES
('Mansouri', 'Amine', 'amine@learnhub.tn', 'etu123', 'ROLE_ETUDIANT', '+216 71 100 001'),
('Chaabane', 'Eya', 'eya@learnhub.tn', 'etu123', 'ROLE_ETUDIANT', '+216 71 100 002'),
('Boughanmi', 'Aziz', 'aziz@learnhub.tn', 'etu123', 'ROLE_ETUDIANT', '+216 71 100 003');

-- Parent
INSERT INTO utilisateur (nom, prenom, email, password, roles, telephone) VALUES
('Mansouri', 'Hedi', 'parent@learnhub.tn', 'parent123', 'ROLE_PARENT', '+216 71 200 001');

-- Médecin
INSERT INTO utilisateur (nom, prenom, email, password, roles, telephone) VALUES
('Dahmani', 'Karim', 'medecin@learnhub.tn', 'med123', 'ROLE_MEDECIN', '+216 71 300 001');

-- Filières
INSERT INTO filiere (nom, description, niveau, duree, responsable) VALUES
('Informatique', 'Formation en développement logiciel et réseaux', 'Licence', 3, 'Ben Ali Mohamed'),
('Gestion', 'Formation en gestion d''entreprise et finance', 'Licence', 3, 'Trabelsi Fatma'),
('Médecine', 'Formation médicale complète', 'Doctorat', 6, 'Dahmani Karim');

-- Modules
INSERT INTO module (titre, description, matiere, professeur_id, niveau, filiere) VALUES
('Programmation Java', 'Introduction à Java et POO', 'Informatique', 2, 'L2', 'Informatique'),
('Bases de Données', 'SQL, MySQL et conception BDD', 'Informatique', 2, 'L2', 'Informatique'),
('Algorithmes', 'Structures de données et algorithmique', 'Mathématiques', 3, 'L1', 'Informatique'),
('Marketing Digital', 'Marketing en ligne et réseaux sociaux', 'Gestion', 3, 'L3', 'Gestion');

-- Séances
INSERT INTO seance (module_id, date, heure_debut, heure_fin, salle, type) VALUES
(1, '2025-04-10', '08:00:00', '10:00:00', 'Salle A1', 'CM'),
(1, '2025-04-14', '10:00:00', '12:00:00', 'Lab Info', 'TP'),
(2, '2025-04-11', '14:00:00', '16:00:00', 'Salle B2', 'TD'),
(3, '2025-04-12', '08:00:00', '10:00:00', 'Amphi 1', 'CM');

-- Notes
INSERT INTO note (etudiant_id, module_id, valeur, type, date_evaluation) VALUES
(4, 1, 15.5, 'Examen', '2025-03-20'),
(4, 2, 13.0, 'Controle', '2025-03-22'),
(5, 1, 17.0, 'Examen', '2025-03-20'),
(5, 3, 11.5, 'TP', '2025-03-25'),
(6, 1, 12.0, 'Examen', '2025-03-20');

-- Présences
INSERT INTO presence (etudiant_id, seance_id, statut, date) VALUES
(4, 1, 'PRESENT', '2025-04-10'),
(4, 2, 'ABSENT', '2025-04-14'),
(5, 1, 'PRESENT', '2025-04-10'),
(5, 2, 'PRESENT', '2025-04-14'),
(6, 1, 'JUSTIFIE', '2025-04-10');

-- Créneaux médecin
INSERT INTO creneau (medecin_id, date, heure_debut, heure_fin, disponible) VALUES
(8, '2025-04-15', '09:00:00', '09:30:00', 1),
(8, '2025-04-15', '09:30:00', '10:00:00', 1),
(8, '2025-04-15', '10:00:00', '10:30:00', 0),
(8, '2025-04-16', '14:00:00', '14:30:00', 1),
(8, '2025-04-16', '14:30:00', '15:00:00', 1);

-- RDV
INSERT INTO rdv (patient_id, medecin_id, creneau_id, date_heure, motif, statut) VALUES
(4, 8, 1, '2025-04-15 09:00:00', 'Consultation générale', 'CONFIRME'),
(5, 8, 3, '2025-04-15 10:00:00', 'Mal de tête persistant', 'TERMINE'),
(6, 8, 4, '2025-04-16 14:00:00', 'Certificat médical', 'EN_ATTENTE');

-- Événements
INSERT INTO evenement (titre, description, date, lieu, organisateur_id, categorie, capacite) VALUES
('Journée Portes Ouvertes', 'Découvrez nos formations et rencontrez nos équipes', '2025-04-20 09:00:00', 'Campus Principal', 1, 'Institutionnel', 500),
('Hackathon LearnHub 2025', 'Compétition de programmation 24h', '2025-05-03 08:00:00', 'Lab Informatique', 2, 'Compétition', 60),
('Conférence Santé Étudiante', 'Bien-être physique et mental des étudiants', '2025-04-25 14:00:00', 'Amphi A', 8, 'Santé', 200),
('Remise des Diplômes 2025', 'Cérémonie officielle de remise des diplômes', '2025-06-15 10:00:00', 'Grande Salle', 1, 'Cérémonie', 1000);
