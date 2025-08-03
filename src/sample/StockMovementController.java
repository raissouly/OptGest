package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class StockMovementController {

    @FXML private ComboBox<String> comboProduit;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtQuantite;
    @FXML private Button btnValider;

    private ObservableList<String> produits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // تعبئة ComboBox للنوع
        comboType.setItems(FXCollections.observableArrayList("ENTREE", "SORTIE"));

        // تحميل المنتجات
        loadProduits();
    }

    private void loadProduits() {
        try (Connection conn = Connectiondb.Connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT reference FROM produits")) {

            produits.clear(); // مسح البيانات السابقة
            while (rs.next()) {
                produits.add(rs.getString("reference"));
            }
            comboProduit.setItems(produits);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void validerOperation() {
        // التحقق من الإدخال
        if (!validateInput()) {
            return;
        }

        String ref = comboProduit.getValue();
        String type = comboType.getValue();
        int quantite = Integer.parseInt(txtQuantite.getText());

        // التحقق من الكمية المتاحة في حالة الخروج
        if (type.equals("SORTIE") && !checkAvailableStock(ref, quantite)) {
            showAlert("Insufficient stock quantity", Alert.AlertType.WARNING);
            return;
        }

        Connection conn = null;
        try {
            conn = Connectiondb.Connection();
            conn.setAutoCommit(false);

            // تسجيل الحركة
            insertMovement(conn, ref, type, quantite);

            // تحديث الكمية
            updateProductQuantity(conn, ref, type, quantite);

            conn.commit();
            showAlert("Operation registered successfully", Alert.AlertType.INFORMATION);
            clearFields();

        } catch (SQLException e) {
            // التراجع عن المعاملة في حالة الخطأ
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            showAlert("Error registering operation: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean validateInput() {
        if (comboProduit.getValue() == null || comboProduit.getValue().isEmpty()) {
            showAlert("Please select a product", Alert.AlertType.WARNING);
            return false;
        }

        if (comboType.getValue() == null || comboType.getValue().isEmpty()) {
            showAlert("Please select operation type", Alert.AlertType.WARNING);
            return false;
        }

        if (txtQuantite.getText() == null || txtQuantite.getText().trim().isEmpty()) {
            showAlert("Please enter quantity", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int quantite = Integer.parseInt(txtQuantite.getText().trim());
            if (quantite <= 0) {
                showAlert("Quantity must be greater than zero", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for quantity", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private boolean checkAvailableStock(String reference, int requestedQuantity) {
        String query = "SELECT quantite FROM produits WHERE reference = ?";
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, reference);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int availableQuantity = rs.getInt("quantite");
                return availableQuantity >= requestedQuantity;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error checking stock: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return false;
    }

    private void insertMovement(Connection conn, String ref, String type, int quantite) throws SQLException {
        String insert = "INSERT INTO mouvements_stock(reference_produit, type_operation, quantite, date_operation) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, ref);
            ps.setString(2, type);
            ps.setInt(3, quantite);
            ps.executeUpdate();
        }
    }

    private void updateProductQuantity(Connection conn, String ref, String type, int quantite) throws SQLException {
        String update = "UPDATE produits SET quantite = quantite " +
                (type.equals("ENTREE") ? "+ ?" : "- ?") +
                " WHERE reference = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setInt(1, quantite);
            ps.setString(2, ref);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Product not found");
            }
        }
    }

    private void clearFields() {
        comboProduit.setValue(null);
        comboType.setValue(null);
        txtQuantite.clear();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // للتوافق مع النسخة القديمة
    private void showAlert(String message) {
        showAlert(message, Alert.AlertType.INFORMATION);
    }
    public void setSelectedProduct(String productReference) {
        if (comboProduit != null && productReference != null) {
            if (produits.contains(productReference)) {
                comboProduit.setValue(productReference);
            } else {
                loadProduits();
                if (produits.contains(productReference)) {
                    comboProduit.setValue(productReference);
                }
            }
        }
    }
}