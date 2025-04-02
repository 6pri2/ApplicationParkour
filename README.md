# Application Parkour 

![Parkour Logo](https://media.istockphoto.com/id/528565136/fr/vectoriel/parkour-saut-silhouette.jpg?s=612x612&w=0&k=20&c=tcklP_GYbr1PukVrEs_Q4QJMCVmpzSKXerojUMZraNY=) 

Application Android pour l'arbitrage des compétitions de parkour - Projet R4.11 BUT Informatique

## 🎯 Fonctionnalités

- **Gestion des compétitions** :
  - Création de compétitions avec parcours et obstacles
  - Définition des catégories (genre, tranche d'âge)
  
- **Gestion des concurrents** :
  - Inscription des athlètes aux compétitions
  - Filtrage par éligibilité (âge, genre)

- **Arbitrage en temps réel** :
  - Chronométrage précis au 10ème de seconde
  - Gestion des chutes et reprises
  - Calcul automatique des classements

- **Synchronisation** :
  - Stockage local pour un arbitrage sans latence

```bash
app/
│
├── manifests/
│   └── AndroidManifest.xml
│
├── kotlin+java/
│   └── iut.gon.applicationparkour/
│       │
│       ├── data/
│       │   ├── api/
│       │   │   ├── ApiClient.kt
│       │   │   └── ApiService.kt
│       │   └── model/
│       │       ├── AddCompetitorRequest.kt
│       │       ├── AddObstacleRequest.kt
│       │       ├── Competition.kt
│       │       ├── Competitor.kt
│       │       ├── Courses.kt
│       │       ├── CourseUpdateRequest.kt
│       │       ├── CreateCourseRequest.kt
│       │       ├── ObstacleCourse.kt
│       │       ├── Obstacles.kt
│       │       └── UpdateObstaclePositionRequest.kt
│       │
│       ├── ui/
│       │   ├── app/
│       │   │   └── ParkourApp.kt
│       │   ├── components/
│       │   │   ├── competition/
│       │   │   │   ├── CompetitionEditDialog.kt
│       │   │   │   ├── CompetitionItem.kt
│       │   │   │   └── DeleteCompetitionDialog.kt
│       │   │   ├── competitor/
│       │   │   │   ├── AddCompetitorDialog.kt
│       │   │   │   ├── calculateAge.kt
│       │   │   │   └── CompetitorCard.kt
│       │   │   ├── courses/
│       │   │   │   ├── CourseAddDialog.kt
│       │   │   │   ├── CourseEditDialog.kt
│       │   │   │   ├── CourseItem.kt
│       │   │   │   └── CourseItemModif.kt
│       │   │   ├── obstacle/
│       │   │   │   ├── AddObstacleDialog.kt
│       │   │   │   └── ObstacleItem.kt
│       │   │   └── scaffold/
│       │   │       └── ScreenScaffold.kt
│       │   └── screens/
│       │       ├── ArbitrageScreen.kt
│       │       ├── CompetitionArbitrageScreen.kt
│       │       ├── CompetitionCompetitorsScreen.kt
│       │       ├── CompetitionCoursesScreen.kt
│       │       ├── CompetitionResultsScreen.kt
│       │       ├── CompetitionScreen.kt
│       │       ├── CompetitorScreen.kt
│       │       ├── CourseObstaclesScreen.kt
│       │       ├── ObstaclesScreen.kt
│       │       ├── ResultScreen.kt
│       │       └── WelcomeScreen.kt
│       │
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
│
└── MainActivity.kt

```

## 📱 Navigation principale

```mermaid
graph TD
    A[Accueil] --> B[Competition]
    A --> C[Competiteur]
    A --> D[Obstacles]
    
    B --> E[Liste Compétitions]
    C --> F[Liste Participants]
    D --> G[Modifier Obstacle]
    D --> H[Supprimer Obstacle]
    
    %% Actions Compétitions
    E --> E1[Modifier Compétition]
    E --> E2[Supprimer Compétition]
    E --> I[Classement]
    E --> J[Compétiteurs]
    
    %% Actions Concurrents
    J --> J1[Ajouter Compétiteur]
    J --> J2[Supprimer Compétiteur]
    
    %% Actions Arbitrage
    E --> L[Arbitrage]
    L --> L1[Chronomètre Obstacle]
    L --> L2[Noter]
    
    %% Actions Participants
    F --> O[Ajouter Participant]
    F --> P[Modifier Participant]
    F --> Q[Supprimer Participant]
    
    %% Actions Obstacles
    D --> G
    D --> H
    

    
