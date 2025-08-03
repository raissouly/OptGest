package sample;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
public class FacturationController implements Initializable {

    // FXML Elements for Factures Tab
    @FXML private Button btnNouvelleFacture;
    @FXML private Button btnModifierFacture;
    @FXML private Button btnSupprimerFacture;
    @FXML private Button btnImprimerFacture;
    @FXML private Button btnExporterPDF;
    @FXML private Button btnRechercher;
    @FXML private TextField txtRechercheFacture;

    @FXML private TableView<Facture> tableFactures;
    @FXML private TableColumn<Facture, String> colNumero;
    @FXML private TableColumn<Facture, String> colDate;
    @FXML private TableColumn<Facture, String> colClient;
    @FXML private TableColumn<Facture, String> colMontantHT;
    @FXML private TableColumn<Facture, String> colTVA;
    @FXML private TableColumn<Facture, String> colMontantTTC;
    @FXML private TableColumn<Facture, String> colStatut;
    @FXML private TableColumn<Facture, String> colDateEcheance;

    @FXML private Label lblFactureInfo;

    // FXML Elements for Clients Tab
    @FXML private Button btnNouveauClient;
    @FXML private Button btnModifierClient;
    @FXML private Button btnSupprimerClient;
    @FXML private Button btnRechercherClient;
    @FXML private TextField txtRechercheClient;

    @FXML private TableView<Clients> tableClients;
    @FXML private TableColumn<Clients, String> colClientNom;
    @FXML private TableColumn<Clients, String> colClientPrenom;
    @FXML private TableColumn<Clients, String> colClientEmail;
    @FXML private TableColumn<Clients, String> colClientTelephone;
    @FXML private TableColumn<Clients, String> colClientVille;

    @FXML private Label lblClientInfo;

    // Footer Elements
    @FXML private Label lblStatusMessage;
    @FXML private Label lblTotalFactures;
    @FXML private Label lblFacturesPayeesFooter;
    @FXML private Label lblFacturesImpayeesFooter;
    @FXML private Label lblCATotal;
    @FXML private Label lblDerniereMAJ;

    // Data Lists
    private ObservableList<Facture> facturesData = FXCollections.observableArrayList();
    private ObservableList<Clients> clientsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFacturesTable();
        initializeClientsTable();
        loadFactures();
        loadClients();
        updateFooterStats();
        setupTableListeners();
    }

    // Initialize Factures Table
    private void initializeFacturesTable() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        colMontantHT.setCellValueFactory(new PropertyValueFactory<>("montantHT"));
        colTVA.setCellValueFactory(new PropertyValueFactory<>("tva"));
        colMontantTTC.setCellValueFactory(new PropertyValueFactory<>("montantTTC"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateEcheance.setCellValueFactory(new PropertyValueFactory<>("dateEcheance"));

        tableFactures.setItems(facturesData);
    }

    // Initialize Clients Table
    private void initializeClientsTable() {
        colClientNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClientPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colClientTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colClientVille.setCellValueFactory(new PropertyValueFactory<>("ville"));

        tableClients.setItems(clientsData);
    }

    // Setup Table Selection Listeners
    private void setupTableListeners() {
        tableFactures.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateFactureInfo(newValue));

        tableClients.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateClientInfo(newValue));
    }
    // Load Factures from Database
    private void loadFactures() {
        facturesData.clear();
        try {
            String query = "SELECT f.numero_facture, f.date_facture, f.date_echeance, f.montant_ht, " +
                    "f.montant_tva, f.montant_ttc, f.statut, " +
                    "CONCAT(c.Nom, ' ', c.Prenom) as client_nom " +
                    "FROM factures f " +
                    "JOIN clients c ON f.ID_Client = c.id " +
                    "ORDER BY f.date_facture DESC";

            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Facture facture = new Facture(
                        rs.getString("numero_facture"),
                        rs.getString("date_facture"),
                        rs.getString("client_nom"),
                        String.format("%.2f", rs.getDouble("montant_ht")),
                        String.format("%.2f", rs.getDouble("montant_tva")),
                        String.format("%.2f", rs.getDouble("montant_ttc")),
                        rs.getString("statut"),
                        rs.getString("date_echeance")
                );
                facturesData.add(facture);
            }
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les factures: " + e.getMessage());
        }
    }

    private void loadClients() {
        clientsData.clear();
        try {
            String query = "SELECT * FROM clients ORDER BY Nom, Prenom";
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Clients client = new Clients(
                        rs.getInt("id"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Telephone"),
                        rs.getString("Email")
                );
                clientsData.add(client);
            }
        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les clients: " + e.getMessage());
        }
    }
    // إضافة الـ imports اللازمة في بداية الملف
    private ObservableList<ProduitFacture> loadProduitsParCommandeOldSystem(String numeroCommande) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();

        // Pour l'ancien système où les produits sont directement dans la table commandes
        String query = "SELECT p.designation, c.quantite, c.prix_unitaire " +
                "FROM commandes c " +
                "JOIN produits p ON c.ID_produits = p.reference " +
                "WHERE c.numero_commande = ?";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setString(1, numeroCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("designation");
                    int quantite = rs.getInt("quantite");
                    double prixUnitaire = rs.getDouble("prix_unitaire");

                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                        produits.add(new ProduitFacture(nom, quantite, prixUnitaire));
                    }
                }
            }
        }

        return produits;
    }
    @FXML
    private void creerNouvelleFacture() {
        if (clientsData.isEmpty()) {
            showWarning("Aucun client", "Veuillez d'abord créer un client avant de créer une facture.");
            return;
        }

        try {
            // Étape 1: Sélectionner le client
            Clients selectedClient = showClientSelectionDialog();
            if (selectedClient == null) return;

            // Étape 2: Sélectionner la commande
            String numeroCommande = showCommandeSelectionDialog(selectedClient.getId());
            if (numeroCommande == null) return;

            // Étape 3: Vérifier si la facture existe déjà
            if (isFactureAlreadyExists(numeroCommande)) {
                showWarning("Facture existante",
                        "Une facture existe déjà pour cette commande: " + numeroCommande);
                return;
            }

            // Étape 4: Charger les produits (MÉTHODE CORRIGÉE)
            ObservableList<ProduitFacture> produits = loadProduitsParCommandeCorrect(numeroCommande);

            if (produits.isEmpty()) {
                showWarning("Commande vide",
                        "Cette commande ne contient aucun produit valide.\n" +
                                "Commande: " + numeroCommande);
                return;
            }

            // Étape 5: Afficher un récapitulatif avant création
            if (!showProductSummary(produits, numeroCommande)) {
                return; // L'utilisateur a annulé
            }

            // Étape 6: Calculer les montants
            double montantHT = produits.stream().mapToDouble(ProduitFacture::getTotal).sum();
            double tauxTVA = 20.0;
            double montantTVA = montantHT * tauxTVA / 100.0;
            double montantTTC = montantHT + montantTVA;

            // Étape 7: Générer le numéro de facture
            String numeroFacture = generateNewInvoiceNumber();

            // Étape 8: Enregistrer en base de données
            String query = "INSERT INTO factures (numero_facture, numero_commande, ID_Client, " +
                    "date_facture, date_echeance, montant_ht, taux_tva, montant_tva, " +
                    "montant_ttc, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
                ps.setString(1, numeroFacture);
                ps.setString(2, numeroCommande);
                ps.setInt(3, selectedClient.getId());
                ps.setString(4, LocalDate.now().toString());
                ps.setString(5, LocalDate.now().plusDays(30).toString()); // Échéance 30 jours
                ps.setDouble(6, montantHT);
                ps.setDouble(7, tauxTVA);
                ps.setDouble(8, montantTVA);
                ps.setDouble(9, montantTTC);
                ps.setString(10, "Brouillon");

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    // Étape 9: Actualiser l'interface
                    loadFactures();
                    updateFooterStats();

                    // Étape 10: Afficher le succès avec détails
                    String message = String.format(
                            "Facture créée avec succès!\n\n" +
                                    "Numéro: %s\n" +
                                    "Client: %s %s\n" +
                                    "Commande: %s\n" +
                                    "Montant HT: %.2f DH\n" +
                                    "TVA (%.1f%%): %.2f DH\n" +
                                    "Montant TTC: %.2f DH\n" +
                                    "Produits: %d",
                            numeroFacture,
                            selectedClient.getNom(), selectedClient.getPrenom(),
                            numeroCommande,
                            montantHT, tauxTVA, montantTVA, montantTTC,
                            produits.size()
                    );

                    showInfo("Succès", message);

                    // Mettre à jour le statut
                    lblStatusMessage.setText("Facture " + numeroFacture + " créée avec succès");

                } else {
                    showError("Erreur", "Aucune ligne n'a été insérée dans la base de données.");
                }
            }

        } catch (SQLException e) {
            String errorMsg = "Erreur de base de données lors de la création de la facture:\n" + e.getMessage();
            showError("Erreur SQL", errorMsg);
            e.printStackTrace();
        } catch (Exception e) {
            String errorMsg = "Erreur inattendue lors de la création de la facture:\n" + e.getMessage();
            showError("Erreur", errorMsg);
            e.printStackTrace();
        }
    }


    private ObservableList<ProduitFacture> loadProduitsParCommandeCorrect(String numeroCommande) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();
        Map<String, ProduitFacture> produitsMap = new HashMap<>(); // Pour éviter les doublons

        System.out.println("=== CHARGEMENT PRODUITS POUR COMMANDE: " + numeroCommande + " ===");

        // Méthode 1: Essayer avec la nouvelle structure (commande_produits)
        String queryNew = "SELECT p.designation, cp.quantite, cp.prix_unitaire " +
                "FROM commande_produits cp " +
                "JOIN produits p ON cp.produit_id = p.reference " +
                "JOIN commandes c ON cp.commande_id = c.id " +
                "WHERE c.numero_commande = ? " +
                "GROUP BY p.designation, cp.quantite, cp.prix_unitaire " + // Éviter les doublons
                "ORDER BY p.designation";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(queryNew)) {
            ps.setString(1, numeroCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("designation");
                    int quantite = rs.getInt("quantite");
                    double prixUnitaire = rs.getDouble("prix_unitaire");

                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                        String key = nom + "_" + prixUnitaire; // Clé unique
                        if (produitsMap.containsKey(key)) {
                            // Si le produit existe déjà, additionner les quantités
                            ProduitFacture existing = produitsMap.get(key);
                            existing.setQuantite(existing.getQuantite() + quantite);
                        } else {
                            produitsMap.put(key, new ProduitFacture(nom, quantite, prixUnitaire));
                        }
                    }
                }
            }
        }

        // Si aucun produit trouvé avec la nouvelle méthode, essayer l'ancienne structure
        if (produitsMap.isEmpty()) {
            System.out.println("Aucun produit trouvé avec la nouvelle structure, essai avec l'ancienne...");

            String queryOld = "SELECT p.designation, c.quantite, c.prix_unitaire " +
                    "FROM commandes c " +
                    "JOIN produits p ON c.ID_produits = p.reference " +
                    "WHERE c.numero_commande = ? " +
                    "ORDER BY p.designation";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(queryOld)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String nom = rs.getString("designation");
                        int quantite = rs.getInt("quantite");
                        double prixUnitaire = rs.getDouble("prix_unitaire");

                        if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                            String key = nom + "_" + prixUnitaire;
                            if (produitsMap.containsKey(key)) {
                                ProduitFacture existing = produitsMap.get(key);
                                existing.setQuantite(existing.getQuantite() + quantite);
                            } else {
                                produitsMap.put(key, new ProduitFacture(nom, quantite, prixUnitaire));
                            }
                        }
                    }
                }
            }
        }

        // Convertir la Map en ObservableList
        produits.addAll(produitsMap.values());

        // Debugging output
        System.out.println("Commande: " + numeroCommande + " - Produits uniques trouvés: " + produits.size());
        double totalCalcule = 0;
        for (ProduitFacture p : produits) {
            System.out.println("  - " + p.getNom() + " x" + p.getQuantite() +
                    " @ " + p.getPrixUnitaire() + " DH = " + p.getTotal() + " DH");
            totalCalcule += p.getTotal();
        }
        System.out.println("Total calculé: " + totalCalcule + " DH");

        return produits;
    }
    private void nettoyerDoublonsCommandeProduits() {
        try {
            System.out.println("=== NETTOYAGE DES DOUBLONS ===");

            // Supprimer les doublons en gardant seulement l'ID le plus élevé
            String deleteQuery = "DELETE cp1 FROM commande_produits cp1 " +
                    "INNER JOIN commande_produits cp2 " +
                    "WHERE cp1.id < cp2.id " +
                    "AND cp1.commande_id = cp2.commande_id " +
                    "AND cp1.produit_id = cp2.produit_id " +
                    "AND cp1.prix_unitaire = cp2.prix_unitaire";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(deleteQuery)) {
                int deletedRows = ps.executeUpdate();
                System.out.println("Doublons supprimés: " + deletedRows + " lignes");

                if (deletedRows > 0) {
                    showInfo("Nettoyage", "Nettoyage terminé: " + deletedRows + " doublons supprimés de commande_produits");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du nettoyage: " + e.getMessage());
            showError("Erreur", "Erreur lors du nettoyage des doublons: " + e.getMessage());
        }
    }


    @FXML
    private void nettoyerBase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nettoyage de la base");
        alert.setHeaderText("Supprimer les doublons dans commande_produits");
        alert.setContentText("Cette action va supprimer les entrées en double. Continuer ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            nettoyerDoublonsCommandeProduits();
        }
    }

    // Méthode pour diagnostiquer une commande spécifique
    private void diagnostiquerCommande(String numeroCommande) {
        try {
            System.out.println("=== DIAGNOSTIC DÉTAILLÉ COMMANDE: " + numeroCommande + " ===");

            // Vérifier dans commande_produits
            String query1 = "SELECT cp.id, cp.commande_id, cp.produit_id, cp.quantite, cp.prix_unitaire, cp.sous_total, " +
                    "p.designation, c.numero_commande " +
                    "FROM commande_produits cp " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "LEFT JOIN produits p ON cp.produit_id = p.reference " +
                    "WHERE c.numero_commande = ? " +
                    "ORDER BY cp.id";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query1)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("Entrées dans commande_produits:");
                    double totalCP = 0;
                    while (rs.next()) {
                        System.out.printf("  ID:%d | Produit:%s (%s) | Qté:%d | Prix:%.2f | Sous-total:%.2f%n",
                                rs.getInt("id"),
                                rs.getString("designation"),
                                rs.getString("produit_id"),
                                rs.getInt("quantite"),
                                rs.getDouble("prix_unitaire"),
                                rs.getDouble("sous_total"));
                        totalCP += rs.getDouble("sous_total");
                    }
                    System.out.println("Total commande_produits: " + totalCP + " DH");
                }
            }

            // Vérifier dans commandes (ancien système)
            String query2 = "SELECT * FROM commandes WHERE numero_commande = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query2)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Données dans table commandes:");
                        System.out.printf("  Total commande: %.2f DH%n", rs.getDouble("total_commande"));
                        System.out.printf("  Produit: %s | Qté: %d | Prix unitaire: %.2f%n",
                                rs.getString("ID_produits"),
                                rs.getInt("quantite"),
                                rs.getDouble("prix_unitaire"));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur diagnostic: " + e.getMessage());
        }
    }
    private ObservableList<ProduitFacture> loadProduitsParCommandeAvecPrixActuel(String numeroCommande) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();

        // Query to get products with current prices from the produits table
        String query = "SELECT p.designation, cp.quantite, p.prix as prix_actuel " +
                "FROM commande_produits cp " +
                "JOIN produits p ON cp.produit_id = p.reference " +
                "JOIN commandes c ON cp.commande_id = c.id " +
                "WHERE c.numero_commande = ? " +
                "ORDER BY p.designation";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setString(1, numeroCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("designation");
                    int quantite = rs.getInt("quantite");
                    double prixActuel = rs.getDouble("prix_actuel");

                    // Verify data before adding
                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixActuel >= 0) {
                        produits.add(new ProduitFacture(nom, quantite, prixActuel));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading products with current prices: " + e.getMessage());

            // Fallback: try alternative column names for price
            String[] alternativePriceColumns = {"prix_unitaire", "price", "prix_vente", "montant"};

            for (String priceColumn : alternativePriceColumns) {
                try {
                    String fallbackQuery = "SELECT p.designation, cp.quantite, p." + priceColumn + " as prix_actuel " +
                            "FROM commande_produits cp " +
                            "JOIN produits p ON cp.produit_id = p.reference " +
                            "JOIN commandes c ON cp.commande_id = c.id " +
                            "WHERE c.numero_commande = ? " +
                            "ORDER BY p.designation";

                    try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(fallbackQuery)) {
                        ps.setString(1, numeroCommande);
                        try (ResultSet rs = ps.executeQuery()) {
                            produits.clear(); // Clear any previous attempts
                            while (rs.next()) {
                                String nom = rs.getString("designation");
                                int quantite = rs.getInt("quantite");
                                double prixActuel = rs.getDouble("prix_actuel");

                                if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixActuel >= 0) {
                                    produits.add(new ProduitFacture(nom, quantite, prixActuel));
                                }
                            }
                            System.out.println("Successfully used fallback price column: " + priceColumn);
                            break; // Exit loop if successful
                        }
                    }
                } catch (SQLException fallbackEx) {
                    // Continue to next column
                    continue;
                }
            }

            // If all fallbacks failed, rethrow original exception
            if (produits.isEmpty()) {
                throw e;
            }
        }

        // Debug output
        System.out.println("Commande: " + numeroCommande + " - Produits avec prix actuels: " + produits.size());
        for (ProduitFacture p : produits) {
            System.out.println("  - " + p.getNom() + " x" + p.getQuantite() + " @ " + p.getPrixUnitaire() + " DH (prix actuel)");
        }

        return produits;
    }
    private String showCommandeSelectionDialog(int clientId) throws SQLException {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sélection de la commande");
        dialog.setHeaderText("Choisissez une commande pour créer la facture");

        ButtonType selectButtonType = new ButtonType("Sélectionner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ComboBox<String> comboBox = new ComboBox<>();
        ObservableList<String> commandes = FXCollections.observableArrayList();

        // Requête pour les commandes NON facturées
        String query = "SELECT c.numero_commande, c.Date_Commande " +
                "FROM commandes c " +
                "WHERE c.ID_Client = ? " +
                "AND c.numero_commande NOT IN (" +
                "    SELECT COALESCE(f.numero_commande, '') " +
                "    FROM factures f " +
                "    WHERE f.numero_commande IS NOT NULL" +
                ") " +
                "ORDER BY c.Date_Commande DESC";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String numeroCommande = rs.getString("numero_commande");
                    String dateCommande = rs.getString("Date_Commande");
                    String displayText = numeroCommande + " (" + dateCommande + ")";
                    commandes.add(numeroCommande);
                    comboBox.getItems().add(displayText);
                }
            }
        }

        if (commandes.isEmpty()) {
            // Vérifier si le client a des commandes du tout
            String checkQuery = "SELECT COUNT(*) as total FROM commandes WHERE ID_Client = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(checkQuery)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int totalCommandes = rs.getInt("total");
                    if (totalCommandes == 0) {
                        showWarning("Aucune commande", "Ce client n'a aucune commande enregistrée.");
                    } else {
                        showWarning("Commandes déjà facturées",
                                "Ce client a " + totalCommandes + " commande(s) mais toutes ont déjà été facturées.");
                    }
                }
            }
            return null;
        }

        comboBox.setPrefWidth(300);
        comboBox.setPromptText("Sélectionnez une commande...");

        dialog.getDialogPane().setContent(comboBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType && comboBox.getValue() != null) {
                // Extraire le numéro de commande du texte affiché
                String selectedText = comboBox.getValue().toString();
                return selectedText.split(" \\(")[0]; // Prendre la partie avant la parenthèse
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // Méthode utilitaire pour vérifier si une facture existe déjà pour une commande
    // طريقة للتحقق من وجود فاتورة مكررة
    private boolean isFactureAlreadyExists(String numeroCommande) {
        try {
            String query = "SELECT COUNT(*) FROM factures WHERE numero_commande = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Méthode pour afficher un récapitulatif des produits avant création de facture
    private boolean showProductSummary(ObservableList<ProduitFacture> produits, String numeroCommande) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Récapitulatif des produits");
        dialog.setHeaderText("Vérifiez les produits de la commande " + numeroCommande);

        ButtonType confirmButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Créer un tableau pour afficher les produits
        TableView<ProduitFacture> table = new TableView<>();
        table.setPrefHeight(200);

        TableColumn<ProduitFacture, String> colNom = new TableColumn<>("Produit");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(200);

        TableColumn<ProduitFacture, Integer> colQuantite = new TableColumn<>("Qté");
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQuantite.setPrefWidth(50);

        TableColumn<ProduitFacture, Double> colPrix = new TableColumn<>("Prix Unit.");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colPrix.setPrefWidth(80);

        TableColumn<ProduitFacture, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(80);

        table.getColumns().addAll(colNom, colQuantite, colPrix, colTotal);
        table.setItems(produits);

        // Calculer et afficher le total
        double totalGeneral = produits.stream().mapToDouble(ProduitFacture::getTotal).sum();
        Label lblTotal = new Label(String.format("Total HT: %.2f DH", totalGeneral));
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox content = new VBox(10);
        content.getChildren().addAll(table, lblTotal);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> dialogButton == confirmButtonType);

        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }
    private void diagnosticCommande(String numeroCommande) {
        try {
            System.out.println("=== DIAGNOSTIC COMMANDE: " + numeroCommande + " ===");

            // Vérifier si la commande existe
            String queryCommande = "SELECT * FROM commandes WHERE numero_commande = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(queryCommande)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Commande trouvée - ID: " + rs.getInt("id"));
                        System.out.println("Client ID: " + rs.getInt("ID_Client"));
                        System.out.println("Date: " + rs.getDate("Date_Commande"));
                    } else {
                        System.out.println("ERREUR: Commande non trouvée!");
                        return;
                    }
                }
            }

            // Vérifier les produits dans commande_produits
            String queryProduits = "SELECT cp.*, p.designation " +
                    "FROM commande_produits cp " +
                    "LEFT JOIN produits p ON cp.produit_id = p.reference " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "WHERE c.numero_commande = ?";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(queryProduits)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("Produit " + count + ":");
                        System.out.println("  - ID produit: " + rs.getString("produit_id"));
                        System.out.println("  - Désignation: " + rs.getString("designation"));
                        System.out.println("  - Quantité: " + rs.getInt("quantite"));
                        System.out.println("  - Prix unitaire: " + rs.getDouble("prix_unitaire"));
                    }
                    if (count == 0) {
                        System.out.println("ERREUR: Aucun produit trouvé dans commande_produits!");

                        // Vérifier si le produit est dans la table commandes (ancien système)
                        String queryOldSystem = "SELECT ID_produits, quantite, prix_unitaire FROM commandes WHERE numero_commande = ?";
                        try (PreparedStatement ps2 = Connectiondb.Connection().prepareStatement(queryOldSystem)) {
                            ps2.setString(1, numeroCommande);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    System.out.println("Produit trouvé dans l'ancien système (table commandes):");
                                    System.out.println("  - ID produit: " + rs2.getString("ID_produits"));
                                    System.out.println("  - Quantité: " + rs2.getInt("quantite"));
                                    System.out.println("  - Prix unitaire: " + rs2.getDouble("prix_unitaire"));
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur diagnostic: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String showCommandeSelectionDialogFiltered(int clientId) throws SQLException {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sélection de la commande");
        dialog.setHeaderText("Choisissez une commande pour créer la facture");

        ButtonType selectButtonType = new ButtonType("Sélectionner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ComboBox<String> comboBox = new ComboBox<>();
        ObservableList<String> commandes = FXCollections.observableArrayList();

        // Requête pour les commandes NON facturées
        String query = "SELECT c.numero_commande, c.Date_Commande " +
                "FROM commandes c " +
                "WHERE c.ID_Client = ? " +
                "AND c.numero_commande NOT IN (" +
                "    SELECT COALESCE(f.numero_commande, '') " +
                "    FROM factures f " +
                "    WHERE f.numero_commande IS NOT NULL" +
                ") " +
                "ORDER BY c.Date_Commande DESC";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String numeroCommande = rs.getString("numero_commande");
                    String dateCommande = rs.getString("Date_Commande");
                    commandes.add(numeroCommande);
                }
            }
        }

        if (commandes.isEmpty()) {
            // Vérifier si le client a des commandes du tout
            String checkQuery = "SELECT COUNT(*) as total FROM commandes WHERE ID_Client = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(checkQuery)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int totalCommandes = rs.getInt("total");
                    if (totalCommandes == 0) {
                        showWarning("Aucune commande", "Ce client n'a aucune commande enregistrée.");
                    } else {
                        showWarning("Commandes déjà facturées",
                                "Ce client a " + totalCommandes + " commande(s) mais toutes ont déjà été facturées.");
                    }
                }
            }
            return null;
        }

        comboBox.setItems(commandes);
        comboBox.setPrefWidth(300);
        comboBox.setPromptText("Sélectionnez une commande...");

        dialog.getDialogPane().setContent(comboBox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType && comboBox.getValue() != null) {
                return comboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    // Fixed method to load products from order - حل مشكلة التكرار (بدون Set)
    // Méthode corrigée pour charger les produits d'une commande
    private ObservableList<ProduitFacture> loadProduitsParCommande(String numeroCommande) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();

        // First, let's check the actual column names in your produits table
        // Try different possible column names for price
        String[] possiblePriceColumns = {"prix", "price", "prix_unitaire", "prix_vente", "montant"};
        String workingQuery = null;

        // Test each possible column name
        for (String priceColumn : possiblePriceColumns) {
            String testQuery = "SELECT p.designation, cp.quantite, p." + priceColumn + " as prix_produit " +
                    "FROM commande_produits cp " +
                    "JOIN produits p ON cp.produit_id = p.reference " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "WHERE c.numero_commande = ? " +
                    "LIMIT 1";

            try (PreparedStatement testPs = Connectiondb.Connection().prepareStatement(testQuery)) {
                testPs.setString(1, numeroCommande);
                testPs.executeQuery();
                // If we get here, the column exists
                workingQuery = "SELECT p.designation, cp.quantite, p." + priceColumn + " as prix_produit " +
                        "FROM commande_produits cp " +
                        "JOIN produits p ON cp.produit_id = p.reference " +
                        "JOIN commandes c ON cp.commande_id = c.id " +
                        "WHERE c.numero_commande = ? " +
                        "ORDER BY p.designation";
                System.out.println("Using price column: " + priceColumn);
                break;
            } catch (SQLException e) {
                // Column doesn't exist, try next one
                continue;
            }
        }

        // If no price column found in produits table, use price from commande_produits
        if (workingQuery == null) {
            System.out.println("No price column found in produits table, using prix_unitaire from commande_produits");
            workingQuery = "SELECT p.designation, cp.quantite, cp.prix_unitaire as prix_produit " +
                    "FROM commande_produits cp " +
                    "JOIN produits p ON cp.produit_id = p.reference " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "WHERE c.numero_commande = ? " +
                    "ORDER BY p.designation";
        }

        // Execute the working query
        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(workingQuery)) {
            ps.setString(1, numeroCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("designation");
                    int quantite = rs.getInt("quantite");
                    double prixUnitaire = rs.getDouble("prix_produit");

                    // Verify data before adding
                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                        produits.add(new ProduitFacture(nom, quantite, prixUnitaire));
                    }
                }
            }
        }

        // Debugging output
        System.out.println("Commande: " + numeroCommande + " - Produits trouvés: " + produits.size());
        for (ProduitFacture p : produits) {
            System.out.println("  - " + p.getNom() + " x" + p.getQuantite() + " @ " + p.getPrixUnitaire() + " DH");
        }

        return produits;
    }

    private ObservableList<ProduitFacture> loadProduitsParCommandeAvecChoixPrix(String numeroCommande, boolean utiliserPrixReel) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();

        String query;
        if (utiliserPrixReel) {
            // Utiliser le prix actuel du produit
            query = "SELECT p.designation, cp.quantite, p.prix as prix_unitaire " +
                    "FROM commande_produits cp " +
                    "JOIN produits p ON cp.produit_id = p.reference " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "WHERE c.numero_commande = ? " +
                    "ORDER BY p.designation";
        } else {
            // Utiliser le prix historique de la commande
            query = "SELECT p.designation, cp.quantite, cp.prix_unitaire " +
                    "FROM commande_produits cp " +
                    "JOIN produits p ON cp.produit_id = p.reference " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "WHERE c.numero_commande = ? " +
                    "ORDER BY p.designation";
        }

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setString(1, numeroCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("designation");
                    int quantite = rs.getInt("quantite");
                    double prixUnitaire = rs.getDouble("prix_unitaire");

                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                        produits.add(new ProduitFacture(nom, quantite, prixUnitaire));
                    }
                }
            }
        }

        return produits;
    }
    private void diagnosticPrix(String numeroCommande) {
        try {
            System.out.println("=== DIAGNOSTIC PRIX POUR COMMANDE: " + numeroCommande + " ===");

            String query = "SELECT p.designation, cp.quantite, " +
                    "cp.prix_unitaire as prix_commande, " +
                    "p.prix as prix_actuel, " +
                    "(cp.prix_unitaire - p.prix) as difference " +
                    "FROM commande_produits cp " +
                    "JOIN produits p ON cp.produit_id = p.reference " +
                    "JOIN commandes c ON cp.commande_id = c.id " +
                    "WHERE c.numero_commande = ?";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
                ps.setString(1, numeroCommande);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String designation = rs.getString("designation");
                        double prixCommande = rs.getDouble("prix_commande");
                        double prixActuel = rs.getDouble("prix_actuel");
                        double difference = rs.getDouble("difference");

                        System.out.println("Produit: " + designation);
                        System.out.println("  - Prix dans commande: " + prixCommande + " DH");
                        System.out.println("  - Prix actuel produit: " + prixActuel + " DH");
                        System.out.println("  - Différence: " + difference + " DH");

                        if (Math.abs(difference) > 0.01) {
                            System.out.println("  ⚠️  ATTENTION: Différence de prix détectée!");
                        }
                        System.out.println();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur diagnostic prix: " + e.getMessage());
        }
    }
    @FXML
    private void creerNouvelleFactureAvecChoixPrix() {
        if (clientsData.isEmpty()) {
            showWarning("Aucun client", "Veuillez d'abord créer un client avant de créer une facture.");
            return;
        }

        try {
            Clients selectedClient = showClientSelectionDialog();
            if (selectedClient == null) return;

            String numeroCommande = showCommandeSelectionDialog(selectedClient.getId());
            if (numeroCommande == null) return;

            // Diagnostic des prix avant de continuer
            diagnosticPrix(numeroCommande);

            // Demander à l'utilisateur quel prix utiliser
            Alert choixPrixAlert = new Alert(Alert.AlertType.CONFIRMATION);
            choixPrixAlert.setTitle("Choix du prix");
            choixPrixAlert.setHeaderText("Quel prix voulez-vous utiliser ?");
            choixPrixAlert.setContentText("Prix actuel du produit ou prix historique de la commande ?");

            ButtonType btnPrixActuel = new ButtonType("Prix actuel");
            ButtonType btnPrixCommande = new ButtonType("Prix de commande");
            ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            choixPrixAlert.getButtonTypes().setAll(btnPrixActuel, btnPrixCommande, btnAnnuler);

            Optional<ButtonType> choix = choixPrixAlert.showAndWait();
            if (choix.get() == btnAnnuler) return;

            boolean utiliserPrixReel = (choix.get() == btnPrixActuel);

            ObservableList<ProduitFacture> produits = loadProduitsParCommandeAvecChoixPrix(numeroCommande, utiliserPrixReel);

            if (produits.isEmpty()) {
                showWarning("Commande vide", "Cette commande ne contient aucun produit.");
                return;
            }

            // Calculate amounts
            double montantHT = produits.stream().mapToDouble(ProduitFacture::getTotal).sum();
            double tauxTVA = 20.0;
            double montantTVA = montantHT * tauxTVA / 100.0;
            double montantTTC = montantHT + montantTVA;

            String numeroFacture = generateNewInvoiceNumber();

            // Save to database
            String query = "INSERT INTO factures (numero_facture, numero_commande, ID_Client, date_facture, date_echeance, montant_ht, taux_tva, montant_tva, montant_ttc, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
                ps.setString(1, numeroFacture);
                ps.setString(2, numeroCommande);
                ps.setInt(3, selectedClient.getId());
                ps.setString(4, LocalDate.now().toString());
                ps.setString(5, LocalDate.now().plusDays(30).toString());
                ps.setDouble(6, montantHT);
                ps.setDouble(7, tauxTVA);
                ps.setDouble(8, montantTVA);
                ps.setDouble(9, montantTTC);
                ps.setString(10, "Brouillon");

                ps.executeUpdate();
            }

            loadFactures();
            updateFooterStats();

            String typePrix = utiliserPrixReel ? "prix actuels" : "prix de commande";
            showInfo("Succès", "Facture créée avec succès: " + numeroFacture + " (utilisant les " + typePrix + ")");

        } catch (Exception e) {
            showError("Erreur", "Impossible de créer la facture: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private ObservableList<ProduitFacture> loadProduitsParCommandeSimple(String numeroCommande) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();

        // Use only commande_produits table to avoid column issues
        String query = "SELECT cp.produit_id, cp.quantite, cp.prix_unitaire, " +
                "COALESCE(p.designation, cp.produit_id) as nom_produit " +
                "FROM commande_produits cp " +
                "LEFT JOIN produits p ON cp.produit_id = p.reference " +
                "JOIN commandes c ON cp.commande_id = c.id " +
                "WHERE c.numero_commande = ? " +
                "ORDER BY cp.produit_id";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setString(1, numeroCommande);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("nom_produit");
                    int quantite = rs.getInt("quantite");
                    double prixUnitaire = rs.getDouble("prix_unitaire");

                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                        produits.add(new ProduitFacture(nom, quantite, prixUnitaire));
                    }
                }
            }
        }

        return produits;
    }
    private void debugProduitTableStructure() {
        try {
            // Get table structure
            String query = "DESCRIBE produits";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("=== STRUCTURE TABLE PRODUITS ===");
                    while (rs.next()) {
                        String columnName = rs.getString("Field");
                        String columnType = rs.getString("Type");
                        System.out.println("Column: " + columnName + " - Type: " + columnType);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting table structure: " + e.getMessage());

            // Alternative: try to select all columns from one row
            try {
                String query2 = "SELECT * FROM produits LIMIT 1";
                try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query2)) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("=== SAMPLE PRODUITS DATA ===");
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                String columnName = rs.getMetaData().getColumnName(i);
                                Object value = rs.getObject(i);
                                System.out.println(columnName + ": " + value);
                            }
                        }
                    }
                }
            } catch (SQLException e2) {
                System.err.println("Error getting sample data: " + e2.getMessage());
            }
        }
    }
    private ObservableList<ProduitFacture> loadProduitsParFacture(String numeroFacture) throws SQLException {
        ObservableList<ProduitFacture> produits = FXCollections.observableArrayList();

        // Requête corrigée
        String query = "SELECT p.designation, cp.quantite, cp.prix_unitaire " +
                "FROM commande_produits cp " +
                "JOIN produits p ON cp.produit_id = p.reference " +
                "JOIN commandes c ON cp.commande_id = c.id " +
                "JOIN factures f ON c.numero_commande = f.numero_commande " +
                "WHERE f.numero_facture = ? " +
                "ORDER BY p.designation";

        try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(query)) {
            ps.setString(1, numeroFacture);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nom = rs.getString("designation");
                    int quantite = rs.getInt("quantite");
                    double prixUnitaire = rs.getDouble("prix_unitaire");

                    if (nom != null && !nom.trim().isEmpty() && quantite > 0 && prixUnitaire >= 0) {
                        produits.add(new ProduitFacture(nom, quantite, prixUnitaire));
                    }
                }
            }
        }

        return produits;
    }
    private void diagnosticClientCommandes(int clientId) {
        try {
            System.out.println("=== DIAGNOSTIC COMPLET CLIENT " + clientId + " ===");

            // Vérifier le client
            String clientQuery = "SELECT * FROM clients WHERE id = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(clientQuery)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Client trouvé: " + rs.getString("Nom") + " " + rs.getString("Prenom"));
                    } else {
                        System.out.println("ERREUR: Client non trouvé!");
                        return;
                    }
                }
            }

            // Compter les commandes
            String countQuery = "SELECT COUNT(*) as total FROM commandes WHERE ID_Client = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(countQuery)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    System.out.println("Nombre total de commandes: " + rs.getInt("total"));
                }
            }

            // Lister toutes les commandes
            String listQuery = "SELECT numero_commande, Date_Commande, etat FROM commandes WHERE ID_Client = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(listQuery)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("Liste des commandes:");
                    while (rs.next()) {
                        System.out.println("  - " + rs.getString("numero_commande") +
                                " | " + rs.getDate("Date_Commande") +
                                " | " + rs.getString("etat"));
                    }
                }
            }

            // Vérifier les factures pour ce client
            String factQuery = "SELECT f.numero_facture, f.numero_commande " +
                    "FROM factures f " +
                    "WHERE f.ID_Client = ?";
            try (PreparedStatement ps = Connectiondb.Connection().prepareStatement(factQuery)) {
                ps.setInt(1, clientId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("Factures existantes pour ce client:");
                    while (rs.next()) {
                        System.out.println("  - Facture: " + rs.getString("numero_facture") +
                                " | Commande: " + rs.getString("numero_commande"));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur diagnostic: " + e.getMessage());
        }
    }



    @FXML
    private void modifierFacture() {
        Facture selectedFacture = tableFactures.getSelectionModel().getSelectedItem();
        if (selectedFacture == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une facture à modifier.");
            return;
        }

        // Create a simple dialog for editing
        Dialog<Facture> dialog = new Dialog<>();
        dialog.setTitle("Modifier Facture");
        dialog.setHeaderText("Modifier les informations de la facture");

        // Set up the dialog buttons
        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numeroField = new TextField(selectedFacture.getNumero());
        TextField dateField = new TextField(selectedFacture.getDate());
        TextField clientField = new TextField(selectedFacture.getClient());
        TextField montantHTField = new TextField(selectedFacture.getMontantHT());
        TextField tvaField = new TextField(selectedFacture.getTva());
        TextField montantTTCField = new TextField(selectedFacture.getMontantTTC());
        TextField statutField = new TextField(selectedFacture.getStatut());
        TextField dateEcheanceField = new TextField(selectedFacture.getDateEcheance());

        grid.add(new Label("Numéro:"), 0, 0);
        grid.add(numeroField, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(dateField, 1, 1);
        grid.add(new Label("Client:"), 0, 2);
        grid.add(clientField, 1, 2);
        grid.add(new Label("Montant HT:"), 0, 3);
        grid.add(montantHTField, 1, 3);
        grid.add(new Label("TVA:"), 0, 4);
        grid.add(tvaField, 1, 4);
        grid.add(new Label("Montant TTC:"), 0, 5);
        grid.add(montantTTCField, 1, 5);
        grid.add(new Label("Statut:"), 0, 6);
        grid.add(statutField, 1, 6);
        grid.add(new Label("Date Échéance:"), 0, 7);
        grid.add(dateEcheanceField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Convert the result when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update the selected facture with new values
                selectedFacture.setNumero(numeroField.getText());
                selectedFacture.setDate(dateField.getText());
                selectedFacture.setClient(clientField.getText());
                selectedFacture.setMontantHT(montantHTField.getText());
                selectedFacture.setTva(tvaField.getText());
                selectedFacture.setMontantTTC(montantTTCField.getText());
                selectedFacture.setStatut(statutField.getText());
                selectedFacture.setDateEcheance(dateEcheanceField.getText());
                return selectedFacture;
            }
            return null;
        });

        Optional<Facture> result = dialog.showAndWait();
        if (result.isPresent()) {
            // Refresh the table to show updated data
            tableFactures.refresh();
            showInfo("Succès", "Facture modifiée avec succès!");
        }
    }

    @FXML
    private void supprimerFacture() {
        Facture selectedFacture = tableFactures.getSelectionModel().getSelectedItem();
        if (selectedFacture == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une facture à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la facture");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la facture " + selectedFacture.getNumero() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String query = "DELETE FROM factures WHERE numero_facture = '" + selectedFacture.getNumero() + "'";
                Connectiondb.Delete(query);
                loadFactures();
                updateFooterStats();
                showInfo("Succès", "Facture supprimée avec succès.");
            } catch (Exception e) {
                showError("Erreur", "Impossible de supprimer la facture: " + e.getMessage());
            }
        }
    }



    @FXML
    private void imprimerFacture() {
        Facture selected = tableFactures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Aucune facture sélectionnée", "Veuillez sélectionner une facture à imprimer.");
            return;
        }

        VBox factureLayout = new VBox(10);
        factureLayout.setPadding(new Insets(20));
        factureLayout.setStyle("-fx-border-color: black; -fx-border-width: 2;");

        Label titre = new Label("FACTURE");
        titre.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-alignment: center;");

        Label numero = new Label("N° Facture : " + selected.getNumero());
        Label date = new Label("Date : " + selected.getDate());
        Label client = new Label("Client : " + selected.getClient());

        VBox infosBox = new VBox(5, numero, date, client);
        infosBox.setPadding(new Insets(10, 0, 10, 0));

        TableView<ProduitFacture> produitsTable = new TableView<>();
        produitsTable.setPrefHeight(150);

        TableColumn<ProduitFacture, String> colProduit = new TableColumn<>("Produit");
        colProduit.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<ProduitFacture, Integer> colQuantite = new TableColumn<>("Quantité");
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        TableColumn<ProduitFacture, Double> colPrix = new TableColumn<>("Prix Unitaire");
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));

        TableColumn<ProduitFacture, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        produitsTable.getColumns().addAll(colProduit, colQuantite, colPrix, colTotal);

        try {
            ObservableList<ProduitFacture> produits = loadProduitsParFacture(selected.getNumero());
            produitsTable.setItems(produits);

            double totalGeneral = produits.stream().mapToDouble(ProduitFacture::getTotal).sum();
            Label totalLabel = new Label("Total général : " + totalGeneral + " DH");
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            factureLayout.getChildren().addAll(titre, infosBox, produitsTable, totalLabel);

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(tableFactures.getScene().getWindow())) {
                boolean success = job.printPage(factureLayout);
                if (success) {
                    job.endJob();
                    lblStatusMessage.setText("Facture imprimée: " + selected.getNumero());
                } else {
                    lblStatusMessage.setText("Échec de l'impression.");
                }
            }

        } catch (SQLException e) {
            showError("Erreur", "Erreur lors du chargement des produits: " + e.getMessage());
        }
    }


    @FXML
    private void exporterPDF() {
        Facture selected = tableFactures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Aucune facture sélectionnée", "Veuillez sélectionner une facture à exporter.");
            return;
        }

        Document document = new Document();
        try {
            String filename = "facture_" + selected.getNumero() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Titre
            document.add(new Paragraph("FACTURE"));
            document.add(new Paragraph("N° Facture : " + selected.getNumero()));
            document.add(new Paragraph("Date : " + selected.getDate()));
            document.add(new Paragraph("Client : " + selected.getClient()));
            document.add(new Paragraph("Date d'échéance : " + selected.getDateEcheance()));
            document.add(new Paragraph("\n"));

            // Tableau des montants
            PdfPTable table = new PdfPTable(2);
            table.addCell("Montant HT");
            table.addCell(selected.getMontantHT() + " DH");
            table.addCell("TVA");
            table.addCell(selected.getTva() + " %");
            table.addCell("Montant TTC");
            table.addCell(selected.getMontantTTC() + " DH");
            table.addCell("Statut");
            table.addCell(selected.getStatut());

            document.add(table);
            document.close();

            lblStatusMessage.setText("PDF exporté : " + filename);

            // فتح الملف تلقائيًا
            java.awt.Desktop.getDesktop().open(new File(filename));

        } catch (DocumentException | IOException e) {
            lblStatusMessage.setText("Erreur lors de l'export PDF.");
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void rechercherFacture() {
        String searchTerm = txtRechercheFacture.getText().trim();
        if (searchTerm.isEmpty()) {
            loadFactures();
            return;
        }

        facturesData.clear();
        try {
            String query = String.format(
                    "SELECT f.numero_facture, f.date_facture, f.date_echeance, f.montant_ht, " +
                            "f.montant_tva, f.montant_ttc, f.statut, " +
                            "CONCAT(c.Nom, ' ', c.Prenom) as client_nom " +
                            "FROM factures f " +
                            "JOIN clients c ON f.ID_Client = c.id " +
                            "WHERE f.numero_facture LIKE '%%%s%%' OR CONCAT(c.Nom, ' ', c.Prenom) LIKE '%%%s%%' " +
                            "ORDER BY f.date_facture DESC",
                    searchTerm, searchTerm);

            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Facture facture = new Facture(
                        rs.getString("numero_facture"),
                        rs.getString("date_facture"),
                        rs.getString("client_nom"),
                        String.format("%.2f", rs.getDouble("montant_ht")),
                        String.format("%.2f", rs.getDouble("montant_tva")),
                        String.format("%.2f", rs.getDouble("montant_ttc")),
                        rs.getString("statut"),
                        rs.getString("date_echeance")
                );
                facturesData.add(facture);
            }
        } catch (SQLException e) {
            showError("Erreur de recherche", "Impossible de rechercher les factures: " + e.getMessage());
        }
    }

    // Clients Action Handlers
    @FXML
    private void creerNouveauClient() {
        showClientDialog(null);
    }

    @FXML
    private void modifierClient() {
        Clients selectedClient = tableClients.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un client à modifier.");
            return;
        }

        showClientDialog(selectedClient);
    }

    @FXML
    private void supprimerClient() {
        Clients selectedClient = tableClients.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un client à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le client");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le client " + selectedClient.getNom() + " " + selectedClient.getPrenom() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String query = "DELETE FROM clients WHERE id = " + selectedClient.getId();
                Connectiondb.Delete(query);
                loadClients();
                showInfo("Succès", "Client supprimé avec succès.");
            } catch (Exception e) {
                showError("Erreur", "Impossible de supprimer le client: " + e.getMessage());
            }
        }
    }

    @FXML
    private void rechercherClient() {
        String searchTerm = txtRechercheClient.getText().trim();
        if (searchTerm.isEmpty()) {
            loadClients();
            return;
        }

        clientsData.clear();
        try {
            String query = String.format(
                    "SELECT * FROM clients " +
                            "WHERE Nom LIKE '%%%s%%' OR Prenom LIKE '%%%s%%' OR Email LIKE '%%%s%%' OR Telephone LIKE '%%%s%%' " +
                            "ORDER BY Nom, Prenom",
                    searchTerm, searchTerm, searchTerm, searchTerm);

            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Clients client = new Clients(
                        rs.getInt("id"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Telephone"),
                        rs.getString("Email")
                );
                clientsData.add(client);
            }
        } catch (SQLException e) {
            showError("Erreur de recherche", "Impossible de rechercher les clients: " + e.getMessage());
        }
    }

    // Helper Methods
    private String generateNewInvoiceNumber() {
        try {
            // First, try to find the highest existing invoice number
            String query = "SELECT numero_facture FROM factures WHERE numero_facture LIKE 'FAC-2025-%' ORDER BY numero_facture DESC LIMIT 1";
            ResultSet rs = Connectiondb.Dispaly(query);

            int nextNum = 1;
            if (rs.next()) {
                String lastInvoice = rs.getString("numero_facture");
                // Extract the number part from "FAC-2025-XXX"
                String numberPart = lastInvoice.substring(9); // Remove "FAC-2025-"
                try {
                    nextNum = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNum = 1;
                }
            }

            // Generate the new invoice number
            String newInvoiceNumber = String.format("FAC-2025-%03d", nextNum);

            // Double-check that this number doesn't already exist
            String checkQuery = "SELECT COUNT(*) as count FROM factures WHERE numero_facture = '" + newInvoiceNumber + "'";
            ResultSet checkRs = Connectiondb.Dispaly(checkQuery);
            checkRs.next();

            // If it exists, increment until we find a unique one
            while (checkRs.getInt("count") > 0) {
                nextNum++;
                newInvoiceNumber = String.format("FAC-2025-%03d", nextNum);
                checkQuery = "SELECT COUNT(*) as count FROM factures WHERE numero_facture = '" + newInvoiceNumber + "'";
                checkRs = Connectiondb.Dispaly(checkQuery);
                checkRs.next();
            }

            return newInvoiceNumber;

        } catch (SQLException e) {
            System.err.println("Error generating invoice number: " + e.getMessage());
            // Fallback: use timestamp-based number
            long timestamp = System.currentTimeMillis() % 1000;
            return String.format("FAC-2025-%03d", (int)timestamp);
        }
    }

    private Clients showClientSelectionDialog() {
        Dialog<Clients> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner un client");
        dialog.setHeaderText("Choisissez un client pour cette facture");

        ButtonType selectButtonType = new ButtonType("Sélectionner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ListView<Clients> listView = new ListView<>();
        listView.setItems(clientsData);
        listView.setCellFactory(param -> new ListCell<Clients>() {
            @Override
            protected void updateItem(Clients item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom() + " - " + item.getEmail());
                }
            }
        });

        dialog.getDialogPane().setContent(listView);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Clients> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void showClientDialog(Clients client) {
        Dialog<Clients> dialog = new Dialog<>();
        dialog.setTitle(client == null ? "Nouveau Client" : "Modifier Client");
        dialog.setHeaderText(client == null ? "Créer un nouveau client" : "Modifier les informations du client");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        TextField txtNom = new TextField(client != null ? client.getNom() : "");
        TextField txtPrenom = new TextField(client != null ? client.getPrenom() : "");
        TextField txtTelephone = new TextField(client != null ? client.getTelephone() : "");
        TextField txtEmail = new TextField(client != null ? client.getEmail() : "");

        vbox.getChildren().addAll(
                new Label("Nom:"), txtNom,
                new Label("Prénom:"), txtPrenom,
                new Label("Téléphone:"), txtTelephone,
                new Label("Email:"), txtEmail
        );

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (client == null) {
                        // Create new client
                        String query = String.format(
                                "INSERT INTO clients (Nom, Prenom, Telephone, Email) " +
                                        "VALUES ('%s', '%s', '%s', '%s')",
                                txtNom.getText().trim(),
                                txtPrenom.getText().trim(),
                                txtTelephone.getText().trim(),
                                txtEmail.getText().trim()
                        );
                        Connectiondb.Insert(query);
                    } else {
                        // Update existing client
                        String query = String.format(
                                "UPDATE clients SET Nom='%s', Prenom='%s', Telephone='%s', Email='%s' " +
                                        "WHERE id=%d",
                                txtNom.getText().trim(),
                                txtPrenom.getText().trim(),
                                txtTelephone.getText().trim(),
                                txtEmail.getText().trim(),
                                client.getId()
                        );
                        Connectiondb.Upadate(query);
                    }
                    loadClients();
                    return new Clients(0, txtNom.getText(), txtPrenom.getText(), txtTelephone.getText(), txtEmail.getText());
                } catch (Exception e) {
                    showError("Erreur", "Impossible d'enregistrer le client: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateFactureInfo(Facture facture) {
        if (facture == null) {
            lblFactureInfo.setText("Sélectionnez une facture pour voir les détails");
        } else {
            lblFactureInfo.setText(String.format(
                    "Facture: %s | Client: %s | Montant TTC: %s DH | Statut: %s | Échéance: %s",
                    facture.getNumero(), facture.getClient(), facture.getMontantTTC(),
                    facture.getStatut(), facture.getDateEcheance()
            ));
        }
    }

    private void updateClientInfo(Clients client) {
        if (client == null) {
            lblClientInfo.setText("Sélectionnez un client pour voir les détails");
        } else {
            lblClientInfo.setText(String.format(
                    "Client: %s %s | Téléphone: %s | Email: %s",
                    client.getNom(), client.getPrenom(), client.getTelephone(), client.getEmail()
            ));
        }
    }

    private void updateFooterStats() {
        try {
            // Count total invoices
            ResultSet rs = Connectiondb.Dispaly("SELECT COUNT(*) as total FROM factures");
            rs.next();
            int totalFactures = rs.getInt("total");
            lblTotalFactures.setText("Total factures: " + totalFactures);

            // Count paid invoices
            rs = Connectiondb.Dispaly("SELECT COUNT(*) as paid FROM factures WHERE statut = 'Payée'");
            rs.next();
            int facturesPayees = rs.getInt("paid");
            lblFacturesPayeesFooter.setText("Payées: " + facturesPayees);

            // Count unpaid invoices
            rs = Connectiondb.Dispaly("SELECT COUNT(*) as unpaid FROM factures WHERE statut != 'Payée'");
            rs.next();
            int facturesImpayees = rs.getInt("unpaid");
            lblFacturesImpayeesFooter.setText("Impayées: " + facturesImpayees);

            // Calculate total revenue
            rs = Connectiondb.Dispaly("SELECT SUM(montant_ttc) as total_ca FROM factures WHERE statut = 'Payée'");
            rs.next();
            double caTotal = rs.getDouble("total_ca");
            lblCATotal.setText(String.format("CA total: %.2f DH", caTotal));

            // Last update time
            lblDerniereMAJ.setText("Dernière MAJ: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        } catch (SQLException e) {
            System.err.println("Error updating footer stats: " + e.getMessage());
        }
    }

    // Utility Methods for Dialogs
    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}