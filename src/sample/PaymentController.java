package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

// Import à ajouter en haut de votre classe

public class PaymentController {

    @FXML private ComboBox<String> clientComboBox;
    @FXML private TextField orderNumberField, invoiceNumberField, totalAmountField, paidAmountField, remainingAmountField;
    @FXML private DatePicker paymentDatePicker;
    @FXML private RadioButton cashRadio, cardRadio, transferRadio;
    @FXML private CheckBox partialPaymentCheck;
    @FXML private Label paymentStatusLabel;
    @FXML private TableView<Produit> orderProductsTable;
    @FXML private TableColumn<Produit, String> productNameColumn;
    @FXML private TableColumn<Produit, Integer> quantityColumn;
    @FXML private TableColumn<Produit, Double> unitPriceColumn, lineTotalColumn;
    @FXML private TableView<Payment> paymentHistoryTable;
    @FXML private TableColumn<Payment, String> dateColumn, methodColumn, statusColumn, receiptColumn;
    @FXML private TableColumn<Payment, Double> amountColumn;
    @FXML private Button processPaymentBtn;

    private final ObservableList<Produit> orderProducts = FXCollections.observableArrayList();
    private final ObservableList<Payment> paymentHistory = FXCollections.observableArrayList();

    // Current client's order ID for payment tracking
    private int currentOrderId = 0;

    @FXML
    public void initialize() {
        setupTables();
        setupEventHandlers();
        paymentDatePicker.setValue(LocalDate.now());
        loadClients();
        clearOrderFields();
        clearPaymentFields();
        checkPaymentTableStructure();
    }

    private void setupTables() {
        // Table Produits
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("designation"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        lineTotalColumn.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue();
            double total = produit.getQuantite() * produit.getPrixUnitaire();
            return new javafx.beans.property.SimpleDoubleProperty(total).asObject();
        });
        orderProductsTable.setItems(orderProducts);

        // Table Historique
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("methode"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        receiptColumn.setCellValueFactory(new PropertyValueFactory<>("recu"));
        paymentHistoryTable.setItems(paymentHistory);
    }

    private void setupEventHandlers() {
        clientComboBox.setOnAction(event -> {
            String client = clientComboBox.getValue();
            if (client != null && !client.isEmpty()) {
                loadOrderAndInvoice(client);
                loadOrderProducts(client);
                loadPaymentHistory(client);
            }
        });

        // Enhanced validation for paid amount field
        paidAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Allow digits, comma, and dot
            if (!newVal.matches("\\d*[,.]?\\d*")) {
                paidAmountField.setText(oldVal);
            } else {
                // Prevent multiple decimal separators
                long commaCount = newVal.chars().filter(ch -> ch == ',').count();
                long dotCount = newVal.chars().filter(ch -> ch == '.').count();

                if (commaCount + dotCount > 1) {
                    paidAmountField.setText(oldVal);
                } else {
                    calculateRemainingAmount();
                }
            }
        });

        // Auto-calculate when total amount changes
        totalAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateRemainingAmount();
        });
    }

    /** Charger clients */
    private void loadClients() {
        clientComboBox.getItems().clear();
        try (Connection conn = Connectiondb.Connection()) {
            String sql = "SELECT CONCAT(nom, ' ', prenom, ' (', telephone, ')') AS client_display FROM clients ORDER BY nom, prenom";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                ObservableList<String> clients = FXCollections.observableArrayList();
                while (rs.next()) {
                    clients.add(rs.getString("client_display"));
                }
                clientComboBox.setItems(clients);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des clients", e.getMessage());
        }
    }

    /** Charger commande et facture - FIXED VERSION */
    private void loadOrderAndInvoice(String client) {
        clearOrderFields();
        currentOrderId = 0;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = Connectiondb.Connection();

            String sql = "SELECT c.id AS commande_id, " +
                    "COALESCE(c.numero_commande, CONCAT('CMD-', c.id)) as numero_commande, " +
                    "SUM(c.quantite * c.prix_unitaire) as total_amount, " +
                    "f.numero_facture " +
                    "FROM commandes c " +
                    "LEFT JOIN factures f ON f.commande_id = c.id " +
                    "JOIN clients cl ON cl.id = c.ID_Client " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ? " +
                    "GROUP BY c.id, c.numero_commande, f.numero_facture " +
                    "ORDER BY c.Date_Commande DESC LIMIT 1";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, client);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Extract all values immediately
                currentOrderId = rs.getInt("commande_id");
                String numeroCommande = rs.getString("numero_commande");
                String invoiceNumber = rs.getString("numero_facture");
                double totalAmount = rs.getDouble("total_amount");

                // Use extracted values
                orderNumberField.setText(numeroCommande != null ? numeroCommande : "");
                invoiceNumberField.setText(invoiceNumber != null ? invoiceNumber : generateInvoiceNumber());
                totalAmountField.setText(formatAmount(totalAmount));

                calculateRemainingAmount();
            } else {
                orderNumberField.setText("Aucune commande");
                invoiceNumberField.setText("N/A");
                totalAmountField.setText("0,00");
            }

        } catch (SQLException e) {
            showError("Erreur lors du chargement de la commande", e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    /** Charger produits de la commande - FIXED VERSION */
    private void loadOrderProducts(String client) {
        orderProducts.clear();
        try (Connection conn = Connectiondb.Connection()) {
            String sql = "SELECT c.ID_produits, c.quantite, c.prix_unitaire " +
                    "FROM commandes c " +
                    "JOIN clients cl ON cl.id = c.ID_Client " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ? " +
                    "ORDER BY c.Date_Commande DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, client);
                try (ResultSet rs = pstmt.executeQuery()) {
                    double totalCalculated = 0;
                    while (rs.next()) {
                        String productId = rs.getString("ID_produits");
                        int qty = rs.getInt("quantite");
                        double price = rs.getDouble("prix_unitaire");

                        String designation = getProductDesignation(conn, productId);
                        String displayName = (designation != null && !designation.isEmpty()) ?
                                designation : "Produit " + productId;

                        Produit pr = new Produit();
                        pr.setDesignation(displayName);
                        pr.setQuantite(qty);
                        pr.setPrixUnitaire(price);
                        orderProducts.add(pr);

                        totalCalculated += (qty * price);
                    }

                    if (totalCalculated > 0) {
                        totalAmountField.setText(formatAmount(totalCalculated));
                    }
                }
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des produits", e.getMessage());
            e.printStackTrace();
        }
    }

    /** Get product designation by ID */
    private String getProductDesignation(Connection conn, String productId) {
        if (productId == null || productId.isEmpty()) {
            return "Produit sans nom";
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT designation FROM produits WHERE reference = ? OR designation LIKE ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, productId);
            pstmt.setString(2, "%" + productId + "%");

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("designation");
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche produit: " + e.getMessage());
            try {
                if (pstmt != null) pstmt.close();
                if (rs != null) rs.close();

                String sql2 = "SELECT designation FROM produits WHERE reference = ?";
                pstmt = conn.prepareStatement(sql2);
                pstmt.setString(1, productId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    return rs.getString("designation");
                }
            } catch (SQLException e2) {
                System.err.println("Erreur recherche produit (tentative 2): " + e2.getMessage());
            }
        } finally {
            closeResources(rs, pstmt, null);
        }

        return "Produit " + productId;
    }

    /** Load payment history - FIXED VERSION */
    private void loadPaymentHistory(String client) {
        paymentHistory.clear();
        try (Connection conn = Connectiondb.Connection()) {
            String sql = "SELECT p.id, p.date_paiement, p.montant, p.methode, " +
                    "CASE WHEN p.montant > 0 THEN 'Confirmé' ELSE 'En attente' END as statut " +
                    "FROM paiements p " +
                    "JOIN clients cl ON cl.id = p.client_id " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ? " +
                    "ORDER BY p.date_paiement DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, client);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        Timestamp timestamp = rs.getTimestamp("date_paiement");
                        LocalDateTime date = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
                        double montant = rs.getDouble("montant");
                        String methode = rs.getString("methode");
                        String statut = rs.getString("statut");

                        Payment payment = new Payment(id, date, montant, methode, statut, 0);
                        paymentHistory.add(payment);
                    }
                }
            }

        } catch (SQLException e) {
            showError("Erreur lors du chargement de l'historique", e.getMessage());
            e.printStackTrace();
        }
    }

    /** FIXED: Calculate remaining amount with proper logic */
    private void calculateRemainingAmount() {
        try {
            String totalText = totalAmountField.getText().trim();
            String paidText = paidAmountField.getText().trim();

            System.out.println("=== CALCULATION DEBUG ===");
            System.out.println("Raw total text: '" + totalText + "'");
            System.out.println("Raw paid text: '" + paidText + "'");

            BigDecimal total = parseBigDecimal(totalText);
            BigDecimal currentPaid = parseBigDecimal(paidText);

            System.out.println("Parsed total: " + total);
            System.out.println("Parsed current paid: " + currentPaid);

            // FIXED: Get already paid amount for the current ORDER, not all client payments
            BigDecimal alreadyPaid = BigDecimal.valueOf(getTotalPaidForCurrentOrder());
            System.out.println("Already paid from DB: " + alreadyPaid);

            BigDecimal totalPaid = alreadyPaid.add(currentPaid);
            BigDecimal remaining = total.subtract(totalPaid);

            // Ensure remaining is not negative
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO;
            }

            System.out.println("Total paid: " + totalPaid);
            System.out.println("Remaining: " + remaining);
            System.out.println("========================");

            // Format the result properly for French locale
            remainingAmountField.setText(formatAmount(remaining.doubleValue()));

            updatePaymentStatus(remaining.doubleValue());

        } catch (Exception e) {
            System.err.println("Calculation error: " + e.getMessage());
            e.printStackTrace();
            remainingAmountField.setText("Erreur");
            paymentStatusLabel.setText("Erreur de calcul");
            paymentStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /** FIXED: Get total paid for current order only */
    private double getTotalPaidForCurrentOrder() {
        String client = clientComboBox.getValue();
        if (client == null || client.isEmpty() || currentOrderId == 0) {
            System.out.println("No client selected or no current order, returning 0");
            return 0.0;
        }

        double totalPaid = 0.0;

        try (Connection conn = Connectiondb.Connection()) {
            // Get payments for the specific order/client combination
            String sql = "SELECT COALESCE(SUM(p.montant), 0) as total_paye " +
                    "FROM paiements p " +
                    "JOIN clients cl ON cl.id = p.client_id " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ? " +
                    "AND (p.commande_id = ? OR p.commande_id IS NULL)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, client);
                pstmt.setInt(2, currentOrderId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        totalPaid = rs.getDouble("total_paye");
                        System.out.println("Total paid from DB for client '" + client + "' order " + currentOrderId + ": " + totalPaid);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }

        return totalPaid;
    }

    /** Update payment status */
    private void updatePaymentStatus(double remaining) {
        final double TOLERANCE = 0.01;

        if (remaining <= TOLERANCE) {
            paymentStatusLabel.setText("Entièrement payé");
            paymentStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            processPaymentBtn.setDisable(true);
        } else {
            paymentStatusLabel.setText("Paiement partiel");
            paymentStatusLabel.setStyle("-fx-text-fill: #FF8C00; -fx-font-weight: bold;");
            processPaymentBtn.setDisable(false);
        }
    }

    /** FIXED: Process payment with proper validation */
    /** Enhanced processPayment with better error handling and debugging */
    /** Complete processPayment method with final payment confirmation */
    @FXML
    private void processPayment(ActionEvent event) {
        if (!validatePaymentInput()) {
            return;
        }

        String client = clientComboBox.getValue();
        BigDecimal paidAmount;

        try {
            paidAmount = parseBigDecimal(paidAmountField.getText());
        } catch (Exception e) {
            showError("Erreur de saisie", "Le montant payé doit être un nombre valide.");
            return;
        }

        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Erreur de saisie", "Le montant payé doit être supérieur à 0.");
            return;
        }

        BigDecimal remaining = parseBigDecimal(remainingAmountField.getText().replace(",", "."));
        BigDecimal tolerance = new BigDecimal("0.01");

        // Check if payment amount exceeds remaining amount
        if (paidAmount.subtract(remaining).compareTo(tolerance) > 0) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Montant supérieur au reste");
            confirmAlert.setContentText(String.format(
                    "Le montant saisi (%.2f) dépasse le reste à payer (%.2f).\n" +
                            "Voulez-vous continuer avec ce montant?",
                    paidAmount.doubleValue(),
                    remaining.doubleValue()
            ));

            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        // Check if this payment completes the order
        BigDecimal total = parseBigDecimal(totalAmountField.getText());
        BigDecimal alreadyPaid = BigDecimal.valueOf(getTotalPaidForCurrentOrder());
        BigDecimal newTotal = alreadyPaid.add(paidAmount);

        if (newTotal.compareTo(total) >= 0) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation - Paiement Final");
            confirmAlert.setHeaderText("Ce paiement complète la commande");
            confirmAlert.setContentText(String.format(
                    "Montant total: %.2f DH\n" +
                            "Déjà payé: %.2f DH\n" +
                            "Ce paiement: %.2f DH\n" +
                            "Total après paiement: %.2f DH\n\n" +
                            "La commande sera marquée comme entièrement payée.\n" +
                            "Confirmez-vous ce paiement final?",
                    total.doubleValue(),
                    alreadyPaid.doubleValue(),
                    paidAmount.doubleValue(),
                    newTotal.doubleValue()
            ));

            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        String method = getSelectedPaymentMethod();
        LocalDate date = paymentDatePicker.getValue();

        // Debug output
        System.out.println("Parsed '" + paidAmountField.getText() + "' -> " + paidAmount);
        System.out.println("Parsed '" + totalAmountField.getText() + "' -> " + parseBigDecimal(totalAmountField.getText()));

        try (Connection conn = Connectiondb.Connection()) {
            conn.setAutoCommit(false);

            // Get client ID
            String sqlIds = "SELECT cl.id as client_id " +
                    "FROM clients cl " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ?";

            int clientId = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlIds)) {
                pstmt.setString(1, client);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        clientId = rs.getInt("client_id");
                    }
                }
            }

            if (clientId == 0) {
                showError("Erreur", "Client introuvable.");
                conn.rollback();
                return;
            }

            // Debug payment data before insertion
            System.out.println("=== PAYMENT DEBUG ===");
            System.out.println("Client ID: " + clientId);
            System.out.println("Order ID: " + currentOrderId);
            System.out.println("Amount: " + paidAmount.doubleValue());
            System.out.println("Method: '" + method + "'");
            System.out.println("Date: " + date.atStartOfDay());
            System.out.println("====================");

            // Insert payment with proper order ID
            String sqlPayment = "INSERT INTO paiements (client_id, commande_id, montant, methode, date_paiement) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlPayment)) {
                pstmt.setInt(1, clientId);
                pstmt.setInt(2, currentOrderId > 0 ? currentOrderId : clientId);
                pstmt.setDouble(3, paidAmount.doubleValue());
                pstmt.setString(4, method);
                pstmt.setTimestamp(5, Timestamp.valueOf(date.atStartOfDay()));

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    updateInvoiceIfNeeded(conn, client);

                    // Update order status if fully paid
                    if (newTotal.compareTo(total) >= 0) {
                        updateOrderStatusToCompleted(conn, currentOrderId);
                    }

                    conn.commit();

                    // Show success message with payment details
                    String successMessage;
                    if (newTotal.compareTo(total) >= 0) {
                        successMessage = String.format(
                                "✅ Paiement enregistré avec succès !\n\n" +
                                        "🎉 COMMANDE ENTIÈREMENT PAYÉE 🎉\n\n" +
                                        "Montant payé: %.2f DH\n" +
                                        "Total commande: %.2f DH\n" +
                                        "Statut: Complète",
                                paidAmount.doubleValue(),
                                total.doubleValue()
                        );
                    } else {
                        BigDecimal remainingAfter = total.subtract(newTotal);
                        successMessage = String.format(
                                "✅ Paiement enregistré avec succès !\n\n" +
                                        "Montant payé: %.2f DH\n" +
                                        "Reste à payer: %.2f DH",
                                paidAmount.doubleValue(),
                                remainingAfter.doubleValue()
                        );
                    }

                    showSuccess(successMessage);
                    clearPaymentFields();
                    loadPaymentHistory(client);
                    calculateRemainingAmount();

                } else {
                    conn.rollback();
                    showError("Erreur", "Aucun paiement n'a été enregistré.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error Details:");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());

            showError("Erreur base de données", "Impossible d'enregistrer le paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Update order status to completed when fully paid */
    private void updateOrderStatusToCompleted(Connection conn, int orderId) {
        if (orderId <= 0) return;

        try {
            String sql = "UPDATE commandes SET statut = 'Payée' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, orderId);
                int updated = pstmt.executeUpdate();
                if (updated > 0) {
                    System.out.println("✅ Order " + orderId + " marked as completed/paid");
                }
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not update order status: " + e.getMessage());
            // Don't throw exception, just log the warning
        }
    }

    /** Parse BigDecimal with French format support */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            String cleanValue = value.trim();

            // Handle French number format (comma as decimal separator)
            if (cleanValue.contains(",")) {
                cleanValue = cleanValue.replace(",", ".");
                System.out.println("Converted French format '" + value + "' to '" + cleanValue + "'");
            }

            // Remove whitespace and currency symbols, keep only digits, dots, and minus
            cleanValue = cleanValue.replaceAll("\\s+", "").replaceAll("[^0-9.-]", "");

            if (cleanValue.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal result = new BigDecimal(cleanValue);
            System.out.println("Parsed '" + value + "' -> " + result);
            return result;

        } catch (NumberFormatException e) {
            System.err.println("Failed to parse BigDecimal: '" + value + "' - " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /** Format amount for French locale */
    private String formatAmount(double amount) {
        return String.format("%.2f", amount).replace(".", ",");
    }

    /** Update or create invoice */
    private void updateInvoiceIfNeeded(Connection conn, String client) throws SQLException {
        if (invoiceNumberField.getText().equals("N/A") || invoiceNumberField.getText().isEmpty()) {
            String invoiceNum = generateInvoiceNumber();

            String sql = "INSERT INTO factures (numero_facture, commande_id, date_facture, montant_total) " +
                    "SELECT ?, c.id, NOW(), ? " +
                    "FROM commandes c " +
                    "JOIN clients cl ON cl.id = c.ID_Client " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ? " +
                    "LIMIT 1";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, invoiceNum);
                pstmt.setDouble(2, parseBigDecimal(totalAmountField.getText()).doubleValue());
                pstmt.setString(3, client);
                pstmt.executeUpdate();

                invoiceNumberField.setText(invoiceNum);
            }
        }
    }

    /** Generate unique invoice number */
    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "FAC-" + now.format(formatter);
    }

    /** Validate payment input */
    /** Enhanced validate payment input with method validation */
    private boolean validatePaymentInput() {
        if (clientComboBox.getValue() == null || clientComboBox.getValue().isEmpty()) {
            showError("Validation", "Sélectionnez un client avant de traiter le paiement.");
            return false;
        }

        if (paidAmountField.getText().isEmpty()) {
            showError("Validation", "Saisissez le montant payé.");
            return false;
        }

        if (!cashRadio.isSelected() && !cardRadio.isSelected() && !transferRadio.isSelected()) {
            showError("Validation", "Sélectionnez une méthode de paiement.");
            return false;
        }

        if (paymentDatePicker.getValue() == null) {
            showError("Validation", "Sélectionnez une date de paiement.");
            return false;
        }

        // Validate payment method matches database enum values
        String method = getSelectedPaymentMethod();
        if (!method.equals("Espece") && !method.equals("Carte") && !method.equals("Virement")) {
            showError("Validation", "Méthode de paiement invalide: " + method);
            return false;
        }

        return true;
    }


    @FXML
    void generateReceipt(ActionEvent event) {
        String client = clientComboBox.getValue();
        if (client == null || client.isEmpty()) {
            showError("Erreur", "Veuillez sélectionner un client d'abord");
            return;
        }

        try {
            // Recharger les produits réels
            loadRealOrderProducts(client);
            String receiptContent = generateReceiptContent(client);

            // Créer une boîte de dialogue personnalisée avec plus d'options
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reçu de Paiement - OptiGest");
            alert.setHeaderText("Cabinet OptiGest - Tanger");

            TextArea textArea = new TextArea(receiptContent);
            textArea.setEditable(false);
            textArea.setPrefRowCount(30);
            textArea.setPrefColumnCount(70);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10px;");

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(700, 650);

            // Boutons d'action améliorés
            ButtonType printWithDialogButton = new ButtonType("🖨️ Imprimer (avec dialogue)", ButtonBar.ButtonData.OTHER);
            ButtonType printDirectButton = new ButtonType("⚡ Impression rapide", ButtonBar.ButtonData.OTHER);
            ButtonType saveButton = new ButtonType("💾 Sauvegarder", ButtonBar.ButtonData.OTHER);
            ButtonType showPrintersButton = new ButtonType("🖨️ Voir les imprimantes", ButtonBar.ButtonData.OTHER);

            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(printWithDialogButton, printDirectButton, saveButton, showPrintersButton, ButtonType.CLOSE);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == printWithDialogButton) {
                    printReceipt(receiptContent); // Avec dialogue de sélection d'imprimante
                } else if (result.get() == printDirectButton) {
                    printReceiptDirect(receiptContent); // Impression directe sur imprimante par défaut
                } else if (result.get() == saveButton) {
                    saveReceiptToFile(receiptContent, client);
                } else if (result.get() == showPrintersButton) {
                    showAvailablePrinters();
                    generateReceipt(event); // Relancer la boîte de dialogue
                }
            }

        } catch (Exception e) {
            showError("Erreur de génération du reçu", e.getMessage());
            e.printStackTrace();
        }
    }

    private void printReceipt(String content) {
        try {
            // Créer un PrinterJob
            PrinterJob printerJob = PrinterJob.createPrinterJob();

            if (printerJob == null) {
                showError("Erreur d'impression", "Aucune imprimante trouvée sur le système.");
                return;
            }

            // Afficher la boîte de dialogue d'impression
            boolean proceed = printerJob.showPrintDialog(null);

            if (!proceed) {
                System.out.println("Impression annulée par l'utilisateur");
                return;
            }

            // Créer le contenu à imprimer
            Node printNode = createPrintableNode(content);

            // Configurer les paramètres d'impression
            PageLayout pageLayout = printerJob.getPrinter().createPageLayout(
                    Paper.A4,
                    PageOrientation.PORTRAIT,
                    Printer.MarginType.DEFAULT
            );
            printerJob.getJobSettings().setPageLayout(pageLayout);

            // Lancer l'impression
            boolean success = printerJob.printPage(printNode);

            if (success) {
                printerJob.endJob();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Impression réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("✅ Reçu imprimé avec succès !");
                successAlert.showAndWait();
            } else {
                showError("Erreur d'impression", "Échec de l'impression du reçu.");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'impression: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'impression", "Impossible d'imprimer le reçu: " + e.getMessage());
        }
    }

    // ** MÉTHODE POUR CRÉER UN NODE IMPRIMABLE **
    private Node createPrintableNode(String content) {
        VBox printBox = new VBox();
        printBox.setPadding(new Insets(20));
        printBox.setSpacing(2);

        // Diviser le contenu en lignes
        String[] lines = content.split("\n");

        for (String line : lines) {
            Text textLine = new Text(line);

            // Configuration de la police selon le type de ligne
            if (line.contains("CABINET OPTIGEST") || line.contains("REÇU DE PAIEMENT")) {
                textLine.setFont(Font.font("Courier New", 14));
            } else if (line.contains("TOTAL") || line.contains("PAYÉ") || line.contains("RESTE")) {
                textLine.setFont(Font.font("Courier New", 12));
            } else {
                textLine.setFont(Font.font("Courier New", 10));
            }

            printBox.getChildren().add(textLine);
        }

        return printBox;
    }

    // ** ALTERNATIVE : IMPRESSION DIRECTE (Sans dialogue) **
    private void printReceiptDirect(String content) {
        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();

            if (printerJob == null) {
                showError("Erreur d'impression", "Aucune imprimante trouvée.");
                return;
            }

            // Utiliser l'imprimante par défaut sans dialogue
            Node printNode = createPrintableNode(content);

            boolean success = printerJob.printPage(printNode);

            if (success) {
                printerJob.endJob();
                showSuccess("✅ Reçu envoyé à l'imprimante !");
            } else {
                showError("Erreur", "Échec de l'impression.");
            }

        } catch (Exception e) {
            showError("Erreur d'impression", e.getMessage());
        }
    }

    // ** MÉTHODE POUR LISTER LES IMPRIMANTES DISPONIBLES **
    private void showAvailablePrinters() {
        try {
            ObservableSet<Printer> printersSet = Printer.getAllPrinters();

            if (printersSet.isEmpty()) {
                showError("Info", "Aucune imprimante trouvée sur le système.");
                return;
            }

            StringBuilder printersInfo = new StringBuilder("Imprimantes disponibles:\n\n");

            for (Printer printer : printersSet) {
                printersInfo.append("• ").append(printer.getName());
                if (printer == Printer.getDefaultPrinter()) {
                    printersInfo.append(" (Défaut)");
                }
                printersInfo.append("\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Imprimantes disponibles");
            alert.setHeaderText(null);
            alert.setContentText(printersInfo.toString());
            alert.showAndWait();

        } catch (Exception e) {
            showError("Erreur", "Impossible de lister les imprimantes: " + e.getMessage());
        }
    }


    private String generateReceiptContent(String client) {
        StringBuilder receipt = new StringBuilder();

        // ======= HEADER =======
        receipt.append(repeatChar('=', 60)).append("\n");
        receipt.append("                   CABINET OPTIGEST\n");
        receipt.append("                     MDINA TANGER\n");
        receipt.append("              Tel: 05 34 56 77 00\n");
        receipt.append("              Email: contact@optigest.ma\n");
        receipt.append(repeatChar('=', 60)).append("\n\n");

        receipt.append("                  REÇU DE PAIEMENT\n");
        receipt.append(repeatChar('-', 60)).append("\n");

        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        String receiptNumber = generateReceiptNumber();

        receipt.append(String.format("Date: %-20s Reçu N°: %s\n", currentDate, receiptNumber));
        receipt.append(String.format("Client: %s\n", client));
        receipt.append("\n").append(repeatChar('-', 60)).append("\n");

        // ======= DÉTAILS DES PRODUITS RÉELS =======
        receipt.append("DÉTAILS DU PAIEMENT:\n");
        receipt.append(repeatChar('-', 60)).append("\n");

        // ** CORRECTION : Utiliser les vrais produits depuis orderProducts **
        double totalCalculated = 0.0;

        if (orderProducts.isEmpty()) {
            receipt.append("Aucun produit trouvé\n");
        } else {
            for (Produit produit : orderProducts) {
                String designation = produit.getDesignation();
                int quantite = produit.getQuantite();
                double prixUnitaire = produit.getPrixUnitaire();
                double sousTotal = quantite * prixUnitaire;

                // Format: "Nom du produit (Qté: x)" puis prix
                String productLine = String.format("%s (Qté: %d)", designation, quantite);
                receipt.append(String.format("%-40s %10.2f DH\n", productLine, sousTotal));
                totalCalculated += sousTotal;
            }
        }

        receipt.append(repeatChar('-', 60)).append("\n");

        // ** CORRECTION : Utiliser les vrais montants **
        double totalAmount = parseBigDecimal(totalAmountField.getText()).doubleValue();
        double alreadyPaid = getTotalPaidForCurrentOrder();
        double currentPayment = 0.0;

        // Si un paiement est en cours de saisie
        if (!paidAmountField.getText().trim().isEmpty()) {
            currentPayment = parseBigDecimal(paidAmountField.getText()).doubleValue();
        }

        double totalPaid = alreadyPaid + currentPayment;
        double remaining = Math.max(0, totalAmount - totalPaid);

        // ======= RÉSUMÉ AVEC VRAIS MONTANTS =======
        receipt.append(String.format("TOTAL COMMANDE: %35.2f DH\n", totalAmount));

        if (alreadyPaid > 0) {
            receipt.append(String.format("DÉJÀ PAYÉ: %41.2f DH\n", alreadyPaid));
        }

        if (currentPayment > 0) {
            receipt.append(String.format("CE PAIEMENT: %39.2f DH\n", currentPayment));
        }

        receipt.append(String.format("TOTAL PAYÉ: %41.2f DH\n", totalPaid));
        receipt.append(String.format("RESTE À PAYER: %37.2f DH\n", remaining));
        receipt.append(repeatChar('-', 60)).append("\n\n");

        // ======= INFORMATIONS PAIEMENT =======
        if (currentPayment > 0) {
            String method = getSelectedPaymentMethod();
            String methodDisplay = method.equals("Espece") ? "Espèces" :
                    method.equals("Carte") ? "Carte bancaire" :
                            "Virement bancaire";
            receipt.append(String.format("Méthode de paiement: %s\n", methodDisplay));
            receipt.append(String.format("Date de paiement: %s\n",
                    paymentDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            receipt.append("\n");
        }

        // ======= STATUT =======
        String status = remaining <= 0.01 ? "✅ ENTIÈREMENT PAYÉ" : "⏳ PAIEMENT PARTIEL";
        receipt.append(String.format("STATUT: %s\n\n", status));

        // ======= FOOTER =======
        receipt.append("Merci pour votre confiance!\n");
        receipt.append("Ce reçu fait foi de paiement.\n\n");
        receipt.append("Signature: _________________________\n\n");
        receipt.append(repeatChar('=', 60)).append("\n");

        return receipt.toString();
    }

    // ** MÉTHODE ADDITIONNELLE POUR OBTENIR LES PRODUITS DEPUIS LA BD **
    private void loadRealOrderProducts(String client) {
        orderProducts.clear();

        try (Connection conn = Connectiondb.Connection()) {
            String sql = "SELECT p.designation, c.quantite, c.prix_unitaire " +
                    "FROM commandes c " +
                    "JOIN produits p ON p.reference = c.ID_produits " +
                    "JOIN clients cl ON cl.id = c.ID_Client " +
                    "WHERE CONCAT(cl.nom,' ',cl.prenom,' (',cl.telephone,')') = ? " +
                    "AND c.id = ? " +
                    "ORDER BY c.Date_Commande DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, client);
                pstmt.setInt(2, currentOrderId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String designation = rs.getString("designation");
                        int quantite = rs.getInt("quantite");
                        double prixUnitaire = rs.getDouble("prix_unitaire");

                        Produit produit = new Produit();
                        produit.setDesignation(designation != null ? designation : "Produit sans nom");
                        produit.setQuantite(quantite);
                        produit.setPrixUnitaire(prixUnitaire);

                        orderProducts.add(produit);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des produits pour le reçu: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void saveReceiptToFile(String content, String clientName) {
        try {
            String fileName = "Recu_" + clientName.replaceAll("\\s+", "_") + "_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";

            System.out.println("Saving receipt to: " + fileName);
            System.out.println(content);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sauvegarde");
            alert.setHeaderText(null);
            alert.setContentText("Reçu sauvegardé: " + fileName);
            alert.showAndWait();
        } catch (Exception e) {
            showError("Erreur de sauvegarde", "Impossible de sauvegarder le reçu: " + e.getMessage());
        }
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    private String generateReceiptNumber() {
        return "RCP-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    }

    @FXML
    void viewHistory(ActionEvent event) {
        String client = clientComboBox.getValue();
        if (client == null || client.isEmpty()) {
            showError("Error", "Please select a client first");
            return;
        }

        try {
            loadPaymentHistory(client);

            double totalPaid = getTotalPaidForCurrentOrder();
            double totalAmount = parseBigDecimal(totalAmountField.getText()).doubleValue();
            double remaining = Math.max(0, totalAmount - totalPaid);

            String summary = String.format(
                    "Payment summary for client: %s\n\n" +
                            "Total amount: %.2f DH\n" +
                            "Amount paid: %.2f DH\n" +
                            "Remaining: %.2f DH\n" +
                            "Number of payments: %d\n\n" +
                            "Status: %s",
                    client,
                    totalAmount,
                    totalPaid,
                    remaining,
                    paymentHistory.size(),
                    remaining <= 0.01 ? "Fully Paid" : "Partial Payment"
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment History");
            alert.setHeaderText("Payment Summary");
            alert.setContentText(summary);
            alert.showAndWait();

        } catch (Exception e) {
            showError("History View Error", e.getMessage());
        }
    }

    /** Generate receipt content */

    /** Get selected payment method */
    /** Get selected payment method - FIXED to match database ENUM values */
    private String getSelectedPaymentMethod() {
        if (cashRadio.isSelected()) return "Espece";    // Changed from "Espèces" to "Espece"
        if (cardRadio.isSelected()) return "Carte";
        if (transferRadio.isSelected()) return "Virement";
        return "Espece"; // Default fallback changed to match DB enum
    }

    /** Check payment table structure */
    public void checkPaymentTableStructure() {
        try (Connection conn = Connectiondb.Connection()) {
            String sql = "DESCRIBE paiements";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                System.out.println("=== PAIEMENTS TABLE STRUCTURE ===");
                while (rs.next()) {
                    String columnName = rs.getString("Field");
                    String dataType = rs.getString("Type");
                    System.out.println("Column: " + columnName + " | Type: " + dataType);
                }
                System.out.println("================================");
            }
        } catch (SQLException e) {
            System.err.println("Error checking table structure: " + e.getMessage());
        }
    }

    /** Clear order fields */
    private void clearOrderFields() {
        orderNumberField.setText("");
        invoiceNumberField.setText("");
        totalAmountField.setText("0,00");
        remainingAmountField.setText("0,00");
        orderProducts.clear();
        paymentStatusLabel.setText("Sélectionnez un client");
        paymentStatusLabel.setStyle("-fx-text-fill: gray;");
        currentOrderId = 0;
    }

    /** Clear payment fields */
    private void clearPaymentFields() {
        paidAmountField.setText("");
        cashRadio.setSelected(true);
        partialPaymentCheck.setSelected(false);
    }

    /** Close database resources */
    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (pstmt != null) {
            try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    /** Show error dialog */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Show success dialog */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}