package sample;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class RendezVousController implements Initializable {

    // Éléments FXML principaux
    @FXML private VBox notificationBox;
    @FXML private ListView<String> notificationList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private Button btnNewAppointment, btnClearFilters;

    // Tableau des rendez-vous
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colDate, colTime, colClient, colPhone, colOptician, colService, colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;

    // Statistiques
    @FXML private Label lblTotalAppointments, lblTodayAppointments, lblConfirmedAppointments, lblPendingAppointments;

    // Formulaire (Dialog)
    @FXML private Dialog<ButtonType> appointmentDialog;
    @FXML private TextField txtClientName, txtClientPhone;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timePicker, serviceCombo, opticianCombo, statusCombo;
    @FXML private TextArea txtNotes;
    @FXML private Label lblValidationError;

    // Dialogs
    @FXML private Alert confirmationAlert;
    @FXML private Dialog<ButtonType> detailsDialog;
    @FXML private VBox detailsContainer;

    // Service de base de données
    private RendezVousService rendezVousService;

    // Données
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private FilteredList<Appointment> filteredAppointments;
    private Appointment currentAppointment = null;
    private Timer notificationTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service de base de données
        rendezVousService = new RendezVousService();

        initializeData();
        setupTable();
        setupFilters();
        setupNotifications();
        loadDataFromDatabase(); // Charger depuis la base de données
        updateStatistics();
    }

    private void initializeData() {
        filteredAppointments = new FilteredList<>(appointments);
        appointmentTable.setItems(filteredAppointments);

        // Initialiser les ComboBox
        statusFilter.setItems(FXCollections.observableArrayList(
                "Tous", "En attente", "Confirmé", "Annulé", "Terminé"
        ));
        statusFilter.setValue("Tous");

        // Heures de consultation (9h-18h, intervalles de 15min)
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 9; hour <= 18; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                if (hour == 18 && minute > 0) break;
                timeSlots.add(String.format("%02d:%02d", hour, minute));
            }
        }

        timePicker.setItems(timeSlots);

        // Services disponibles
        serviceCombo.setItems(FXCollections.observableArrayList(
                "Examen de vue", "Contrôle lunettes", "Examen complet",
                "Consultation urgente", "Essayage montures", "Livraison lunettes",
                "Réparation", "Ajustement", "Consultation enfant"
        ));

        try {
            opticianCombo.setItems(rendezVousService.getAllDoctors());
        } catch (Exception e) {
            showError("Erreur chargement des opticiens", "Impossible de charger les opticiens : " + e.getMessage());
        }



        // Statuts
        statusCombo.setItems(FXCollections.observableArrayList(
                "En attente", "Confirmé", "Annulé", "Terminé"
        ));
        statusCombo.setValue("En attente");
    }

    private void setupTable() {
        // Configuration des colonnes
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("clientPhone"));
        colOptician.setCellValueFactory(new PropertyValueFactory<>("optician"));
        colService.setCellValueFactory(new PropertyValueFactory<>("service"));

        // Colonne statut avec style
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toLowerCase()) {
                        case "confirmé":
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                            break;
                        case "en attente":
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                            break;
                        case "annulé":
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                            break;
                        case "terminé":
                            setStyle("-fx-background-color: #e2e3e5; -fx-text-fill: #6c757d;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne actions
        colActions.setCellFactory(createActionButtonsCallback());
    }

    private Callback<TableColumn<Appointment, Void>, TableCell<Appointment, Void>> createActionButtonsCallback() {
        return param -> new TableCell<Appointment, Void>() {
            private final Button btnView = new Button("👁️");
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox buttons = new HBox(5);

            {
                buttons.getChildren().addAll(btnView, btnEdit, btnDelete);
                buttons.setPadding(new Insets(3));

                // Style des boutons
                btnView.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-pref-width: 30;");
                btnEdit.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-pref-width: 30;");
                btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-pref-width: 30;");

                // Actions
                btnView.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    showAppointmentDetails(appointment);
                });

                btnEdit.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    editAppointment(appointment);
                });

                btnDelete.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    deleteAppointment(appointment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
    }

    private void setupFilters() {
        // Filtre de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAppointments();
        });

        // Filtre par statut
        statusFilter.setOnAction(e -> filterAppointments());

        // Filtre par date
        dateFilter.setOnAction(e -> filterAppointments());
    }

    private void setupNotifications() {
        // Timer pour vérifier les notifications toutes les minutes
        notificationTimer = new Timer(true);
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateNotifications());
            }
        }, 0, 60000);
    }

    private void updateNotifications() {
        ObservableList<String> notifications = FXCollections.observableArrayList();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        for (Appointment apt : appointments) {
            if ("Confirmé".equals(apt.getStatus())) {
                LocalDateTime aptDateTime = LocalDateTime.of(apt.getDate(), apt.getTime());

                // Notifications pour les RDV dans les prochaines 24h
                if (aptDateTime.isAfter(now) && aptDateTime.isBefore(tomorrow)) {
                    long minutesUntil = java.time.Duration.between(now, aptDateTime).toMinutes();
                    String timeInfo;

                    if (minutesUntil < 60) {
                        timeInfo = minutesUntil + " min";
                    } else {
                        timeInfo = (minutesUntil / 60) + "h" + (minutesUntil % 60) + "min";
                    }

                    notifications.add(String.format("🔔 %s - %s dans %s",
                            apt.getClientName(), apt.getService(), timeInfo));
                }
            }
        }

        if (notifications.isEmpty()) {
            notifications.add("✅ Aucun rendez-vous proche");
        }

        notificationList.setItems(notifications);
    }

    @FXML
    private void showNewAppointmentForm() {
        currentAppointment = null;
        clearForm();
        showAppointmentDialog();
    }

    @FXML
    private void filterAppointments() {
        filteredAppointments.setPredicate(appointment -> {
            // Filtre de recherche
            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                boolean matchesSearch = appointment.getClientName().toLowerCase().contains(searchText) ||
                        appointment.getOptician().toLowerCase().contains(searchText) ||
                        appointment.getService().toLowerCase().contains(searchText);
                if (!matchesSearch) return false;
            }

            // Filtre par statut
            String statusValue = statusFilter.getValue();
            if (!"Tous".equals(statusValue) && !appointment.getStatus().equals(statusValue)) {
                return false;
            }

            // Filtre par date
            LocalDate selectedDate = dateFilter.getValue();
            if (selectedDate != null && !appointment.getDate().equals(selectedDate)) {
                return false;
            }

            return true;
        });

        updateStatistics();
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        statusFilter.setValue("Tous");
        dateFilter.setValue(null);
        filterAppointments();
    }

    private void showAppointmentDialog() {
        appointmentDialog.setTitle(currentAppointment == null ? "Nouveau Rendez-vous" : "Modifier Rendez-vous");

        Optional<ButtonType> result = appointmentDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (validateForm()) {
                saveAppointment();
            }
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtClientName.getText().trim().isEmpty()) {
            errors.append("Le nom du client est requis.\n");
        }

        if (txtClientPhone.getText().trim().isEmpty()) {
            errors.append("Le téléphone est requis.\n");
        }

        if (datePicker.getValue() == null) {
            errors.append("La date est requise.\n");
        }

        if (timePicker.getValue() == null) {
            errors.append("L'heure est requise.\n");
        }

        if (serviceCombo.getValue() == null) {
            errors.append("Le service est requis.\n");
        }

        if (opticianCombo.getValue() == null) {
            errors.append("L'opticien est requis.\n");
        }

        if (errors.length() > 0) {
            lblValidationError.setText(errors.toString());
            lblValidationError.setVisible(true);
            return false;
        }

        lblValidationError.setVisible(false);
        return true;
    }

    private void saveAppointment() {
        try {
            if (currentAppointment == null) {
                // Nouveau rendez-vous
                Appointment newAppointment = new Appointment(
                        txtClientName.getText().trim(),
                        txtClientPhone.getText().trim(),
                        datePicker.getValue(),
                        LocalTime.parse(timePicker.getValue()),
                        opticianCombo.getValue(),
                        serviceCombo.getValue(),
                        statusCombo.getValue(),
                        txtNotes.getText()
                );

                // Insérer en base de données
                rendezVousService.insertAppointment(newAppointment);

                // Recharger les données depuis la base
                loadDataFromDatabase();
            } else {
                // Modification
                currentAppointment.setClientName(txtClientName.getText().trim());
                currentAppointment.setClientPhone(txtClientPhone.getText().trim());
                currentAppointment.setDate(datePicker.getValue());
                currentAppointment.setTime(LocalTime.parse(timePicker.getValue()));
                currentAppointment.setOptician(opticianCombo.getValue());
                currentAppointment.setService(serviceCombo.getValue());
                currentAppointment.setStatus(statusCombo.getValue());
                currentAppointment.setNotes(txtNotes.getText());

                // Mettre à jour en base de données
                rendezVousService.updateAppointment(currentAppointment, currentAppointment.getId());

                // Recharger les données depuis la base
                loadDataFromDatabase();
            }

            appointmentTable.refresh();
            updateStatistics();
            updateNotifications();

        } catch (Exception e) {
            showError("Erreur lors de la sauvegarde", "Une erreur s'est produite lors de la sauvegarde : " + e.getMessage());
        }
    }

    private void editAppointment(Appointment appointment) {
        currentAppointment = appointment;
        populateForm(appointment);
        showAppointmentDialog();
    }

    private void deleteAppointment(Appointment appointment) {
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer le rendez-vous de " +
                appointment.getClientName() + " ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprimer de la base de données
                rendezVousService.deleteAppointment(appointment.getId());

                // Recharger les données depuis la base
                loadDataFromDatabase();

                updateStatistics();
                updateNotifications();

            } catch (Exception e) {
                showError("Erreur lors de la suppression", "Une erreur s'est produite lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void showAppointmentDetails(Appointment appointment) {
        detailsContainer.getChildren().clear();

        addDetailRow("👤 Client", appointment.getClientName());
        addDetailRow("📞 Téléphone", appointment.getClientPhone());
        addDetailRow("📅 Date", appointment.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addDetailRow("🕐 Heure", appointment.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        addDetailRow("👨‍⚕️ Opticien", appointment.getOptician());
        addDetailRow("🔬 Service", appointment.getService());
        addDetailRow("📊 Statut", appointment.getStatus());

        if (appointment.getNotes() != null && !appointment.getNotes().trim().isEmpty()) {
            addDetailRow("📝 Notes", appointment.getNotes());
        }

        detailsDialog.showAndWait();
    }

    private void addDetailRow(String label, String value) {
        HBox row = new HBox(10);
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 100px;");
        Label lblValue = new Label(value);
        lblValue.setWrapText(true);
        row.getChildren().addAll(lblLabel, lblValue);
        detailsContainer.getChildren().add(row);
    }

    private void clearForm() {
        txtClientName.clear();
        txtClientPhone.clear();
        datePicker.setValue(LocalDate.now());
        timePicker.setValue(null);
        serviceCombo.setValue(null);
        opticianCombo.setValue(null);
        statusCombo.setValue("En attente");
        txtNotes.clear();
        lblValidationError.setVisible(false);
    }

    private void populateForm(Appointment appointment) {
        txtClientName.setText(appointment.getClientName());
        txtClientPhone.setText(appointment.getClientPhone());
        datePicker.setValue(appointment.getDate());
        timePicker.setValue(appointment.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        serviceCombo.setValue(appointment.getService());
        opticianCombo.setValue(appointment.getOptician());
        statusCombo.setValue(appointment.getStatus());
        txtNotes.setText(appointment.getNotes());
        lblValidationError.setVisible(false);
    }

    private void updateStatistics() {
        int total = appointments.size();
        LocalDate today = LocalDate.now();

        long todayCount = appointments.stream()
                .filter(apt -> apt.getDate().equals(today))
                .count();

        long confirmedCount = appointments.stream()
                .filter(apt -> "Confirmé".equals(apt.getStatus()))
                .count();

        long pendingCount = appointments.stream()
                .filter(apt -> "En attente".equals(apt.getStatus()))
                .count();

        lblTotalAppointments.setText(String.valueOf(total));
        lblTodayAppointments.setText(String.valueOf(todayCount));
        lblConfirmedAppointments.setText(String.valueOf(confirmedCount));
        lblPendingAppointments.setText(String.valueOf(pendingCount));
    }

    private void loadDataFromDatabase() {
        try {
            appointments.clear();
            ObservableList<Appointment> dbAppointments = rendezVousService.getAllAppointments();
            appointments.addAll(dbAppointments);
            appointmentTable.refresh();
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode appelée lors de la fermeture de l'application
    public void shutdown() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
        if (rendezVousService != null) {
            rendezVousService.closeConnection();
        }
    }
}