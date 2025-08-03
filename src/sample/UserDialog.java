package sample;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class UserDialog extends Dialog<User> {

    private TextField tfUsername = new TextField();
    private PasswordField pfPassword = new PasswordField();
    private TextField tfFullName = new TextField();
    private TextField tfEmail = new TextField();
    private TextField tfPhone = new TextField();
    private ComboBox<String> cbRole = new ComboBox<>(FXCollections.observableArrayList(
            "admin", "opticien", "docteur", "employee"
    ));
    private CheckBox chkIsActive = new CheckBox("Actif");

    public UserDialog(User user) {
        setTitle(user == null ? "Ajouter Utilisateur" : "Modifier Utilisateur");
        setHeaderText(user == null ? "Créer un nouvel utilisateur" : "Modifier les informations de l'utilisateur");

        ButtonType okButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Username
        grid.add(new Label("Nom d'utilisateur:"), 0, 0);
        grid.add(tfUsername, 1, 0);

        // Password
        grid.add(new Label("Mot de passe:"), 0, 1);
        grid.add(pfPassword, 1, 1);

        // Full Name
        grid.add(new Label("Nom complet:"), 0, 2);
        grid.add(tfFullName, 1, 2);

        // Email
        grid.add(new Label("Email:"), 0, 3);
        grid.add(tfEmail, 1, 3);

        // Phone
        grid.add(new Label("Téléphone:"), 0, 4);
        grid.add(tfPhone, 1, 4);

        // Role
        grid.add(new Label("Rôle:"), 0, 5);
        grid.add(cbRole, 1, 5);

        // Active status
        grid.add(new Label("Statut:"), 0, 6);
        grid.add(chkIsActive, 1, 6);

        getDialogPane().setContent(grid);

        // Set default values
        cbRole.setValue("employee");
        chkIsActive.setSelected(true);

        // Fill fields if editing
        if (user != null) {
            tfUsername.setText(user.getUsername());
            // leave password empty for security when editing
            tfFullName.setText(user.getFullName() != null ? user.getFullName() : "");
            tfEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            tfPhone.setText(user.getPhone() != null ? user.getPhone() : "");
            cbRole.setValue(user.getRole());
            chkIsActive.setSelected(user.isActive());
        }

        // Validation
        Node okButton = getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        // Enable/disable OK button based on input validation
        tfUsername.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() ||
                    (user == null && pfPassword.getText().trim().isEmpty()));
        });

        pfPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(tfUsername.getText().trim().isEmpty() ||
                    (user == null && newValue.trim().isEmpty()));
        });

        // Convert result to User when OK clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                if (isValidInput()) {
                    User u = new User();
                    u.setUsername(tfUsername.getText().trim());

                    // Only set password if it's not empty
                    if (!pfPassword.getText().trim().isEmpty()) {
                        u.setPassword(pfPassword.getText());
                    }

                    u.setFullName(tfFullName.getText().trim());
                    u.setEmail(tfEmail.getText().trim());
                    u.setPhone(tfPhone.getText().trim());
                    u.setRole(cbRole.getValue());
                    u.setActive(chkIsActive.isSelected());

                    if (user != null) {
                        u.setId(user.getId());
                    }
                    return u;
                }
            }
            return null;
        });
    }

    private boolean isValidInput() {
        // Basic validation
        if (tfUsername.getText().trim().isEmpty()) {
            showValidationError("Le nom d'utilisateur ne peut pas être vide");
            return false;
        }

        if (tfUsername.getText().length() < 3) {
            showValidationError("Le nom d'utilisateur doit contenir au moins 3 caractères");
            return false;
        }

        // Password validation (only for new users)
        if (pfPassword.getText().trim().isEmpty() && getTitle().contains("Ajouter")) {
            showValidationError("Le mot de passe ne peut pas être vide");
            return false;
        }

        if (!pfPassword.getText().trim().isEmpty() && pfPassword.getText().length() < 4) {
            showValidationError("Le mot de passe doit contenir au moins 4 caractères");
            return false;
        }

        // Email validation (if provided)
        if (!tfEmail.getText().trim().isEmpty() && !isValidEmail(tfEmail.getText().trim())) {
            showValidationError("Format d'email invalide");
            return false;
        }

        // Role validation
        if (cbRole.getValue() == null) {
            showValidationError("Veuillez sélectionner un rôle");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText("Données invalides");
        alert.setContentText(message);
        alert.showAndWait();
    }
}