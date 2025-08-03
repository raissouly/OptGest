package sample;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ViewOrdonnancesController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private TextField searchField;
    @FXML private TableView<OrdonnanceTableModel> ordonnanceTable;
    @FXML private TableColumn<OrdonnanceTableModel, Integer> colId;
    @FXML private TableColumn<OrdonnanceTableModel, String> colClient;
    @FXML private TableColumn<OrdonnanceTableModel, String> colDoctor;
    @FXML private TableColumn<OrdonnanceTableModel, String> colDate;
    @FXML private TableColumn<OrdonnanceTableModel, String> colTypeVerre;
    @FXML private TableColumn<OrdonnanceTableModel, String> colPriorite;
    @FXML private TableColumn<OrdonnanceTableModel, String> colScan;
    @FXML private TableColumn<OrdonnanceTableModel, Void> colActions;

    // IMPORTANT: Inject the OrdonnanceController using its fx:id from the include
    // The name convention is [fx:id]Controller (e.g., ordonnanceFormTabController for fx:id="ordonnanceFormTab")
    @FXML private OrdonnanceController ordonnanceFormTabController;

    private ObservableList<OrdonnanceTableModel> ordonnancesList = FXCollections.observableArrayList();
    private ObservableList<OrdonnanceTableModel> filteredList = FXCollections.observableArrayList();

    // Services
    private ClientService clientService;
    private DoctorService doctorService;
    private OrdonnanceService ordonnanceService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        clientService = new ClientService();
        doctorService = new DoctorService();
        ordonnanceService = new OrdonnanceService();

        // يمكنك إضافة هذا التحقق للتأكد من أن المتحكم تم حقنه بنجاح
        if (ordonnanceFormTabController != null) {
            System.out.println("OrdonnanceController injected into ViewOrdonnancesController successfully via FXML!");
        } else {
            System.err.println("Error: OrdonnanceController was not injected. Check FXML fx:include fx:id and controller variable name.");
        }

        // التحقق من سلامة البيانات قبل التحميل
        validateDataIntegrity(); // تم إعادة إضافة هذه الدالة

        setupTableColumns(); // تم إعادة إضافة هذه الدالة
        loadDataFromServices(); // تم إعادة إضافة هذه الدالة
        ordonnanceTable.setItems(filteredList);

        // Setup search functionality
        setupSearchFilter(); // تم إعادة إضافة هذه الدالة
    }

    private void setupTableColumns() {
        // Configuration des colonnes - حل مخصص لعمود ID
        colId.setCellValueFactory(cellData -> {
            Integer id = cellData.getValue().getId();
            return new SimpleIntegerProperty(id).asObject();
        });

        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateFormatted"));
        colTypeVerre.setCellValueFactory(new PropertyValueFactory<>("typeVerre"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colScan.setCellValueFactory(new PropertyValueFactory<>("scanFileName"));

        // تحسين عرض عمود العميل
        colClient.setCellFactory(column -> new TableCell<OrdonnanceTableModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Données manquantes")) {
                        setStyle("-fx-text-fill: red; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        // Configuration de la colonne Actions avec des boutons
        colActions.setCellFactory(param -> new TableCell<OrdonnanceTableModel, Void>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button scanBtn = new Button("📄");
            private final HBox buttonsBox = new HBox(3);

            {
                viewBtn.setTooltip(new Tooltip("Voir les détails"));
                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                scanBtn.setTooltip(new Tooltip("Voir le scan"));

                viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 30;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-pref-width: 30;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 30;");
                scanBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-pref-width: 30;");

                buttonsBox.setAlignment(Pos.CENTER);
                buttonsBox.getChildren().addAll(viewBtn, editBtn, scanBtn, deleteBtn);

                viewBtn.setOnAction(event -> {
                    OrdonnanceTableModel model = getTableView().getItems().get(getIndex());
                    handleViewOrdonnance(model);
                });

                editBtn.setOnAction(event -> {
                    OrdonnanceTableModel model = getTableView().getItems().get(getIndex());
                    handleEditOrdonnance(model);
                });

                scanBtn.setOnAction(event -> {
                    OrdonnanceTableModel model = getTableView().getItems().get(getIndex());
                    handleViewScan(model);
                });

                deleteBtn.setOnAction(event -> {
                    OrdonnanceTableModel model = getTableView().getItems().get(getIndex());
                    handleDeleteOrdonnance(model);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterOrdonnances(newValue);
        });
    }

    private void filterOrdonnances(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredList.setAll(ordonnancesList);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            List<OrdonnanceTableModel> filtered = ordonnancesList.stream()
                    .filter(ordonnance ->
                            ordonnance.getClientName().toLowerCase().contains(lowerCaseFilter) ||
                                    ordonnance.getDoctorName().toLowerCase().contains(lowerCaseFilter) ||
                                    ordonnance.getTypeVerre().toLowerCase().contains(lowerCaseFilter) ||
                                    ordonnance.getPriorite().toLowerCase().contains(lowerCaseFilter) ||
                                    String.valueOf(ordonnance.getId()).contains(lowerCaseFilter)
                    )
                    .sorted((o1, o2) -> Integer.compare(o1.getId(), o2.getId())) // ترتيب النتائج
                    .collect(Collectors.toList());

            filteredList.setAll(filtered);
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText();
        filterOrdonnances(searchText);

        if (filteredList.isEmpty() && !searchText.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Recherche", "Aucune ordonnance trouvée pour: " + searchText);
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDataFromServices();
        searchField.clear();
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Liste des ordonnances actualisée");
    }

    // تحسين طريقة تحميل البيانات مع إضافة المزيد من التفاصيل
    private void loadDataFromServices() {
        ordonnancesList.clear();
        List<Ordonnance> ordonnances = ordonnanceService.getAllOrdonnances();

        System.out.println("Loading " + ordonnances.size() + " ordonnances...");

        for (Ordonnance ord : ordonnances) {
            System.out.println("Processing Ordonnance ID: " + ord.getId() + ", Client ID: " + ord.getClientId());

            // تحميل بيانات العميل والطبيب
            Clients client = null;
            Doctor doctor = null;

            try {
                client = clientService.getClientById(ord.getClientId());
                doctor = doctorService.getDoctorById(ord.getDoctorId());
            } catch (Exception e) {
                System.err.println("Error loading client/doctor data: " + e.getMessage());
            }

            // تحسين معالجة اسم العميل
            String clientName = buildClientDisplayName(client, ord.getClientId());
            String doctorName = buildDoctorDisplayName(doctor, ord.getDoctorId());

            System.out.println("Client Name: " + clientName);
            System.out.println("Doctor Name: " + doctorName);

            // تحويل التاريخ إلى String إذا لزم الأمر
            String dateString = "";
            if (ord.getDate_Ordonnance() != null) {
                dateString = ord.getDate_Ordonnance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                dateString = "Date non spécifiée";
            }

            OrdonnanceTableModel model = new OrdonnanceTableModel(
                    ord.getId(),
                    clientName,
                    doctorName,
                    dateString,
                    ord.getLens_type() != null ? ord.getLens_type() : "Non spécifié",
                    ord.getPriority_level() != null ? ord.getPriority_level() : "Normale",
                    ord.getScan_path() != null ? extractFileName(ord.getScan_path()) : "Aucun scan"
            );
            model.setOriginalOrdonnance(ord);
            ordonnancesList.add(model);
        }

        // ترتيب القائمة بناءً على ID قبل عرضها
        ordonnancesList.sort((o1, o2) -> Integer.compare(o1.getId(), o2.getId()));

        filteredList.setAll(ordonnancesList);
        System.out.println("Loaded " + ordonnancesList.size() + " ordonnances successfully.");

        // طباعة أسماء العملاء للتحقق
        System.out.println("\n=== Liste des clients chargés ===");
        for (OrdonnanceTableModel model : ordonnancesList) {
            System.out.println("ID: " + model.getId() + " -> Client: " + model.getClientName());
        }

        // تطبيق الترتيب على الجدول
        colId.setSortType(TableColumn.SortType.ASCENDING);
        ordonnanceTable.getSortOrder().clear();
        ordonnanceTable.getSortOrder().add(colId);
    }

    private String buildClientDisplayName(Clients client, int clientId) {
        if (client != null) {
            String prenom = client.getPrenom() != null ? client.getPrenom().trim() : "";
            String nom = client.getNom() != null ? client.getNom().trim() : "";

            System.out.println("Client data - ID: " + clientId + ", Prenom: '" + prenom + "', Nom: '" + nom + "'");

            // إذا كان كلا الاسمين متوفرين
            if (!prenom.isEmpty() && !nom.isEmpty()) {
                return prenom + " " + nom;
            }
            // إذا كان الاسم الأول فقط متوفر
            else if (!prenom.isEmpty()) {
                return prenom;
            }
            // إذا كان اسم العائلة فقط متوفر
            else if (!nom.isEmpty()) {
                return nom;
            }
            // إذا لم يكن هناك اسم متوفر
            else {
                return "Client #" + clientId + " (Nom manquant)";
            }
        } else {
            System.err.println("⚠️  Client not found for ID: " + clientId);
            return "Client #" + clientId + " (Données manquantes)";
        }
    }

    // طريقة محسنة لعرض اسم الطبيب
    private String buildDoctorDisplayName(Doctor doctor, int doctorId) {
        if (doctor != null) {
            String prenom = doctor.getPrenom() != null ? doctor.getPrenom().trim() : "";
            String nom = doctor.getNom() != null ? doctor.getNom().trim() : "";

            System.out.println("Doctor data - ID: " + doctorId + ", Prenom: '" + prenom + "', Nom: '" + nom + "'");

            if (!prenom.isEmpty() && !nom.isEmpty()) {
                return "Dr. " + prenom + " " + nom;
            } else if (!prenom.isEmpty()) {
                return "Dr. " + prenom;
            } else if (!nom.isEmpty()) {
                return "Dr. " + nom;
            } else {
                return "Docteur #" + doctorId + " (Nom manquant)";
            }
        } else {
            System.err.println("⚠️  Doctor not found for ID: " + doctorId);
            return "Docteur #" + doctorId + " (Données manquantes)";
        }
    }

    // طريقة للتحقق من سلامة البيانات
    private void validateDataIntegrity() {
        try {
            System.out.println("=== Validation de l'intégrité des données ===");

            List<Ordonnance> ordonnances = ordonnanceService.getAllOrdonnances();
            int invalidClients = 0;
            int invalidDoctors = 0;
            int totalOrdonnances = ordonnances.size();

            System.out.println("Total ordonnances: " + totalOrdonnances);

            for (Ordonnance ord : ordonnances) {
                Clients client = clientService.getClientById(ord.getClientId());
                Doctor doctor = doctorService.getDoctorById(ord.getDoctorId());

                if (client == null) {
                    invalidClients++;
                    System.err.println("⚠️  Ordonnance #" + ord.getId() + " has invalid client ID: " + ord.getClientId());
                }

                if (doctor == null) {
                    invalidDoctors++;
                    System.err.println("⚠️  Ordonnance #" + ord.getId() + " has invalid doctor ID: " + ord.getDoctorId());
                }
            }

            System.out.println("\n=== Résumé de validation ===");
            System.out.println("Total ordonnances: " + totalOrdonnances);
            System.out.println("Clients valides: " + (totalOrdonnances - invalidClients));
            System.out.println("Médecins valides: " + (totalOrdonnances - invalidDoctors));

            if (invalidClients > 0 || invalidDoctors > 0) {
                System.err.println("\n⚠️  Problèmes détectés:");
                System.err.println("- Clients invalides: " + invalidClients);
                System.err.println("- Médecins invalides: " + invalidDoctors);
            } else {
                System.out.println("✅ Validation réussie - Toutes les données sont cohérentes.");
            }

        } catch (Exception e) {
            System.err.println("Error during data integrity validation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) return "Aucun scan";
        File file = new File(path);
        return file.getName();
    }

    private void handleViewOrdonnance(OrdonnanceTableModel model) {
        Ordonnance ordonnance = model.getOriginalOrdonnance();

        // الحصول على بيانات العميل والطبيب مرة أخرى للتأكد
        Clients client = clientService.getClientById(ordonnance.getClientId());
        Doctor doctor = doctorService.getDoctorById(ordonnance.getDoctorId());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'ordonnance");
        alert.setHeaderText("Ordonnance #" + ordonnance.getId());

        StringBuilder details = new StringBuilder();

        // معلومات العميل
        details.append("=== INFORMATIONS PATIENT ===\n");
        if (client != null) {
            details.append(String.format("Nom: %s %s\n",
                    client.getPrenom() != null ? client.getPrenom() : "",
                    client.getNom() != null ? client.getNom() : ""));
            details.append(String.format("ID Client: %d\n", client.getId()));
            details.append(String.format("Téléphone: %s\n",
                    client.getTelephone() != null ? client.getTelephone() : "Non renseigné"));
            details.append(String.format("Email: %s\n",
                    client.getEmail() != null ? client.getEmail() : "Non renseigné"));
        } else {
            details.append(String.format("⚠️  Client ID: %d (Données manquantes)\n", ordonnance.getClientId()));
        }

        // معلومات الطبيب
        details.append("\n=== INFORMATIONS MÉDECIN ===\n");
        if (doctor != null) {
            details.append(String.format("Médecin: Dr. %s %s\n",
                    doctor.getPrenom() != null ? doctor.getPrenom() : "",
                    doctor.getNom() != null ? doctor.getNom() : ""));
            details.append(String.format("ID Médecin: %d\n", doctor.getId()));
            details.append(String.format("Spécialité: %s\n",
                    doctor.getSpecialite() != null ? doctor.getSpecialite() : "Non spécifiée"));
        } else {
            details.append(String.format("⚠️  Médecin ID: %d (Données manquantes)\n", ordonnance.getDoctorId()));
        }

        // معلومات الطلب
        details.append("\n=== INFORMATIONS ORDONNANCE ===\n");
        details.append(String.format("Date: %s\n", model.getDateFormatted()));
        details.append(String.format("Type de verre: %s\n", model.getTypeVerre()));
        details.append(String.format("Priorité: %s\n", model.getPriorite()));
        details.append(String.format("Description: %s\n",
                ordonnance.getDescription() != null ? ordonnance.getDescription() : "Aucune"));

        // Optical details
        details.append("\n=== MESURES OPTIQUES ===\n");
        if (ordonnance.hasRightEyePrescription()) {
            details.append("👁️ Œil Droit: ").append(ordonnance.getRightEyeSummary()).append("\n");
        }
        if (ordonnance.hasLeftEyePrescription()) {
            details.append("👁️ Œil Gauche: ").append(ordonnance.getLeftEyeSummary()).append("\n");
        }

        if (ordonnance.getDistance_Pupillaire() != null) { // تم تصحيح الاسم هنا
            details.append("📏 Distance Pupillaire: ").append(ordonnance.getDistance_Pupillaire()).append("\n");
        }
        if (ordonnance.getHauteur_Montage() != null) { // تم تصحيح الاسم هنا
            details.append("📐 Hauteur de Montage: ").append(ordonnance.getHauteur_Montage()).append("\n");
        }

        // معلومات الملف
        details.append("\n=== FICHIER SCAN ===\n");
        details.append(String.format("Fichier: %s\n", model.getScanFileName()));
        if (ordonnance.getScan_path() != null) { // تم تصحيح الاسم هنا
            details.append(String.format("Chemin: %s\n", ordonnance.getScan_path()));
        }

        alert.setContentText(details.toString());
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setPrefHeight(500);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private Integer editingOrdonnanceId = null;

    private void handleEditOrdonnance(OrdonnanceTableModel model) {
        try {
            // اختيار التبويب الخاص بإنشاء Ordonnance
            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().select(0); // أول تبويب
            }

            // تحميل Ordonnance كاملة من قاعدة البيانات
            OrdonnanceService service = new OrdonnanceService();
            Ordonnance fullOrd = service.getOrdonnanceById(model.getId());

            if (fullOrd != null) {
                editingOrdonnanceId = fullOrd.getId(); // نحتفظ بالـ ID أثناء التعديل

                // تعبئة الفورم داخل OrdonnanceController
                // الآن نستخدم المتحكم الذي تم حقنه: ordonnanceFormTabController
                if (ordonnanceFormTabController != null) {
                    ordonnanceFormTabController.fillFormFromOrdonnance(fullOrd);

                    showAlert(Alert.AlertType.INFORMATION, "Modification", "Ordonnance #" + fullOrd.getId() + " chargée pour modification.");
                } else {
                    // هذا الخطأ يجب ألا يظهر إذا كان الحقن عبر FXML يعمل بشكل صحيح!
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le contrôleur d'ordonnance est null ! (حقن FXML فشل)");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ordonnance introuvable.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l’ordonnance:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleViewScan(OrdonnanceTableModel model) {
        Ordonnance ordonnance = model.getOriginalOrdonnance();
        String scanPath = ordonnance.getScan_path(); // تم تصحيح الاسم هنا

        if (scanPath == null || scanPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Scan", "Aucun scan disponible pour cette ordonnance");
            return;
        }

        File scanFile = new File(scanPath);
        if (!scanFile.exists()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier scan n'existe pas: " + scanPath);
            return;
        }

        try {
            Desktop.getDesktop().open(scanFile);
        } catch (IOException e) { // تم تغيير Exception إلى IOException
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
            e.printStackTrace(); // إضافة طباعة تتبع الخطأ للمساعدة في التصحيح
        } catch (UnsupportedOperationException e) { // إضافة معالجة لعدم دعم العملية
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier: Opération non supportée sur ce système.");
            e.printStackTrace();
        }
    }

    // دالة showAlert الموحدة
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleDeleteOrdonnance(OrdonnanceTableModel model) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'ordonnance #" + model.getId());
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette ordonnance?\n" +
                "Patient: " + model.getClientName() + "\n" +
                "Date: " + model.getDateFormatted() + "\n\n" +
                "⚠️ Cette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Get the ID from the original ordonnance object
                    Ordonnance originalOrdonnance = model.getOriginalOrdonnance();
                    int ordonnanceId = originalOrdonnance.getId();

                    boolean success = ordonnanceService.deleteOrdonnance(ordonnanceId);
                    if (success) {
                        ordonnancesList.remove(model);
                        filteredList.remove(model);
                        showAlert(Alert.AlertType.INFORMATION, "Suppression", "Ordonnance supprimée avec succès");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'ordonnance");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // تم حذف الدوال القديمة showAlert هنا لتجنب التكرار والخلط
    // private void showAlert(String title, String message, Alert.AlertType type) { ... }
    // private String getClientDisplayName(Clients client, int clientId) { ... }
    // private String getDoctorDisplayName(Doctor doctor, int doctorId) { ... }
}
