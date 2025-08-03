package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;


public class StatistiquesController implements Initializable {

    @FXML
    private Label lblClients;

    @FXML
    private Label lblRevenus;

    @FXML
    private Label lblCommandes;

    @FXML
    private Label lblImpayes;

    @FXML
    private LineChart<String, Number> lineChart; // Changed from BarChart

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChart(); // Call setupChart first to configure axes etc.
        loadStatistics();
    }

    private void loadStatistics() {
        loadClientsCount();
        loadRevenus();
        loadCommandesCount();
        loadImpayesCount();
        loadChartData();
        // setupChartColors(); // This method directly adds data AND applies bar-specific colors,
        // which won't work for LineChart. We'll rely on CSS for line colors.
    }

    // This method is primarily for styling bars. For a LineChart,
    // you'd typically style the lines and data points using CSS or
    // set the series color. It's safe to remove or modify this for LineChart.
    // For now, let's keep it but note it won't apply "bar-fill"
    // private void setupChartColors() {
    //     // ... (logic for bar colors, which is not applicable here)
    // }

    private void loadClientsCount() {
        String query = "SELECT COUNT(*) as total FROM clients";
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                lblClients.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblClients.setText("0");
        }
    }

    private void loadRevenus() {
        String query = "SELECT SUM(COALESCE(quantite * prix_unitaire, 0)) as total_revenus FROM commandes";
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                double revenus = rs.getDouble("total_revenus");
                DecimalFormat df = new DecimalFormat("#,##0.00");
                lblRevenus.setText(df.format(revenus) + " DH");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des revenus : " + e.getMessage());
            lblRevenus.setText("0.00 DH");
        }
    }





    // Add these debug methods to your controller class

    private void debugRevenusProblem() {
        try (Connection conn = Connectiondb.Connection()) {

            // 1. Check if table exists and has data
            String countQuery = "SELECT COUNT(*) as count FROM commandes";
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("DEBUG: Total commandes count: " + count);
                }
            }

            // 2. Check column values
            String dataQuery = "SELECT total_commande FROM commandes LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(dataQuery);
                 ResultSet rs = stmt.executeQuery()) {
                System.out.println("DEBUG: Sample total_commande values:");
                while (rs.next()) {
                    double value = rs.getDouble("total_commande");
                    System.out.println("  Value: " + value + " (NULL: " + rs.wasNull() + ")");
                }
            }

            // 3. Check for NULL values
            String nullQuery = "SELECT COUNT(*) as null_count FROM commandes WHERE total_commande IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(nullQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int nullCount = rs.getInt("null_count");
                    System.out.println("DEBUG: NULL total_commande count: " + nullCount);
                }
            }

            // 4. Check for zero values
            String zeroQuery = "SELECT COUNT(*) as zero_count FROM commandes WHERE total_commande = 0";
            try (PreparedStatement stmt = conn.prepareStatement(zeroQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int zeroCount = rs.getInt("zero_count");
                    System.out.println("DEBUG: Zero total_commande count: " + zeroCount);
                }
            }

            // 5. Manual SUM calculation
            String sumQuery = "SELECT SUM(total_commande) as manual_sum FROM commandes WHERE total_commande IS NOT NULL";
            try (PreparedStatement stmt = conn.prepareStatement(sumQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double sum = rs.getDouble("manual_sum");
                    System.out.println("DEBUG: Manual SUM (excluding NULL): " + sum);
                }
            }

        } catch (SQLException e) {
            System.out.println("DEBUG: Error in debug method: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCommandesCount() {
        String query = "SELECT COUNT(*) as total FROM commandes";
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                lblCommandes.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblCommandes.setText("0");
        }
    }

    private void loadImpayesCount() {
        String query = "SELECT COUNT(*) as total FROM commandes WHERE etat = 'En cours'";
        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                lblImpayes.setText(String.valueOf(rs.getInt("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblImpayes.setText("0");
        }
    }

    private void setupChart() {
        lineChart.setTitle("Aperçu des Données"); // Changed from barChart
        xAxis.setLabel("Catégories");
        yAxis.setLabel("Valeurs");
        lineChart.setAnimated(true); // Changed from barChart
        lineChart.setLegendVisible(false); // Changed from barChart
    }

    private void loadChartData() {
        try (Connection conn = Connectiondb.Connection()) {
            lineChart.getData().clear(); // Changed from barChart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Statistiques");

            int clients = getClientsCount(conn);
            int commandes = getCommandesCount(conn);
            int impayes = getImpayesCount(conn);
            double revenus = getRevenus(conn);

            // Adding data points for a line chart.
            // For a line chart, it often makes more sense if the categories
            // represent a progression (e.g., time), but it will still plot
            // points for discrete categories.
            series.getData().add(new XYChart.Data<>("Clients", clients));
            series.getData().add(new XYChart.Data<>("Commandes", commandes));
            series.getData().add(new XYChart.Data<>("Impayés", impayes));
            series.getData().add(new XYChart.Data<>("Revenus", revenus));

            lineChart.getData().add(series); // Changed from barChart

            // For LineChart, you usually style the line and its symbols via CSS
            // or directly on the series. The "bar-fill" styling below will not apply.
            // If you want to color the data points/symbols, you'd target them in CSS.
            /*
            Platform.runLater(() -> {
                int index = 0;
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        // These styles are for bars, they won't apply to lines directly.
                        // You would use CSS for line colors or series styling.
                        // node.setStyle("-fx-bar-fill: #4CAF50;");
                        // For LineChart points, you might do:
                        // node.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 5;");
                        // Or rely on CSS for series colors.
                        index++;
                    }
                }
            });
            */

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getClientsCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) as total FROM clients";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private int getCommandesCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) as total FROM commandes";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private int getImpayesCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) as total FROM commandes WHERE etat = 'En cours'";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    private double getRevenus(Connection conn) throws SQLException {
        String query = "SELECT SUM(total_commande) as total_revenus FROM commandes";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getDouble("total_revenus") : 0.0;
        }
    }

    public void refreshStatistics() {
        loadStatistics();
    }

    public void updateStatistics(int clients, double revenus, int commandes, int impayes) {
        lblClients.setText(String.valueOf(clients));
        DecimalFormat df = new DecimalFormat("#,##0.00");
        lblRevenus.setText(df.format(revenus) + " DH");
        lblCommandes.setText(String.valueOf(commandes));
        lblImpayes.setText(String.valueOf(impayes));
        updateChart(clients, revenus, commandes, impayes);
    }

    private void updateChart(int clients, double revenus, int commandes, int impayes) {
        lineChart.getData().clear(); // Changed from barChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Statistiques");

        series.getData().add(new XYChart.Data<>("Clients", clients));
        series.getData().add(new XYChart.Data<>("Commandes", commandes));
        series.getData().add(new XYChart.Data<>("Impayés", impayes));
        series.getData().add(new XYChart.Data<>("Revenus", revenus));

        lineChart.getData().add(series); // Changed from barChart

        // The bar-specific styling won't apply here.
        // For line charts, consider styling via CSS on .chart-series-line or .chart-line-symbol
        /*
        Platform.runLater(() -> {
            int index = 0;
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    // These styles are for bars, they won't apply to lines directly.
                    index++;
                }
            }
        });
        */
    }
}