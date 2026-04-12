# 🎓 LearnHub JavaFX

Projet JavaFX complet reproduisant le projet Symfony web de référence.

## Architecture

```
learnhub-javafx/
├── src/main/java/com/learnhub/
│   ├── MainApp.java                   ← Point d'entrée
│   ├── models/                        ← Entités (Utilisateur, Module, Note...)
│   ├── dao/                           ← Accès base de données (JDBC)
│   ├── controller/
│   │   ├── auth/                      ← Login, Register
│   │   ├── visiteur/                  ← Page d'accueil publique
│   │   ├── admin/                     ← CRUD complet (Admin)
│   │   ├── professor/                 ← Espace Professeur
│   │   ├── student/                   ← Espace Étudiant
│   │   ├── parent/                    ← Espace Parent
│   │   └── medecin/                   ← Espace Médecin
│   └── util/                          ← DatabaseConnection, SessionManager, NavigationUtil
├── src/main/resources/
│   ├── fxml/                          ← Interfaces FXML par rôle
│   ├── css/styles.css                 ← Style Bootstrap-like
│   └── database.sql                   ← Schéma + données de démo
└── pom.xml                            ← Dépendances Maven
```

## Interfaces disponibles

| Interface       | Description                                   |
|----------------|-----------------------------------------------|
| Visiteur Home  | Page d'accueil publique avec stats et events  |
| Login          | Connexion avec redirection par rôle           |
| Register       | Inscription pour étudiants/parents/etc.       |
| Admin Dashboard| Stats, accès rapide à tous les modules        |
| Admin CRUD     | Utilisateurs, Modules, Notes, RDV, Séances... |
| Étudiant       | Notes, présences, RDV médical, emploi du temps|
| Professeur     | Modules, séances, saisie notes & présences    |
| Parent         | Suivi notes et présences de l'enfant          |
| Médecin        | Gestion créneaux et RDV médicaux              |

## Installation

### 1. Prérequis
- Java 17 ou supérieur
- Maven 3.8+
- MySQL 8.x

### 2. Base de données
```sql
-- Dans MySQL Workbench ou phpMyAdmin :
SOURCE /chemin/vers/learnhub-javafx/src/main/resources/database.sql;
```

### 3. Configuration DB (si besoin)
Modifier `src/main/java/com/learnhub/util/DatabaseConnection.java` :
```java
private static final String URL = "jdbc:mysql://localhost:3306/learnhub_db?...";
private static final String USER = "root";
private static final String PASSWORD = "";  // votre mot de passe MySQL
```

### 4. Lancer le projet
```bash
mvn javafx:run
```

## Comptes de démonstration

| Rôle        | Email                   | Mot de passe |
|-------------|-------------------------|--------------|
| Admin       | admin@learnhub.tn       | admin123     |
| Professeur  | prof1@learnhub.tn       | prof123      |
| Étudiant    | amine@learnhub.tn       | etu123       |
| Parent      | parent@learnhub.tn      | parent123    |
| Médecin     | medecin@learnhub.tn     | med123       |

> ⚠️ Les mots de passe dans la démo sont en clair pour faciliter les tests.
> En production, utiliser BCrypt (déjà inclus dans RegisterController).

## Entités / Modules CRUD

- ✅ Utilisateurs (tous rôles)
- ✅ Modules d'enseignement
- ✅ Séances de cours
- ✅ Notes des étudiants
- ✅ Présences
- ✅ Créneaux médicaux
- ✅ Rendez-vous médicaux (RDV)
- ✅ Événements du campus
- ✅ Filières

## Dépendances

| Dépendance       | Version   | Rôle                        |
|------------------|-----------|-----------------------------|
| JavaFX           | 21.0.1    | Interface graphique         |
| MySQL Connector  | 8.3.0     | Connexion base de données   |
| BCrypt           | 0.10.2    | Hashage des mots de passe   |
| iText PDF        | 5.5.13.3  | Génération de bulletins PDF |
