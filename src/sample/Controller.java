package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Label ErrorMessagelable;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button login;

    @FXML
    private Button cancel;

    // متغيرات لتخزين معلومات المستخدم بعد تسجيل الدخول
    private String userRole;
    private String userName;
    private int userId;

    @FXML
    void btncancelOnAction(ActionEvent event) {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void btnloginOnAction(ActionEvent event) {
        validateLogin();
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public void validateLogin() {
        if (!IsEmpty()) {
            // التحقق من UserSessionManager أولاً
            UserSessionManager sessionManager = UserSessionManager.getInstance();
            String currentUsername = username.getText();

            // التحقق من إمكانية المحاولة
            if (!sessionManager.canAttemptLogin(currentUsername)) {
                Notification("خطأ", "", "تم تجاوز عدد المحاولات المسموح. يرجى الاتصال بالمدير.", Alert.AlertType.ERROR);
                return;
            }

            // الاتصال بقاعدة البيانات
            Connection conn = Connectiondb.Connection();
            if (conn == null) {
                Notification("Erreur", "", "Connexion à la base de données échouée", Alert.AlertType.ERROR);
                return;
            }

            String sql = "SELECT id, username, password, role FROM user WHERE username = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, currentUsername);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    // المقارنة هنا مباشرة (تأكد من تشفير كلمة المرور لاحقاً)
                    if (dbPassword.equals(password.getText())) {
                        // جمع معلومات المستخدم
                        userId = rs.getInt("id");
                        userRole = rs.getString("role");
                        userName = rs.getString("username");

                        // إذا لم يكن هناك full_name، استخدم username
                        if (userName == null || userName.trim().isEmpty()) {
                            userName = rs.getString("username");
                        }

                        // تسجيل الدخول في SessionManager
                        sessionManager.resetFailedAttempts(currentUsername);
                        sessionManager.setPrimaryStage((Stage) login.getScene().getWindow());

                        // إنشاء نافذة Dashboard
                        createAccountForm();

                        // تسجيل العملية
                        System.out.println("تسجيل دخول ناجح: " + userName + " - " + userRole);

                    } else {
                        // تسجيل محاولة فاشلة
                        sessionManager.recordFailedLogin(currentUsername);
                        int remaining = sessionManager.getRemainingAttempts(currentUsername);
                        String message = "Mot de passe incorrect. Tentatives restantes: " + remaining;
                        Notification("Erreur", "", message, Alert.AlertType.ERROR);
                    }
                } else {
                    // تسجيل محاولة فاشلة
                    sessionManager.recordFailedLogin(currentUsername);
                    int remaining = sessionManager.getRemainingAttempts(currentUsername);
                    String message = "Nom d'utilisateur non trouvé. Tentatives restantes: " + remaining;
                    Notification("Erreur", "", message, Alert.AlertType.ERROR);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                Notification("Erreur SQL", "", ex.getMessage(), Alert.AlertType.ERROR);
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Notification("Information", "", "Veuillez remplir tous les champs", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void createAccountForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();

            // تمرير الدور واسم المستخدم للكلاس ديال DashboardController
            DashboardController dashboardController = loader.getController();

            // الآن نمرر معاملين كما هو مطلوب
            dashboardController.setUserRole(this.userRole, this.userName);

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Cabinet d'Optique - " + userName);
            dashboardStage.setScene(new Scene(root));
            dashboardStage.show();

            // إغلاق نافذة تسجيل الدخول
            Stage stage = (Stage) login.getScene().getWindow();
            stage.close();

        } catch (IOException e) {
            e.printStackTrace();
            Notification("Erreur", "", "Erreur lors du chargement du dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void Notification(String title, String header, String content, Alert.AlertType e) {
        Alert msg = new Alert(e);
        msg.setTitle(title);
        msg.setHeaderText(header);
        msg.setContentText(content);
        msg.show();
    }

    private boolean IsEmpty() {
        return username.getText().isEmpty() || password.getText().isEmpty();
    }

    // طريقة للحصول على معلومات المستخدم الحالي
    public String getCurrentUserRole() {
        return userRole;
    }

    public String getCurrentUserName() {
        return userName;
    }

    public int getCurrentUserId() {
        return userId;
    }

    // طريقة لمسح الحقول
    public void clearFields() {
        username.clear();
        password.clear();
        ErrorMessagelable.setText("");
    }

    // طريقة لتعيين رسالة خطأ
    public void setErrorMessage(String message) {
        ErrorMessagelable.setText(message);
        ErrorMessagelable.setStyle("-fx-text-fill: red;");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connectiondb.Connection();

        // إضافة مستمع لإخفاء رسالة الخطأ عند الكتابة
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            if (ErrorMessagelable.getText() != null && !ErrorMessagelable.getText().isEmpty()) {
                ErrorMessagelable.setText("");
            }
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            if (ErrorMessagelable.getText() != null && !ErrorMessagelable.getText().isEmpty()) {
                ErrorMessagelable.setText("");
            }
        });
    }
}