# LearnHub JavaFX 

## Architecture MVC
```
src/main/java/com/learnhub/
├── MainApp.java
├── controller/
│   ├── admin/      → AdminDashboard, Utilisateurs, Modules, Notes, Rdv, Partenaires, OffresStage, DemandesStage
│   ├── auth/       → Login, Register
│   ├── professor/  → ProfessorDashboard
│   ├── student/    → StudentDashboard
│   ├── parent/     → ParentDashboard
│   ├── medecin/    → MedecinDashboard
│   └── visiteur/   → Home, Events, Programs, Partners
├── dao/            → UtilisateurDAO, ModuleDAO, FiliereDAO, EvenementDAO...
├── models/         → Utilisateur, Module, Filiere, Evenement, Rdv...
└── util/           → DatabaseConnection, NavigationUtil, SessionManager

src/main/resources/
├── css/styles.css  → Design complet 
└── fxml/
    ├── admin/      → dashboard, utilisateurs, modules, notes, rdv, partenaires, offrestage, demandestage
    ├── auth/       → login , register
    ├── professor/  → dashboard 
    ├── student/    → dashboard 
    ├── parent/     → dashboard 
    ├── medecin/    → dashboard 
    └── visiteur/   → home, events, programs, partners 
```


## Démarrage
1. Ouvrir le dossier dans IntelliJ IDEA
2. Maven → Reload Project
3. MySQL démarré + base `gestion_universitaire`
4. Run `MainApp.java`
