package binairo.gui;

import binairo.modele.EtatBinairo;
import binairo.recherche.MoteurBinairoCSP;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ApplicationBinairo extends JFrame {
    private EtatBinairo etatCourant;
    private EtatBinairo etatInitial; // Store initial state
    private JButton[][] boutonsGrille;
    private JPanel panneauGrille;
    private MoteurBinairoCSP moteurCSP;
    private int tailleGrille = 6; // Défaut

    // Options CSP
    private JCheckBox cbMRV, cbDegree, cbLCV, cbFC, cbAC3, cbAC4;

    public ApplicationBinairo() {
        super("Jeu Binairo - CSP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        moteurCSP = new MoteurBinairoCSP();
        etatCourant = new EtatBinairo(tailleGrille);
        etatInitial = new EtatBinairo(etatCourant); // Store initial state

        initialiserComposants();
       
        genererGrilleAleatoire();
    }

    private void initialiserComposants() {
        setLayout(new BorderLayout());

        // Panneau Haut: 4 boutons principaux uniquement
        JPanel panneauControles = new JPanel(new FlowLayout(FlowLayout.LEFT));

        String[] tailles = { "6x6", "8x8", "10x10" };
        JComboBox<String> comboTaille = new JComboBox<>(tailles);
        comboTaille.setSelectedIndex(0);
        comboTaille.addActionListener(e -> {
            String s = (String) comboTaille.getSelectedItem();
            tailleGrille = Integer.parseInt(s.split("x")[0]);
            etatCourant = new EtatBinairo(tailleGrille);
            etatInitial = new EtatBinairo(etatCourant); // Store initial state
            reconstruireGrilleUI();
        });

        JButton btnGenerer = new JButton("Générer");
        JButton btnVerifier = new JButton("Vérifier Validité & Résolubilité");
        JButton btnResoudre = new JButton("Résoudre (CSP)");
        JButton btnReset = new JButton("Reset"); // Add reset button

        panneauControles.add(new JLabel("Taille:"));
        panneauControles.add(comboTaille);
        panneauControles.add(btnGenerer);
        panneauControles.add(btnVerifier);
        panneauControles.add(btnResoudre);
        panneauControles.add(btnReset); // Add reset button to panel

        add(panneauControles, BorderLayout.NORTH);

        // Panneau Gauche: Autres boutons + Options CSP
        JPanel panneauGauche = new JPanel();
        panneauGauche.setLayout(new BoxLayout(panneauGauche, BoxLayout.Y_AXIS));
        panneauGauche.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panneauGauche.setPreferredSize(new Dimension(200, 0));

        // Section Fichier
        JPanel sectionFichier = new JPanel();
        sectionFichier.setLayout(new BoxLayout(sectionFichier, BoxLayout.Y_AXIS));
        sectionFichier.setBorder(BorderFactory.createTitledBorder("Fichier"));
        sectionFichier.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSauvegarder = new JButton("Sauvegarder");
        JButton btnCharger = new JButton("Charger");
        btnSauvegarder.setMaximumSize(new Dimension(180, 30));
        btnCharger.setMaximumSize(new Dimension(180, 30));
        btnSauvegarder.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCharger.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionFichier.add(btnSauvegarder);
        sectionFichier.add(Box.createRigidArea(new Dimension(0, 5)));
        sectionFichier.add(btnCharger);

        // Section Aide
        JPanel sectionAide = new JPanel();
        sectionAide.setLayout(new BoxLayout(sectionAide, BoxLayout.Y_AXIS));
        sectionAide.setBorder(BorderFactory.createTitledBorder("Assistance"));
        sectionAide.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAide = new JButton("Aide / Suggestion");
        JButton btnRetour = new JButton("Retour Arrière");
        btnAide.setMaximumSize(new Dimension(180, 30));
        btnRetour.setMaximumSize(new Dimension(180, 30));
        btnAide.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRetour.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionAide.add(btnAide);
        sectionAide.add(Box.createRigidArea(new Dimension(0, 5)));
        sectionAide.add(btnRetour);

        // Section Heuristiques CSP
        JPanel panneauOptions = new JPanel();
        panneauOptions.setLayout(new BoxLayout(panneauOptions, BoxLayout.Y_AXIS));
        panneauOptions.setBorder(BorderFactory.createTitledBorder("Heuristiques CSP"));
        panneauOptions.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbMRV = new JCheckBox("MVR", true);
        cbDegree = new JCheckBox("Degree", true);
        cbLCV = new JCheckBox("LCV", true);
        cbFC = new JCheckBox("FC", false);
        cbAC3 = new JCheckBox("AC-3", false);
        cbAC4 = new JCheckBox("AC-4", false);

        panneauOptions.add(cbMRV);
        panneauOptions.add(cbDegree);
        panneauOptions.add(cbLCV);
        panneauOptions.add(cbFC);
        panneauOptions.add(cbAC3);
        panneauOptions.add(cbAC4);

        // Ajouter toutes les sections au panneau gauche
        panneauGauche.add(sectionFichier);
        panneauGauche.add(Box.createRigidArea(new Dimension(0, 10)));
        panneauGauche.add(sectionAide);
        panneauGauche.add(Box.createRigidArea(new Dimension(0, 10)));
        panneauGauche.add(panneauOptions);
        panneauGauche.add(Box.createVerticalGlue());

        add(panneauGauche, BorderLayout.WEST);

        // Panneau Centre: Grille
        panneauGrille = new JPanel();
        reconstruireGrilleUI();
        add(panneauGrille, BorderLayout.CENTER);

        // Actions des boutons
        btnGenerer.addActionListener(e -> genererGrilleAleatoire());

        btnSauvegarder.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Sauvegarder la grille");
            fileChooser.setSelectedFile(new java.io.File("grille_binairo.bin"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    etatCourant.sauvegarder(fileChooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this,
                            "Grille sauvegardée avec succès:\n" + fileChooser.getSelectedFile().getName(),
                            "Sauvegarde Réussie", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors de la sauvegarde:\n" + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCharger.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Charger une grille");
            int userSelection = fileChooser.showOpenDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    etatCourant = EtatBinairo.charger(fileChooser.getSelectedFile().getAbsolutePath());
                    etatInitial = new EtatBinairo(etatCourant); // Store initial state when loading
                    tailleGrille = etatCourant.getTaille();
                    reconstruireGrilleUI();
                    JOptionPane.showMessageDialog(this,
                            "Grille chargée avec succès:\n" + fileChooser.getSelectedFile().getName(),
                            "Chargement Réussi", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors du chargement:\n" + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnVerifier.addActionListener(e -> {
            StringBuilder message = new StringBuilder();
            message.append("=== VÉRIFICATION ===\n\n");

            java.util.List<String> violations = etatCourant.getViolations();

            if (violations.isEmpty()) {
                if (etatCourant.estComplet()) {
                    message.append("✓ La grille est VALIDE et COMPLÈTE!\n\n");
                } else {
                    message.append("✓ La grille est valide (mais incomplète).\n\n");
                }
            } else {
                message.append("✗ RÈGLES VIOLÉES:\n");
                for (String violation : violations) {
                    message.append("  • ").append(violation).append("\n");
                }
                message.append("\n");
            }

            JOptionPane.showMessageDialog(this, message.toString(),
                    "Vérification", JOptionPane.INFORMATION_MESSAGE);

            new Thread(() -> {
                EtatBinairo copie = new EtatBinairo(etatCourant);
                moteurCSP.configurer(true, true, true, false, false, false);
                EtatBinairo solution = moteurCSP.resoudre(copie);

                SwingUtilities.invokeLater(() -> {
                    String result = solution != null
                            ? "✓ La grille est RÉSOLUBLE\n(solution trouvée en " + moteurCSP.getTempsExecution() + "ms)"
                            : "✗ La grille est NON RÉSOLUBLE\n(aucune solution trouvée)";
                    JOptionPane.showMessageDialog(this, result,
                            "Résolubilité",
                            solution != null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                });
            }).start();
        });

        btnAide.addActionListener(e -> {
            int[] suggestion = etatCourant.getSuggestion();
            if (suggestion != null) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Aucune suggestion évidente trouvée.\nEssayez l'analyse manuelle.",
                        "Pas de Suggestion", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRetour.addActionListener(e -> {
            if (etatCourant.annulerCoup()) {
                mettreAJourGrilleUI();
            } else {
                JOptionPane.showMessageDialog(this, "Historique vide, impossible d'annuler.",
                        "Annulation Impossible", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnResoudre.addActionListener(e -> {
            java.util.List<String> algosActifs = new java.util.ArrayList<>();
            if (cbMRV.isSelected())
                algosActifs.add("MVR");
            if (cbDegree.isSelected())
                algosActifs.add("Degree");
            if (cbLCV.isSelected())
                algosActifs.add("LCV");
            if (cbFC.isSelected())
                algosActifs.add("FC");
            if (cbAC3.isSelected())
                algosActifs.add("AC-3");
            if (cbAC4.isSelected())
                algosActifs.add("AC-4");

            String algosUtilises = algosActifs.isEmpty() ? "Aucun (backtracking simple)"
                    : String.join(", ", algosActifs);

            moteurCSP.configurer(cbMRV.isSelected(), cbDegree.isSelected(), cbLCV.isSelected(),
                    cbFC.isSelected(), cbAC3.isSelected(), cbAC4.isSelected());

            JOptionPane.showMessageDialog(this,
                    "RÉSOLUTION CSP EN COURS\n\n" +
                            "Algorithmes: " + algosUtilises + "\n" +
                            "Taille: " + tailleGrille + "x" + tailleGrille + "\n\n" +
                            "Veuillez patienter...",
                    "Résolution CSP", JOptionPane.INFORMATION_MESSAGE);

            new Thread(() -> {
                EtatBinairo solution = moteurCSP.resoudre(new EtatBinairo(etatCourant));
                SwingUtilities.invokeLater(() -> {
                    StringBuilder result = new StringBuilder();
                    if (solution != null) {
                        etatCourant = solution;
                        mettreAJourGrilleUI();
                        result.append("✓ SOLUTION TROUVÉE!\n\n");
                        result.append("  • Temps: ").append(moteurCSP.getTempsExecution()).append(" ms\n");
                        result.append("  • Nœuds: ").append(moteurCSP.getNoeudsExplores()).append("\n");
                        result.append("  • Algorithmes: ").append(algosUtilises).append("\n");

                        JOptionPane.showMessageDialog(this, result.toString(),
                                "Solution Trouvée", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        result.append("✗ AUCUNE SOLUTION!\n\n");
                        result.append("  • Temps: ").append(moteurCSP.getTempsExecution()).append(" ms\n");
                        result.append("  • Nœuds: ").append(moteurCSP.getNoeudsExplores()).append("\n");

                        JOptionPane.showMessageDialog(this, result.toString(),
                                "Aucune Solution", JOptionPane.WARNING_MESSAGE);
                    }
                });
            }).start();
        });

        // Add action listener for reset button
        btnReset.addActionListener(e -> resetGrille());
    }

    // Add reset method
    private void resetGrille() {
        int response = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir réinitialiser la grille à son état initial?",
                "Confirmation de Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (response == JOptionPane.YES_OPTION) {
            etatCourant = new EtatBinairo(etatInitial);
            mettreAJourGrilleUI();
            JOptionPane.showMessageDialog(this,
                    "Grille réinitialisée avec succès!",
                    "Reset Réussi",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void reconstruireGrilleUI() {
        panneauGrille.removeAll();
        panneauGrille.setLayout(new GridLayout(tailleGrille, tailleGrille, 2, 2));
        panneauGrille.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boutonsGrille = new JButton[tailleGrille][tailleGrille];

        for (int i = 0; i < tailleGrille; i++) {
            for (int j = 0; j < tailleGrille; j++) {
                JButton btn = new JButton();
                btn.setFont(new Font("Arial", Font.BOLD, 24));
                final int r = i;
                final int c = j;

                btn.addActionListener(e -> {
                    Integer val = etatCourant.getValeur(r, c);
                    if (val == null || val == EtatBinairo.VIDE) {
                        etatCourant.setValeur(r, c, 0);
                    } else if (val == 0) {
                        etatCourant.setValeur(r, c, 1);
                    } else {
                        etatCourant.setValeur(r, c, EtatBinairo.VIDE);
                    }
                    mettreAJourBouton(r, c);
                });

                boutonsGrille[i][j] = btn;
                panneauGrille.add(btn);
            }
        }
        mettreAJourGrilleUI();
        panneauGrille.revalidate();
        panneauGrille.repaint();
    }

    private void mettreAJourGrilleUI() {
        for (int i = 0; i < tailleGrille; i++) {
            for (int j = 0; j < tailleGrille; j++) {
                mettreAJourBouton(i, j);
            }
        }
    }

    private void mettreAJourBouton(int i, int j) {
        Integer val = etatCourant.getValeur(i, j);
        if (val == null || val == EtatBinairo.VIDE) {
            boutonsGrille[i][j].setText("");
            boutonsGrille[i][j].setBackground(Color.WHITE);
        } else {
            boutonsGrille[i][j].setText(val.toString());
            boutonsGrille[i][j].setBackground(val == 0 ? new Color(200, 220, 255) : new Color(255, 200, 200));
        }
    }

    private void genererGrilleAleatoire() {
        etatCourant = new EtatBinairo(tailleGrille);
        Random rand = new Random();
        int nbCases = tailleGrille * tailleGrille / 4;

        for (int k = 0; k < nbCases; k++) {
            int r = rand.nextInt(tailleGrille);
            int c = rand.nextInt(tailleGrille);
            int v = rand.nextInt(2);
            etatCourant.setValeur(r, c, v);
            if (!etatCourant.estValide()) {
                etatCourant.setValeur(r, c, EtatBinairo.VIDE);
            }
        }
        etatInitial = new EtatBinairo(etatCourant); // Update initial state when generating new grid
        mettreAJourGrilleUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ApplicationBinairo().setVisible(true);
        });
    }
}