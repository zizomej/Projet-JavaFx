-- Script pour créer les tables de Quiz

CREATE TABLE IF NOT EXISTS quiz (
    id INT AUTO_INCREMENT PRIMARY KEY,
    module_id INT NOT NULL,
    professeur_id INT NOT NULL,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    deadline DATETIME NOT NULL,
    is_visible BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (module_id) REFERENCES module(id) ON DELETE CASCADE,
    FOREIGN KEY (professeur_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    texte_question TEXT NOT NULL,
    type_question VARCHAR(50) DEFAULT 'QCM',
    points INT DEFAULT 1,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_option (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    texte_option TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES quiz_question(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_soumission (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    etudiant_id INT NOT NULL,
    note_obtenue INT NOT NULL,
    date_soumission DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE,
    FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);
