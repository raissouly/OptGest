package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Label; // تأكد من استيراد Label بشكل صحيح

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private BorderPane border;
    @FXML
    private Button Clients;
    @FXML
    private Button Produits;
    @FXML
    private Button Doctor;
    @FXML
    private Button Ordonnances;
    @FXML
    private Button Commandes;
    @FXML
    private Button Rendez_vous;
    @FXML
    private Button Fournisseurs;
    @FXML
    private Button Factures;
    @FXML
    private Button Dashboard;
    @FXML
    private Button btnUtilisateurs;

    private String userRole;
    private String userName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // لا تحمل الدشبورد هنا مباشرة، بل عند تعيين الدور
        // loadDashboardOnStart(); // سيتم استدعاؤها لاحقاً في applyRolePermissions

        // تفعيل زر Dashboard افتراضياً، أو يمكن ترك هذا الجزء
        setActiveButton(Dashboard);

        // إخفاء زر المستخدمين افتراضياً حتى يتم تحديد الدور
        if (btnUtilisateurs != null) {
            btnUtilisateurs.setVisible(false);
        }
    }

    // Method to set user role and name - Fixed method signature
    public void setUserRole(String role, String name) {
        this.userRole = role;
        this.userName = name;
        applyRolePermissions(); // استدعاء هذا عند تعيين الدور
        loadDashboardBasedOnRole(); // استدعاء جديد لتحميل لوحة التحكم بناءً على الدور
    }

    // Overloaded method for backwards compatibility
    public void setUserRole(String role) {
        this.userRole = role;
        this.userName = "User";
        applyRolePermissions();
        loadDashboardBasedOnRole(); // استدعاء جديد لتحميل لوحة التحكم بناءً على الدور
    }

    // *** الدالة الجديدة أو المعدلة لتحميل لوحة التحكم بناءً على الدور ***
    private void loadDashboardBasedOnRole() {
        try {
            AnchorPane root;
            URL fxmlLocation; // متغير لتخزين مسار ملف FXML

            if ("admin".equalsIgnoreCase(userRole)) {
                // إذا كان المدير، يحمل Statistiques.fxml
                fxmlLocation = getClass().getResource("Statistiques.fxml");
                System.out.println("Loading for Admin: Statistiques.fxml path: " + fxmlLocation);
                if (fxmlLocation == null) {
                    System.err.println("ERROR: Statistiques.fxml not found at expected path!");
                }
            } else {
                // *** التعديل هنا: استخدام "Image.fxml" بدلاً من "AccessDeniedImage.fxml" ***
                fxmlLocation = getClass().getResource("Image.fxml"); // تم التعديل هنا!
                System.out.println("Loading for Non-Admin: Image.fxml path: " + fxmlLocation);
                if (fxmlLocation == null) {
                    System.err.println("ERROR: Image.fxml not found at expected path!");
                }
            }

            // لن يتم المتابعة إلا إذا تم العثور على ملف FXML
            if (fxmlLocation != null) {
                root = FXMLLoader.load(fxmlLocation); // استخدم متغير URL هنا
                border.setCenter(root);
            } else {
                createSimpleDashboard("فشل تحميل لوحة التحكم: ملف الواجهة غير موجود في المسار المحدد.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            createSimpleDashboard("فشل تحميل لوحة التحكم بسبب خطأ في الإدخال/الإخراج: " + e.getMessage());
        }
    }

    // إنشاء دشبورد بسيط إذا فشل التحميل أو لرسائل الخطأ
    // (عدلت هذه الدالة لتقبل رسالة)
    private void createSimpleDashboard(String message) {
        AnchorPane dashboardPane = new AnchorPane();
        dashboardPane.setPrefSize(1079, 700);
        dashboardPane.setStyle("-fx-background-color: #f8f9fa;");

        Label welcomeLabel = new Label(message);
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;"); // لون أحمر للخطأ
        welcomeLabel.setLayoutX(50);
        welcomeLabel.setLayoutY(50);

        dashboardPane.getChildren().add(welcomeLabel);
        border.setCenter(dashboardPane);
    }


    // تفعيل الزر النشط
    private void setActiveButton(Button activeButton) {
        // إزالة التفعيل من جميع الأزرار
        resetAllButtons();

        // تفعيل الزر المحدد
        if (activeButton != null) {
            activeButton.getStyleClass().add("active-button");
        }
    }

    // إعادة تعيين جميع الأزرار
    private void resetAllButtons() {
        Button[] buttons = {Dashboard, Clients, Produits, Doctor, Ordonnances,
                Commandes, Rendez_vous, Fournisseurs, Factures, btnUtilisateurs};

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("active-button");
            }
        }
    }

    @FXML
    void btnOnActionClients(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("TableClients.fxml"));
            border.setCenter(root);
            setActiveButton(Clients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionCommandes(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Commande.fxml"));
            border.setCenter(root);
            setActiveButton(Commandes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionDashboard(ActionEvent event) {
        // عند النقر على زر Dashboard، نستخدم نفس المنطق
        loadDashboardBasedOnRole();
        setActiveButton(Dashboard);
    }

    @FXML
    void btnOnActionDoctor(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Doctor.fxml"));
            border.setCenter(root);
            setActiveButton(Doctor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionFactures(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Facturation.fxml"));
            border.setCenter(root);
            setActiveButton(Factures);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionFournisseurs(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Fournisseur.fxml"));
            border.setCenter(root);
            setActiveButton(Fournisseurs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionOrdonnances(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("ViewOrdonnances.fxml"));
            border.setCenter(root);
            setActiveButton(Ordonnances);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionProduits(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Produits.fxml"));
            border.setCenter(root);
            setActiveButton(Produits);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionRendezVous(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("RendezVous.fxml"));
            border.setCenter(root);
            setActiveButton(Rendez_vous);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnOnActionPayment(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Payment.fxml"));
            border.setCenter(root);
            // يمكنك إضافة زر Payment إذا كان موجود في FXML
            // setActiveButton(btnServicePayment); // إذا كان لديك زر مخصص
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // دوال إدارة الأدوار
    private void applyRolePermissions() {
        if (userRole == null) return;

        // إخفاء جميع الأزرار أولاً
        hideAllButtons();

        switch (userRole.toLowerCase()) {
            case "admin":
                // Admin - يرى كل شيء بما في ذلك إدارة المستخدمين
                showAllButtons();
                if (btnUtilisateurs != null) {
                    btnUtilisateurs.setVisible(true);
                    btnUtilisateurs.setDisable(false);
                }
                break;

            case "docteur":
            case "doctor":
                // Doctor - فقط الوصفات والعملاء
                if (Clients != null) Clients.setVisible(true);
                if (Doctor != null) Doctor.setVisible(true);
                if (Ordonnances != null) Ordonnances.setVisible(true);
                if (Rendez_vous != null) Rendez_vous.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "opticien":
                // Opticien - البيع والعملاء والمنتجات
                if (Clients != null) Clients.setVisible(true);
                if (Produits != null) Produits.setVisible(true);
                if (Ordonnances != null) Ordonnances.setVisible(true);
                if (Commandes != null) Commandes.setVisible(true);
                if (Rendez_vous != null) Rendez_vous.setVisible(true);
                if (Factures != null) Factures.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "pharmacien":
            case "pharmacist":
                // Pharmacien - الأدوية والوصفات
                if (Clients != null) Clients.setVisible(true);
                if (Produits != null) Produits.setVisible(true);
                if (Ordonnances != null) Ordonnances.setVisible(true);
                if (Commandes != null) Commandes.setVisible(true);
                if (Fournisseurs != null) Fournisseurs.setVisible(true);
                if (Factures != null) Factures.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "secretaire":
            case "secretary":
                // Secrétaire - المواعيد والعملاء
                if (Clients != null) Clients.setVisible(true);
                if (Rendez_vous != null) Rendez_vous.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "comptable":
            case "accountant":
                // Comptable - الفواتير والمالية
                if (Clients != null) Clients.setVisible(true);
                if (Commandes != null) Commandes.setVisible(true);
                if (Fournisseurs != null) Fournisseurs.setVisible(true);
                if (Factures != null) Factures.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "magasinier":
            case "stock_manager":
                // Magasinier - المخزون والمنتجات
                if (Produits != null) Produits.setVisible(true);
                if (Commandes != null) Commandes.setVisible(true);
                if (Fournisseurs != null) Fournisseurs.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "vendeur":
            case "seller":
                // Vendeur - البيع والعملاء
                if (Clients != null) Clients.setVisible(true);
                if (Produits != null) Produits.setVisible(true);
                if (Ordonnances != null) Ordonnances.setVisible(true);
                if (Commandes != null) Commandes.setVisible(true);
                if (Rendez_vous != null) Rendez_vous.setVisible(true);
                if (Factures != null) Factures.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            case "invite":
            case "guest":
                // Invité - عرض محدود فقط
                if (Produits != null) Produits.setVisible(true);
                if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
                // إخفاء زر إدارة المستخدمين
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;

            default:
                // Default - إخفاء كل شيء بما في ذلك إدارة المستخدمين
                hideAllButtons();
                if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
                break;
        }

        // Apply visual styling based on role
        applyRoleTheme();
    }

    // إخفاء جميع الأزرار
    private void hideAllButtons() {
        // إضافة فحص null قبل الوصول إلى الأزرار
        if (btnUtilisateurs != null) btnUtilisateurs.setVisible(false);
        if (Clients != null) Clients.setVisible(false);
        if (Produits != null) Produits.setVisible(false);
        if (Doctor != null) Doctor.setVisible(false);
        if (Ordonnances != null) Ordonnances.setVisible(false);
        if (Commandes != null) Commandes.setVisible(false);
        if (Rendez_vous != null) Rendez_vous.setVisible(false);
        if (Fournisseurs != null) Fournisseurs.setVisible(false);
        if (Factures != null) Factures.setVisible(false);
        if (Dashboard != null) Dashboard.setVisible(false); // أخفي زر Dashboard أيضاً عند إخفاء الكل
    }

    // إظهار جميع الأزرار (للمدير فقط)
    private void showAllButtons() {
        if (btnUtilisateurs != null) btnUtilisateurs.setVisible(true);
        if (Clients != null) Clients.setVisible(true);
        if (Produits != null) Produits.setVisible(true);
        if (Doctor != null) Doctor.setVisible(true);
        if (Ordonnances != null) Ordonnances.setVisible(true);
        if (Commandes != null) Commandes.setVisible(true);
        if (Rendez_vous != null) Rendez_vous.setVisible(true);
        if (Fournisseurs != null) Fournisseurs.setVisible(true);
        if (Factures != null) Factures.setVisible(true);
        if (Dashboard != null) Dashboard.setVisible(true); // تأكد من إظهار زر Dashboard
    }

    // Add method to apply different themes for different roles
    private void applyRoleTheme() {
        if (userRole == null) return;

        String themeColor = "";

        switch (userRole.toLowerCase()) {
            case "admin":
                break;
            case "docteur":
            case "doctor":
                break;
            case "opticien":
                break;
            case "pharmacien":
            case "pharmacist":
                break;
            case "secretaire":
            case "secretary":
                break;
            case "comptable":
            case "accountant":
                break;
            case "magasinier":
            case "stock_manager":
                break;
            case "vendeur":
            case "seller":
                break;
            case "invite":
            case "guest":
                break;
            default:
        }

        // Apply theme to the sidebar or main container
        if (border != null && border.getLeft() != null) {
        }
    }

    // Add method to get role display name in Arabic
    public String getRoleDisplayName(String role) {
        switch (role.toLowerCase()) {
            case "admin": return "مدير النظام";
            case "docteur":
            case "doctor": return "طبيب";
            case "opticien": return "أخصائي البصريات";
            case "pharmacien":
            case "pharmacist": return "صيدلي";
            case "secretaire":
            case "secretary": return "سكرتير";
            case "comptable":
            case "accountant": return "محاسب";
            case "magasinier":
            case "stock_manager": return "أمين المخزون";
            case "vendeur":
            case "seller": return "بائع";
            case "invite":
            case "guest": return "زائر";
            default: return "مستخدم";
        }
    }

    // Add method to check if user has specific permission
    public boolean hasPermission(String permission) {
        if (userRole == null) return false;

        switch (userRole.toLowerCase()) {
            case "admin":
                return true; // Admin has all permissions including user management

            case "docteur":
            case "doctor":
                return permission.equals("clients") ||
                        permission.equals("ordonnances") ||
                        permission.equals("rendez_vous") ||
                        permission.equals("doctor") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "opticien":
                return permission.equals("clients") ||
                        permission.equals("produits") ||
                        permission.equals("ordonnances") ||
                        permission.equals("commandes") ||
                        permission.equals("rendez_vous") ||
                        permission.equals("factures") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "pharmacien":
            case "pharmacist":
                return permission.equals("clients") ||
                        permission.equals("produits") ||
                        permission.equals("ordonnances") ||
                        permission.equals("commandes") ||
                        permission.equals("fournisseurs") ||
                        permission.equals("factures") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "secretaire":
            case "secretary":
                return permission.equals("clients") ||
                        permission.equals("rendez_vous") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "comptable":
            case "accountant":
                return permission.equals("clients") ||
                        permission.equals("commandes") ||
                        permission.equals("fournisseurs") ||
                        permission.equals("factures") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "magasinier":
            case "stock_manager":
                return permission.equals("produits") ||
                        permission.equals("commandes") ||
                        permission.equals("fournisseurs") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "vendeur":
            case "seller":
                return permission.equals("clients") ||
                        permission.equals("produits") ||
                        permission.equals("ordonnances") ||
                        permission.equals("commandes") ||
                        permission.equals("rendez_vous") ||
                        permission.equals("factures") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد

            case "invite":
            case "guest":
                return permission.equals("produits") ||
                        permission.equals("dashboard"); // إضافة إذن الدشبورد (عرض محدود)

            default:
                return false;
        }
    }

    @FXML
    void btnOnActionUtilisateurs(ActionEvent event) {
        if (!"admin".equalsIgnoreCase(userRole)) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé",
                    "Permission insuffisante",
                    "Seuls les administrateursن.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GestionUtilisateurs.fxml"));
            AnchorPane root = loader.load();

            // طريقة مباشرة وآمنة لتمرير الدور
            GestionUtilisateursController controller = loader.getController();
            if (controller != null) {
                controller.setUserRole(this.userRole);
            }

            border.setCenter(root);
            setActiveButton(btnUtilisateurs);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur système",
                    "Impossible d'afficher l'interface",
                    "Une erreur est survenue lors du chargement de GestionUtilisateurs.fxml.");
        }
    }

    // إضافة طريقة لإظهار التنبيهات
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getters
    public String getUserRole() {
        return userRole;
    }

    public String getUserName() {
        return userName;
    }

    @FXML
    void btnOnActionServicePayment(ActionEvent event) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("Payment.fxml"));
            border.setCenter(root);
            // يمكنك إضافة زر Payment إذا كان موجود في FXML
            // setActiveButton(btnServicePayment); // إذا كان لديك زر مخصص
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button btnServicePayment; // يجب أن يكون هذا الزر معرّفاً في FXML أيضاً

}