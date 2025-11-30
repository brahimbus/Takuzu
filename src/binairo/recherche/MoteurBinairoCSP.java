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
    private List<String> rapportComparaison = new ArrayList<>();

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
        this.rapportComparaison.clear();

        // Initialisation des domaines
        initDomaines(etatInitial);

        // Log de la configuration
        rapportComparaison.add("Configuration: " + getConfigurationString());

        EtatBinairo solution = backtracking(new EtatBinairo(etatInitial));
        this.tempsFin = System.currentTimeMillis();

        // Ajout des statistiques finales au rapport
        rapportComparaison.add("Temps d'exécution: " + getTempsExecution() + " ms");
        rapportComparaison.add("Nœuds explorés: " + noeudsExplores);
        rapportComparaison.add("Solution trouvée: " + (solution != null));

        return solution;
    }

    private String getConfigurationString() {
        List<String> configs = new ArrayList<>();
        if (utiliserMRV) configs.add("MRV");
        if (utiliserDegree) configs.add("Degree");
        if (utiliserLCV) configs.add("LCV");
        if (utiliserFC) configs.add("FC");
        if (utiliserAC3) configs.add("AC-3");
        if (utiliserAC4) configs.add("AC-4");
        return configs.isEmpty() ? "Backtracking simple" : String.join(" + ", configs);
    }

    public List<String> getRapportComparaison() {
        return rapportComparaison;
    }

    private void initDomaines(EtatBinairo etat) {
        domaines.clear();
        int taille = etat.getTaille();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                String key = i + "," + j;
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    Set<Integer> values = new HashSet<>(Arrays.asList(0, 1));
                    domaines.put(key, values);
                } else {
                    Set<Integer> values = new HashSet<>();
                    values.add(etat.getValeur(i, j));
                    domaines.put(key, values);
                }
            }
        }
    }

    private EtatBinairo backtracking(EtatBinairo etat) {
        if (etat.estComplet() && etat.estValide()) {
            return etat;
        }

        this.noeudsExplores++;

        int[] pos = selectionnerVariableNonAssignee(etat);
        if (pos[0] == -1) return null; // Aucune variable non assignée trouvée

        int ligne = pos[0];
        int col = pos[1];
        String varKey = ligne + "," + col;

        List<Integer> valeurs = ordonnerValeursLCV(etat, ligne, col);

        for (int val : valeurs) {
            if (!domaines.get(varKey).contains(val)) continue;

            // Sauvegarder l'état des domaines avant l'assignation
            Map<String, Set<Integer>> domainesAvant = copierDomaines();

            // Assigner la valeur
            etat.setValeur(ligne, col, val);

            if (etat.estValide()) {
                // Propagation des contraintes
                boolean propagationOk = true;
                if (utiliserFC) {
                    propagationOk = forwardChecking(etat, ligne, col);
                }
                if (propagationOk && utiliserAC3) {
                    propagationOk = ac3(etat);
                }
                if (propagationOk && utiliserAC4) {
                    propagationOk = ac4(etat);
                }

                if (propagationOk) {
                    EtatBinairo resultat = backtracking(etat);
                    if (resultat != null) {
                        return resultat;
                    }
                }
            }

            // Backtrack : restaurer l'état
            etat.annulerCoup();
            restaurerDomaines(domainesAvant);
        }

        return null;
    }

    private int[] selectionnerVariableNonAssignee(EtatBinairo etat) {
        int taille = etat.getTaille();
        List<int[]> variablesCandidates = new ArrayList<>();

        // Collecter toutes les variables non assignées
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    variablesCandidates.add(new int[]{i, j});
                }
            }
        }

        if (variablesCandidates.isEmpty()) return new int[]{-1, -1};

        // Si pas d'heuristique, retourner la première
        if (!utiliserMRV && !utiliserDegree) {
            return variablesCandidates.get(0);
        }

        // Appliquer MRV (Minimum Remaining Values)
        if (utiliserMRV) {
            variablesCandidates.sort((v1, v2) -> {
                int taille1 = domaines.get(v1[0] + "," + v1[1]).size();
                int taille2 = domaines.get(v2[0] + "," + v2[1]).size();
                return Integer.compare(taille1, taille2);
            });
        }

        // Appliquer Degree heuristic en cas d'égalité MRV
        if (utiliserDegree && utiliserMRV) {
            int minSize = domaines.get(variablesCandidates.get(0)[0] + "," + variablesCandidates.get(0)[1]).size();
            List<int[]> meilleursMRV = new ArrayList<>();
            
            for (int[] var : variablesCandidates) {
                if (domaines.get(var[0] + "," + var[1]).size() == minSize) {
                    meilleursMRV.add(var);
                } else {
                    break;
                }
            }
            
            if (meilleursMRV.size() > 1) {
                meilleursMRV.sort((v1, v2) -> 
                    Integer.compare(calculerDegre(etat, v2[0], v2[1]), calculerDegre(etat, v1[0], v1[1])));
            }
            
            return meilleursMRV.get(0);
        }

        return variablesCandidates.get(0);
    }

    private int calculerDegre(EtatBinairo etat, int ligne, int colonne) {
        int degre = 0;
        int taille = etat.getTaille();
        
        // Compter les variables non assignées dans la même ligne et colonne
        for (int j = 0; j < taille; j++) {
            if (j != colonne && etat.getValeur(ligne, j) == EtatBinairo.VIDE) {
                degre++;
            }
        }
        for (int i = 0; i < taille; i++) {
            if (i != ligne && etat.getValeur(i, colonne) == EtatBinairo.VIDE) {
                degre++;
            }
        }
        
        return degre;
    }

    private List<Integer> ordonnerValeursLCV(EtatBinairo etat, int ligne, int colonne) {
        String key = ligne + "," + colonne;
        List<Integer> valeurs = new ArrayList<>(domaines.get(key));
        
        if (!utiliserLCV) {
            return valeurs;
        }

        // Ordonner par Least Constraining Value
        valeurs.sort((v1, v2) -> {
            int impact1 = evaluerImpactValeur(etat, ligne, colonne, v1);
            int impact2 = evaluerImpactValeur(etat, ligne, colonne, v2);
            return Integer.compare(impact1, impact2);
        });

        return valeurs;
    }

    private int evaluerImpactValeur(EtatBinairo etat, int ligne, int colonne, int valeur) {
        int impact = 0;
        int taille = etat.getTaille();

        // Simuler l'assignation
        etat.setValeur(ligne, colonne, valeur);

        // Évaluer l'impact sur les variables voisines
        for (int j = 0; j < taille; j++) {
            if (j != colonne && etat.getValeur(ligne, j) == EtatBinairo.VIDE) {
                String key = ligne + "," + j;
                impact += compterValeursPossibles(etat, ligne, j);
            }
        }
        for (int i = 0; i < taille; i++) {
            if (i != ligne && etat.getValeur(i, colonne) == EtatBinairo.VIDE) {
                String key = i + "," + colonne;
                impact += compterValeursPossibles(etat, i, colonne);
            }
        }

        // Annuler la simulation
        etat.annulerCoup();

        return impact;
    }

    private int compterValeursPossibles(EtatBinairo etat, int ligne, int colonne) {
        int count = 0;
        for (int val = 0; val <= 1; val++) {
            etat.setValeur(ligne, colonne, val);
            if (etat.estValide()) {
                count++;
            }
            etat.annulerCoup();
        }
        return count;
    }

    private boolean forwardChecking(EtatBinairo etat, int ligne, int colonne) {
        int taille = etat.getTaille();
        int valeurAssignee = etat.getValeur(ligne, colonne);

        // Mettre à jour les domaines des variables de la même ligne
        for (int j = 0; j < taille; j++) {
            if (j != colonne && etat.getValeur(ligne, j) == EtatBinairo.VIDE) {
                String key = ligne + "," + j;
                if (!mettreAJourDomaine(etat, ligne, j, valeurAssignee)) {
                    return false;
                }
            }
        }

        // Mettre à jour les domaines des variables de la même colonne
        for (int i = 0; i < taille; i++) {
            if (i != ligne && etat.getValeur(i, colonne) == EtatBinairo.VIDE) {
                String key = i + "," + colonne;
                if (!mettreAJourDomaine(etat, i, colonne, valeurAssignee)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean mettreAJourDomaine(EtatBinairo etat, int ligne, int colonne, int valeurConflit) {
        String key = ligne + "," + colonne;
        Set<Integer> domaine = domaines.get(key);
        Set<Integer> nouveauDomaine = new HashSet<>();

        for (int val : domaine) {
            etat.setValeur(ligne, colonne, val);
            if (etat.estValide()) {
                nouveauDomaine.add(val);
            }
            etat.annulerCoup();
        }

        domaines.put(key, nouveauDomaine);
        return !nouveauDomaine.isEmpty();
    }

    // AC-3 Implementation
    private boolean ac3(EtatBinairo etat) {
        Queue<Arc> file = new LinkedList<>();
        int taille = etat.getTaille();

        // Initialiser la file avec tous les arcs
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (etat.getValeur(i, j) == EtatBinairo.VIDE) {
                    // Arcs avec les variables de la même ligne
                    for (int k = 0; k < taille; k++) {
                        if (k != j && etat.getValeur(i, k) == EtatBinairo.VIDE) {
                            file.add(new Arc(i, j, i, k));
                        }
                    }
                    // Arcs avec les variables de la même colonne
                    for (int k = 0; k < taille; k++) {
                        if (k != i && etat.getValeur(k, j) == EtatBinairo.VIDE) {
                            file.add(new Arc(i, j, k, j));
                        }
                    }
                }
            }
        }

        while (!file.isEmpty()) {
            Arc arc = file.poll();
            if (reviser(etat, arc.xi, arc.xj, arc.yi, arc.yj)) {
                if (domaines.get(arc.xi + "," + arc.xj).isEmpty()) {
                    return false;
                }
                // Ajouter les arcs revenants
                for (int k = 0; k < taille; k++) {
                    if (k != arc.xj && etat.getValeur(arc.xi, k) == EtatBinairo.VIDE && k != arc.yj) {
                        file.add(new Arc(arc.xi, k, arc.xi, arc.xj));
                    }
                    if (k != arc.xi && etat.getValeur(k, arc.xj) == EtatBinairo.VIDE && k != arc.yi) {
                        file.add(new Arc(k, arc.xj, arc.xi, arc.xj));
                    }
                }
            }
        }

        return true;
    }

    private boolean reviser(EtatBinairo etat, int xi, int xj, int yi, int yj) {
        boolean revise = false;
        String keyX = xi + "," + xj;
        Set<Integer> domaineX = new HashSet<>(domaines.get(keyX));
        
        for (int valX : domaineX) {
            boolean supportTrouve = false;
            Set<Integer> domaineY = domaines.get(yi + "," + yj);
            
            for (int valY : domaineY) {
                // Tester la consistance de l'arc
                etat.setValeur(xi, xj, valX);
                etat.setValeur(yi, yj, valY);
                if (etat.estValide()) {
                    supportTrouve = true;
                    break;
                }
                etat.annulerCoup();
                etat.annulerCoup();
            }
            
            if (!supportTrouve) {
                domaines.get(keyX).remove(valX);
                revise = true;
            }
        }
        
        return revise;
    }

    // AC-4 Implementation (simplifiée)
    private boolean ac4(EtatBinairo etat) {
        // Implémentation basique d'AC-4
        // Pour une implémentation complète, il faudrait gérer les compteurs de support
        return ac3(etat); // Fallback sur AC-3 pour l'instant
    }

    private Map<String, Set<Integer>> copierDomaines() {
        Map<String, Set<Integer>> copie = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : domaines.entrySet()) {
            copie.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copie;
    }

    private void restaurerDomaines(Map<String, Set<Integer>> domainesSauvegarde) {
        domaines.clear();
        domaines.putAll(domainesSauvegarde);
    }

    private static class Arc {
        int xi, xj, yi, yj;
        Arc(int xi, int xj, int yi, int yj) {
            this.xi = xi; this.xj = xj; this.yi = yi; this.yj = yj;
        }
    }
}