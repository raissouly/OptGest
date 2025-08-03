package sample;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

// JavaFX
// Java IO
// iTextPDF



// JavaFX classes
// SQL classes
// Java IO

public class FournisseurController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Tab listeFournisseursTab;
    @FXML private TextField rechercheField;
    @FXML private Button btnRechercher;
    @FXML private Button btnNouveauFournisseur;
    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Integer> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colTelephone;
    @FXML private TableColumn<Fournisseur, String> colEmail;
    @FXML private TableColumn<Fournisseur, String> colAdresse;
    @FXML private TableColumn<Fournisseur, String> colStatut;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVoirDetails;

    // Onglet Coordonnées
    @FXML private Tab tabCoordonnees;
    @FXML private TextField nomField;
    @FXML private TextField codeFournisseurField;
    @FXML private TextField telephoneField;
    @FXML private TextField faxField;
    @FXML private TextField emailField;
    @FXML private TextField siteWebField;
    @FXML private TextArea adresseArea;
    @FXML private TextField villeField;
    @FXML private TextField codePostalField;
    @FXML private ComboBox<String> paysCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextArea notesArea;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnReinitialiser;

    // Onglet Produits Associés
    @FXML private Tab produitsAssociesTab;
    @FXML private ComboBox<Fournisseur> fournisseurSelectCombo;
    @FXML private Button btnNouveauProduit;
    @FXML private TableView<ProduitFournisseur> tableProduitsAssocies;
    @FXML private TableColumn<ProduitFournisseur, String> colProduitId;
    @FXML private TableColumn<ProduitFournisseur, String> colProduitNom;
    @FXML private TableColumn<ProduitFournisseur, String> colProduitCategorie;
    @FXML private TableColumn<ProduitFournisseur, Double> colPrixFournisseur;
    @FXML private TableColumn<ProduitFournisseur, Integer> colDelaiLivraison;
    @FXML private TableColumn<ProduitFournisseur, Integer> colQteMinCommande;
    @FXML private TableColumn<ProduitFournisseur, String> colProduitStatut;
    @FXML private TableColumn<ProduitFournisseur, String> colProduitActions;
    @FXML private Button btnModifierProduit;
    @FXML private Button btnSupprimerAssociation;

    // Onglet Historique Livraisons
    @FXML private Tab historiqueLivraisonsTab;
    @FXML private ComboBox<Fournisseur> fournisseurHistoriqueCombo;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button btnFiltrerHistorique;
    @FXML private Button btnExporterHistorique;
    @FXML private TableView<HistoriqueLivraison> tableHistoriqueLivraisons;
    @FXML private TableColumn<HistoriqueLivraison, String> colNumCommande;
    @FXML private TableColumn<HistoriqueLivraison, LocalDate> colDateCommande;
    @FXML private TableColumn<HistoriqueLivraison, LocalDate> colDateLivraison;
    @FXML private TableColumn<HistoriqueLivraison, String> colFournisseurHist;
    @FXML private TableColumn<HistoriqueLivraison, String> colProduitsLivres;
    @FXML private TableColumn<HistoriqueLivraison, Integer> colQuantiteLivree;
    @FXML private TableColumn<HistoriqueLivraison, Double> colMontantLivraison;
    @FXML private TableColumn<HistoriqueLivraison, String> colStatutLivraison;
    @FXML private TableColumn<HistoriqueLivraison, String> colActionsHist;

    // Labels pour statistiques
    @FXML private Label lblTotalLivraisons;
    @FXML private Label lblMontantTotal;
    @FXML private Label lblDelaiMoyen;
    @FXML private Label lblTauxPonctualite;
    @FXML private Label lblStatutBar;
    @FXML private Label lblNombreFournisseurs;

    private ObservableList<Fournisseur> fournisseursList = FXCollections.observableArrayList();
    private ObservableList<ProduitFournisseur> produitsFournisseurList = FXCollections.observableArrayList();
    private ObservableList<HistoriqueLivraison> historiqueLivraisonsList = FXCollections.observableArrayList();
    private Fournisseur selectedFournisseur = null;
    private boolean isEditing = false;
    private int currentFournisseurId = -1; // -1 si aucun fournisseur n'est sélectionné
    private LocalDate currentStartDate = null;
    private LocalDate currentEndDate = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        loadFournisseurs();
        loadFournisseursInComboBoxes();
        setupTableSelectionListeners();
        updateStatistics();
        currentFournisseurId = -1;
        currentStartDate = null;
        currentEndDate = null;

        // Configurer les listeners
        setupTableSelectionListeners();

        // Charger les données initiales
        loadFournisseurs();
        loadFournisseursInComboBoxes();

        // Initialiser les statistiques à zéro
        updateDeliveryStatistics();

        System.out.println("Initialisation terminée");
    }

    private void setupTableColumns() {
        // Table Fournisseurs
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Table Produits Fournisseurs
        colProduitId.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        colProduitNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colProduitCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrixFournisseur.setCellValueFactory(new PropertyValueFactory<>("prixFournisseur"));
        colDelaiLivraison.setCellValueFactory(new PropertyValueFactory<>("delaiLivraison"));
        colQteMinCommande.setCellValueFactory(new PropertyValueFactory<>("qteMinCommande"));
        colProduitStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Table Historique Livraisons
        colNumCommande.setCellValueFactory(new PropertyValueFactory<>("numCommande"));
        colDateCommande.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        colDateLivraison.setCellValueFactory(new PropertyValueFactory<>("dateLivraison"));
        colFournisseurHist.setCellValueFactory(new PropertyValueFactory<>("fournisseurNom"));
        colProduitsLivres.setCellValueFactory(new PropertyValueFactory<>("produitsLivres"));
        colQuantiteLivree.setCellValueFactory(new PropertyValueFactory<>("quantiteLivree"));
        colMontantLivraison.setCellValueFactory(new PropertyValueFactory<>("montantLivraison"));
        colStatutLivraison.setCellValueFactory(new PropertyValueFactory<>("statutLivraison"));
    }

    private void setupComboBoxes() {
        // ComboBox Statut
        statutCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif", "En attente", "Suspendu"));
        statutCombo.setValue("Actif");

        // ComboBox Pays
        paysCombo.setItems(FXCollections.observableArrayList("Maroc", "France", "Espagne", "Italie", "Allemagne"));
        paysCombo.setValue("Maroc");

        // Setup StringConverter pour les ComboBox de fournisseurs
        StringConverter<Fournisseur> fournisseurConverter = new StringConverter<Fournisseur>() {
            @Override
            public String toString(Fournisseur fournisseur) {
                return fournisseur != null ? fournisseur.getNom() : "";
            }

            @Override
            public Fournisseur fromString(String string) {
                return null;
            }
        };

        fournisseurSelectCombo.setConverter(fournisseurConverter);
        fournisseurHistoriqueCombo.setConverter(fournisseurConverter);
    }
    private String buildLivraisonsQuery(LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder(
                "SELECT " +
                        "CONCAT('LIV-', l.id) as num_commande, " +
                        "l.date_livraison as date_commande, " +
                        "l.date_livraison, " +
                        "f.nom as fournisseur_nom, " +
                        "p.designation as produits_livres, " +
                        "l.quantite_livree, " +
                        "(l.quantite_livree * l.prix_unitaire) as montant_livraison, " +
                        "COALESCE(l.statut, 'Livré') as statut_livraison " +
                        "FROM livraisons l " +
                        "JOIN fournisseurs f ON l.fournisseur_id = f.id " +
                        "LEFT JOIN produits p ON l.produit_id = p.id " +
                        "WHERE l.fournisseur_id = ?"
        );

        // Add date filtering if dates are provided
        if (startDate != null) {
            query.append(" AND l.date_livraison >= ?");
        }
        if (endDate != null) {
            query.append(" AND l.date_livraison <= ?");
        }

        query.append(" ORDER BY l.date_livraison DESC");

        return query.toString();
    }

    // Helper method to build query for historique_livraisons table
    private String buildHistoriqueLivraisonsQuery(LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder(
                "SELECT " +
                        "num_commande, " +
                        "date_commande, " +
                        "date_livraison, " +
                        "fournisseur_nom, " +
                        "produits_livres, " +
                        "quantite_livree, " +
                        "montant_livraison, " +
                        "statut_livraison " +
                        "FROM historique_livraisons " +
                        "WHERE fournisseur_id = ?"
        );

        // Add date filtering if dates are provided
        if (startDate != null) {
            query.append(" AND date_livraison >= ?");
        }
        if (endDate != null) {
            query.append(" AND date_livraison <= ?");
        }

        query.append(" ORDER BY date_livraison DESC");

        return query.toString();
    }

    private void setupTableSelectionListeners() {
        // Selection listener for main fournisseurs table
        tableFournisseurs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedFournisseur = newSelection;
            btnModifier.setDisable(newSelection == null);
            btnSupprimer.setDisable(newSelection == null);
            btnVoirDetails.setDisable(newSelection == null);
        });

        // Selection listener for produits associés combo
        fournisseurSelectCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadProduitsAssocies(newSelection.getId());
            } else {
                produitsFournisseurList.clear();
                tableProduitsAssocies.setItems(produitsFournisseurList);
            }
        });

        // CORRECTION: Selection listener pour historique combo
        fournisseurHistoriqueCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Mettre à jour les variables globales
                currentFournisseurId = newSelection.getId();

                // Réinitialiser les filtres de date
                dateDebutPicker.setValue(null);
                dateFinPicker.setValue(null);
                currentStartDate = null;
                currentEndDate = null;

                // Charger l'historique complet
                loadHistoriqueLivraisons(newSelection.getId(), null, null);

                // Mise à jour du statut
                if (lblStatutBar != null) {
                    lblStatutBar.setText("Historique complet pour: " + newSelection.getNom());
                }

                System.out.println("Fournisseur sélectionné: " + newSelection.getNom() + " (ID: " + newSelection.getId() + ")");
            } else {
                // Réinitialiser toutes les variables
                currentFournisseurId = -1;
                currentStartDate = null;
                currentEndDate = null;

                // Vider les listes
                historiqueLivraisonsList.clear();
                tableHistoriqueLivraisons.setItems(historiqueLivraisonsList);

                // Réinitialiser les statistiques
                updateDeliveryStatistics();

                if (lblStatutBar != null) {
                    lblStatutBar.setText("Aucun fournisseur sélectionné");
                }
            }
        });
    }


    private void loadFournisseurs() {
        fournisseursList.clear();
        try {
            String query = "SELECT * FROM fournisseurs ORDER BY nom";
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseur.setCodeFournisseur(rs.getString("code_fournisseur"));
                fournisseur.setTelephone(rs.getString("telephone"));
                fournisseur.setFax(rs.getString("fax"));
                fournisseur.setEmail(rs.getString("email"));
                fournisseur.setSiteWeb(rs.getString("site_web"));
                fournisseur.setAdresse(rs.getString("adresse"));
                fournisseur.setVille(rs.getString("ville"));
                fournisseur.setCodePostal(rs.getString("code_postal"));
                fournisseur.setPays(rs.getString("pays"));
                fournisseur.setStatut(rs.getString("statut"));
                fournisseur.setNotes(rs.getString("notes"));
                fournisseursList.add(fournisseur);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des fournisseurs: " + e.getMessage());
        }
        tableFournisseurs.setItems(fournisseursList);
        updateStatistics();
    }

    private void loadFournisseursInComboBoxes() {
        ObservableList<Fournisseur> fournisseurs = FXCollections.observableArrayList();
        try {
            String query = "SELECT id, nom FROM fournisseurs WHERE statut = 'Actif' ORDER BY nom";
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseurs.add(fournisseur);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des fournisseurs: " + e.getMessage());
        }
        fournisseurSelectCombo.setItems(fournisseurs);
        fournisseurHistoriqueCombo.setItems(fournisseurs);
    }

    private void loadProduitsAssocies(int fournisseurId) {
        produitsFournisseurList.clear();
        try {
            String query = "SELECT * FROM produits_fournisseurs WHERE fournisseur_id = " + fournisseurId;
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                ProduitFournisseur pf = new ProduitFournisseur();
                pf.setId(rs.getInt("id"));
                pf.setFournisseurId(rs.getInt("fournisseur_id"));
                pf.setProduitId(rs.getString("produit_id"));
                pf.setNomProduit(rs.getString("nom_produit"));
                pf.setCategorie(rs.getString("categorie"));
                pf.setPrixFournisseur(rs.getDouble("prix_fournisseur"));
                pf.setDelaiLivraison(rs.getInt("delai_livraison"));
                pf.setQteMinCommande(rs.getInt("qte_min_commande"));
                pf.setStatut(rs.getString("statut"));
                produitsFournisseurList.add(pf);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des produits: " + e.getMessage());
        }
        tableProduitsAssocies.setItems(produitsFournisseurList);
    }


    // Alternative: If you want to keep the original method and add an overloaded version
    private void loadHistoriqueLivraisons(int fournisseurId) {
        // Call the main method with null dates to load all records
        loadHistoriqueLivraisons(fournisseurId, null, null);
    }

    // Alternative approach using your existing Connectiondb.Display method (simpler but less flexible)
    private void loadHistoriqueLivraisonsSimple(int fournisseurId, LocalDate startDate, LocalDate endDate) {
        try {
            // Clear the existing list
            historiqueLivraisonsList.clear();

            // Build the SQL query as a string
            StringBuilder query = new StringBuilder(
                    "SELECT l.id, l.date_livraison, l.quantite_livree, l.prix_unitaire, " +
                            "p.nom_produit, p.description " +
                            "FROM livraisons l " +
                            "JOIN produits p ON l.produit_id = p.id " +
                            "WHERE l.fournisseur_id = " + fournisseurId
            );

            // Add date filtering if dates are provided
            if (startDate != null) {
                query.append(" AND l.date_livraison >= '").append(startDate).append("'");
            }
            if (endDate != null) {
                query.append(" AND l.date_livraison <= '").append(endDate).append("'");
            }

            query.append(" ORDER BY l.date_livraison DESC");

            // Execute using your existing Display method
            ResultSet rs = Connectiondb.Dispaly(query.toString());

            // Process the results
            while (rs.next()) {
                // Create your delivery history object here
                // This depends on your data model structure
                // Example:
            /*
            LivraisonHistorique livraison = new LivraisonHistorique(
                rs.getInt("id"),
                rs.getDate("date_livraison").toLocalDate(),
                rs.getInt("quantite_livree"),
                rs.getDouble("prix_unitaire"),
                rs.getString("nom_produit"),
                rs.getString("description")
            );
            historiqueLivraisonsList.add(livraison);
            */
            }

            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'historique des livraisons: " + e.getMessage());
        }
    }
    private void updateStatistics() {
        try {
            // Nombre total de fournisseurs
            String countQuery = "SELECT COUNT(*) as total FROM fournisseurs";
            ResultSet rs = Connectiondb.Dispaly(countQuery);
            if (rs.next()) {
                lblNombreFournisseurs.setText("Nombre de fournisseurs: " + rs.getInt("total"));
            }

            // Check which table exists for statistics
            Connection connection = Connectiondb.Connection();
            String checkTableQuery = "SHOW TABLES LIKE 'historique_livraisons'";
            ResultSet tableCheck = connection.createStatement().executeQuery(checkTableQuery);

            if (tableCheck.next()) {
                // Use historique_livraisons table
                String statsQuery = "SELECT COUNT(*) as total, COALESCE(SUM(montant_livraison), 0) as montant FROM historique_livraisons";
                rs = Connectiondb.Dispaly(statsQuery);
                if (rs.next()) {
                    lblTotalLivraisons.setText("Total livraisons: " + rs.getInt("total"));
                    lblMontantTotal.setText("Montant total: " + String.format("%.2f", rs.getDouble("montant")) + " MAD");
                }
            } else {
                // Check for livraisons table
                checkTableQuery = "SHOW TABLES LIKE 'livraisons'";
                tableCheck = connection.createStatement().executeQuery(checkTableQuery);

                if (tableCheck.next()) {
                    // Use livraisons table for statistics
                    String statsQuery = "SELECT COUNT(*) as total, COALESCE(SUM(quantite_livree * prix_unitaire), 0) as montant FROM livraisons";
                    rs = Connectiondb.Dispaly(statsQuery);
                    if (rs.next()) {
                        lblTotalLivraisons.setText("Total livraisons: " + rs.getInt("total"));
                        lblMontantTotal.setText("Montant total: " + String.format("%.2f", rs.getDouble("montant")) + " MAD");
                    }
                } else {
                    // No delivery table found
                    lblTotalLivraisons.setText("Total livraisons: 0");
                    lblMontantTotal.setText("Montant total: 0.00 MAD");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour des statistiques: " + e.getMessage());
            // Set default values on error
            lblTotalLivraisons.setText("Total livraisons: N/A");
            lblMontantTotal.setText("Montant total: N/A");
        }
    }

    // Add this method to your FournisseurController class

    @FXML
    void handleFiltrerHistorique(ActionEvent event) {
        Fournisseur selectedFournisseur = fournisseurHistoriqueCombo.getSelectionModel().getSelectedItem();

        if (selectedFournisseur == null) {
            showAlert("Attention", "Veuillez sélectionner un fournisseur pour filtrer l'historique.");
            return;
        }

        LocalDate startDate = dateDebutPicker.getValue();
        LocalDate endDate = dateFinPicker.getValue();

        // Date validation
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            showAlert("Erreur", "La date de début doit être antérieure à la date de fin.");
            return;
        }

        // تحديث المتغيرات الحالية
        currentFournisseurId = selectedFournisseur.getId();
        currentStartDate = startDate;
        currentEndDate = endDate;

        // Load filtered history
        loadHistoriqueLivraisons(selectedFournisseur.getId(), startDate, endDate);

        // Update status bar with filter info
        updateStatusBarWithFilter(startDate, endDate);
    }



    // Méthode pour mettre à jour la barre de statut
    private void updateStatusBarWithFilter(LocalDate startDate, LocalDate endDate) {
        if (startDate != null || endDate != null) {
            String filterInfo = "Filtre appliqué";
            if (startDate != null && endDate != null) {
                filterInfo += " du " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " au " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (startDate != null) {
                filterInfo += " à partir du " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                filterInfo += " jusqu'au " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            if (lblStatutBar != null) {
                lblStatutBar.setText(filterInfo);
            }
        } else {
            if (lblStatutBar != null) {
                lblStatutBar.setText("Tous les enregistrements");
            }
        }
    }



    // Remplacez votre méthode loadHistoriqueLivraisons actuelle par cette version corrigée

    private void loadHistoriqueLivraisons(int fournisseurId, LocalDate startDate, LocalDate endDate) {
        System.out.println("=== DEBUT CHARGEMENT HISTORIQUE ===");
        System.out.println("Fournisseur ID: " + fournisseurId);
        System.out.println("Date début: " + startDate);
        System.out.println("Date fin: " + endDate);

        // IMPORTANT: Mettre à jour les variables globales AVANT le chargement
        currentFournisseurId = fournisseurId;
        currentStartDate = startDate;
        currentEndDate = endDate;

        // Vider la liste existante
        historiqueLivraisonsList.clear();

        try {
            // Diagnostic préalable
            diagnosticData(fournisseurId);

            // Essayer d'abord la table historique_livraisons
            if (loadFromHistoriqueLivraisonsTable(fournisseurId, startDate, endDate)) {
                System.out.println("Données chargées depuis historique_livraisons");
            }
            // Si pas de données, essayer de générer depuis commandes
            else if (loadFromCommandesTable(fournisseurId, startDate, endDate)) {
                System.out.println("Données générées depuis commandes");
            }
            // Si toujours pas de données, essayer sans filtre pour debug
            else if (loadAllCommandesForDebugging(fournisseurId)) {
                System.out.println("Données de debug chargées");
            }
            else {
                System.out.println("Aucune donnée trouvée");
            }

            // Appliquer les données à la table
            tableHistoriqueLivraisons.setItems(historiqueLivraisonsList);

            // CRITIQUE: Mettre à jour les statistiques APRÈS avoir chargé les données
            updateDeliveryStatistics();

            // Mettre à jour la barre de statut
            updateStatusBar();

            System.out.println("Total des enregistrements chargés: " + historiqueLivraisonsList.size());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de l'historique: " + e.getMessage());

            // En cas d'erreur, vider tout et réinitialiser les stats
            historiqueLivraisonsList.clear();
            tableHistoriqueLivraisons.setItems(historiqueLivraisonsList);
            updateDeliveryStatistics(); // Réinitialiser les stats
        }

        System.out.println("=== FIN CHARGEMENT HISTORIQUE ===");
    }

    // Méthode pour charger depuis la table historique_livraisons
    private boolean loadFromHistoriqueLivraisonsTable(int fournisseurId, LocalDate startDate, LocalDate endDate) {
        try {
            StringBuilder query = new StringBuilder(
                    "SELECT num_commande, date_commande, date_livraison, fournisseur_nom, " +
                            "produits_livres, quantite_livree, montant_livraison, statut_livraison " +
                            "FROM historique_livraisons WHERE fournisseur_id = ?"
            );

            if (startDate != null) {
                query.append(" AND date_livraison >= ?");
            }
            if (endDate != null) {
                query.append(" AND date_livraison <= ?");
            }
            query.append(" ORDER BY date_livraison DESC");

            System.out.println("Query historique_livraisons: " + query.toString());

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            stmt.setInt(paramIndex++, fournisseurId);
            if (startDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                HistoriqueLivraison livraison = new HistoriqueLivraison();

                // Handle num_commande (can be null)
                livraison.setNumCommande(rs.getString("num_commande"));

                // Handle date_commande with null check
                java.sql.Date dateCommande = rs.getDate("date_commande");
                if (dateCommande != null) {
                    livraison.setDateCommande(dateCommande.toLocalDate());
                } else {
                    livraison.setDateCommande(null); // or set a default date if needed
                }

                // Handle date_livraison with null check
                java.sql.Date dateLivraison = rs.getDate("date_livraison");
                if (dateLivraison != null) {
                    livraison.setDateLivraison(dateLivraison.toLocalDate());
                } else {
                    livraison.setDateLivraison(null); // or set a default date if needed
                }

                // Handle other fields (with null safety)
                livraison.setFournisseurNom(rs.getString("fournisseur_nom"));
                livraison.setProduitsLivres(rs.getString("produits_livres"));
                livraison.setQuantiteLivree(rs.getInt("quantite_livree"));
                livraison.setMontantLivraison(rs.getDouble("montant_livraison"));
                livraison.setStatutLivraison(rs.getString("statut_livraison"));

                historiqueLivraisonsList.add(livraison);
                hasData = true;
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("Résultats de historique_livraisons: " + (hasData ? "TROUVÉ" : "VIDE"));
            return hasData;

        } catch (SQLException e) {
            System.out.println("Erreur table historique_livraisons: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Méthode CORRIGÉE pour charger depuis la table commandes
    private boolean loadFromCommandesTable(int fournisseurId, LocalDate startDate, LocalDate endDate) {
        try {
            StringBuilder query = new StringBuilder(
                    "SELECT c.numero_commande, c.Date_Commande, c.date_livraison, " +
                            "c.ID_produits, c.quantite, c.total_commande, c.etat, " +
                            "f.nom as fournisseur_nom, p.designation, p.marque, " +
                            "CONCAT(cl.Nom, ' ', cl.Prenom) as client_nom " +
                            "FROM commandes c " +
                            "LEFT JOIN produits p ON c.ID_produits = p.reference " +
                            "LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id " +
                            "LEFT JOIN clients cl ON c.ID_Client = cl.id " +
                            "WHERE p.fournisseur_id = ? AND c.etat IN ('Livré', 'Terminé')"
            );

            if (startDate != null) {
                query.append(" AND c.Date_Commande >= ?");
            }
            if (endDate != null) {
                query.append(" AND c.Date_Commande <= ?");
            }
            query.append(" ORDER BY c.Date_Commande DESC");

            System.out.println("Query commandes: " + query.toString());

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            stmt.setInt(paramIndex++, fournisseurId);
            if (startDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                HistoriqueLivraison livraison = new HistoriqueLivraison();
                livraison.setNumCommande(rs.getString("numero_commande"));
                livraison.setDateCommande(rs.getDate("Date_Commande").toLocalDate());

                // Utiliser date_livraison si disponible, sinon Date_Commande
                Date dateLivraison = rs.getDate("date_livraison");
                if (dateLivraison != null) {
                    livraison.setDateLivraison(dateLivraison.toLocalDate());
                } else {
                    livraison.setDateLivraison(rs.getDate("Date_Commande").toLocalDate());
                }

                livraison.setFournisseurNom(rs.getString("fournisseur_nom"));

                // Construire la description des produits
                String designation = rs.getString("designation");
                String marque = rs.getString("marque");
                String refProduit = rs.getString("ID_produits");
                String produitsDesc = String.format("%s (%s) - Ref: %s",
                        designation != null ? designation : "Produit",
                        marque != null ? marque : "N/A",
                        refProduit);
                livraison.setProduitsLivres(produitsDesc);

                livraison.setQuantiteLivree(rs.getInt("quantite"));
                livraison.setMontantLivraison(rs.getDouble("total_commande"));
                livraison.setStatutLivraison("Livré");

                historiqueLivraisonsList.add(livraison);
                hasData = true;
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("Résultats de commandes: " + (hasData ? "TROUVÉ" : "VIDE"));
            return hasData;

        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement depuis commandes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Nouvelle méthode pour debug - charge toutes les commandes pour voir ce qui est disponible
    private boolean loadAllCommandesForDebugging(int fournisseurId) {
        try {
            System.out.println("=== DEBUT DEBUG - Toutes les commandes ===");

            StringBuilder query = new StringBuilder(
                    "SELECT c.numero_commande, c.Date_Commande, c.date_livraison, " +
                            "c.ID_produits, c.quantite, c.total_commande, c.etat, " +
                            "f.nom as fournisseur_nom, f.id as fournisseur_id, " +
                            "p.designation, p.marque, p.fournisseur_id as produit_fournisseur_id " +
                            "FROM commandes c " +
                            "LEFT JOIN produits p ON c.ID_produits = p.reference " +
                            "LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id " +
                            "ORDER BY c.Date_Commande DESC LIMIT 10"
            );

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());
            ResultSet rs = stmt.executeQuery();

            boolean hasData = false;
            int count = 0;

            while (rs.next()) {
                count++;
                System.out.println("--- Commande " + count + " ---");
                System.out.println("Numéro: " + rs.getString("numero_commande"));
                System.out.println("Date: " + rs.getDate("Date_Commande"));
                System.out.println("Etat: " + rs.getString("etat"));
                System.out.println("Produit ID: " + rs.getString("ID_produits"));
                System.out.println("Fournisseur DB ID: " + rs.getInt("fournisseur_id"));
                System.out.println("Fournisseur recherché: " + fournisseurId);
                System.out.println("Produit fournisseur ID: " + rs.getInt("produit_fournisseur_id"));

                // Si c'est le bon fournisseur, l'ajouter même si l'état n'est pas "Livré"
                if (rs.getInt("produit_fournisseur_id") == fournisseurId) {
                    HistoriqueLivraison livraison = new HistoriqueLivraison();
                    livraison.setNumCommande(rs.getString("numero_commande"));
                    livraison.setDateCommande(rs.getDate("Date_Commande").toLocalDate());

                    Date dateLivraison = rs.getDate("date_livraison");
                    if (dateLivraison != null) {
                        livraison.setDateLivraison(dateLivraison.toLocalDate());
                    } else {
                        livraison.setDateLivraison(rs.getDate("Date_Commande").toLocalDate());
                    }

                    livraison.setFournisseurNom(rs.getString("fournisseur_nom"));

                    String designation = rs.getString("designation");
                    String marque = rs.getString("marque");
                    String refProduit = rs.getString("ID_produits");
                    String produitsDesc = String.format("%s (%s) - Ref: %s [DEBUG - Etat: %s]",
                            designation != null ? designation : "Produit",
                            marque != null ? marque : "N/A",
                            refProduit,
                            rs.getString("etat"));
                    livraison.setProduitsLivres(produitsDesc);

                    livraison.setQuantiteLivree(rs.getInt("quantite"));
                    livraison.setMontantLivraison(rs.getDouble("total_commande"));
                    livraison.setStatutLivraison(rs.getString("etat"));

                    historiqueLivraisonsList.add(livraison);
                    hasData = true;

                    System.out.println("*** AJOUTÉ À LA LISTE ***");
                }
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("=== FIN DEBUG - Total trouvé: " + count + " ===");
            return hasData;

        } catch (SQLException e) {
            System.out.println("Erreur lors du debug: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour forcer la génération de l'historique depuis les commandes
    private void generateHistoriqueFromCommandes() {
        try {
            Connection conn = Connectiondb.Connection();

            // Vérifier d'abord si la procédure existe
            CallableStatement stmt = conn.prepareCall("{CALL auto_generate_delivery_history()}");
            stmt.execute();
            stmt.close();
            conn.close();

            System.out.println("Historique généré automatiquement depuis les commandes");
            showAlert("Information", "Historique des livraisons généré avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la génération automatique: " + e.getMessage());
            System.out.println("Tentative de génération manuelle...");

            // Si la procédure échoue, essayer une génération manuelle
            generateHistoriqueManually();
        }
    }

    // Méthode de génération manuelle en cas d'échec de la procédure stockée
    private void generateHistoriqueManually() {
        try {
            Connection conn = Connectiondb.Connection();

            // Requête pour récupérer toutes les commandes livrées sans historique
            String selectQuery = "SELECT DISTINCT " +
                    "c.numero_commande, c.Date_Commande, c.date_livraison, " +
                    "c.ID_produits, c.quantite, c.total_commande, c.etat, " +
                    "p.fournisseur_id, f.nom as fournisseur_nom, " +
                    "p.designation, p.marque " +
                    "FROM commandes c " +
                    "LEFT JOIN produits p ON c.ID_produits = p.reference " +
                    "LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id " +
                    "WHERE c.etat IN ('Livré', 'Terminé') " +
                    "AND p.fournisseur_id IS NOT NULL " +
                    "AND NOT EXISTS (SELECT 1 FROM historique_livraisons hl WHERE hl.num_commande = c.numero_commande)";

            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery();

            String insertQuery = "INSERT INTO historique_livraisons " +
                    "(num_commande, date_commande, date_livraison, fournisseur_id, fournisseur_nom, " +
                    "produits_livres, quantite_livree, montant_livraison, statut_livraison) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            int insertCount = 0;
            while (rs.next()) {
                insertStmt.setString(1, rs.getString("numero_commande"));
                insertStmt.setDate(2, rs.getDate("Date_Commande"));

                Date dateLivraison = rs.getDate("date_livraison");
                if (dateLivraison != null) {
                    insertStmt.setDate(3, dateLivraison);
                } else {
                    insertStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                }

                insertStmt.setInt(4, rs.getInt("fournisseur_id"));
                insertStmt.setString(5, rs.getString("fournisseur_nom"));

                String produitsDesc = String.format("%s (%s) - Ref: %s",
                        rs.getString("designation") != null ? rs.getString("designation") : "Produit",
                        rs.getString("marque") != null ? rs.getString("marque") : "N/A",
                        rs.getString("ID_produits"));
                insertStmt.setString(6, produitsDesc);

                insertStmt.setInt(7, rs.getInt("quantite"));
                insertStmt.setDouble(8, rs.getDouble("total_commande"));
                insertStmt.setString(9, "Livré");

                insertStmt.executeUpdate();
                insertCount++;
            }

            rs.close();
            selectStmt.close();
            insertStmt.close();
            conn.close();

            System.out.println("Génération manuelle terminée: " + insertCount + " enregistrements créés");
            if (insertCount > 0) {
                showAlert("Information", "Historique généré manuellement avec succès ! " + insertCount + " livraisons ajoutées.");
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la génération manuelle: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    public void handleRefreshStatistics() {
        System.out.println("=== REFRESH STATISTIQUES MANUEL ===");

        if (currentFournisseurId != -1) {
            // Recharger les données et mettre à jour les stats
            loadHistoriqueLivraisons(currentFournisseurId, currentStartDate, currentEndDate);
        } else {
            // Juste mettre à jour les stats générales
            updateStatistics();
        }
    }



    public void testStatisticsDisplay() {
        System.out.println("=== TEST AFFICHAGE STATISTIQUES ===");

        // Test avec des valeurs fixes pour vérifier l'affichage
        if (lblTotalLivraisons != null) {
            lblTotalLivraisons.setText("Total livraisons: 99 (TEST)");
            System.out.println("Test label Total Livraisons: OK");
        } else {
            System.out.println("ERREUR: lblTotalLivraisons est NULL !");
        }

        if (lblMontantTotal != null) {
            lblMontantTotal.setText("Montant total: 12345.67 MAD (TEST)");
            System.out.println("Test label Montant Total: OK");
        } else {
            System.out.println("ERREUR: lblMontantTotal est NULL !");
        }

        if (lblDelaiMoyen != null) {
            lblDelaiMoyen.setText("5 jours (TEST)");
            System.out.println("Test label Délai Moyen: OK");
        } else {
            System.out.println("ERREUR: lblDelaiMoyen est NULL !");
        }

        if (lblTauxPonctualite != null) {
            lblTauxPonctualite.setText("85% (TEST)");
            System.out.println("Test label Taux Ponctualité: OK");
        } else {
            System.out.println("ERREUR: lblTauxPonctualite est NULL !");
        }
    }

    // Méthode utilitaire pour diagnostiquer les données
    private void diagnosticData(int fournisseurId) {
        try {
            Connection conn = Connectiondb.Connection();

            // Vérifier les commandes pour ce fournisseur
            PreparedStatement stmt1 = conn.prepareStatement(
                    "SELECT COUNT(*) as count, " +
                            "COUNT(CASE WHEN c.etat IN ('Livré', 'Terminé') THEN 1 END) as livrees " +
                            "FROM commandes c " +
                            "LEFT JOIN produits p ON c.ID_produits = p.reference " +
                            "WHERE p.fournisseur_id = ?"
            );
            stmt1.setInt(1, fournisseurId);
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                System.out.println("=== DIAGNOSTIC FOURNISSEUR " + fournisseurId + " ===");
                System.out.println("Total commandes: " + rs1.getInt("count"));
                System.out.println("Commandes livrées: " + rs1.getInt("livrees"));
            }

            // Vérifier l'historique existant
            PreparedStatement stmt2 = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM historique_livraisons WHERE fournisseur_id = ?"
            );
            stmt2.setInt(1, fournisseurId);
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) {
                System.out.println("Historique existant: " + rs2.getInt("count"));
            }

            // Vérifier les produits du fournisseur
            PreparedStatement stmt3 = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM produits WHERE fournisseur_id = ?"
            );
            stmt3.setInt(1, fournisseurId);
            ResultSet rs3 = stmt3.executeQuery();
            if (rs3.next()) {
                System.out.println("Produits du fournisseur: " + rs3.getInt("count"));
            }

            System.out.println("=== FIN DIAGNOSTIC ===");

            rs1.close();
            rs2.close();
            rs3.close();
            stmt1.close();
            stmt2.close();
            stmt3.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private int countLivraisons(int fournisseurId, LocalDate startDate, LocalDate endDate) {
        int count = 0;
        try {
            StringBuilder query = new StringBuilder(
                    "SELECT COUNT(*) as total FROM historique_livraisons WHERE fournisseur_id = ?"
            );
            if (startDate != null) {
                query.append(" AND date_livraison >= ?");
            }
            if (endDate != null) {
                query.append(" AND date_livraison <= ?");
            }

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            stmt.setInt(paramIndex++, fournisseurId);
            if (startDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            }
            if (endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
            }
            rs.close();
            stmt.close();
            conn.close();

            System.out.println("Count livraisons pour fournisseur " + fournisseurId + ": " + count);

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des livraisons: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }
    public void refreshAllData() {
        loadFournisseurs();
        loadFournisseursInComboBoxes();

        // If a supplier is selected, reload its history and update statistics
        if (currentFournisseurId != -1) {
            loadHistoriqueLivraisons(currentFournisseurId, currentStartDate, currentEndDate);
        } else {
            updateDeliveryStatistics(); // Reset statistics
        }
    }

    private void updateStatusBar() {
        if (currentFournisseurId == -1) {
            if (lblStatutBar != null) {
                lblStatutBar.setText("Aucun fournisseur sélectionné");
            }
            return;
        }

        int total = countLivraisons(currentFournisseurId, currentStartDate, currentEndDate);

        if (total == 0) {
            if (lblStatutBar != null) {
                lblStatutBar.setText("Aucune livraison trouvée pour ce fournisseur");
            }
        } else {
            String statusText = "Chargé " + total + " livraison(s)";

            // إضافة معلومات التاريخ إذا كان هناك فلتر
            if (currentStartDate != null || currentEndDate != null) {
                statusText += " (filtrées)";
            }

            if (lblStatutBar != null) {
                lblStatutBar.setText(statusText);
            }
        }
    }
    private void updateDetailedStatistics() {
        if (currentFournisseurId == -1) return;

        try {
            StringBuilder query = new StringBuilder(
                    "SELECT COUNT(*) as total_count, " +
                            "SUM(quantite_livree) as total_quantity, " +
                            "SUM(montant_livraison) as total_amount " +
                            "FROM historique_livraisons WHERE fournisseur_id = ?"
            );

            if (currentStartDate != null) {
                query.append(" AND date_livraison >= ?");
            }
            if (currentEndDate != null) {
                query.append(" AND date_livraison <= ?");
            }

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            stmt.setInt(paramIndex++, currentFournisseurId);
            if (currentStartDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(currentStartDate));
            }
            if (currentEndDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(currentEndDate));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalCount = rs.getInt("total_count");
                int totalQuantity = rs.getInt("total_quantity");
                double totalAmount = rs.getDouble("total_amount");

                // يمكنك استخدام هذه القيم لتحديث labels إضافية في الواجهة
                System.out.println("إحصائيات مفصلة:");
                System.out.println("عدد الطلبات: " + totalCount);
                System.out.println("إجمالي الكمية: " + totalQuantity);
                System.out.println("إجمالي المبلغ: " + totalAmount + " MAD");

                // إذا كان لديك labels إضافية في الواجهة، يمكنك تحديثها هنا
                // مثال:
                // if (lblTotalQuantity != null) {
                //     lblTotalQuantity.setText("Quantité totale: " + totalQuantity);
                // }
                // if (lblTotalAmount != null) {
                //     lblTotalAmount.setText("Montant total: " + String.format("%.2f", totalAmount) + " MAD");
                // }
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques détaillées: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void updateDeliveryStatistics() {
        System.out.println("=== MISE À JOUR DES STATISTIQUES ===");
        System.out.println("Current Fournisseur ID: " + currentFournisseurId);
        System.out.println("Current Start Date: " + currentStartDate);
        System.out.println("Current End Date: " + currentEndDate);

        if (currentFournisseurId == -1) {
            // Réinitialiser les statistiques quand aucun fournisseur n'est sélectionné
            if (lblTotalLivraisons != null) {
                lblTotalLivraisons.setText("Total livraisons: 0");
            }
            if (lblMontantTotal != null) {
                lblMontantTotal.setText("Montant total: 0.00 MAD");
            }
            if (lblDelaiMoyen != null) {
                lblDelaiMoyen.setText("0 jours");
            }
            if (lblTauxPonctualite != null) {
                lblTauxPonctualite.setText("0%");
            }
            System.out.println("Statistiques réinitialisées (aucun fournisseur)");
            return;
        }

        try {
            // Première requête pour les statistiques de base
            StringBuilder query = new StringBuilder(
                    "SELECT COUNT(*) as total_count, " +
                            "COALESCE(SUM(quantite_livree), 0) as total_quantity, " +
                            "COALESCE(SUM(montant_livraison), 0) as total_amount " +
                            "FROM historique_livraisons WHERE fournisseur_id = ?"
            );

            // Ajouter les filtres de date si définis
            if (currentStartDate != null) {
                query.append(" AND date_livraison >= ?");
            }
            if (currentEndDate != null) {
                query.append(" AND date_livraison <= ?");
            }

            System.out.println("Requête statistiques: " + query.toString());

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            stmt.setInt(paramIndex++, currentFournisseurId);
            if (currentStartDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(currentStartDate));
            }
            if (currentEndDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(currentEndDate));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalCount = rs.getInt("total_count");
                double totalAmount = rs.getDouble("total_amount");
                int totalQuantity = rs.getInt("total_quantity");

                System.out.println("Résultats requête:");
                System.out.println("- Total Count: " + totalCount);
                System.out.println("- Total Amount: " + totalAmount);
                System.out.println("- Total Quantity: " + totalQuantity);

                // Mettre à jour les labels principaux
                if (lblTotalLivraisons != null) {
                    lblTotalLivraisons.setText("Total livraisons: " + totalCount);
                    System.out.println("Label Total Livraisons mis à jour: " + totalCount);
                }

                if (lblMontantTotal != null) {
                    lblMontantTotal.setText("Montant total: " + String.format("%.2f", totalAmount) + " MAD");
                    System.out.println("Label Montant Total mis à jour: " + totalAmount);
                }

                // Calculer les statistiques additionnelles si on a des données
                if (totalCount > 0) {
                    calculateAdditionalStatistics(totalCount);
                } else {
                    // Pas de données, mettre à zéro
                    if (lblDelaiMoyen != null) {
                        lblDelaiMoyen.setText("0 jours");
                    }
                    if (lblTauxPonctualite != null) {
                        lblTauxPonctualite.setText("0%");
                    }
                }
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("Statistiques mises à jour avec succès");

        } catch (SQLException e) {
            System.err.println("ERREUR lors de la mise à jour des statistiques: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, afficher "Erreur"
            if (lblTotalLivraisons != null) {
                lblTotalLivraisons.setText("Total livraisons: Erreur");
            }
            if (lblMontantTotal != null) {
                lblMontantTotal.setText("Montant total: Erreur");
            }
        }

        System.out.println("=== FIN MISE À JOUR STATISTIQUES ===");
    }





    private void updateDeliveryStatisticsGlobal() {
        try {
            // Vérifier quelle table existe et utiliser la bonne
            Connection connection = Connectiondb.Connection();
            String checkTableQuery = "SHOW TABLES LIKE 'historique_livraisons'";
            ResultSet tableCheck = connection.createStatement().executeQuery(checkTableQuery);

            if (tableCheck.next()) {
                // Utiliser la table historique_livraisons
                String statsQuery = "SELECT COUNT(*) as total, " +
                        "COALESCE(SUM(montant_livraison), 0) as montant " +
                        "FROM historique_livraisons";
                ResultSet rs = Connectiondb.Dispaly(statsQuery);
                if (rs.next()) {
                    lblTotalLivraisons.setText("Total livraisons: " + rs.getInt("total"));
                    lblMontantTotal.setText("Montant total: " +
                            String.format("%.2f", rs.getDouble("montant")) + " MAD");
                }
                rs.close();
            } else {
                // Vérifier la table commandes
                checkTableQuery = "SHOW TABLES LIKE 'commandes'";
                tableCheck = connection.createStatement().executeQuery(checkTableQuery);

                if (tableCheck.next()) {
                    // Utiliser la table commandes pour les statistiques
                    String statsQuery = "SELECT COUNT(*) as total, " +
                            "COALESCE(SUM(total_commande), 0) as montant " +
                            "FROM commandes WHERE etat IN ('Livré', 'Terminé')";
                    ResultSet rs = Connectiondb.Dispaly(statsQuery);
                    if (rs.next()) {
                        lblTotalLivraisons.setText("Total livraisons: " + rs.getInt("total"));
                        lblMontantTotal.setText("Montant total: " +
                                String.format("%.2f", rs.getDouble("montant")) + " MAD");
                    }
                    rs.close();
                } else {
                    // Aucune table trouvée
                    lblTotalLivraisons.setText("Total livraisons: 0");
                    lblMontantTotal.setText("Montant total: 0.00 MAD");
                }
            }

            tableCheck.close();
            connection.close();

        } catch (SQLException e) {
            System.out.println("Erreur lors des statistiques globales: " + e.getMessage());
            lblTotalLivraisons.setText("Total livraisons: N/A");
            lblMontantTotal.setText("Montant total: N/A");
        }
    }

    private void calculateAdditionalStatistics(int totalDeliveries) {
        if (currentFournisseurId == -1 || totalDeliveries == 0) {
            // Reset additional statistics
            updateAdditionalLabels(0, 0.0);
            return;
        }

        try {
            StringBuilder query = new StringBuilder(
                    "SELECT " +
                            "AVG(DATEDIFF(date_livraison, date_commande)) as avg_delay, " +
                            "COUNT(CASE WHEN DATEDIFF(date_livraison, date_commande) <= 7 THEN 1 END) as on_time_count " +
                            "FROM historique_livraisons WHERE fournisseur_id = ?"
            );

            if (currentStartDate != null) {
                query.append(" AND date_livraison >= ?");
            }
            if (currentEndDate != null) {
                query.append(" AND date_livraison <= ?");
            }

            Connection conn = Connectiondb.Connection();
            PreparedStatement stmt = conn.prepareStatement(query.toString());

            int paramIndex = 1;
            stmt.setInt(paramIndex++, currentFournisseurId);
            if (currentStartDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(currentStartDate));
            }
            if (currentEndDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(currentEndDate));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int avgDelay = rs.getInt("avg_delay");
                int onTimeCount = rs.getInt("on_time_count");
                double punctualityRate = (double) onTimeCount / totalDeliveries * 100;

                updateAdditionalLabels(avgDelay, punctualityRate);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des statistiques additionnelles: " + e.getMessage());
            updateAdditionalLabels(0, 0.0);
        }
    }
    private void updateAdditionalLabels(int avgDelay, double punctualityRate) {
        // Update average delay label if it exists
        if (lblDelaiMoyen != null) { // Assuming you have this label
            lblDelaiMoyen.setText(avgDelay + " jours");
        }

        // Update punctuality rate label if it exists
        if (lblTauxPonctualite != null) { // Assuming you have this label
            lblTauxPonctualite.setText(String.format("%.0f", punctualityRate) + "%");
        }
    }

    @FXML
    void handleRechercher(ActionEvent event) {
        String searchTerm = rechercheField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadFournisseurs();
            return;
        }

        fournisseursList.clear();
        try {
            String query = "SELECT * FROM fournisseurs WHERE nom LIKE '%" + searchTerm + "%' OR email LIKE '%" + searchTerm + "%' OR telephone LIKE '%" + searchTerm + "%' ORDER BY nom";
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setId(rs.getInt("id"));
                fournisseur.setNom(rs.getString("nom"));
                fournisseur.setCodeFournisseur(rs.getString("code_fournisseur"));
                fournisseur.setTelephone(rs.getString("telephone"));
                fournisseur.setFax(rs.getString("fax"));
                fournisseur.setEmail(rs.getString("email"));
                fournisseur.setSiteWeb(rs.getString("site_web"));
                fournisseur.setAdresse(rs.getString("adresse"));
                fournisseur.setVille(rs.getString("ville"));
                fournisseur.setCodePostal(rs.getString("code_postal"));
                fournisseur.setPays(rs.getString("pays"));
                fournisseur.setStatut(rs.getString("statut"));
                fournisseur.setNotes(rs.getString("notes"));
                fournisseursList.add(fournisseur);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
        tableFournisseurs.setItems(fournisseursList);
    }

    @FXML
    void handleNouveauFournisseur(ActionEvent event) {
        clearForm();
        isEditing = false;
        tabPane.getSelectionModel().select(tabCoordonnees);
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (selectedFournisseur == null) {
            showAlert("Attention", "Veuillez sélectionner un fournisseur à modifier.");
            return;
        }
        fillForm(selectedFournisseur);
        isEditing = true;
        tabPane.getSelectionModel().select(tabCoordonnees);
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        if (selectedFournisseur == null) {
            showAlert("Attention", "Veuillez sélectionner un fournisseur à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le fournisseur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce fournisseur?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                String query = "DELETE FROM fournisseurs WHERE id = " + selectedFournisseur.getId();
                Connectiondb.Delete(query);
                loadFournisseurs();
                loadFournisseursInComboBoxes();
                showAlert("Succès", "Fournisseur supprimé avec succès!");
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleVoirDetails(ActionEvent event) {
        if (selectedFournisseur == null) {
            showAlert("Attention", "Veuillez sélectionner un fournisseur.");
            return;
        }
        fillForm(selectedFournisseur);
        tabPane.getSelectionModel().select(tabCoordonnees);
    }

    @FXML
    void handleEnregistrer(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            String query;
            if (isEditing) {
                query = "UPDATE fournisseurs SET " +
                        "nom = '" + nomField.getText() + "', " +
                        "code_fournisseur = '" + codeFournisseurField.getText() + "', " +
                        "telephone = '" + telephoneField.getText() + "', " +
                        "fax = '" + faxField.getText() + "', " +
                        "email = '" + emailField.getText() + "', " +
                        "site_web = '" + siteWebField.getText() + "', " +
                        "adresse = '" + adresseArea.getText() + "', " +
                        "ville = '" + villeField.getText() + "', " +
                        "code_postal = '" + codePostalField.getText() + "', " +
                        "pays = '" + paysCombo.getValue() + "', " +
                        "statut = '" + statutCombo.getValue() + "', " +
                        "notes = '" + notesArea.getText() + "' " +
                        "WHERE id = " + selectedFournisseur.getId();
            } else {
                query = "INSERT INTO fournisseurs (nom, code_fournisseur, telephone, fax, email, site_web, adresse, ville, code_postal, pays, statut, notes) VALUES (" +
                        "'" + nomField.getText() + "', " +
                        "'" + codeFournisseurField.getText() + "', " +
                        "'" + telephoneField.getText() + "', " +
                        "'" + faxField.getText() + "', " +
                        "'" + emailField.getText() + "', " +
                        "'" + siteWebField.getText() + "', " +
                        "'" + adresseArea.getText() + "', " +
                        "'" + villeField.getText() + "', " +
                        "'" + codePostalField.getText() + "', " +
                        "'" + paysCombo.getValue() + "', " +
                        "'" + statutCombo.getValue() + "', " +
                        "'" + notesArea.getText() + "')";
            }

            Connectiondb.Insert(query);
            loadFournisseurs();
            loadFournisseursInComboBoxes();
            clearForm();
            tabPane.getSelectionModel().select(listeFournisseursTab);
            showAlert("Succès", "Fournisseur enregistré avec succès!");

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    @FXML
    void handleAnnuler(ActionEvent event) {
        clearForm();
        tabPane.getSelectionModel().select(listeFournisseursTab);
    }

    @FXML
    void handleReinitialiser(ActionEvent event) {
        clearForm();
    }


    @FXML
    void handleNouveauProduit(ActionEvent event) {
// Remplacez la méthode handleNouveauProduit dans votre FournisseurController.java par cette version :


            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ProduitFournisseur.fxml"));
                javafx.scene.Parent root = loader.load();

                ProduitFournisseurFormController controller = loader.getController();
                controller.setRefreshCallback(() -> {
                    // Rafraîchir la liste des produits associés si nécessaire
                    Fournisseur selectedFournisseur = fournisseurSelectCombo.getSelectionModel().getSelectedItem();
                    if (selectedFournisseur != null) {
                        loadProduitsAssocies(selectedFournisseur.getId());
                    }
                });

                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Nouveau Produit");
                stage.setScene(new javafx.scene.Scene(root));
                stage.setResizable(false);
                stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                stage.show();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'ouvrir le formulaire produit: " + e.getMessage());
                e.printStackTrace();
            }
        }

    // In FournisseurController.java
    @FXML
    void handleModifierProduit(ActionEvent event) {
        ProduitFournisseur selectedProduitFournisseur = tableProduitsAssocies.getSelectionModel().getSelectedItem();

        if (selectedProduitFournisseur == null) {
            showAlert("Attention", "Veuillez sélectionner un produit à modifier.");
            return;
        }

        try {
            // Charger l'objet Produit complet à partir de la base de données
            Produit produitToModify = getProduitByReference(selectedProduitFournisseur.getProduitId());

            if (produitToModify == null) {
                showAlert("Erreur", "Impossible de trouver les détails du produit.");
                return;
            }

            // Charger le formulaire de modification FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProduitFournisseur.fxml"));
            Parent root = loader.load();

            // Passer le produit au controller
            ProduitFournisseurFormController controller = loader.getController();
            controller.setProduitToEdit(produitToModify);
            controller.setRefreshCallback(() -> {
                Fournisseur selectedFournisseur = fournisseurSelectCombo.getSelectionModel().getSelectedItem();
                if (selectedFournisseur != null) {
                    loadProduitsAssocies(selectedFournisseur.getId());
                }
            });

            // Affichage dans une nouvelle fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Modifier Produit Associé");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture du formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Produit getProduitByReference(String reference) {
        String query = "SELECT * FROM produits WHERE reference = ?";

        try (PreparedStatement stmt = Connectiondb.Connection().prepareStatement(query)) {
            stmt.setString(1, reference);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Produit produit = new Produit();
                produit.setReference(rs.getString("reference"));
                produit.setDesignation(rs.getString("designation"));
                produit.setTaille(rs.getString("taille"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                produit.setCouleur(rs.getString("couleur"));
                produit.setType(rs.getString("type"));
                produit.setMarque(rs.getString("marque"));
                produit.setStockMinimal(rs.getInt("stock_minimal"));

                Object fournisseurId = rs.getObject("fournisseur_id");
                if (fournisseurId != null) {
                    produit.setFournisseurId(rs.getInt("fournisseur_id"));
                }

                return produit;
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération produit: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @FXML
    void handleSupprimerProduit(ActionEvent event) {
        ProduitFournisseur selectedProduit = tableProduitsAssocies.getSelectionModel().getSelectedItem();

        if (selectedProduit == null) {
            showAlert("Attention", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le produit sélectionné ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce produit : " + selectedProduit.getNomProduit() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = Connectiondb.Connection()) {
                String query = "DELETE FROM produits_fournisseurs WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, selectedProduit.getId());
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    tableProduitsAssocies.getItems().remove(selectedProduit);
                    showAlert("Succès", "Produit supprimé avec succès.");
                } else {
                    showAlert("Erreur", "Échec de la suppression du produit.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }


    private void clearForm() {
        nomField.clear();
        codeFournisseurField.clear();
        telephoneField.clear();
        faxField.clear();
        emailField.clear();
        siteWebField.clear();
        adresseArea.clear();
        villeField.clear();
        codePostalField.clear();
        paysCombo.setValue("Maroc");
        statutCombo.setValue("Actif");
        notesArea.clear();
        selectedFournisseur = null;
        isEditing = false;
    }

    private void fillForm(Fournisseur fournisseur) {
        nomField.setText(fournisseur.getNom());
        codeFournisseurField.setText(fournisseur.getCodeFournisseur());
        telephoneField.setText(fournisseur.getTelephone());
        faxField.setText(fournisseur.getFax());
        emailField.setText(fournisseur.getEmail());
        siteWebField.setText(fournisseur.getSiteWeb());
        adresseArea.setText(fournisseur.getAdresse());
        villeField.setText(fournisseur.getVille());
        codePostalField.setText(fournisseur.getCodePostal());
        paysCombo.setValue(fournisseur.getPays());
        statutCombo.setValue(fournisseur.getStatut());
        notesArea.setText(fournisseur.getNotes());
    }

    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom du fournisseur est obligatoire.");
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showAlert("Erreur", "L'email est obligatoire.");
            return false;
        }
        if (codeFournisseurField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le code fournisseur est obligatoire.");
            return false;
        }
        return true;
    }
    @FXML
    void handleExporterHistorique(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'historique");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                document.add(new Paragraph("Historique des Fournisseurs"));
                document.add(new Paragraph(" ")); // Espace vide

                PdfPTable table = new PdfPTable(3); // عدد الأعمدة
                table.addCell("Nom");
                table.addCell("Téléphone");
                table.addCell("Email");

                for (Fournisseur f : tableFournisseurs.getItems()) {
                    table.addCell(f.getNom());
                    table.addCell(f.getTelephone());
                    table.addCell(f.getEmail());
                }

                document.add(table);
                showAlert("Succès", "L'historique a été exporté avec succès.");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur est survenue lors de l'exportation.");
            } finally {
                document.close();
            }
        }
    }




    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}