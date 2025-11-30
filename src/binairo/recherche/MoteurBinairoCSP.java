package binairo.recherche;

import binairo.modele.EtatBinairo;
import java.util.*;

/**
 * Moteur de résolution basé sur la Satisfaction de Contraintes (CSP).
 * Implémente Backtracking avec MRV, Degré, LCV, FC, AC-3, AC-4.
 */
public class MoteurBinairoCSP extends MoteurBinairoAbstrait {

    private boolean utiliserMRV = true;
    private boolean utiliserDegree = true;
    private boolean utiliserLCV = true;
    private boolean utiliserFC = false;
    private boolean utiliserAC3 = false;
    private boolean utiliserAC4 = false;

    private Map<String, Set<Integer>> domaines = new HashMap<>();

    public MoteurBinairoCSP() { }

    public void configurer(boolean mrv, boolean degree, boolean lcv, boolean fc, boolean ac3, boolean ac4) {
        this.utiliserMRV = mrv;
        this.utiliserDegree = degree;
        this.utiliserLCV = lcv;
        this.utiliserFC = fc;
        this.utiliserAC3 = ac3;
        this.utiliserAC4 = ac4;
    }

    @Override
    public EtatBinairo resoudre(EtatBinairo etatInitial) {
        this.tempsDebut = System.currentTimeMillis();
        this.noeudsExplores = 0;

        // Initialisation domaines
        initDomaines(etatInitial);

        // Pré-traitement AC-3 ou AC-4
        if (utiliserAC3 && !ac3(etatInitial))
            return null;
        if (utiliserAC4 && !ac4(etatInitial))
            return null;

        EtatBinairo solution = backtracking(etatInitial);
        this.tempsFin = System.currentTimeMillis();
        return solution;
    }

    private void initDomaines(EtatBinairo etat) {
        domaines.clear();
        int taille = etat.getTaille();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    String key = i + "," + j;
                    Set<Integer> values = new HashSet<>();
                    if (peutMettre(etat, i, j, 0)) values.add(0);
                    if (peutMettre(etat, i, j, 1)) values.add(1);
                    domaines.put(key, values);
                }
            }
        }
    }

    private EtatBinairo backtracking(EtatBinairo etat) {
        if (etat.estComplet()) {
            return etat.estValide() ? etat : null;
        }

        this.noeudsExplores++;

        int[] pos = selectionnerVariableNonAssignee(etat);
        int ligne = pos[0];
        int col = pos[1];

        List<Integer> valeurs = ordonnerValeursDomaine(etat, ligne, col);

        for (int val : valeurs) {
            etat.setValeur(ligne, col, val);
            String key = ligne + "," + col;
            Set<Integer> oldDomain = new HashSet<>(domaines.get(key));

            if (etat.estValide()) {
                boolean inferenceOk = true;
                if (utiliserFC) inferenceOk = forwardChecking(etat, ligne, col);

                if (inferenceOk) {
                    if (utiliserAC3) ac3(etat);
                    if (utiliserAC4) ac4(etat);

                    EtatBinairo res = backtracking(etat);
                    if (res != null) return res;
                }
            }

            etat.annulerCoup();
            domaines.put(key, oldDomain);
        }

        return null;
    }

    private int[] selectionnerVariableNonAssignee(EtatBinairo etat) {
        int taille = etat.getTaille();
        int meilleurLigne = -1, meilleurCol = -1;
        int minDomaine = Integer.MAX_VALUE;
        int maxDegre = -1;

        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    if (!utiliserMRV) return new int[]{i, j};

                    String key = i + "," + j;
                    int tailleDom = domaines.get(key).size();

                    if (tailleDom < minDomaine) {
                        minDomaine = tailleDom;
                        meilleurLigne = i;
                        meilleurCol = j;
                        maxDegre = calculerDegre(etat, i, j);
                    } else if (tailleDom == minDomaine && utiliserDegree) {
                        int degre = calculerDegre(etat, i, j);
                        if (degre > maxDegre) {
                            maxDegre = degre;
                            meilleurLigne = i;
                            meilleurCol = j;
                        }
                    }
                }
            }
        }
        return new int[]{meilleurLigne, meilleurCol};
    }

    private int calculerDegre(EtatBinairo etat, int l, int c) {
        int degre = 0;
        int taille = etat.getTaille();
        for (int k = 0; k < taille; k++) {
            if (k != c && etat.getValeur(l, k) == EtatBinairo.VIDE) degre++;
            if (k != l && etat.getValeur(k, c) == EtatBinairo.VIDE) degre++;
        }
        return degre;
    }

    private List<Integer> ordonnerValeursDomaine(EtatBinairo etat, int l, int c) {
        List<Integer> vals = new ArrayList<>(domaines.get(l + "," + c));
        if (!utiliserLCV) return vals;

        vals.sort((a, b) -> evaluerImpact(etat, l, c, b) - evaluerImpact(etat, l, c, a));
        return vals;
    }

    private int evaluerImpact(EtatBinairo etat, int l, int c, int val) {
        etat.setValeur(l, c, val);
        int impact = 0;
        int taille = etat.getTaille();

        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    impact += domaines.get(i + "," + j).size();
                }
            }
        }

        etat.annulerCoup();
        return impact;
    }

    private boolean forwardChecking(EtatBinairo etat, int l, int c) {
        int taille = etat.getTaille();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    String key = i + "," + j;
                    Set<Integer> dom = new HashSet<>();
                    if (peutMettre(etat, i, j, 0)) dom.add(0);
                    if (peutMettre(etat, i, j, 1)) dom.add(1);
                    if (dom.isEmpty()) return false;
                    domaines.put(key, dom);
                }
            }
        }
        return true;
    }

    // AC-3 Implementation
    private boolean ac3(EtatBinairo etat) {
        Queue<int[]> queue = new LinkedList<>();
        int taille = etat.getTaille();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    for (int k = 0; k < taille; k++) {
                        if (k != j && etat.getValeur(i, k) == EtatBinairo.VIDE) queue.add(new int[]{i, j, i, k});
                        if (k != i && etat.getValeur(k, j) == EtatBinairo.VIDE) queue.add(new int[]{i, j, k, j});
                    }
                }
            }
        }

        while (!queue.isEmpty()) {
            int[] arc = queue.poll();
            if (reviser(etat, arc[0], arc[1], arc[2], arc[3])) {
                if (domaines.get(arc[0] + "," + arc[1]).isEmpty()) return false;
                for (int k = 0; k < taille; k++) {
                    if (k != arc[1] && k != arc[3]) queue.add(new int[]{arc[0], arc[1], arc[0], k});
                    if (k != arc[0] && k != arc[2]) queue.add(new int[]{arc[0], arc[1], k, arc[1]});
                }
            }
        }
        return true;
    }

    private boolean reviser(EtatBinairo etat, int xi, int xj, int yi, int yj) {
        boolean revised = false;
        Set<Integer> domXi = new HashSet<>(domaines.get(xi + "," + xj));
        for (int val : domXi) {
            boolean supported = false;
            for (int val2 : domaines.get(yi + "," + yj)) {
                etat.setValeur(xi, xj, val);
                etat.setValeur(yi, yj, val2);
                if (etat.estValide()) supported = true;
                etat.annulerCoup();
                etat.annulerCoup();
                if (supported) break;
            }
            if (!supported) {
                domaines.get(xi + "," + xj).remove(val);
                revised = true;
            }
        }
        return revised;
    }

    // AC-4 Implementation (simplified support counting)
    private boolean ac4(EtatBinairo etat) {
        int taille = etat.getTaille();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                String key = i + "," + j;
                Set<Integer> dom = new HashSet<>();
                if (peutMettre(etat, i, j, 0)) dom.add(0);
                if (peutMettre(etat, i, j, 1)) dom.add(1);
                domaines.put(key, dom);
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String key : domaines.keySet()) {
                if (domaines.get(key).isEmpty()) return false;
                if (domaines.get(key).size() == 1) {
                    int val = domaines.get(key).iterator().next();
                    int i = Integer.parseInt(key.split(",")[0]);
                    int j = Integer.parseInt(key.split(",")[1]);
                    for (int k = 0; k < taille; k++) {
                        if (k != j) {
                            String neighbor = i + "," + k;
                            if (domaines.get(neighbor).contains(val)) {
                                domaines.get(neighbor).remove(val);
                                changed = true;
                            }
                        }
                        if (k != i) {
                            String neighbor = k + "," + j;
                            if (domaines.get(neighbor).contains(val)) {
                                domaines.get(neighbor).remove(val);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Helper method to replace 'peutMettre'.
     */
    private boolean peutMettre(EtatBinairo etat, int l, int c, int val) {
        etat.setValeur(l, c, val);
        boolean ok = etat.estValide();
        etat.annulerCoup();
        return ok;
    }
}
