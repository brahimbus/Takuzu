package binairo.modele;

import java.io.*;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

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
        if (grille[i][j] == null)
            return VIDE;
        return grille[i][j];
    }

    public void setValeur(int i, int j, Integer valeur) {
        Integer ancienne = grille[i][j];
        if (ancienne == null)
            ancienne = VIDE;

        int valFinale = (valeur == null) ? VIDE : valeur;

        grille[i][j] = valFinale;
        historique.push(new Coup(i, j, ancienne, valFinale));
    }

    /**
     * Annule le dernier coup.
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
        return verifierAdjacence() && verifierEquilibre() && verifierUnicite();
    }

    // Règle 1: Pas plus de deux chiffres identiques consécutifs
    private boolean verifierAdjacence() {
        // Vérification horizontale
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille - 2; j++) {
                if (grille[i][j] != VIDE && grille[i][j + 1] != VIDE && grille[i][j + 2] != VIDE) {
                    if (grille[i][j].equals(grille[i][j + 1]) && grille[i][j].equals(grille[i][j + 2])) {
                        return false;
                    }
                }
            }
        }

        // Vérification verticale
        for (int j = 0; j < taille; j++) {
            for (int i = 0; i < taille - 2; i++) {
                if (grille[i][j] != VIDE && grille[i + 1][j] != VIDE && grille[i + 2][j] != VIDE) {
                    if (grille[i][j].equals(grille[i + 1][j]) && grille[i][j].equals(grille[i + 2][j])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Règle 2: Autant de 0 que de 1 par ligne et colonne
    private boolean verifierEquilibre() {
        int demiTaille = taille / 2;

        for (int i = 0; i < taille; i++) {
            int nb0Ligne = 0, nb1Ligne = 0;
            int nb0Colonne = 0, nb1Colonne = 0;

            for (int j = 0; j < taille; j++) {
                // Compter ligne
                if (grille[i][j] == ZERO) nb0Ligne++;
                else if (grille[i][j] == UN) nb1Ligne++;

                // Compter colonne
                if (grille[j][i] == ZERO) nb0Colonne++;
                else if (grille[j][i] == UN) nb1Colonne++;
            }

            if (nb0Ligne > demiTaille || nb1Ligne > demiTaille) return false;
            if (nb0Colonne > demiTaille || nb1Colonne > demiTaille) return false;
        }
        return true;
    }

    // Règle 3: Unicité des lignes et colonnes
    private boolean verifierUnicite() {
        // Vérifier l'unicité des lignes
        for (int i = 0; i < taille; i++) {
            for (int k = i + 1; k < taille; k++) {
                if (lignesIdentiques(i, k)) {
                    return false;
                }
            }
        }

        // Vérifier l'unicité des colonnes
        for (int j = 0; j < taille; j++) {
            for (int k = j + 1; k < taille; k++) {
                if (colonnesIdentiques(j, k)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean lignesIdentiques(int l1, int l2) {
        for (int j = 0; j < taille; j++) {
            // Si une case est vide dans l'une des lignes, elles ne sont pas considérées comme identiques
            if (grille[l1][j] == VIDE || grille[l2][j] == VIDE) {
                return false;
            }
            if (!grille[l1][j].equals(grille[l2][j])) {
                return false;
            }
        }
        return true;
    }

    private boolean colonnesIdentiques(int c1, int c2) {
        for (int i = 0; i < taille; i++) {
            // Si une case est vide dans l'une des colonnes, elles ne sont pas considérées comme identiques
            if (grille[i][c1] == VIDE || grille[i][c2] == VIDE) {
                return false;
            }
            if (!grille[i][c1].equals(grille[i][c2])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cherche une suggestion simple (coup forcé).
     * Améliorations :
     *  - vérifie les triplets horizontaux et verticaux
     *  - vérifie les motifs _ x _ (par exemple 0 _ 0 => milieu = 1)
     *  - tente pour chaque case vide d'assigner 0 puis 1 et retourne
     *    une valeur forcée si l'autre mène à une contradiction
     */
    public int[] getSuggestion() {
        int demiTaille = taille / 2;

        // 1) Triplets horizontaux (existants) et motifs 0 _ 0 ou 1 _ 1
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille - 2; j++) {
                // Horizontal: 0 0 _ → doit être 1
                if (grille[i][j] == ZERO && grille[i][j + 1] == ZERO && grille[i][j + 2] == VIDE) {
                    if (isValidAfterSetting(i, j + 2, UN)) return new int[]{i, j + 2, UN};
                }
                // Horizontal: 1 1 _ → doit être 0
                if (grille[i][j] == UN && grille[i][j + 1] == UN && grille[i][j + 2] == VIDE) {
                    if (isValidAfterSetting(i, j + 2, ZERO)) return new int[]{i, j + 2, ZERO};
                }
                // Horizontal: _ 0 0 → doit être 1
                if (grille[i][j] == VIDE && grille[i][j + 1] == ZERO && grille[i][j + 2] == ZERO) {
                    if (isValidAfterSetting(i, j, UN)) return new int[]{i, j, UN};
                }
                // Horizontal: _ 1 1 → doit être 0
                if (grille[i][j] == VIDE && grille[i][j + 1] == UN && grille[i][j + 2] == UN) {
                    if (isValidAfterSetting(i, j, ZERO)) return new int[]{i, j, ZERO};
                }

                // Horizontal: pattern 0 _ 0 -> middle must be 1
                if (grille[i][j] == ZERO && grille[i][j + 1] == VIDE && grille[i][j + 2] == ZERO) {
                    if (isValidAfterSetting(i, j + 1, UN)) return new int[]{i, j + 1, UN};
                }
                if (grille[i][j] == UN && grille[i][j + 1] == VIDE && grille[i][j + 2] == UN) {
                    if (isValidAfterSetting(i, j + 1, ZERO)) return new int[]{i, j + 1, ZERO};
                }
            }
        }

        // 1b) Triplets verticaux et motifs 0 _ 0 / 1 _ 1 vertical
        for (int j = 0; j < taille; j++) {
            for (int i = 0; i < taille - 2; i++) {
                if (grille[i][j] == ZERO && grille[i + 1][j] == ZERO && grille[i + 2][j] == VIDE) {
                    if (isValidAfterSetting(i + 2, j, UN)) return new int[]{i + 2, j, UN};
                }
                if (grille[i][j] == UN && grille[i + 1][j] == UN && grille[i + 2][j] == VIDE) {
                    if (isValidAfterSetting(i + 2, j, ZERO)) return new int[]{i + 2, j, ZERO};
                }
                if (grille[i][j] == VIDE && grille[i + 1][j] == ZERO && grille[i + 2][j] == ZERO) {
                    if (isValidAfterSetting(i, j, UN)) return new int[]{i, j, UN};
                }
                if (grille[i][j] == VIDE && grille[i + 1][j] == UN && grille[i + 2][j] == UN) {
                    if (isValidAfterSetting(i, j, ZERO)) return new int[]{i, j, ZERO};
                }

                // vertical pattern 0 _ 0 -> middle = 1
                if (grille[i][j] == ZERO && grille[i + 1][j] == VIDE && grille[i + 2][j] == ZERO) {
                    if (isValidAfterSetting(i + 1, j, UN)) return new int[]{i + 1, j, UN};
                }
                if (grille[i][j] == UN && grille[i + 1][j] == VIDE && grille[i + 2][j] == UN) {
                    if (isValidAfterSetting(i + 1, j, ZERO)) return new int[]{i + 1, j, ZERO};
                }
            }
        }

        // 2) Vérifier l'équilibre par ligne/colonne (cas plus forts):
        for (int i = 0; i < taille; i++) {
            int nb0Ligne = 0, nb1Ligne = 0;
            int nb0Colonne = 0, nb1Colonne = 0;

            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == ZERO) nb0Ligne++;
                else if (grille[i][j] == UN) nb1Ligne++;

                if (grille[j][i] == ZERO) nb0Colonne++;
                else if (grille[j][i] == UN) nb1Colonne++;
            }

            // Si une ligne a déjà le maximum de 0, les cases vides doivent être 1
            if (nb0Ligne == demiTaille) {
                for (int j = 0; j < taille; j++) {
                    if (grille[i][j] == VIDE && isValidAfterSetting(i, j, UN)) {
                        return new int[]{i, j, UN};
                    }
                }
            }

            // Si une ligne a déjà le maximum de 1, les cases vides doivent être 0
            if (nb1Ligne == demiTaille) {
                for (int j = 0; j < taille; j++) {
                    if (grille[i][j] == VIDE && isValidAfterSetting(i, j, ZERO)) {
                        return new int[]{i, j, ZERO};
                    }
                }
            }

            // Même chose pour les colonnes
            if (nb0Colonne == demiTaille) {
                for (int j = 0; j < taille; j++) {
                    if (grille[j][i] == VIDE && isValidAfterSetting(j, i, UN)) {
                        return new int[]{j, i, UN};
                    }
                }
            }

            if (nb1Colonne == demiTaille) {
                for (int j = 0; j < taille; j++) {
                    if (grille[j][i] == VIDE && isValidAfterSetting(j, i, ZERO)) {
                        return new int[]{j, i, ZERO};
                    }
                }
            }
        }

        // 3) Tentative simple (mini-CSP sur 1 variable) : tester chaque case vide
        //    si une valeur mène immédiatement à une contradiction, l'autre est forcée
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == VIDE) {
                    boolean zeroOk = isValidAfterSetting(i, j, ZERO);
                    boolean unOk = isValidAfterSetting(i, j, UN);

                    if (zeroOk && !unOk) return new int[]{i, j, ZERO};
                    if (!zeroOk && unOk) return new int[]{i, j, UN};
                }
            }
        }

        // 4) Pas de suggestion simple trouvée
        return null;
    }

    /**
     * Teste si l'assignation (i,j)=valeur est compatible avec les règles
     * sans enregistrer le coup dans l'historique.
     */
    private boolean isValidAfterSetting(int i, int j, int valeur) {
        int ancienne = grille[i][j];
        grille[i][j] = valeur;
        boolean ok = estValide();
        grille[i][j] = ancienne;
        return ok;
    }

    /**
     * Retourne une liste détaillée des violations de règles.
     */
    public List<String> getViolations() {
        List<String> violations = new ArrayList<>();

        // Vérifier les adjacences
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille - 2; j++) {
                // Horizontal
                if (grille[i][j] != VIDE && grille[i][j + 1] != VIDE && grille[i][j + 2] != VIDE) {
                    if (grille[i][j].equals(grille[i][j + 1]) && grille[i][j].equals(grille[i][j + 2])) {
                        violations.add("Trois " + grille[i][j] + " consécutifs en ligne " + (i+1) + ", colonnes " + (j+1) + "-" + (j+3));
                    }
                }
                // Vertical
                if (grille[j][i] != VIDE && grille[j + 1][i] != VIDE && grille[j + 2][i] != VIDE) {
                    if (grille[j][i].equals(grille[j + 1][i]) && grille[j][i].equals(grille[j + 2][i])) {
                        violations.add("Trois " + grille[j][i] + " consécutifs en colonne " + (i+1) + ", lignes " + (j+1) + "-" + (j+3));
                    }
                }
            }
        }

        // Vérifier l'équilibre
        int demiTaille = taille / 2;
        for (int i = 0; i < taille; i++) {
            int nb0Ligne = 0, nb1Ligne = 0;
            int nb0Colonne = 0, nb1Colonne = 0;

            for (int j = 0; j < taille; j++) {
                if (grille[i][j] == ZERO) nb0Ligne++;
                else if (grille[i][j] == UN) nb1Ligne++;

                if (grille[j][i] == ZERO) nb0Colonne++;
                else if (grille[j][i] == UN) nb1Colonne++;
            }

            if (nb0Ligne > demiTaille) {
                violations.add("Trop de 0 en ligne " + (i+1) + " (" + nb0Ligne + "/" + demiTaille + ")");
            }
            if (nb1Ligne > demiTaille) {
                violations.add("Trop de 1 en ligne " + (i+1) + " (" + nb1Ligne + "/" + demiTaille + ")");
            }
            if (nb0Colonne > demiTaille) {
                violations.add("Trop de 0 en colonne " + (i+1) + " (" + nb0Colonne + "/" + demiTaille + ")");
            }
            if (nb1Colonne > demiTaille) {
                violations.add("Trop de 1 en colonne " + (i+1) + " (" + nb1Colonne + "/" + demiTaille + ")");
            }
        }

        // Vérifier l'unicité (seulement pour les lignes/colonnes complètes)
        for (int i = 0; i < taille; i++) {
            for (int k = i + 1; k < taille; k++) {
                if (ligneEstComplete(i) && ligneEstComplete(k) && lignesIdentiques(i, k)) {
                    violations.add("Lignes " + (i+1) + " et " + (k+1) + " identiques");
                }
            }
        }

        for (int j = 0; j < taille; j++) {
            for (int k = j + 1; k < taille; k++) {
                if (colonneEstComplete(j) && colonneEstComplete(k) && colonnesIdentiques(j, k)) {
                    violations.add("Colonnes " + (j+1) + " et " + (k+1) + " identiques");
                }
            }
        }

        return violations;
    }

    private boolean ligneEstComplete(int ligne) {
        for (int j = 0; j < taille; j++) {
            if (grille[ligne][j] == VIDE) return false;
        }
        return true;
    }

    private boolean colonneEstComplete(int colonne) {
        for (int i = 0; i < taille; i++) {
            if (grille[i][colonne] == VIDE) return false;
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
