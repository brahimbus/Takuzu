# Jeu Binairo - Résolution par CSP

## Description

Application Java pour jouer au **Binairo** (également connu sous le nom de Takuzu), un jeu de logique similaire au Sudoku. Le programme permet de jouer manuellement et inclut un solveur avancé basé sur des algorithmes de **Satisfaction de Contraintes (CSP)**.

## Règles du Jeu

Le Binairo se joue sur une grille carrée (6x6, 8x8 ou 10x10) qui doit être remplie de 0 et de 1 en respectant trois règles :

1. **Pas d'adjacence** : Pas plus de deux chiffres identiques consécutifs (horizontalement ou verticalement)
2. **Équilibre** : Chaque ligne et chaque colonne doit contenir autant de 0 que de 1
3. **Unicité** : Toutes les lignes sont différentes et toutes les colonnes sont différentes

## Structure du Projet


```
Game/binairo/
├── gui/
│   └── ApplicationBinairo.java    # Interface graphique Swing
├── modele/
│   └── EtatBinairo.java           # Modèle de la grille et règles
└── recherche/
    ├── MoteurBinairoAbstrait.java # Classe abstraite pour les solveurs
    └── MoteurBinairoCSP.java      # Solveur CSP avec heuristiques
```

## Fonctionnalités

### Interface Utilisateur
- **Interface Swing moderne** avec disposition organisée
- **Panneau supérieur** : Taille de grille, Générer, Vérifier, Résoudre
- **Panneau latéral gauche** :
  - Section Fichier : Sauvegarder/Charger des grilles
  - Section Assistance : Aide/Suggestion, Retour Arrière
  - Section Heuristiques CSP : Sélection des algorithmes

###  Jeu Manuel
- **Génération aléatoire** de grilles partiellement remplies
- **Clic sur les cases** pour cycler entre Vide → 0 → 1 → Vide
- **Retour arrière** pour annuler les coups
- **Aide/Suggestion** pour obtenir un coup forcé
- **Vérification détaillée** avec localisation précise des violations de règles

### Sauvegarde et Chargement
- **Sérialisation Java** pour sauvegarder l'état complet de la grille
- Format de fichier: `.bin`
- Historique des coups préservé

### Solveur CSP Avancé

Le solveur implémente un **algorithme de backtracking** avec plusieurs heuristiques configurables :

#### Heuristiques de Sélection de Variables
- **MVR (Minimum Remaining  Values)** : Choisit la variable avec le plus petit domaine
- **Degree Heuristic** : En cas d'égalité, choisit la variable avec le plus de contraintes

#### Heuristique d'Ordonnancement des Valeurs
- **LCV (Least Constraining Value)** : Essaie d'abord les valeurs qui laissent le plus d'options

#### Techniques de Propagation de Contraintes
- **FC (Forward Checking)** : Vérifie que l'assignation ne vide pas le domaine des voisins
- **AC-3 (Arc Consistency 3)** : Maintient la cohérence d'arcs avec propagation
- **AC-4 (Arc Consistency 4)** : Version optimisée d'AC-3 avec compteurs de support

### Statistiques de Résolution
Après résolution, l'application affiche :
- **Temps d'exécution** (en millisecondes)
- **Nombre de nœuds explorés**
- **Algorithmes utilisés**
- **Succès ou échec** de la résolution

## Compilation et Exécution

### Prérequis
- Java JDK 8 ou supérieur
- Aucune dépendance externe (utilise uniquement Swing)

### Compilation
```bash
# Depuis le répertoire Game/binairo
javac gui/ApplicationBinairo.java modele/EtatBinairo.java recherche/*.java
```

### Exécution
```bash
java binairo.gui.ApplicationBinairo
```

Ou directement depuis votre IDE en exécutant la classe `ApplicationBinairo`.

## Utilisation

### Jouer Manuellement
1. **Générer** une nouvelle grille avec le bouton "Générer"
2. **Cliquer** sur les cases pour entrer 0, 1 ou laisser vide
3. Utiliser **"Aide / Suggestion"** si bloqué
4. **"Vérifier"** pour voir les violations et tester la résolubilité

### Résolution Automatique
1. **Sélectionner** les algorithmes souhaités dans le panneau de gauche
2. Cliquer sur **"Résoudre (CSP)"**
3. La solution s'affiche avec les statistiques dans une popup



## Architecture Technique

### Modèle (`EtatBinairo`)
- Gestion de la grille avec `Integer[][]`
- Constante `VIDE = -1` pour les cases non remplies
- **Stack** pour l'historique des coups (fonction Retour Arrière)
- Méthodes de validation pour chaque règle
- **Sérialisation** pour la sauvegarde/chargement
- Détection détaillée des violations

### Moteur CSP (`MoteurBinairoCSP`)
- **Domaines** : Map<String, Set<Integer>> pour chaque case vide
- **Backtracking récursif** avec retour arrière automatique
- **Inférence** : FC réduit les domaines après chaque assignation
- **AC-3** : Queue d'arcs à réviser avec propagation
- **AC-4** : Propagation optimisée par élimination de valeurs inconsistantes

### Interface (`ApplicationBinairo`)
- Architecture **Swing** avec `BorderLayout`
- **Popups** pour tous les messages 
- **Threading** pour ne pas bloquer l'UI pendant la résolution
- Mise à jour visuelle de la grille (bleu pour 0, rouge pour 1)

## Exemples de Résultats

### Grille 6x6
- **Algorithmes** : MVR, Degree, LCV
- **Temps** : ~5-15 ms
- **Nœuds** : ~50-150

### Grille 8x8
- **Algorithmes** : MVR, Degree, LCV, FC
- **Temps** : ~20-100 ms
- **Nœuds** : ~200-800

### Grille 10x10
- **Algorithmes** : MVR, Degree, LCV, FC, AC-3
- **Temps** : ~100-500 ms
- **Nœuds** : ~1000-5000

## Améliorations Possibles

- [ ] Générateur de grilles avec garantie d'unicité de solution
- [ ] Niveaux de difficulté (facile, moyen, difficile)
- [ ] Mode compétition avec chronomètre
- [ ] Statistiques de jeu (parties jouées, temps moyen, etc.)
- [ ] Thèmes visuels personnalisables
- [ ] Export/Import au format texte
- [ ] Solveur par A* pour comparaison de performance

## Auteur

Projet développé dans le cadre d'un cours sur les algorithmes de recherche et de satisfaction de contraintes.

## Licence

Projet académique - Libre d'utilisation pour l'apprentissage.

---

**Note** : Ce programme illustre l'application pratique des algorithmes CSP pour résoudre des problèmes combinatoires. Les différentes heuristiques peuvent être activées/désactivées pour observer leur impact sur les performances.
