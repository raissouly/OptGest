package sample;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrdonnanceController {

    // Original FXML elements
    @FXML private ComboBox<String> comboClient;
    @FXML private ComboBox<String> comboDoctor;
    @FXML private DatePicker dateOrdonnance;
    @FXML private TextArea txtDescription;
    @FXML private Label lblScanPath;

    // New FXML elements for enhanced interface
    @FXML private RadioButton radioNormal;
    @FXML private RadioButton radioUrgent;
    @FXML private RadioButton radioCritical;
    @FXML private Button btnCancel;
    @FXML private Button btnReset;
    @FXML private Button btnClearScan;
    @FXML private Button btnSaveOrdonnance;
    @FXML private Button btnFixData;
    @FXML private Button btnRefreshData;
    @FXML private VBox imagePreviewContainer;
    @FXML private ImageView imagePreview;
    @FXML
    private TextField txtSphereLeft;

    @FXML
    private TextField txtCylindreLeft;

    @FXML
    private TextField txtAxeLeft;

    @FXML
    private TextField txtAdditionLeft;

    @FXML
    private TextField txtSphereRight;

    @FXML
    private TextField txtCylindreRight;

    @FXML
    private TextField txtAxeRight;

    @FXML
    private TextField txtAdditionRight;

    @FXML
    private TextField txtPD;

    @FXML
    private TextField txtHauteurMontage;

    @FXML
    private Button btnChooseScan;




    // Priority toggle group
    private ToggleGroup priorityGroup;

    // Database and file handling
    private File selectedFile;
    private Connection connection;
    private ExecutorService executorService;

    @FXML
    public void initialize() {
        executorService = Executors.newCachedThreadPool();
        setupPriorityGroup();
        setupDatabase();
        setupUIComponents();
        loadDataAsync();
        setupEventListeners();
    }

    private void setupPriorityGroup() {
        priorityGroup = new ToggleGroup();
        radioNormal.setToggleGroup(priorityGroup);
        radioUrgent.setToggleGroup(priorityGroup);
        radioCritical.setToggleGroup(priorityGroup);

        // Set default selection
        radioNormal.setSelected(true);
    }

    private void setupDatabase() {
        try {
            connection = Connectiondb.Connection();
            if (connection == null) {
                showAlert(Alert.AlertType.ERROR, "Database Connection Error",
                        "Failed to connect to database. Please check your connection settings.");
                return;
            }

            // Check database data integrity
            checkDatabaseData();

            System.out.println("Database connection established successfully");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Error initializing database connection: " + e.getMessage());
        }
    }

    private void setupUIComponents() {
        // Set current date as default
        dateOrdonnance.setValue(LocalDate.now());

        // Add tooltips for better user experience
        comboClient.setTooltip(new Tooltip("Select the patient for this prescription"));
        comboDoctor.setTooltip(new Tooltip("Select the prescribing doctor"));
        dateOrdonnance.setTooltip(new Tooltip("Select the prescription date"));
        txtDescription.setTooltip(new Tooltip("Enter detailed prescription information and instructions"));

        // Set text area properties
        txtDescription.setWrapText(true);

        // Initially hide image preview
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setVisible(false);
            imagePreviewContainer.setManaged(false);
        }
    }

    private void setupEventListeners() {
        // Add listener for client selection
        comboClient.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Selected client: " + newValue);
            }
        });

        // Add listener for doctor selection
        comboDoctor.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Selected doctor: " + newValue);
            }
        });
    }

    private void loadDataAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadClients();
                loadDoctors();
                return null;
            }

            @Override
            protected void succeeded() {
                // Enable UI components after data is loaded
                javafx.application.Platform.runLater(() -> {
                    comboClient.setDisable(false);
                    comboDoctor.setDisable(false);
                    System.out.println("Data loading completed successfully");
                });
            }

            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Loading Error",
                            "Failed to load data from database: " + (getException() != null ? getException().getMessage() : "Unknown error"));
                });
            }
        };

        // Disable comboboxes during loading
        comboClient.setDisable(true);
        comboDoctor.setDisable(true);

        executorService.submit(loadTask);
    }

    private void loadClients() {
        try {
            // Enhanced query to avoid empty names
            String query = "SELECT id, nom, prenom FROM clients WHERE nom IS NOT NULL AND nom != '' AND TRIM(nom) != '' ORDER BY nom";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            // Clear existing items
            javafx.application.Platform.runLater(() -> comboClient.getItems().clear());

            int clientCount = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");

                // Validate and clean name
                if (nom == null || nom.trim().isEmpty()) {
                    nom = "Unknown Client";
                }

                // Handle empty first name
                if (prenom == null || prenom.trim().isEmpty()) {
                    prenom = "";
                }

                String clientInfo = String.format("%d - %s %s", id, nom.trim(), prenom.trim()).trim();
                clientCount++;

                javafx.application.Platform.runLater(() -> {
                    comboClient.getItems().add(clientInfo);
                });
            }

            System.out.println("Loaded " + clientCount + " clients");

            // Handle empty client list
            if (clientCount == 0) {
                javafx.application.Platform.runLater(() -> {
                    comboClient.getItems().add("No clients found");
                    comboClient.setDisable(true);
                    showAlert(Alert.AlertType.WARNING, "No Clients",
                            "No clients found in the database. Please add clients first.");
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Error loading clients: " + e.getMessage());

                comboClient.getItems().add("Error loading data");
                comboClient.setDisable(true);
            });
        }
    }

    private void loadDoctors() {
        try {
            // Enhanced query to avoid empty names
            String query = "SELECT id, nom, prenom, specialite FROM doctor WHERE nom IS NOT NULL AND nom != '' AND TRIM(nom) != '' ORDER BY nom";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            // Clear existing items
            javafx.application.Platform.runLater(() -> comboDoctor.getItems().clear());

            int doctorCount = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String specialite = rs.getString("specialite");

                // Validate and clean name
                if (nom == null || nom.trim().isEmpty()) {
                    nom = "Unknown Doctor";
                }

                // Handle empty first name
                if (prenom == null || prenom.trim().isEmpty()) {
                    prenom = "";
                }

                // Handle empty specialty
                if (specialite == null || specialite.trim().isEmpty()) {
                    specialite = "General";
                }

                String doctorInfo = String.format("%d - Dr. %s %s (%s)",
                        id, nom.trim(), prenom.trim(), specialite.trim()).trim();
                doctorCount++;

                javafx.application.Platform.runLater(() -> {
                    comboDoctor.getItems().add(doctorInfo);
                });
            }

            System.out.println("Loaded " + doctorCount + " doctors");

            // Handle empty doctor list
            if (doctorCount == 0) {
                javafx.application.Platform.runLater(() -> {
                    comboDoctor.getItems().add("No doctors found");
                    comboDoctor.setDisable(true);
                    showAlert(Alert.AlertType.WARNING, "No Doctors",
                            "No doctors found in the database. Please add doctors first.");
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Error loading doctors: " + e.getMessage());

                comboDoctor.getItems().add("Error loading data");
                comboDoctor.setDisable(true);
            });
        }
    }

    private void checkDatabaseData() {
        try {
            // Check clients table
            String clientQuery = "SELECT COUNT(*) as total, " +
                    "COUNT(CASE WHEN nom IS NULL OR nom = '' OR TRIM(nom) = '' THEN 1 END) as empty_names " +
                    "FROM clients";
            PreparedStatement ps = connection.prepareStatement(clientQuery);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int totalClients = rs.getInt("total");
                int emptyNames = rs.getInt("empty_names");

                System.out.println("Total clients: " + totalClients);
                System.out.println("Clients with empty names: " + emptyNames);

                if (emptyNames > 0) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert(Alert.AlertType.WARNING, "Data Issue",
                                String.format("Found %d clients with empty names out of %d total clients. Consider using the 'Fix Data' button.",
                                        emptyNames, totalClients));
                    });
                }
            }

            // Check doctors table
            String doctorQuery = "SELECT COUNT(*) as total, " +
                    "COUNT(CASE WHEN nom IS NULL OR nom = '' OR TRIM(nom) = '' THEN 1 END) as empty_names " +
                    "FROM doctor";
            ps = connection.prepareStatement(doctorQuery);
            rs = ps.executeQuery();

            if (rs.next()) {
                int totalDoctors = rs.getInt("total");
                int emptyNames = rs.getInt("empty_names");

                System.out.println("Total doctors: " + totalDoctors);
                System.out.println("Doctors with empty names: " + emptyNames);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error checking database data: " + e.getMessage());
        }
    }










    public void fillFormFromOrdonnance(Ordonnance ord) {
        try {
            // تحديد المريض والطبيب من ComboBox بناءً على ID
            for (String item : comboClient.getItems()) {
                if (item.startsWith(ord.getClientId() + " -")) {
                    comboClient.setValue(item);
                    break;
                }
            }

            for (String item : comboDoctor.getItems()) {
                if (item.startsWith(ord.getDoctorId() + " -")) {
                    comboDoctor.setValue(item);
                    break;
                }
            }

            // باقي البيانات
            dateOrdonnance.setValue(ord.getDate_Ordonnance());
            txtDescription.setText(ord.getDescription());

            // Prescription OD (Right Eye)
            txtSphereRight.setText(ord.getOD_Sph());
            txtCylindreRight.setText(ord.getOD_Cyl());
            txtAxeRight.setText(ord.getOD_Axe());
            txtAdditionRight.setText(ord.getOD_Add());

            // Prescription OG (Left Eye)
            txtSphereLeft.setText(ord.getOG_Sph());
            txtCylindreLeft.setText(ord.getOG_Cyl());
            txtAxeLeft.setText(ord.getOG_Axe());
            txtAdditionLeft.setText(ord.getOG_Add());

            // PD و Hauteur
            txtPD.setText(ord.getDistance_Pupillaire());
            txtHauteurMontage.setText(ord.getHauteur_Montage());

            // الأولوية
            String priority = ord.getPriority_level();
            switch (priority) {
                case "Urgent":
                    radioUrgent.setSelected(true);
                    break;
                case "Critical":
                    radioCritical.setSelected(true);
                    break;
                default:
                    radioNormal.setSelected(true);
                    break;
            }

            // تحميل ملف scan
            selectedFile = (ord.getScan_path() != null && !ord.getScan_path().trim().isEmpty())
                    ? new File(ord.getScan_path()) : null;

            if (selectedFile != null && selectedFile.exists()) {
                lblScanPath.setText(selectedFile.getName());
                lblScanPath.setStyle("-fx-text-fill: #27ae60;");
                if (isImageFile(selectedFile)) {
                    displayImagePreview(selectedFile);
                } else {
                    hideImagePreview();
                }
            } else {
                handleClearScan(null);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de remplir le formulaire :\n" + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void handleFixData(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Fix Data");
        confirmAlert.setHeaderText("Fix Empty Names");
        confirmAlert.setContentText("This will update all empty names in the database. Are you sure you want to continue?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            fixEmptyNames();
        }
    }

    @FXML
    void handleRefreshData(ActionEvent event) {
        loadDataAsync();
        showAlert(Alert.AlertType.INFORMATION, "Refresh Complete", "Data has been refreshed successfully.");
    }

    private void fixEmptyNames() {
        Task<Void> fixTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int clientsUpdated = 0;
                int doctorsUpdated = 0;

                // Fix empty client names
                String updateClients = "UPDATE clients SET nom = 'Unknown Client' WHERE nom IS NULL OR nom = '' OR TRIM(nom) = ''";
                PreparedStatement ps = connection.prepareStatement(updateClients);
                clientsUpdated = ps.executeUpdate();

                // Fix empty doctor names
                String updateDoctors = "UPDATE doctor SET nom = 'Unknown Doctor' WHERE nom IS NULL OR nom = '' OR TRIM(nom) = ''";
                ps = connection.prepareStatement(updateDoctors);
                doctorsUpdated = ps.executeUpdate();

                System.out.println("Fixed " + clientsUpdated + " clients and " + doctorsUpdated + " doctors");

                final int finalClientsUpdated = clientsUpdated;
                final int finalDoctorsUpdated = doctorsUpdated;

                javafx.application.Platform.runLater(() -> {
                    if (finalClientsUpdated > 0 || finalDoctorsUpdated > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Data Fixed",
                                String.format("Successfully fixed %d clients and %d doctors. Refreshing data...",
                                        finalClientsUpdated, finalDoctorsUpdated));

                        // Reload data after fixing
                        loadDataAsync();
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "No Changes",
                                "No empty names were found to fix.");
                    }
                });

                return null;
            }

            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Fix Failed",
                            "Failed to fix empty names: " + (getException() != null ? getException().getMessage() : "Unknown error"));
                });
            }
        };

        executorService.submit(fixTask);
    }

    @FXML
    void handleChooseScan(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Prescription Scan");

        // Add file filters
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf");
        FileChooser.ExtensionFilter allFilter =
                new FileChooser.ExtensionFilter("All Files", "*.*");

        fileChooser.getExtensionFilters().addAll(imageFilter, pdfFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(imageFilter);

        selectedFile = fileChooser.showOpenDialog(comboClient.getScene().getWindow());

        if (selectedFile != null) {
            lblScanPath.setText(selectedFile.getName());
            lblScanPath.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

            // Show image preview if it's an image file
            if (isImageFile(selectedFile)) {
                displayImagePreview(selectedFile);
            } else {
                hideImagePreview();
            }
        }
    }

    @FXML
    void handleClearScan(ActionEvent event) {
        selectedFile = null;
        lblScanPath.setText("No file selected");
        lblScanPath.setStyle("-fx-text-fill: #e74c3c;");
        hideImagePreview();
    }

    @FXML
    void handleSaveOrdonnance(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Save");
        confirmAlert.setHeaderText("Save Prescription");
        confirmAlert.setContentText("Are you sure you want to save this prescription?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            savePrescriptionAsync();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancel");
        confirmAlert.setHeaderText("Cancel Operation");
        confirmAlert.setContentText("Are you sure you want to cancel? All unsaved changes will be lost.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearFields();
        }
    }

    @FXML
    void handleReset(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Reset");
        confirmAlert.setHeaderText("Reset Form");
        confirmAlert.setContentText("Are you sure you want to reset all fields?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearFields();
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (comboClient.getValue() == null || comboClient.getValue().trim().isEmpty() ||
                comboClient.getValue().contains("No clients found") || comboClient.getValue().contains("Error loading")) {
            errors.append("• Please select a valid patient\n");
        }

        if (comboDoctor.getValue() == null || comboDoctor.getValue().trim().isEmpty() ||
                comboDoctor.getValue().contains("No doctors found") || comboDoctor.getValue().contains("Error loading")) {
            errors.append("• Please select a valid doctor\n");
        }

        if (dateOrdonnance.getValue() == null) {
            errors.append("• Please select a prescription date\n");
        } else if (dateOrdonnance.getValue().isAfter(LocalDate.now())) {
            errors.append("• Prescription date cannot be in the future\n");
        }

        if (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty()) {
            errors.append("• Please enter prescription description\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please correct the following errors:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void savePrescriptionAsync() {
        // Disable save button during save operation
        btnSaveOrdonnance.setDisable(true);
        btnSaveOrdonnance.setText("Saving...");

        Task<Boolean> saveTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return savePrescriptionToDatabase();
            }

            @Override
            protected void succeeded() {
                javafx.application.Platform.runLater(() -> {
                    btnSaveOrdonnance.setDisable(false);
                    btnSaveOrdonnance.setText("Save Prescription");

                    if (getValue()) {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Prescription saved successfully!");
                        clearFields();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Save Error",
                                "Failed to save prescription. Please try again.");
                    }
                });
            }

            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    btnSaveOrdonnance.setDisable(false);
                    btnSaveOrdonnance.setText("Save Prescription");
                    showAlert(Alert.AlertType.ERROR, "Save Error",
                            "An error occurred while saving: " + (getException() != null ? getException().getMessage() : "Unknown error"));
                });
            }
        };

        executorService.submit(saveTask);
    }

    private boolean savePrescriptionToDatabase() {
        try {
            String clientSelection = comboClient.getValue();
            String doctorSelection = comboDoctor.getValue();

            // Validate selections
            if (clientSelection == null || clientSelection.contains("No clients found") ||
                    clientSelection.contains("Error loading")) {
                throw new Exception("Invalid client selection");
            }

            if (doctorSelection == null || doctorSelection.contains("No doctors found") ||
                    doctorSelection.contains("Error loading")) {
                throw new Exception("Invalid doctor selection");
            }

            LocalDate date = dateOrdonnance.getValue();
            String description = txtDescription.getText().trim();
            String scanPath = (selectedFile != null) ? selectedFile.getAbsolutePath() : null;

            // Get priority level
            String priority = "Normal";
            if (radioUrgent.isSelected()) priority = "Urgent";
            else if (radioCritical.isSelected()) priority = "Critical";

            // Extract IDs from selections
            int idClient = Integer.parseInt(clientSelection.split(" - ")[0]);
            int idDoctor = Integer.parseInt(doctorSelection.split(" - ")[0]);

            // Verify that client and doctor exist
            if (!verifyClientExists(idClient)) {
                throw new Exception("Selected client does not exist in database");
            }

            if (!verifyDoctorExists(idDoctor)) {
                throw new Exception("Selected doctor does not exist in database");
            }

            String sql = "INSERT INTO ordonnances (ID_Client, Date_Ordonnance, Description, ID_Doctor, scan_path, priority_level, created_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idClient);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, description);
            ps.setInt(4, idDoctor);
            ps.setString(5, scanPath);
            ps.setString(6, priority);
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int result = ps.executeUpdate();
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving prescription: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyClientExists(int clientId) {
        try {
            String query = "SELECT COUNT(*) FROM clients WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyDoctorExists(int doctorId) {
        try {
            String query = "SELECT COUNT(*) FROM doctor WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void clearFields() {
        comboClient.setValue(null);
        comboDoctor.setValue(null);
        dateOrdonnance.setValue(LocalDate.now());
        txtDescription.clear();
        radioNormal.setSelected(true);
        handleClearScan(null);

        // Add fade transition for smooth clearing
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), txtDescription);
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") ||
                fileName.endsWith(".jpeg") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp");
    }

    private void displayImagePreview(File imageFile) {
        try {
            if (imagePreview != null && imagePreviewContainer != null) {
                Image image = new Image(new FileInputStream(imageFile));
                imagePreview.setImage(image);

                imagePreviewContainer.setVisible(true);
                imagePreviewContainer.setManaged(true);

                // Add fade-in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), imagePreviewContainer);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.WARNING, "Image Preview",
                    "Cannot preview selected image file.");
        }
    }

    private void hideImagePreview() {
        if (imagePreviewContainer != null && imagePreview != null && imagePreviewContainer.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), imagePreviewContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                imagePreviewContainer.setVisible(false);
                imagePreviewContainer.setManaged(false);
                imagePreview.setImage(null);
            });
            fadeOut.play();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        try {
            Alert alert = new Alert(alertType);
            alert.setTitle(title != null ? title : "Alert");
            alert.setHeaderText(null);
            alert.setContentText(message != null ? message : "No message");

            // Only add styles if the CSS file exists
            try {
                DialogPane dialogPane = alert.getDialogPane();
                String cssPath = getClass().getResource("styles.css").toExternalForm();
                if (cssPath != null) {
                    dialogPane.getStylesheets().add(cssPath);
                }
            } catch (Exception cssException) {
                // Ignore CSS loading errors - alert will still work without styling
                System.out.println("Warning: Could not load CSS styles for alert");
            }

            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cleanup method for when the controller is destroyed
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}