package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddProduitController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AddProduitController.class.getName());

    // FXML Components
    @FXML private Label txtLabel;
    @FXML private TextField Designation;
    @FXML private TextField couleur;
    @FXML private TextField Quantite;
    @FXML private TextField Taille;
    @FXML private TextField stockMinimal;
    @FXML private TextField prix_Unitaire;
    @FXML private ComboBox<String> Marque;
    @FXML private ComboBox<String> Type;
    @FXML private Button btnAdd;
    @FXML private Button btnCancel;

    // State management
    private boolean isEditMode = false;
    private String originalReference;

    // Constants
    private static final String[] BRANDS = {"","Ray-Ban", "Essilor", "Oakley", "Gucci", "Versace", "Prada", "Tom Ford"};
    private static final String[] TYPES = {"","Lunettes", "Verres", "Lentilles", "Accessoires"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComponents();
        populateComboBoxes();
        setupValidation();

        if (!isEditMode) {
            setAutoReference();
        }
    }

    private void setupComponents() {
        // Set button text based on mode
        updateButtonText();

        // Add input filters for numeric fields
        addNumericFilter(Quantite);
        addNumericFilter(stockMinimal);
        addDecimalFilter(prix_Unitaire);
    }

    private void populateComboBoxes() {
        Marque.getItems().addAll(BRANDS);
        Type.getItems().addAll(TYPES);

        // Set default selections if needed
        if (!isEditMode) {
            Marque.getSelectionModel().selectFirst();
            Type.getSelectionModel().selectFirst();
        }
    }

    private void setupValidation() {
        // Add real-time validation listeners
        Designation.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        couleur.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        Quantite.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        Taille.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        stockMinimal.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        prix_Unitaire.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        Marque.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        Type.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void addNumericFilter(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                textField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void addDecimalFilter(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                textField.setText(oldVal);
            }
        });
    }

    private void validateForm() {
        boolean isValid = !isFormInvalid();
        if (btnAdd != null) { // إضافة فحص null هنا
            btnAdd.setDisable(!isValid);
        }
    }

    private void updateButtonText() {
        if (btnAdd != null) {
            btnAdd.setText(isEditMode ? "Modifier" : "Ajouter");
        }
    }


    private void setAutoReference() {
        if (txtLabel == null) {
            LOGGER.warning("txtLabel is null, cannot set auto reference.");
            return;
        }

        String sql = "SELECT MAX(CAST(SUBSTRING(reference, 4) AS UNSIGNED)) AS maxNum FROM produits WHERE reference REGEXP '^REF[0-9]+$'";

        try (Connection con = Connectiondb.Connection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            int nextNum = 1;
            if (rs.next() && rs.getObject("maxNum") != null) {
                nextNum = rs.getInt("maxNum") + 1;
            }

            String newRef = String.format("REF%03d", nextNum);
            txtLabel.setText(newRef);
            originalReference = newRef;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating auto reference", e);
            txtLabel.setText("REF001");
            originalReference = "REF001";
        }
    }

    @FXML
    void btnOnActionAdd(ActionEvent event) {
        if (isFormInvalid()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs correctement.");
            return;
        }

        // Additional business logic validation
        if (!validateBusinessRules()) {
            return;
        }

        try {
            if (isEditMode) {
                updateProduit();
            } else {
                insertProduit();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing product operation", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private boolean validateBusinessRules() {
        try {
            int quantity = Integer.parseInt(Quantite.getText().trim());
            int stockMin = Integer.parseInt(stockMinimal.getText().trim());
            double price = Double.parseDouble(prix_Unitaire.getText().trim());

            if (quantity < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La quantité ne peut pas être négative.");
                return false;
            }

            if (stockMin < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le stock minimal ne peut pas être négatif.");
                return false;
            }

            if (price <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le prix doit être supérieur à zéro.");
                return false;
            }

            if (quantity < stockMin) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Stock insuffisant");
                alert.setContentText("La quantité actuelle est inférieure au stock minimal. Voulez-vous continuer?");

                return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
            }

            return true;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide", "Veuillez vérifier les valeurs numériques.");
            return false;
        }
    }
    private void insertProduit() throws SQLException {
        String sql = "INSERT INTO produits (reference, designation, couleur, marque, type, quantite, taille, stock_minimal, prix_unitaire) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Connectiondb.Connection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            setPreparedStatementParameters(pst, false);

            int rowsInserted = pst.executeUpdate();
            if (rowsInserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès !");
                closeCurrentStage();
                openProduitsWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec lors de l'ajout du produit.");
            }
        }
    }

    private void updateProduit() throws SQLException {
        String sql = "UPDATE produits SET designation = ?, couleur = ?, marque = ?, type = ?, quantite = ?, taille = ?, stock_minimal = ?, prix_unitaire = ?, date_modification = NOW() WHERE reference = ?";

        try (Connection con = Connectiondb.Connection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            setPreparedStatementParameters(pst, true);

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès !");
                closeCurrentStage();
                openProduitsWindow();
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune modification détectée ou produit introuvable.");
            }
        }
    }

    private void setPreparedStatementParameters(PreparedStatement pst, boolean isUpdate) throws SQLException {
        if (isUpdate) {
            pst.setString(1, Designation.getText().trim());
            pst.setString(2, couleur.getText().trim());
            pst.setString(3, Marque.getValue());
            pst.setString(4, Type.getValue());
            pst.setInt(5, Integer.parseInt(Quantite.getText().trim()));
            pst.setString(6, Taille.getText().trim());
            pst.setInt(7, Integer.parseInt(stockMinimal.getText().trim()));
            pst.setDouble(8, Double.parseDouble(prix_Unitaire.getText().trim()));
            pst.setString(9, txtLabel.getText()); // WHERE clause
        } else {
            pst.setString(1, txtLabel.getText());
            pst.setString(2, Designation.getText().trim());
            pst.setString(3, couleur.getText().trim());
            pst.setString(4, Marque.getValue());
            pst.setString(5, Type.getValue());
            pst.setInt(6, Integer.parseInt(Quantite.getText().trim()));
            pst.setString(7, Taille.getText().trim());
            pst.setInt(8, Integer.parseInt(stockMinimal.getText().trim()));
            pst.setDouble(9, Double.parseDouble(prix_Unitaire.getText().trim()));
        }
    }

    @FXML
    void btnOnActionCancel(ActionEvent event) {
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Modifications non sauvegardées");
            alert.setContentText("Voulez-vous vraiment annuler? Les modifications seront perdues.");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                closeCurrentStage();
            }
        } else {
            closeCurrentStage();
        }
    }

    private boolean hasUnsavedChanges() {
        // Simple check for unsaved changes
        return !Designation.getText().trim().isEmpty()
                || !couleur.getText().trim().isEmpty()
                || Marque.getValue() != null
                || Type.getValue() != null
                || !Quantite.getText().trim().isEmpty()
                || !Taille.getText().trim().isEmpty()
                || !stockMinimal.getText().trim().isEmpty()
                || !prix_Unitaire.getText().trim().isEmpty();
    }

    private boolean isFormInvalid() {
        return Designation.getText().trim().isEmpty()
                || couleur.getText().trim().isEmpty()
                || Marque.getValue() == null
                || Type.getValue() == null
                || Quantite.getText().trim().isEmpty()
                || Taille.getText().trim().isEmpty()
                || stockMinimal.getText().trim().isEmpty()
                || prix_Unitaire.getText().trim().isEmpty();
    }

    private void clearForm() {
        Designation.clear();
        couleur.clear();
        Marque.getSelectionModel().clearSelection();
        Type.getSelectionModel().clearSelection();
        Quantite.clear();
        Taille.clear();
        stockMinimal.clear();
        prix_Unitaire.clear();

        if (!isEditMode) {
            setAutoReference();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeCurrentStage() {
        Stage currentStage = (Stage) Designation.getScene().getWindow();
        currentStage.close();
    }

    private void openProduitsWindow() {
        try {
            Stage newStage = Change.loadFxmlToStage("/sample/Produits.fxml");
            newStage.setTitle("Gestion des Produits");
            newStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening products window", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre des produits.");
        }
    }

    public void loadProduitForEdit(Produit produit) {
        if (produit == null) {
            LOGGER.warning("Attempted to load null product for editing");
            return;
        }

        isEditMode = true;
        originalReference = produit.getReference();

        txtLabel.setText(produit.getReference());
        Designation.setText(produit.getDesignation());
        couleur.setText(produit.getCouleur());
        Marque.setValue(produit.getMarque());
        Type.setValue(produit.getType());
        Quantite.setText(String.valueOf(produit.getQuantite()));
        Taille.setText(produit.getTaille());
        stockMinimal.setText(String.valueOf(produit.getStockMinimal()));
        prix_Unitaire.setText(String.valueOf(produit.getPrixUnitaire()));

        updateButtonText();
        validateForm();
    }

    // Getter for testing purposes
    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        updateButtonText();
    }
}