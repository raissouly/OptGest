package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

// Pour PDF (iText)
// Pour fichiers
// Pour ouvrir fichier PDF après génération
// Pour les dates

public class CommandeController implements Initializable {

    // FXML Controls
    @FXML private TextField txtNumeroCommande;
    @FXML private DatePicker dateCommande;
    @FXML private ComboBox<Clients> comboClient;
    @FXML private ComboBox<Produit> comboProduit;
    @FXML private Spinner<Integer> spinnerQuantite;
    @FXML private Spinner<Integer> spinnerDelaiFabrication;

    @FXML private TextField txtPrixUnitaire;
    @FXML private ComboBox<String> comboEtatCommande;
    @FXML private ComboBox<String> comboPriorite;
    @FXML private DatePicker dateLivraison;
    @FXML private TextArea txtNotes;
    @FXML private Label lblTotal;

    // Buttons
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnNouveau;

    // Filters
    @FXML private ComboBox<String> filtreEtat;
    @FXML private ComboBox<Clients> filtreClient;
    @FXML private TextField txtRecherche;

    // Table
    @FXML private TableView<Commande> tableCommandes;
    @FXML private TableColumn<Commande, Integer> colNumero;
    @FXML private TableColumn<Commande, String> colDate;
    @FXML private TableColumn<Commande, String> colClient;
    @FXML private TableColumn<Commande, String> colProduit;
    @FXML private TableColumn<Commande, Integer> colQuantite;
    @FXML private TableColumn<Commande, Double> colPrixUnitaire;
    @FXML private TableColumn<Commande, Double> colTotal;
    @FXML private TableColumn<Commande, String> colEtat;
    @FXML private TableColumn<Commande, Integer> colDelaiFab;
    @FXML private TableColumn<Commande, String> colDateLivraison;
    @FXML private TableColumn<Commande, String> colPriorite;

    // Status Labels
    @FXML private Label lblStatut;
    @FXML private Label lblNombreCommandes;
    @FXML private Label lblCommandesEnCours;
    @FXML private Label lblCommandesPrete;
    @FXML private Label lblCommandesLivrees;

    // Observable Lists
    private ObservableList<Commande> commandesList = FXCollections.observableArrayList();
    private ObservableList<Commande> commandesListFiltered = FXCollections.observableArrayList();
    private ObservableList<Clients> clientsList = FXCollections.observableArrayList();
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    // Selected Command
    private Commande commandeSelectionnee;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // 1. First, verify critical FXML components are properly injected
            if (txtPrixUnitaire == null) {
                System.err.println("CRITICAL ERROR: txtPrixUnitaire is NULL after FXML load. Check fx:id='txtPrixUnitaire' in FXML.");
                afficherErreur("Erreur d'initialisation", "Le champ de prix unitaire n'a pas été initialisé correctement. Veuillez vérifier votre fichier FXML (fx:id='txtPrixUnitaire').");
                return;
            }

            // 2. Configure UI components that don't depend on data or listeners
            configureSpinners();
            initializeComboBoxes();
            configureTableColumns();

            // 3. Load data from database
            chargerClients();
            chargerProduits();
            chargerCommandes();

            // 4. Configure listeners AFTER all data is loaded and UI is set up
            configureListeners();

            // 5. Set initial form state (this will now safely trigger listeners)
            nouveauCommande();

            System.out.println("Initialisation terminée avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur critique", "Une erreur est survenue lors de l'initialisation: " + e.getMessage());
        }
    }
    private void configureSpinners() {
        try {
            // Spinner for Quantity (1 to 999, default 1)
            SpinnerValueFactory.IntegerSpinnerValueFactory quantiteFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
            spinnerQuantite.setValueFactory(quantiteFactory);
            spinnerQuantite.setEditable(true);

            // Spinner for Fabrication Delay (1 to 30 days, default 1)
            SpinnerValueFactory.IntegerSpinnerValueFactory delaiFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1);
            spinnerDelaiFabrication.setValueFactory(delaiFactory);
            spinnerDelaiFabrication.setEditable(true);

            System.out.println("Spinners configurés avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des spinners: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur de configuration", "Impossible de configurer les spinners: " + e.getMessage());
        }
    }

    private void initializeComboBoxes() {
        try {
            comboEtatCommande.getItems().addAll("⏳ En cours", "✅ Prête", "🚚 Livrée");
            comboEtatCommande.setValue("⏳ En cours");

            comboPriorite.getItems().addAll("🔴 Urgente", "🟡 Normale", "🟢 Faible");
            comboPriorite.setValue("🟡 Normale");

            filtreEtat.getItems().addAll("Tous les états", "En cours", "Prête", "Livrée");
            filtreEtat.setValue("Tous les états");

            // Custom string converter for Clients ComboBoxes
            comboClient.setConverter(new StringConverter<Clients>() {
                @Override
                public String toString(Clients client) {
                    return client != null ? client.getNom() + " " + client.getPrenom() : "";
                }
                @Override
                public Clients fromString(String string) {
                    return null; // Not needed for selection
                }
            });
            filtreClient.setConverter(new StringConverter<Clients>() {
                @Override
                public String toString(Clients client) {
                    // For the "Tous les clients" dummy entry
                    if (client != null && client.getId() == 0) {
                        return "Tous les clients";
                    }
                    return client != null ? client.getNom() + " " + client.getPrenom() : "";
                }
                @Override
                public Clients fromString(String string) {
                    return null;
                }
            });

            // Custom string converter for Produit ComboBox
            comboProduit.setConverter(new StringConverter<Produit>() {
                @Override
                public String toString(Produit produit) {
                    return produit != null ? produit.getDesignation() : "";
                }
                @Override
                public Produit fromString(String string) {
                    return null; // Not needed for selection
                }
            });


            System.out.println("ComboBoxes initialisés avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des ComboBoxes: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur de configuration", "Impossible de configurer les ComboBoxes: " + e.getMessage());
        }
    }

    private void configureTableColumns() {
        try {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("id"));
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
            colClient.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
            colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
            colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
            colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
            colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
            colDelaiFab.setCellValueFactory(new PropertyValueFactory<>("delaiFabrication"));
            colDateLivraison.setCellValueFactory(new PropertyValueFactory<>("dateLivraison"));
            colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));

            // Custom cell factory for currency formatting
            colPrixUnitaire.setCellFactory(column -> new TableCell<Commande, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((empty || item == null) ? null : String.format("%.2f DH", item));
                }
            });

            colTotal.setCellFactory(column -> new TableCell<Commande, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((empty || item == null) ? null : String.format("%.2f DH", item));
                }
            });

            tableCommandes.setItems(commandesListFiltered);
            System.out.println("Colonnes du tableau configurées avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des colonnes: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur de configuration", "Impossible de configurer les colonnes du tableau: " + e.getMessage());
        }
    }

    private void configureListeners() {
        // Table selection listener
        if (tableCommandes != null) {
            tableCommandes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    commandeSelectionnee = newSelection;
                    remplirFormulaire(newSelection);
                }
            });
        }

        // Spinner quantity listener - AMÉLIORÉ
        if (spinnerQuantite != null) {
            spinnerQuantite.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    calculerTotal();
                    // NOUVEAU : Mise à jour immédiate de la table si une commande est sélectionnée
                    if (commandeSelectionnee != null) {
                        Platform.runLater(() -> rafraichirTableCommandes());
                    }
                }
            });
        }

        // Price text field listener - AMÉLIORÉ
        if (txtPrixUnitaire != null) {
            txtPrixUnitaire.textProperty().addListener((obs, oldVal, newVal) -> {
                calculerTotal();
                // NOUVEAU : Mise à jour immédiate de la table si une commande est sélectionnée
                if (commandeSelectionnee != null) {
                    Platform.runLater(() -> rafraichirTableCommandes());
                }
            });
        }

        // Product combo listener - AMÉLIORÉ
        if (comboProduit != null) {
            comboProduit.setOnAction(e -> {
                Produit produitSelectionne = comboProduit.getValue();
                if (produitSelectionne != null && txtPrixUnitaire != null) {
                    txtPrixUnitaire.setText(String.valueOf(produitSelectionne.getPrixUnitaire()));
                    calculerTotal();
                    // NOUVEAU : Mise à jour immédiate de la table
                    if (commandeSelectionnee != null) {
                        Platform.runLater(() -> rafraichirTableCommandes());
                    }
                } else if (txtPrixUnitaire != null) {
                    txtPrixUnitaire.clear();
                    calculerTotal();
                }
            });
        }

        // Delay spinner listener
        if (spinnerDelaiFabrication != null) {
            spinnerDelaiFabrication.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (dateCommande != null && dateCommande.getValue() != null &&
                        newVal != null && dateLivraison != null) {
                    dateLivraison.setValue(dateCommande.getValue().plusDays(newVal));
                }
            });
        }

        // Date Commande listener
        if (dateCommande != null) {
            dateCommande.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null && spinnerDelaiFabrication != null &&
                        spinnerDelaiFabrication.getValue() != null && dateLivraison != null) {
                    dateLivraison.setValue(newDate.plusDays(spinnerDelaiFabrication.getValue()));
                } else if (dateLivraison != null) {
                    dateLivraison.setValue(null);
                }
            });
        }

        // Filter listeners
        if (filtreEtat != null) {
            filtreEtat.setOnAction(e -> appliquerFiltres());
        }
        if (filtreClient != null) {
            filtreClient.setOnAction(e -> appliquerFiltres());
        }
        if (txtRecherche != null) {
            txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        }
    }
    private void chargerClients() {
        clientsList.clear();
        String sql = "SELECT * FROM clients ORDER BY Nom, Prenom";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Clients client = new Clients(
                        rs.getInt("id"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Telephone"),
                        rs.getString("Email")
                );
                clientsList.add(client);
            }

            comboClient.setItems(clientsList);
            // Add a "Tous les clients" option to the filter
            ObservableList<Clients> filterClients = FXCollections.observableArrayList(clientsList);
            filterClients.add(0, new Clients(0, "Tous les clients", "", "", "")); // Dummy client for filter
            filtreClient.setItems(filterClients);
            filtreClient.setValue(filterClients.get(0)); // Select "Tous les clients" by default

        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", "Erreur lors du chargement des clients: " + e.getMessage());
        }
    }

    private void chargerProduits() {
        produitsList.clear();
        String sql = "SELECT * FROM produits ORDER BY designation";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Produit produit = new Produit(
                        rs.getString("reference"),
                        rs.getString("designation"),
                        rs.getString("taille"),
                        rs.getInt("quantite"), // This is stock quantity, not order quantity
                        rs.getDouble("prix_unitaire"),
                        rs.getString("couleur"),
                        rs.getString("type"),
                        rs.getString("marque"),
                        rs.getInt("stock_minimal")
                );
                produitsList.add(produit);
            }

            comboProduit.setItems(produitsList);

        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", "Erreur lors du chargement des produits: " + e.getMessage());
        }
    }

    private void chargerCommandes() {
        commandesList.clear();
        String sql = "SELECT c.*, " +
                "cl.Nom, cl.Prenom, " +
                "p.designation, p.prix_unitaire AS produit_prix_unitaire, " + // Alias for clarity
                "COALESCE(c.quantite, 1) as quantite_cmd, " + // Handle potential NULLs in DB with default
                "COALESCE(c.prix_unitaire, p.prix_unitaire) as prix_cmd, " + // Handle potential NULLs in DB with default from product
                "COALESCE(c.etat, 'En cours') as etat_cmd, " +
                "COALESCE(c.priorite, 'Normale') as priorite_cmd, " +
                "COALESCE(c.delai_fabrication, 1) as delai_cmd " +
                "FROM commandes c " +
                "JOIN clients cl ON c.ID_Client = cl.id " +
                "JOIN produits p ON c.ID_produits = p.reference " +
                "ORDER BY c.Date_Commande DESC, c.id DESC"; // Order by ID to ensure consistent sorting

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setIdClient(rs.getInt("ID_Client"));
                commande.setDateCommande(rs.getDate("Date_Commande").toLocalDate().toString());
                commande.setIdProduit(rs.getString("ID_produits"));
                commande.setNomClient(rs.getString("Nom") + " " + rs.getString("Prenom"));
                commande.setNomProduit(rs.getString("designation"));

                int quantite = rs.getInt("quantite_cmd");
                double prixUnitaire = rs.getDouble("prix_cmd");
                commande.setQuantite(quantite);
                commande.setPrixUnitaire(prixUnitaire);
                commande.setTotal(quantite * prixUnitaire);
                commande.setEtat(rs.getString("etat_cmd"));
                commande.setPriorite(rs.getString("priorite_cmd"));
                commande.setDelaiFabrication(rs.getInt("delai_cmd"));

                // Date de livraison calculée
                LocalDate dateCmd = rs.getDate("Date_Commande").toLocalDate();
                LocalDate dateLiv = dateCmd.plusDays(rs.getInt("delai_cmd"));
                commande.setDateLivraison(dateLiv.toString());
                commande.setNotes(rs.getString("notes"));


                commandesList.add(commande);
            }

            appliquerFiltres();
            mettreAJourStatut();

        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", "Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    @FXML
    private void ajouterCommande() {
        // Enhanced validation with detailed error reporting
        if (!validerFormulaire()) {
            return;
        }

        // Additional null checks before database operation
        try {
            // Validate client selection
            if (comboClient == null || comboClient.getValue() == null) {
                afficherErreur("Erreur de saisie", "Veuillez sélectionner un client");
                return;
            }

            // Validate product selection
            if (comboProduit == null || comboProduit.getValue() == null) {
                afficherErreur("Erreur de saisie", "Veuillez sélectionner un produit");
                return;
            }

            // Validate date selection
            if (dateCommande == null || dateCommande.getValue() == null) {
                afficherErreur("Erreur de saisie", "Veuillez sélectionner une date de commande");
                return;
            }

            // Validate spinner values
            if (spinnerQuantite == null || spinnerQuantite.getValue() == null) {
                afficherErreur("Erreur de saisie", "La quantité n'est pas définie");
                return;
            }

            if (spinnerDelaiFabrication == null || spinnerDelaiFabrication.getValue() == null) {
                afficherErreur("Erreur de saisie", "Le délai de fabrication n'est pas défini");
                return;
            }

            // Validate price field
            if (txtPrixUnitaire == null || txtPrixUnitaire.getText() == null || txtPrixUnitaire.getText().trim().isEmpty()) {
                afficherErreur("Erreur de saisie", "Veuillez saisir le prix unitaire");
                return;
            }

            // Validate combo box selections
            if (comboEtatCommande == null || comboEtatCommande.getValue() == null) {
                afficherErreur("Erreur de saisie", "Veuillez sélectionner l'état de la commande");
                return;
            }

            if (comboPriorite == null || comboPriorite.getValue() == null) {
                afficherErreur("Erreur de saisie", "Veuillez sélectionner la priorité");
                return;
            }

            // Parse price with error handling
            double prixUnitaire;
            try {
                prixUnitaire = Double.parseDouble(txtPrixUnitaire.getText().replace(",", "."));
                if (prixUnitaire <= 0) {
                    afficherErreur("Erreur de saisie", "Le prix unitaire doit être un nombre positif");
                    return;
                }
            } catch (NumberFormatException e) {
                afficherErreur("Erreur de saisie", "Le prix unitaire doit être un nombre valide");
                return;
            }

            // Now proceed with database insertion
            String sql = "INSERT INTO commandes " +
                    "(ID_Client, Date_Commande, ID_produits, quantite, prix_unitaire, " +
                    "etat, priorite, delai_fabrication, date_livraison, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = Connectiondb.Connection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                // Set parameters with validated values
                pstmt.setInt(1, comboClient.getValue().getId());
                pstmt.setDate(2, Date.valueOf(dateCommande.getValue()));
                pstmt.setString(3, comboProduit.getValue().getReference());
                pstmt.setInt(4, spinnerQuantite.getValue());
                pstmt.setDouble(5, prixUnitaire);
                pstmt.setString(6, extraireEtat(comboEtatCommande.getValue()));
                pstmt.setString(7, extrairePriorite(comboPriorite.getValue()));
                pstmt.setInt(8, spinnerDelaiFabrication.getValue());

                // Handle delivery date (can be null)
                if (dateLivraison != null && dateLivraison.getValue() != null) {
                    pstmt.setDate(9, Date.valueOf(dateLivraison.getValue()));
                } else {
                    pstmt.setNull(9, Types.DATE);
                }

                // Handle notes (can be null or empty)
                String notes = (txtNotes != null && txtNotes.getText() != null) ? txtNotes.getText() : "";
                pstmt.setString(10, notes);

                int result = pstmt.executeUpdate();

                if (result > 0) {
                    afficherSucces("Commande ajoutée avec succès !");
                    chargerCommandes();
                    nouveauCommande();
                } else {
                    afficherErreur("Erreur d'ajout", "Aucune ligne n'a été ajoutée à la base de données");
                }

            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de l'ajout de la commande: " + e.getMessage());
                e.printStackTrace();
                afficherErreur("Erreur de base de données", "Erreur lors de l'ajout de la commande: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de l'ajout de la commande: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur inattendue", "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }





    @FXML
    private void modifierCommande() {
        // Check if a command is selected
        if (commandeSelectionnee == null) {
            afficherAvertissement("Sélection requise", "Veuillez sélectionner une commande à modifier");
            return;
        }

        // Validate form
        if (!validerFormulaire()) {
            return;
        }

        // Parse and validate price
        String prixText = txtPrixUnitaire.getText();
        double prixUnitaire;
        try {
            prixUnitaire = Double.parseDouble(prixText.replace(",", "."));
            if (prixUnitaire <= 0) {
                afficherErreur("Erreur de saisie", "Le prix unitaire doit être un nombre positif");
                return;
            }
        } catch (NumberFormatException e) {
            afficherErreur("Erreur de saisie", "Le prix unitaire doit être un nombre valide");
            return;
        }

        // NOUVEAU : Mettre à jour l'objet local immédiatement
        int nouvelleQuantite = spinnerQuantite.getValue();
        double nouveauTotal = nouvelleQuantite * prixUnitaire;

        commandeSelectionnee.setQuantite(nouvelleQuantite);
        commandeSelectionnee.setPrixUnitaire(prixUnitaire);
        commandeSelectionnee.setTotal(nouveauTotal);

        // Rafraîchir la table immédiatement
        if (tableCommandes != null) {
            tableCommandes.refresh();
        }

        // Proceed with database update
        String sql = "UPDATE commandes SET " +
                "ID_Client = ?, Date_Commande = ?, ID_produits = ?, " +
                "quantite = ?, prix_unitaire = ?, etat = ?, priorite = ?, " +
                "delai_fabrication = ?, date_livraison = ?, notes = ? " +
                "WHERE id = ?";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters
            pstmt.setInt(1, comboClient.getValue().getId());
            pstmt.setDate(2, Date.valueOf(dateCommande.getValue()));
            pstmt.setString(3, comboProduit.getValue().getReference());
            pstmt.setInt(4, nouvelleQuantite);
            pstmt.setDouble(5, prixUnitaire);
            pstmt.setString(6, extraireEtat(comboEtatCommande.getValue()));
            pstmt.setString(7, extrairePriorite(comboPriorite.getValue()));
            pstmt.setInt(8, spinnerDelaiFabrication.getValue());

            // Handle delivery date
            if (dateLivraison != null && dateLivraison.getValue() != null) {
                pstmt.setDate(9, Date.valueOf(dateLivraison.getValue()));
            } else {
                pstmt.setNull(9, Types.DATE);
            }

            // Handle notes
            String notes = (txtNotes != null && txtNotes.getText() != null) ? txtNotes.getText() : "";
            pstmt.setString(10, notes);
            pstmt.setInt(11, commandeSelectionnee.getId());

            int result = pstmt.executeUpdate();

            if (result > 0) {
                afficherSucces("Commande modifiée avec succès !");

                // NOUVEAU : Pas besoin de recharger toute la liste, juste rafraîchir la table
                mettreAJourStatut();

            } else {
                afficherErreur("Erreur de modification", "Aucune ligne n'a été modifiée dans la base de données");
                // Revert changes if database update failed
                chargerCommandes();
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la modification: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur de base de données", "Erreur lors de la modification de la commande: " + e.getMessage());
            // Revert changes if database update failed
            chargerCommandes();
        }
    }




    @FXML
    private void supprimerCommande() {
        if (commandeSelectionnee == null) {
            afficherAvertissement("Sélection requise", "Veuillez sélectionner une commande à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la commande #" + commandeSelectionnee.getId());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ? Cette action est irréversible.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM commandes WHERE id = ?";

            try (Connection conn = Connectiondb.Connection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, commandeSelectionnee.getId());
                int deleteResult = pstmt.executeUpdate();

                if (deleteResult > 0) {
                    afficherSucces("Commande supprimée avec succès !");
                    chargerCommandes();
                    nouveauCommande();
                } else {
                    afficherErreur("Échec de suppression", "La commande n'a pas pu être supprimée.");
                }

            } catch (SQLException e) {
                afficherErreur("Erreur de suppression", "Erreur lors de la suppression de la commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void nouveauCommande() {
        // Clear table selection first
        if (tableCommandes != null) {
            tableCommandes.getSelectionModel().clearSelection();
        }
        commandeSelectionnee = null;

        // Clear text fields
        if (txtNumeroCommande != null) txtNumeroCommande.clear();
        if (txtPrixUnitaire != null) txtPrixUnitaire.clear();
        if (txtNotes != null) txtNotes.clear();

        // Reset date fields
        if (dateCommande != null) dateCommande.setValue(LocalDate.now());
        if (dateLivraison != null) dateLivraison.setValue(null);

        // Reset combo box selections - clear first to avoid triggering listeners with invalid states
        if (comboClient != null) {
            comboClient.getSelectionModel().clearSelection();
        }
        if (comboProduit != null) {
            comboProduit.getSelectionModel().clearSelection();
        }

        // Set default values for combo boxes
        if (comboEtatCommande != null) {
            comboEtatCommande.setValue("⏳ En cours");
        }
        if (comboPriorite != null) {
            comboPriorite.setValue("🟡 Normale");
        }

        // Reset spinners
        if (spinnerQuantite != null && spinnerQuantite.getValueFactory() != null) {
            spinnerQuantite.getValueFactory().setValue(1);
        }
        if (spinnerDelaiFabrication != null && spinnerDelaiFabrication.getValueFactory() != null) {
            spinnerDelaiFabrication.getValueFactory().setValue(1);
        }

        // Reset labels
        if (lblTotal != null) {
            lblTotal.setText("0.00 DH");
        }

        // Calculate delivery date based on current values
        if (dateCommande != null && dateCommande.getValue() != null &&
                spinnerDelaiFabrication != null && spinnerDelaiFabrication.getValue() != null &&
                dateLivraison != null) {
            dateLivraison.setValue(dateCommande.getValue().plusDays(spinnerDelaiFabrication.getValue()));
        }

        afficherSucces("Formulaire réinitialisé.");
    }
    @FXML
    private void filtrerCommandes() {
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        commandesListFiltered.clear();

        for (Commande commande : commandesList) {
            boolean inclure = true;

            // Filter by state
            if (filtreEtat.getValue() != null && !filtreEtat.getValue().equals("Tous les états")) {
                // Remove emoji from comparison to match database string
                String cleanedFilterState = filtreEtat.getValue().replaceAll("[⏳✅🚚]", "").trim();
                String cleanedCommandeState = commande.getEtat().replaceAll("[⏳✅🚚]", "").trim();
                if (!cleanedCommandeState.equalsIgnoreCase(cleanedFilterState)) {
                    inclure = false;
                }
            }

            // Filter by client
            if (filtreClient.getValue() != null && filtreClient.getValue().getId() != 0) { // Check for dummy client
                if (commande.getIdClient() != filtreClient.getValue().getId()) {
                    inclure = false;
                }
            }

            // Filter by search text
            if (txtRecherche.getText() != null && !txtRecherche.getText().trim().isEmpty()) {
                String recherche = txtRecherche.getText().toLowerCase();
                if (!String.valueOf(commande.getId()).contains(recherche) &&
                        !commande.getNomClient().toLowerCase().contains(recherche) &&
                        !commande.getNomProduit().toLowerCase().contains(recherche) &&
                        !commande.getEtat().toLowerCase().contains(recherche) && // Allow searching by state string directly
                        !commande.getPriorite().toLowerCase().contains(recherche)) { // Allow searching by priority
                    inclure = false;
                }
            }

            if (inclure) {
                commandesListFiltered.add(commande);
            }
        }
        mettreAJourStatut();
    }

    @FXML
    private void rafraichirListe() {
        chargerCommandes();
        afficherSucces("Liste des commandes actualisée !");
    }


    @FXML
    private void imprimerCommande() {
        if (commandeSelectionnee == null) {
            afficherAvertissement("Sélection requise", "Veuillez sélectionner une commande à imprimer");
            return;
        }

        try {
            // Créer le contenu à imprimer
            VBox contenuImpression = creerContenuImpression(commandeSelectionnee);

            // Initialiser l'imprimante
            PrinterJob job = PrinterJob.createPrinterJob();

            if (job != null && job.showPrintDialog(null)) {
                // Configurer les paramètres d'impression
                PageLayout pageLayout = job.getJobSettings().getPageLayout();

                // Ajuster la taille du contenu à la page
                double scaleX = pageLayout.getPrintableWidth() / contenuImpression.getBoundsInParent().getWidth();
                double scaleY = pageLayout.getPrintableHeight() / contenuImpression.getBoundsInParent().getHeight();
                double scale = Math.min(scaleX, scaleY);

                if (scale < 1.0) {
                    contenuImpression.setScaleX(scale);
                    contenuImpression.setScaleY(scale);
                }

                // Lancer l'impression
                boolean success = job.printPage(contenuImpression);

                if (success) {
                    job.endJob();
                    afficherSucces("Commande #" + commandeSelectionnee.getId() + " imprimée avec succès !");
                } else {
                    afficherErreur("Erreur d'impression", "Échec de l'impression de la commande");
                }
            }
        } catch (Exception e) {
            afficherErreur("Erreur d'impression", "Une erreur est survenue lors de l'impression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void exporterCommandes() {
        try {
            // Créer le sélecteur de fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les commandes");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
                    new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
            );

            // Définir le nom de fichier par défaut
            String nomFichier = "commandes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            fileChooser.setInitialFileName(nomFichier);

            // Afficher le dialogue de sauvegarde
            File fichier = fileChooser.showSaveDialog(null);

            if (fichier != null) {
                // Déterminer le format d'export basé sur l'extension
                String extension = getFileExtension(fichier);

                if (extension.equalsIgnoreCase("csv")) {
                    exporterEnCSV(fichier);
                } else {
                    exporterEnTexte(fichier);
                }

                afficherSucces("Export réussi !\nFichier sauvegardé : " + fichier.getAbsolutePath());
            }
        } catch (Exception e) {
            afficherErreur("Erreur d'exportation", "Une erreur est survenue lors de l'exportation : " + e.getMessage());
            e.printStackTrace();
        }
    }


// Méthodes utilitaires pour l'impression et l'exportation

    private VBox creerContenuImpression(Commande commande) {
        VBox contenu = new VBox(10);
        contenu.setStyle("-fx-padding: 20; -fx-background-color: white;");

        // Titre
        Text titre = new Text("FACTURE DE COMMANDE");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Informations de la commande
        Text infoCommande = new Text(
                "Numéro de commande : #" + commande.getId() + "\n" +
                        "Date de commande : " + commande.getDateCommande() + "\n" +
                        "Date de livraison : " + commande.getDateLivraison() + "\n" +
                        "État : " + commande.getEtat() + "\n" +
                        "Priorité : " + commande.getPriorite()
        );
        infoCommande.setFont(Font.font("Arial", 12));

        // Informations client
        Text infoClient = new Text(
                "CLIENT :\n" +
                        commande.getNomClient()
        );
        infoClient.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Détails produit
        Text detailsProduit = new Text(
                "DÉTAILS DU PRODUIT :\n" +
                        "Produit : " + commande.getNomProduit() + "\n" +
                        "Quantité : " + commande.getQuantite() + "\n" +
                        "Prix unitaire : " + String.format("%.2f DH", commande.getPrixUnitaire()) + "\n" +
                        "Total : " + String.format("%.2f DH", commande.getTotal())
        );
        detailsProduit.setFont(Font.font("Arial", 12));

        // Notes
        if (commande.getNotes() != null && !commande.getNotes().trim().isEmpty()) {
            Text notes = new Text("NOTES :\n" + commande.getNotes());
            notes.setFont(Font.font("Arial", 10));
            contenu.getChildren().add(notes);
        }

        // Date d'impression
        Text dateImpression = new Text(
                "\nDocument généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
        );
        dateImpression.setFont(Font.font("Arial", 8));

        contenu.getChildren().addAll(titre, infoCommande, infoClient, detailsProduit, dateImpression);

        return contenu;
    }

    private void exporterEnCSV(File fichier) throws IOException {
        try (FileWriter writer = new FileWriter(fichier)) {
            // En-têtes CSV
            writer.append("Numéro,Date Commande,Client,Produit,Quantité,Prix Unitaire,Total,État,Priorité,Délai Fabrication,Date Livraison,Notes\n");

            // Données des commandes (utilisez commandesListFiltered pour exporter les données filtrées)
            for (Commande commande : commandesListFiltered) {
                writer.append(String.valueOf(commande.getId())).append(",");
                writer.append(commande.getDateCommande()).append(",");
                writer.append("\"").append(commande.getNomClient()).append("\",");
                writer.append("\"").append(commande.getNomProduit()).append("\",");
                writer.append(String.valueOf(commande.getQuantite())).append(",");
                writer.append(String.valueOf(commande.getPrixUnitaire())).append(",");
                writer.append(String.valueOf(commande.getTotal())).append(",");
                writer.append("\"").append(commande.getEtat()).append("\",");
                writer.append("\"").append(commande.getPriorite()).append("\",");
                writer.append(String.valueOf(commande.getDelaiFabrication())).append(",");
                writer.append(commande.getDateLivraison()).append(",");
                writer.append("\"").append(commande.getNotes() != null ? commande.getNotes() : "").append("\"");
                writer.append("\n");
            }
        }
    }

    private void exporterEnTexte(File fichier) throws IOException {
        try (FileWriter writer = new FileWriter(fichier)) {
            writer.write("RAPPORT DES COMMANDES\n");
            writer.write("=" + repeterCaractere("=", 50) + "\n\n");
            writer.write("Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "\n");
            writer.write("Nombre de commandes : " + commandesListFiltered.size() + "\n\n");

            for (Commande commande : commandesListFiltered) {
                writer.write("COMMANDE #" + commande.getId() + "\n");
                writer.write("-" + repeterCaractere("-", 30) + "\n");
                writer.write("Date : " + commande.getDateCommande() + "\n");
                writer.write("Client : " + commande.getNomClient() + "\n");
                writer.write("Produit : " + commande.getNomProduit() + "\n");
                writer.write("Quantité : " + commande.getQuantite() + "\n");
                writer.write("Prix unitaire : " + String.format("%.2f DH", commande.getPrixUnitaire()) + "\n");
                writer.write("Total : " + String.format("%.2f DH", commande.getTotal()) + "\n");
                writer.write("État : " + commande.getEtat() + "\n");
                writer.write("Priorité : " + commande.getPriorite() + "\n");
                writer.write("Délai de fabrication : " + commande.getDelaiFabrication() + " jours\n");
                writer.write("Date de livraison : " + commande.getDateLivraison() + "\n");

                if (commande.getNotes() != null && !commande.getNotes().trim().isEmpty()) {
                    writer.write("Notes : " + commande.getNotes() + "\n");
                }

                writer.write("\n");
            }

            // Statistiques de fin
            writer.write("\n" + repeterCaractere("=", 50) + "\n");
            writer.write("STATISTIQUES\n");
            writer.write("=" + repeterCaractere("=", 50) + "\n");

            long enCours = commandesListFiltered.stream().filter(c -> extraireEtat(c.getEtat()).equalsIgnoreCase("En cours")).count();
            long prete = commandesListFiltered.stream().filter(c -> extraireEtat(c.getEtat()).equalsIgnoreCase("Prête")).count();
            long livrees = commandesListFiltered.stream().filter(c -> extraireEtat(c.getEtat()).equalsIgnoreCase("Livrée")).count();

            writer.write("Commandes en cours : " + enCours + "\n");
            writer.write("Commandes prêtes : " + prete + "\n");
            writer.write("Commandes livrées : " + livrees + "\n");

            double totalCA = commandesListFiltered.stream().mapToDouble(Commande::getTotal).sum();
            writer.write("Chiffre d'affaires total : " + String.format("%.2f DH", totalCA) + "\n");
        }
    }

    // Méthode utilitaire pour répéter un caractère (équivalent à String.repeat() en Java 8)
    private String repeterCaractere(String caractere, int nombre) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nombre; i++) {
            sb.append(caractere);
        }
        return sb.toString();
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private void calculerTotal() {
        try {
            // Get quantity, defaulting to 0 if spinnerQuantite or its value is null
            int quantite = (spinnerQuantite != null && spinnerQuantite.getValue() != null) ? spinnerQuantite.getValue() : 0;
            double prixUnitaire = 0.0;
            String prixText = (txtPrixUnitaire != null) ? txtPrixUnitaire.getText() : "";

            if (prixText != null && !prixText.trim().isEmpty()) {
                try {
                    prixUnitaire = Double.parseDouble(prixText.replace(",", "."));
                } catch (NumberFormatException e) {
                    System.err.println("Format de prix invalide dans calculerTotal: " + prixText);
                    prixUnitaire = 0.0;
                }
            }

            double total = quantite * prixUnitaire;

            // Mettre à jour le label
            if (lblTotal != null) {
                lblTotal.setText(String.format("%.2f DH", total));
            }

            // NOUVEAU : Mettre à jour l'objet commande sélectionné si il existe
            if (commandeSelectionnee != null) {
                commandeSelectionnee.setQuantite(quantite);
                commandeSelectionnee.setPrixUnitaire(prixUnitaire);
                commandeSelectionnee.setTotal(total);

                // Rafraîchir la table pour refléter les changements
                if (tableCommandes != null) {
                    tableCommandes.refresh();
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur inattendue dans calculerTotal: " + e.getMessage());
            e.printStackTrace();
            if (lblTotal != null) {
                lblTotal.setText("Erreur");
            }
        }
    }

    private void remplirFormulaire(Commande commande) {
        if (commande == null) {
            nouveauCommande(); // Clear the form if no command is selected
            return;
        }
        try {
            if (txtNumeroCommande != null) txtNumeroCommande.setText(String.valueOf(commande.getId()));
            if (dateCommande != null) dateCommande.setValue(LocalDate.parse(commande.getDateCommande()));

            // Set spinner values defensively
            if (spinnerQuantite != null && spinnerQuantite.getValueFactory() != null) {
                spinnerQuantite.getValueFactory().setValue(commande.getQuantite());
            }
            if (spinnerDelaiFabrication != null && spinnerDelaiFabrication.getValueFactory() != null) {
                spinnerDelaiFabrication.getValueFactory().setValue(commande.getDelaiFabrication());
            }

            if (txtPrixUnitaire != null) txtPrixUnitaire.setText(String.valueOf(commande.getPrixUnitaire()));
            if (txtNotes != null) txtNotes.setText(commande.getNotes());

            // Select Client in ComboBox defensively
            if (comboClient != null) {
                Clients selectedClient = clientsList.stream()
                        .filter(c -> c.getId() == commande.getIdClient())
                        .findFirst()
                        .orElse(null);
                comboClient.setValue(selectedClient);
            }


            // Select Produit in ComboBox defensively
            if (comboProduit != null) {
                Produit selectedProduit = produitsList.stream()
                        .filter(p -> p.getReference().equals(commande.getIdProduit()))
                        .findFirst()
                        .orElse(null);
                comboProduit.setValue(selectedProduit);
            }


            // Set ComboBox for Etat and Priorite, handling potential emoji differences
            if (comboEtatCommande != null) setComboBoxValueByCleanText(comboEtatCommande, commande.getEtat());
            if (comboPriorite != null) setComboBoxValueByCleanText(comboPriorite, commande.getPriorite());

            // Date livraison is calculated based on command date and delay
            if (dateCommande.getValue() != null && spinnerDelaiFabrication.getValue() != null && dateLivraison != null) {
                dateLivraison.setValue(dateCommande.getValue().plusDays(spinnerDelaiFabrication.getValue()));
            } else if (dateLivraison != null) {
                dateLivraison.setValue(null); // Clear if components are not ready or values are missing
            }


            calculerTotal(); // Recalculate total for loaded command
        } catch (Exception e) {
            afficherErreur("Erreur de remplissage", "Erreur lors du remplissage du formulaire: " + e.getMessage());
            System.err.println("Erreur lors du remplissage du formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper to set ComboBox value robustly, ignoring emojis for comparison
    private void setComboBoxValueByCleanText(ComboBox<String> comboBox, String value) {
        if (comboBox == null || value == null) return; // Defensive check
        String cleanValue = value.replaceAll("[⏳✅🚚🔴🟡🟢]", "").trim();
        for (String item : comboBox.getItems()) {
            if (item.replaceAll("[⏳✅🚚🔴🟡🟢]", "").trim().equalsIgnoreCase(cleanValue)) {
                comboBox.setValue(item);
                return;
            }
        }
        comboBox.setValue(null); // No match found
    }
    /**
     * Force le rafraîchissement de la table pour refléter les changements
     */
    private void rafraichirTableCommandes() {
        if (tableCommandes != null) {
            // Méthode 1: Refresh simple
            tableCommandes.refresh();

            // Méthode 2: Si refresh() ne fonctionne pas, réappliquer les données
            // tableCommandes.setItems(null);
            // tableCommandes.setItems(commandesListFiltered);

            // Méthode 3: Notifier les colonnes spécifiques
            // Platform.runLater(() -> {
            //     colQuantite.setVisible(false);
            //     colQuantite.setVisible(true);
            //     colTotal.setVisible(false);
            //     colTotal.setVisible(true);
            // });
        }
    }

    /**
     * Mettre à jour une commande spécifique dans la liste et rafraîchir la table
     */
    private void mettreAJourCommandeDansListe(Commande commandeModifiee) {
        // Trouver et remplacer la commande dans la liste principale
        for (int i = 0; i < commandesList.size(); i++) {
            if (commandesList.get(i).getId() == commandeModifiee.getId()) {
                commandesList.set(i, commandeModifiee);
                break;
            }
        }

        // Trouver et remplacer dans la liste filtrée
        for (int i = 0; i < commandesListFiltered.size(); i++) {
            if (commandesListFiltered.get(i).getId() == commandeModifiee.getId()) {
                commandesListFiltered.set(i, commandeModifiee);
                break;
            }
        }

        // Rafraîchir la table
        rafraichirTableCommandes();
    }
    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        // Check date picker
        if (dateCommande == null) {
            erreurs.append("- Le composant date de commande n'est pas initialisé.\n");
        } else if (dateCommande.getValue() == null) {
            erreurs.append("- La date de commande est obligatoire.\n");
        }

        // Check client combo box
        if (comboClient == null) {
            erreurs.append("- Le composant sélection client n'est pas initialisé.\n");
        } else if (comboClient.getValue() == null) {
            erreurs.append("- Le client est obligatoire.\n");
        } else if (comboClient.getValue().getId() == 0) {
            erreurs.append("- Veuillez sélectionner un client valide.\n");
        }

        // Check product combo box
        if (comboProduit == null) {
            erreurs.append("- Le composant sélection produit n'est pas initialisé.\n");
        } else if (comboProduit.getValue() == null) {
            erreurs.append("- Le produit est obligatoire.\n");
        }

        // Check quantity spinner
        if (spinnerQuantite == null) {
            erreurs.append("- Le composant quantité n'est pas initialisé.\n");
        } else if (spinnerQuantite.getValue() == null) {
            erreurs.append("- La quantité est obligatoire.\n");
        } else if (spinnerQuantite.getValue() <= 0) {
            erreurs.append("- La quantité doit être positive.\n");
        }

        // Check delay spinner
        if (spinnerDelaiFabrication == null) {
            erreurs.append("- Le composant délai de fabrication n'est pas initialisé.\n");
        } else if (spinnerDelaiFabrication.getValue() == null) {
            erreurs.append("- Le délai de fabrication est obligatoire.\n");
        } else if (spinnerDelaiFabrication.getValue() <= 0) {
            erreurs.append("- Le délai de fabrication doit être positif.\n");
        }

        // Check price field
        if (txtPrixUnitaire == null) {
            erreurs.append("- Le champ du prix unitaire n'est pas initialisé.\n");
        } else if (txtPrixUnitaire.getText() == null || txtPrixUnitaire.getText().trim().isEmpty()) {
            erreurs.append("- Le prix unitaire est obligatoire.\n");
        } else {
            try {
                double prix = Double.parseDouble(txtPrixUnitaire.getText().replace(",", "."));
                if (prix <= 0) {
                    erreurs.append("- Le prix unitaire doit être un nombre positif.\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- Le prix unitaire doit être un nombre valide.\n");
            }
        }

        // Check state combo box
        if (comboEtatCommande == null) {
            erreurs.append("- Le composant état de commande n'est pas initialisé.\n");
        } else if (comboEtatCommande.getValue() == null) {
            erreurs.append("- L'état de la commande est obligatoire.\n");
        }

        // Check priority combo box
        if (comboPriorite == null) {
            erreurs.append("- Le composant priorité n'est pas initialisé.\n");
        } else if (comboPriorite.getValue() == null) {
            erreurs.append("- La priorité est obligatoire.\n");
        }

        if (erreurs.length() == 0) {
            return true;
        } else {
            afficherAvertissement("Validation du formulaire", "Veuillez corriger les erreurs suivantes:\n" + erreurs.toString());
            return false;
        }
    }

    private String extraireEtat(String etatAvecEmoji) {
        if (etatAvecEmoji == null) return "En cours"; // Default
        return etatAvecEmoji.replaceAll("[⏳✅🚚]", "").trim();
    }

    private String extrairePriorite(String prioriteAvecEmoji) {
        if (prioriteAvecEmoji == null) return "Normale"; // Default
        return prioriteAvecEmoji.replaceAll("[🔴🟡🟢]", "").trim();
    }

    private void mettreAJourStatut() {
        int totalCommandes = commandesListFiltered.size();
        long enCours = commandesListFiltered.stream().filter(c -> extraireEtat(c.getEtat()).equalsIgnoreCase("En cours")).count();
        long prete = commandesListFiltered.stream().filter(c -> extraireEtat(c.getEtat()).equalsIgnoreCase("Prête")).count();
        long livrees = commandesListFiltered.stream().filter(c -> extraireEtat(c.getEtat()).equalsIgnoreCase("Livrée")).count();

        // Defensive checks for labels
        if (lblNombreCommandes != null) lblNombreCommandes.setText("Total: " + totalCommandes);
        if (lblCommandesEnCours != null) lblCommandesEnCours.setText("En cours: " + enCours);
        if (lblCommandesPrete != null) lblCommandesPrete.setText("Prêtes: " + prete);
        if (lblCommandesLivrees != null) lblCommandesLivrees.setText("Livrées: " + livrees);

        if (lblStatut != null) lblStatut.setText("Données actualisées le " + LocalDate.now());
    }

    private void afficherErreur(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }




    private void afficherSucces(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void afficherAvertissement(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void afficherInfo(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}