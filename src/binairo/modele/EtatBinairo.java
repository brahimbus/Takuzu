package binairo.modele;

import java.io.*;
import java.util.Stack;

/**
 * Représente l'état de la grille du jeu Binairo.
 * Gère la grille, les règles de validation et l'historique des coups.
 */
public class EtatBinairo implements Serializable {
    private Integer[][] grille;
    private int taille;
    private Stack<Coup> historique;

    // Constantes pour les valeurs
    public static final int VIDE = -1;
    public static final int ZERO = 0;
    public static final int UN = 1;

    /**
     * Classe interne pour représenter un coup (pour l'historique).
     */
    private static class Coup implements Serializable {
        int ligne, col, ancienneValeur, nouvelleValeur;

        Coup(int l, int c, int av, int nv) {
            this.ligne = l;
            this.col = c;
            this.ancienneValeur = av;
            this.nouvelleValeur = nv;
        }
    }

    public EtatBinairo(int taille) {
        this.taille = taille;
        this.grille = new Integer[taille][taille];
        this.historique = new Stack<>();
        initialiserGrilleVide();
    }

    // Constructeur de copie
    public EtatBinairo(EtatBinairo autre) {
        this.taille = autre.taille;
        this.grille = new Integer[taille][taille];
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                this.grille[i][j] = autre.grille[i][j];
            }
        }
        this.historique = new Stack<>();
    }

    private void initialiserGrilleVide() {
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                grille[i][j] = VIDE;
            }
        }
    }

    public int getTaille() {
        return taille;
    }

    public Integer getValeur(int i, int j) {
        // Fix NPE: Si la case est null (ne devrait pas arriver avec VIDE, mais
        // sécurité), retourner VIDE
        if (grille[i][j] == null)
            return VIDE;
        return grille[i][j];
    }

    public void setValeur(int i, int j, Integer valeur) {
        Integer ancienne = grille[i][j];
        if (ancienne == null)
            ancienne = VIDE;

        // Normaliser la valeur entrante
        int valFinale = (valeur == null) ? VIDE : valeur;

        grille[i][j] = valFinale;
        historique.push(new Coup(i, j, ancienne, valFinale));
    }

    /**
     * Annule le dernier coup.
     * 
     * @return true si un coup a été annulé, false sinon.
     */
    public boolean annulerCoup() {
        if (historique.isEmpty())
            return false;
        Coup dernier = historique.pop();
        grille[dernier.ligne][dernier.col] = dernier.ancienneValeur;
        return true;
    }

    public boolean estComplet() {
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == VIDE)
                    return false;
            }
        }
        return true;
    }

    public boolean estValide() {
        return verifierAdjacence() && verifierEquilibre() && (!estComplet() || verifierUnicite());
    }

    // Règle 1: Pas plus de deux chiffres identiques consécutifs
    private boolean verifierAdjacence() {
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == VIDE)
                    continue;
                int val = grille[i][j];

                // Horizontal
                if (j + 2 < taille) {
                    if (grille[i][j + 1] != VIDE && grille[i][j + 1] == val &&
                            grille[i][j + 2] != VIDE && grille[i][j + 2] == val)
                        return false;
                }
                // Vertical
                if (i + 2 < taille) {
                    if (grille[i + 1][j] != VIDE && grille[i + 1][j] == val &&
                            grille[i + 2][j] != VIDE && grille[i + 2][j] == val)
                        return false;
                }
            }
        }
        return true;
    }

    // Règle 2: Autant de 0 que de 1
    private boolean verifierEquilibre() {
        for (int i = 0; i < taille; i++) {
            int nb0_lig = 0, nb1_lig = 0;
            int nb0_col = 0, nb1_col = 0;

            for (int j = 0; j < taille; j++) {
                // Ligne
                if (grille[i][j] != VIDE) {
                    if (grille[i][j] == 0)
                        nb0_lig++;
                    else
                        nb1_lig++;
                }
                // Colonne
                if (grille[j][i] != VIDE) {
                    if (grille[j][i] == 0)
                        nb0_col++;
                    else
                        nb1_col++;
                }
            }

            if (nb0_lig > taille / 2 || nb1_lig > taille / 2)
                return false;
            if (nb0_col > taille / 2 || nb1_col > taille / 2)
                return false;
        }
        return true;
    }

    // Règle 3: Unicité des lignes et colonnes
    private boolean verifierUnicite() {
        for (int i = 0; i < taille; i++) {
            for (int k = i + 1; k < taille; k++) {
                if (lignesIdentiques(i, k))
                    return false;
            }
        }
        for (int j = 0; j < taille; j++) {
            for (int k = j + 1; k < taille; k++) {
                if (colonnesIdentiques(j, k))
                    return false;
            }
        }
        return true;
    }

    private boolean lignesIdentiques(int l1, int l2) {
        for (int j = 0; j < taille; j++) {
            if (grille[l1][j] == VIDE || grille[l2][j] == VIDE ||
                    !grille[l1][j].equals(grille[l2][j]))
                return false;
        }
        return true;
    }

    private boolean colonnesIdentiques(int c1, int c2) {
        for (int i = 0; i < taille; i++) {
            if (grille[i][c1] == VIDE || grille[i][c2] == VIDE ||
                    !grille[i][c1].equals(grille[i][c2]))
                return false;
        }
        return true;
    }

    // --- Fonctionnalités Avancées ---

    public void sauvegarder(String chemin) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chemin))) {
            oos.writeObject(this);
        }
    }

    public static EtatBinairo charger(String chemin) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chemin))) {
            return (EtatBinairo) ois.readObject();
        }
    }

    /**
     * Cherche une suggestion simple (coup forcé).
     * 
     * @return Un tableau [ligne, col, valeur] ou null si rien d'évident.
     */
    public int[] getSuggestion() {
        // 1. Chercher des triplets potentiels (0 0 -> 1, 1 1 -> 0, 0 . 0 -> 1)
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == VIDE) {
                    // Essayer 0
                    setValeur(i, j, 0);
                    boolean zeroValide = estValide();
                    annulerCoup();

                    // Essayer 1
                    setValeur(i, j, 1);
                    boolean unValide = estValide();
                    annulerCoup();

                    if (zeroValide && !unValide)
                        return new int[] { i, j, 0 };
                    if (!zeroValide && unValide)
                        return new int[] { i, j, 1 };
                }
            }
        }
        return null;
    }

    /**
     * Retourne une liste détaillée des violations de règles.
     * 
     * @return Liste de messages décrivant chaque violation
     */
    public java.util.List<String> getViolations() {
        java.util.List<String> violations = new java.util.ArrayList<>();

        // Vérifier les adjacences
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == VIDE)
                    continue;
                int val = grille[i][j];

                // Horizontal
                if (j + 2 < taille) {
                    if (grille[i][j + 1] != VIDE && grille[i][j + 1] == val &&
                            grille[i][j + 2] != VIDE && grille[i][j + 2] == val) {
                        violations.add("Règle d'adjacence violée: Ligne " + (i + 1) + ", colonnes " +
                                (j + 1) + "-" + (j + 3) + " (trois " + val + " consécutifs)");
                    }
                }
                // Vertical
                if (i + 2 < taille) {
                    if (grille[i + 1][j] != VIDE && grille[i + 1][j] == val &&
                            grille[i + 2][j] != VIDE && grille[i + 2][j] == val) {
                        violations.add("Règle d'adjacence violée: Colonne " + (j + 1) + ", lignes " +
                                (i + 1) + "-" + (i + 3) + " (trois " + val + " consécutifs)");
                    }
                }
            }
        }

        // Vérifier l'équilibre
        for (int i = 0; i < taille; i++) {
            int nb0_lig = 0, nb1_lig = 0;
            int nb0_col = 0, nb1_col = 0;

            for (int j = 0; j < taille; j++) {
                if (grille[i][j] != VIDE) {
                    if (grille[i][j] == 0)
                        nb0_lig++;
                    else
                        nb1_lig++;
                }
                if (grille[j][i] != VIDE) {
                    if (grille[j][i] == 0)
                        nb0_col++;
                    else
                        nb1_col++;
                }
            }

            if (nb0_lig > taille / 2) {
                violations.add("Règle d'équilibre violée: Ligne " + (i + 1) + " a trop de 0 (" + nb0_lig + "/"
                        + (taille / 2) + ")");
            }
            if (nb1_lig > taille / 2) {
                violations.add("Règle d'équilibre violée: Ligne " + (i + 1) + " a trop de 1 (" + nb1_lig + "/"
                        + (taille / 2) + ")");
            }
            if (nb0_col > taille / 2) {
                violations.add("Règle d'équilibre violée: Colonne " + (i + 1) + " a trop de 0 (" + nb0_col + "/"
                        + (taille / 2) + ")");
            }
            if (nb1_col > taille / 2) {
                violations.add("Règle d'équilibre violée: Colonne " + (i + 1) + " a trop de 1 (" + nb1_col + "/"
                        + (taille / 2) + ")");
            }
        }

        // Vérifier l'unicité (seulement si la grille est complète)
        if (estComplet()) {
            for (int i = 0; i < taille; i++) {
                for (int k = i + 1; k < taille; k++) {
                    if (lignesIdentiques(i, k)) {
                        violations.add(
                                "Règle d'unicité violée: Lignes " + (i + 1) + " et " + (k + 1) + " sont identiques");
                    }
                }
            }
            for (int j = 0; j < taille; j++) {
                for (int k = j + 1; k < taille; k++) {
                    if (colonnesIdentiques(j, k)) {
                        violations.add(
                                "Règle d'unicité violée: Colonnes " + (j + 1) + " et " + (k + 1) + " sont identiques");
                    }
                }
            }
        }

        return violations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                sb.append(grille[i][j] == VIDE ? "." : grille[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
