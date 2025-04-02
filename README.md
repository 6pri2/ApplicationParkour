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
 

## 📂 app

### 📂 manifests
- `AndroidManifest.xml`

### 📂 kotlin+java
#### 📂 iut.gon.applicationparkour

##### 📂 data
- 📂 api
  - `ApiClient`
  - `ApiService`
- 📂 model
  - `AddCompetitorRequest`
  - `AddObstacleRequest`
  - `Competition`
  - `Competitor`
  - `Courses`
  - `CourseUpdateRequest`
  - `CreateCourseRequest`
  - `ObstacleCourse`
  - `Obstacles`
  - `UpdateObstaclePositionRequest`

##### 📂 ui
- 📂 app
  - `ParkourApp.kt`
- 📂 components
  - 📂 competition
    - `CompetitionEditDialog.kt`
    - `CompetitionItem.kt`
    - `DeleteCompetitionDialog.kt`
  - 📂 competitor
    - `AddCompetitorDialog.kt`
    - `calculateAge.kt`
    - `CompetitorCard.kt`
  - 📂 courses
    - `CourseAddDialog.kt`
    - `CourseEditDialog.kt`
    - `CourseItem.kt`
    - `CourseItemModif.kt`
  - 📂 obstacle
    - `AddObstacleDialog.kt`
    - `ObstacleItem.kt`
  - 📂 scaffold
    - `ScreenScaffold.kt`

##### 📂 screens
- `ArbitrageScreen.kt`
- `CompetitionArbitrageScreen.kt`
- `CompetitionCompetitorsScreen.kt`
- `CompetitionCoursesScreen.kt`
- `CompetitionResultsScreen.kt`
- `CompetitionScreen.kt`
- `CompetitorScreen.kt`
- `CourseObstaclesScreen.kt`
- `ObstaclesScreen.kt`
- `ResultScreen.kt`
- `WelcomeScreen.kt`

##### 📂 theme
- `Color.kt`
- `Theme.kt`
- `Type.kt`

- `MainActivity.kt`

## 📱 Navigation principale

```mermaid
graph TD
    A[Accueil] --> B[[Créer Compétition]]
    A --> C[[Arbitrer Compétition]]
    B --> D[Liste Compétitions]
    C --> D
    D --> E[Détails Compétition]
    E --> F[Parcours]
    E --> G[Concurrents]
    E --> H[Arbitrage]
    H --> I[Chronomètre Obstacle]
    H --> J[Classement]
