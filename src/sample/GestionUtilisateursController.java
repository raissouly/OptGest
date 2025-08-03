package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.util.Optional;

public class GestionUtilisateursController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;

    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    @FXML private TextField txtNewUsername;
    @FXML private PasswordField txtNewPassword;
    @FXML private ComboBox<String> comboNewRole;

    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private String currentUserRole;

    public void setUserRole(String role) {
        this.currentUserRole = role;
        applyAccessControls();
    }

    @FXML
    void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        comboNewRole.getItems().addAll("admin", "employe", "opticien", "assistant", "docteur");

        loadUsers();

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean selected = newSel != null;
            if ("admin".equals(currentUserRole)) {
                btnModifier.setDisable(!selected);
                btnSupprimer.setDisable(!selected);
            }
        });
    }

    private void applyAccessControls() {
        if (!"admin".equals(currentUserRole)) {
            btnAjouter.setDisable(true);
            btnModifier.setDisable(true);
            btnSupprimer.setDisable(true);
            txtNewUsername.setDisable(true);
            txtNewPassword.setDisable(true);
            comboNewRole.setDisable(true);

            showAlert(Alert.AlertType.WARNING, "Accès refusé", null, "Vous n'avez pas la permission d'accéder à cette section.");
        } else {
            btnAjouter.setDisable(false);
            txtNewUsername.setDisable(false);
            txtNewPassword.setDisable(false);
            comboNewRole.setDisable(false);

            boolean hasSelection = tableUsers.getSelectionModel().getSelectedItem() != null;
            btnModifier.setDisable(!hasSelection);
            btnSupprimer.setDisable(!hasSelection);
        }
    }

    private void loadUsers() {
        usersList.clear();
        try (Connection conn = Connectiondb.Connection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM user")) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                usersList.add(user);
            }
            tableUsers.setItems(usersList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors du chargement des utilisateurs", e.getMessage());
        }
    }

    @FXML
    void btnAjouterOnAction(ActionEvent event) {
        if (!"admin".equals(currentUserRole)) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", null, "Vous n'avez pas la permission d'ajouter un utilisateur.");
            return;
        }

        String username = txtNewUsername.getText().trim();
        String password = txtNewPassword.getText().trim();
        String role = comboNewRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", null, "Veuillez remplir tous les champs.");
            return;
        }

        // Validation de la longueur des champs
        if (username.length() > 50) {
            showAlert(Alert.AlertType.WARNING, "Nom d'utilisateur trop long", null, "Le nom d'utilisateur ne doit pas dépasser 50 caractères.");
            return;
        }

        if (role.length() > 20) {
            showAlert(Alert.AlertType.WARNING, "Rôle invalide", null, "Le rôle sélectionné est invalide.");
            return;
        }

        // التحقق من وجود username مسبقاً
        if (isUsernameExist(username)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Ce nom d'utilisateur existe déjà. Veuillez en choisir un autre.");
            return;
        }

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO user (username, password, role) VALUES (?, ?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            int result = stmt.executeUpdate();

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Utilisateur ajouté avec succès.");
                txtNewUsername.clear();
                txtNewPassword.clear();
                comboNewRole.setValue(null);
                loadUsers();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Échec de l'ajout.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de l'ajout", e.getMessage());
        }
    }

    // دالة مساعدة للتحقق من وجود اسم المستخدم
    private boolean isUsernameExist(String username) {
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM user WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            // يمكنك التعامل مع الخطأ هنا أو طباعته للdebug
            e.printStackTrace();
        }
        return false;
    }

    // دالة مساعدة للتحقق من وجود اسم المستخدم (باستثناء المستخدم الحالي)
    private boolean isUsernameExistForUpdate(String username, int userId) {
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM user WHERE username = ? AND id != ?")) {

            stmt.setString(1, username);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    void btnModifierOnAction(ActionEvent event) {
        if (!"admin".equals(currentUserRole)) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", null, "Vous n'avez pas la permission de modifier un utilisateur.");
            return;
        }

        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", null, "Veuillez sélectionner un utilisateur à modifier.");
            return;
        }

        showModifyUserDialog(selectedUser);
    }

    private void showModifyUserDialog(User user) {
        // إنشاء نافذة حوار مخصصة للتعديل
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modification de l'utilisateur: " + user.getUsername());

        // إعداد الأزرار
        ButtonType okButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // إنشاء عناصر النموذج
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setText(user.getUsername());
        usernameField.setPromptText("Nom d'utilisateur");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe (laisser vide pour conserver l'ancien)");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "employe", "opticien", "assistant", "docteur");
        roleCombo.setValue(user.getRole());

        grid.add(new Label("Nom d'utilisateur:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Nouveau mot de passe:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Rôle:"), 0, 2);
        grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // التركيز على حقل اسم المستخدم
        Platform.runLater(() -> usernameField.requestFocus());

        // تفعيل/إلغاء تفعيل زر OK بناءً على صحة البيانات
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(ActionEvent.ACTION, e -> {
            if (!validateModifyInput(usernameField.getText(), roleCombo.getValue(), user.getId())) {
                e.consume(); // منع إغلاق النافذة
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == okButtonType) {
            String newUsername = usernameField.getText().trim();
            String newPassword = passwordField.getText().trim();
            String newRole = roleCombo.getValue();

            updateUser(user.getId(), newUsername, newPassword, newRole);
        }
    }

    private boolean validateModifyInput(String username, String role, int userId) {
        if (username == null || username.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", null, "Le nom d'utilisateur ne peut pas être vide.");
            return false;
        }

        if (role == null) {
            showAlert(Alert.AlertType.WARNING, "Champ manquant", null, "Veuillez sélectionner un rôle.");
            return false;
        }

        if (isUsernameExistForUpdate(username.trim(), userId)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Ce nom d'utilisateur existe déjà. Veuillez en choisir un autre.");
            return false;
        }

        return true;
    }

    private void updateUser(int userId, String username, String password, String role) {
        String sql;
        boolean updatePassword = !password.isEmpty();

        if (updatePassword) {
            sql = "UPDATE user SET username = ?, password = ?, role = ? WHERE id = ?";
        } else {
            sql = "UPDATE user SET username = ?, role = ? WHERE id = ?";
        }

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            if (updatePassword) {
                stmt.setString(2, password);
                stmt.setString(3, role);
                stmt.setInt(4, userId);
            } else {
                stmt.setString(2, role);
                stmt.setInt(3, userId);
            }

            int result = stmt.executeUpdate();

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Utilisateur modifié avec succès.");
                loadUsers(); // إعادة تحميل القائمة
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Échec de la modification.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    void btnSupprimerOnAction(ActionEvent event) {
        if (!"admin".equals(currentUserRole)) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", null, "Vous n'avez pas la permission de supprimer un utilisateur.");
            return;
        }

        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", null, "Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Suppression d'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur '" + selectedUser.getUsername() + "' ?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            deleteUser(selectedUser);
        }
    }

    private void deleteUser(User user) {
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE id = ?")) {

            stmt.setInt(1, user.getId());
            int result = stmt.executeUpdate();

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Utilisateur supprimé avec succès.");
                loadUsers();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Échec de la suppression.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la suppression", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}