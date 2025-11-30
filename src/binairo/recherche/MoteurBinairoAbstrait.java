package binairo.recherche;

import binairo.modele.EtatBinairo;

/**
 * Classe abstraite pour les moteurs de résolution Binairo.
 * Définit la structure commune pour les algorithmes de recherche.
 */
public abstract class MoteurBinairoAbstrait {
    protected long noeudsExplores;
    protected long tempsDebut;
    protected long tempsFin;

    public MoteurBinairoAbstrait() {
        this.noeudsExplores = 0;
    }

    /**
     * Méthode principale pour résoudre une grille.
     * @param etatInitial L'état de départ de la grille.
     * @return L'état solution ou null si aucune solution n'est trouvée.
     */
    public abstract EtatBinairo resoudre(EtatBinairo etatInitial);

    public long getNoeudsExplores() {
        return noeudsExplores;
    }

    public long getTempsExecution() {
        return tempsFin - tempsDebut;
    }
}
