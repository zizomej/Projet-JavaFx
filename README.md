# LearnHub JavaFX — Design Symfony complet

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
├── css/styles.css  → Design complet Symfony (sidebar, cards, hero, auth...)
└── fxml/
    ├── admin/      → dashboard, utilisateurs, modules, notes, rdv, partenaires, offrestage, demandestage
    ├── auth/       → login (glassmorphism), register
    ├── professor/  → dashboard (topnav horizontal Symfony)
    ├── student/    → dashboard (sidebar bleue)
    ├── parent/     → dashboard (bannière violette)
    ├── medecin/    → dashboard (sidebar bleue)
    └── visiteur/   → home, events, programs, partners (navbar blanche)
```

## Design Symfony respecté
- **Sidebar** : gradient `#0A1F44 → #1E3A8A`, bordure jaune `#FFC107` sur item actif
- **Login** : glassmorphism sur fond gradient bleu foncé, titre jaune `#facc15`
- **Professor topnav** : gradient horizontal, bouton actif `#FFC107 → #FF6F61`
- **Welcome banner** : gradient bleu foncé, avatar jaune-coral
- **Stat cards** : `border-left` colorée selon variante (primary/success/warning/info/danger)
- **Tables** : fond blanc, header `#f9fafb`, colonne header `#1E3A8A`
- **Visiteur hero** : gradient `#0A1F44 → #1E3A8A`, bouton CTA jaune `#FFC107`

## Démarrage
1. Ouvrir le dossier dans IntelliJ IDEA
2. Maven → Reload Project
3. MySQL démarré + base `gestion_universitaire`
4. Run `MainApp.java`
