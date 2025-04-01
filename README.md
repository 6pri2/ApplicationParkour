# Parkour Competition Judge App

![Parkour Logo](https://via.placeholder.com/150) *(Remplacez par votre logo)*

Application Android pour l'arbitrage des compétitions de parkour (type Ninja Warrior) - Projet R4.11 BUT Informatique

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

## 📱 Interfaces clés

```mermaid
graph TD
    A[Accueil] --> B[Liste Compétitions]
    B --> C[Création Compétition]
    B --> D[Détails Compétition]
    D --> E[Parcours]
    D --> F[Concurrents]
    D --> G[Arbitrage]
    G --> H[Chronomètre Obstacle]
    G --> I[Classement]
