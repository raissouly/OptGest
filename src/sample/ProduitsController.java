package sample;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class ProduitsController {

    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colreference;
    @FXML private TableColumn<Produit, String> coldesignation;
    @FXML private TableColumn<Produit, String> colcouleur;
    @FXML private TableColumn<Produit, String> colMarque;
    @FXML private TableColumn<Produit, String> colType;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, String> colTaille;
    @FXML private TableColumn<Produit, Double> colPrixUnitaire;
    @FXML private TableColumn<Produit, Integer> colStock;
    @FXML private TableColumn<Produit, Void> colAction;
    @FXML private TextField SEARCH;
    @FXML private Button btnSuiviStock;
    @FXML private Label lblTotalProduits; // Added for status display

    private final ObservableList<Produit> produitList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureColumns();
        loadProduits();
        tableProduits.setItems(produitList);
        addActionButtonsToTable();
        updateStatusLabel();
    }

    private void configureColumns() {
        colreference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        coldesignation.setCellValueFactory(new PropertyValueFactory<>("designation"));
        colcouleur.setCellValueFactory(new PropertyValueFactory<>("couleur"));
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockMinimal"));
    }

    private void loadProduits() {
        produitList.clear();
        String query = "SELECT * FROM produits";

        try (Connection conn = Connectiondb.Connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit p = new Produit(
                        rs.getString("reference"),
                        rs.getString("designation"),
                        rs.getString("taille"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("couleur"),
                        rs.getString("type"),
                        rs.getString("marque"),
                        rs.getInt("stock_minimal")
                );
                produitList.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorDialog("Database Error", "Unable to load products from database.");
        }
        updateStatusLabel();
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Produit, Void>, TableCell<Produit, Void>> cellFactory =
                new Callback<TableColumn<Produit, Void>, TableCell<Produit, Void>>() {
                    @Override
                    public TableCell<Produit, Void> call(final TableColumn<Produit, Void> param) {
                        return new TableCell<Produit, Void>() {


                            private Button createIconButton(FontAwesomeIcon icon, String colorHex) {
                                FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
                                iconView.setFill(Color.web(colorHex));
                                iconView.setGlyphSize(20); // حجم الأيقونة (تقدر تزاد أو تنقص حسب الحاجة)

                                Button button = new Button();
                                button.setGraphic(iconView);
                                button.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"); // تصميم شفاف مع مؤشر يد

                                // ضبط حجم الزر لتناسق الأيقونة مع الزر
                                button.setMinSize(35, 35);
                                button.setPrefSize(35, 35);
                                button.setMaxSize(35, 35);

                                return button;
                            }

                            // تعريف الأزرار
                            private final Button btnEdit = createIconButton(FontAwesomeIcon.PENCIL, "#4CAF50");
                            private final Button btnDelete = createIconButton(FontAwesomeIcon.TRASH, "#F44336");
                            private final Button btnStock = createIconButton(FontAwesomeIcon.EXCHANGE, "#2196F3");

                            {
                                btnEdit.setOnAction(event -> {
                                    Produit produit = getTableView().getItems().get(getIndex());
                                    openEditDialog(produit);
                                });
                                btnDelete.setOnAction(event -> {
                                    Produit produit = getTableView().getItems().get(getIndex());
                                    deleteProduit(produit);
                                });
                                btnStock.setOnAction(event -> {
                                    Produit produit = getTableView().getItems().get(getIndex());
                                    openStockMovementDialog(produit);
                                });

                                btnStock.setTooltip(new Tooltip("Stock Movement"));
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(new HBox(5, btnEdit, btnDelete, btnStock));
                                }
                            }
                        };
                    }
                };

        colAction.setCellFactory(cellFactory);
    }

    private Button createIconButton(FontAwesomeIcon icon, String color) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setStyle("-fx-fill: " + color + "; -fx-font-size: 14px;");
        Button button = new Button();
        button.setGraphic(iconView);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }

    private void openEditDialog(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/AddProduit.fxml"));
            Parent root = loader.load();
            AddProduitController controller = loader.getController();
            controller.loadProduitForEdit(produit);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Product");
            stage.setOnHiding(e -> loadProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error", "Unable to load edit window.");
        }
    }

    private void openStockMovementDialog(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/StockMovement.fxml"));
            Parent root = loader.load();
            StockMovementController controller = loader.getController();
            controller.setSelectedProduct(produit.getReference());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Stock Movement - " + produit.getReference());
            stage.setOnHiding(e -> loadProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error", "Unable to load stock movement window.");
        }
    }

    private void deleteProduit(Produit produit) {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to delete this product?",
                ButtonType.YES, ButtonType.NO
        );
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM produits WHERE reference = ?";
            try (Connection conn = Connectiondb.Connection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, produit.getReference());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    produitList.remove(produit);
                    updateStatusLabel();
                } else {
                    showErrorDialog("Error", "Product not found in database.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showErrorDialog("Database Error", "Error deleting product.");
            }
        }
    }

    @FXML
    void ajouterProduit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/AddProduit.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Product");
            stage.setOnHiding(e -> loadProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error", "Unable to open add window.");
        }
    }

    @FXML
    void ouvrirSuiviStock(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/StockMovement.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Stock Movement Management");
            stage.setOnHiding(e -> loadProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error", "Unable to load stock movement window.");
        }
    }

    // New method for stock alerts
    @FXML
    void ouvrirAlertes(ActionEvent event) {
        ObservableList<Produit> lowStockProducts = FXCollections.observableArrayList();
        ObservableList<Produit> outOfStockProducts = FXCollections.observableArrayList();

        for (Produit p : produitList) {
            if (p.getQuantite() == 0) {
                outOfStockProducts.add(p);
            } else if (p.getQuantite() <= p.getStockMinimal()) {
                lowStockProducts.add(p);
            }
        }

        if (lowStockProducts.isEmpty() && outOfStockProducts.isEmpty()) {
            showInfoDialog("Stock Alerts", "No stock alerts at this time. All products are well stocked!");
            return;
        }

        StringBuilder alertMessage = new StringBuilder();
        alertMessage.append("STOCK ALERTS:\n\n");

        if (!outOfStockProducts.isEmpty()) {
            alertMessage.append("OUT OF STOCK (" + outOfStockProducts.size() + " products):\n");
            for (Produit p : outOfStockProducts) {
                alertMessage.append("• ").append(p.getReference()).append(" - ").append(p.getDesignation()).append("\n");
            }
            alertMessage.append("\n");
        }

        if (!lowStockProducts.isEmpty()) {
            alertMessage.append("LOW STOCK (" + lowStockProducts.size() + " products):\n");
            for (Produit p : lowStockProducts) {
                alertMessage.append("• ").append(p.getReference()).append(" - ").append(p.getDesignation())
                        .append(" (Current: ").append(p.getQuantite()).append(", Min: ").append(p.getStockMinimal()).append(")\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Stock Alerts");
        alert.setHeaderText("Products requiring attention:");
        alert.setContentText(alertMessage.toString());
        alert.getDialogPane().setPrefSize(500, 400);
        alert.showAndWait();
    }

    // New method for statistics
    @FXML
    void afficherStatistiques(ActionEvent event) {
        int totalProducts = produitList.size();
        int lowStockCount = 0;
        int outOfStockCount = 0;
        double totalValue = 0.0;
        int totalQuantity = 0;

        for (Produit p : produitList) {
            totalQuantity += p.getQuantite();
            totalValue += p.getQuantite() * p.getPrixUnitaire();

            if (p.getQuantite() == 0) {
                outOfStockCount++;
            } else if (p.getQuantite() <= p.getStockMinimal()) {
                lowStockCount++;
            }
        }

        StringBuilder stats = new StringBuilder();
        stats.append("INVENTORY STATISTICS:\n\n");
        stats.append("Total Products: ").append(totalProducts).append("\n");
        stats.append("Total Items in Stock: ").append(totalQuantity).append("\n");
        stats.append("Total Inventory Value: DH").append(String.format("%.2f", totalValue)).append("\n\n");
        stats.append("STOCK STATUS:\n");
        stats.append("• Well Stocked: ").append(totalProducts - lowStockCount - outOfStockCount).append("\n");
        stats.append("• Low Stock: ").append(lowStockCount).append("\n");
        stats.append("• Out of Stock: ").append(outOfStockCount).append("\n\n");

        if (totalProducts > 0) {
            stats.append("Average Value per Product: DH").append(String.format("%.2f", totalValue / totalProducts)).append("\n");
            stats.append("Average Quantity per Product: ").append(String.format("%.1f", (double) totalQuantity / totalProducts));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventory Statistics");
        alert.setHeaderText("Current Inventory Overview");
        alert.setContentText(stats.toString());
        alert.getDialogPane().setPrefSize(400, 350);
        alert.showAndWait();
    }

    // New method for data export
    @FXML
    void exporterDonnees(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Products Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("products_export.csv");

        Stage stage = (Stage) tableProduits.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.append("Reference,Designation,Color,Brand,Type,Quantity,Size,Unit Price,Min Stock\n");

                // Write data
                for (Produit p : produitList) {
                    writer.append(p.getReference()).append(",")
                            .append(escapeCsvField(p.getDesignation())).append(",")
                            .append(escapeCsvField(p.getCouleur())).append(",")
                            .append(escapeCsvField(p.getMarque())).append(",")
                            .append(escapeCsvField(p.getType())).append(",")
                            .append(String.valueOf(p.getQuantite())).append(",")
                            .append(escapeCsvField(p.getTaille())).append(",")
                            .append(String.valueOf(p.getPrixUnitaire())).append(",")
                            .append(String.valueOf(p.getStockMinimal())).append("\n");
                }

                showInfoDialog("Export Successful", "Data exported successfully to:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("Export Error", "Failed to export data: " + e.getMessage());
            }
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private void updateStatusLabel() {
        if (lblTotalProduits != null) {
            int total = produitList.size();
            int lowStock = 0;
            int outOfStock = 0;

            for (Produit p : produitList) {
                if (p.getQuantite() == 0) {
                    outOfStock++;
                } else if (p.getQuantite() <= p.getStockMinimal()) {
                    lowStock++;
                }
            }

            lblTotalProduits.setText("Total: " + total + " | Low Stock: " + lowStock + " | Out of Stock: " + outOfStock);
        }
    }

    @FXML
    private void btnOnActionSearch() {
        String keyword = SEARCH.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            tableProduits.setItems(produitList);
            return;
        }
        ObservableList<Produit> filteredList = FXCollections.observableArrayList();
        for (Produit p : produitList) {
            if (p.getReference().toLowerCase().contains(keyword)
                    || p.getDesignation().toLowerCase().contains(keyword)
                    || p.getCouleur().toLowerCase().contains(keyword)
                    || p.getMarque().toLowerCase().contains(keyword)
                    || p.getType().toLowerCase().contains(keyword)) {
                filteredList.add(p);
            }
        }
        tableProduits.setItems(filteredList);
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }
}