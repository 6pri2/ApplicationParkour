# Parkour Competition Judge App

![Parkour Logo](![ic_launcher_adaptive_fore](https://github.com/user-attachments/assets/75323e31-a224-4ae5-8d29-b6571a1c75f2)
) *(Remplacez par votre logo)*

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
  - Synchronisation avec le serveur distant aux moments opportuns

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
