package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVousService {

    public RendezVousService() {
        // Utilisation de votre classe Connectiondb
        Connection conn = Connectiondb.Connection();
        if (conn != null) {
            System.out.println("Connexion à la base de données établie via Connectiondb.");
        } else {
            System.err.println("Échec de la connexion à la base de données.");
        }
    }

    public ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM rendezvous ORDER BY date_rdv, heure_rdv";

        try {
            ResultSet rs = Connectiondb.Dispaly(sql);

            if (rs != null) {
                while (rs.next()) {
                    Appointment apt = new Appointment(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("client_phone"),
                            rs.getDate("date_rdv").toLocalDate(),
                            rs.getTime("heure_rdv").toLocalTime(),
                            rs.getString("opticien"),
                            rs.getString("service"),
                            rs.getString("statut"),
                            rs.getString("notes")
                    );
                    list.add(apt);
                }
                rs.close();
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des rendez-vous : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    public ObservableList<String> getAllDoctors() {
        ObservableList<String> doctors = FXCollections.observableArrayList();
        String query = "SELECT nom FROM doctor";

        try {
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                doctors.add(rs.getString("nom"));
            }
        } catch (SQLException ex) {
            System.out.println("❌ Error loading doctors: " + ex.getMessage());
        }

        return doctors;
    }


    public boolean insertAppointment(Appointment a) {
        String sql = String.format(
                "INSERT INTO rendezvous (client_name, client_phone, date_rdv, heure_rdv, opticien, service, statut, notes) " +
                        "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                escapeString(a.getClientName()),
                escapeString(a.getClientPhone()),
                a.getDate().toString(),
                a.getTime().toString(),
                escapeString(a.getOptician()),
                escapeString(a.getService()),
                escapeString(a.getStatus()),
                escapeString(a.getNotes())
        );

        try {
            Connectiondb.Insert(sql);
            System.out.println("Rendez-vous ajouté avec succès.");
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'insertion : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppointment(Appointment a, int id) {
        String sql = String.format(
                "UPDATE rendezvous SET client_name='%s', client_phone='%s', date_rdv='%s', " +
                        "heure_rdv='%s', opticien='%s', service='%s', statut='%s', notes='%s' WHERE id=%d",
                escapeString(a.getClientName()),
                escapeString(a.getClientPhone()),
                a.getDate().toString(),
                a.getTime().toString(),
                escapeString(a.getOptician()),
                escapeString(a.getService()),
                escapeString(a.getStatus()),
                escapeString(a.getNotes()),
                id
        );

        try {
            int result = Connectiondb.Upadate(sql);
            if (result > 0) {
                System.out.println("Rendez-vous mis à jour avec succès.");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAppointment(int id) {
        String sql = "DELETE FROM rendezvous WHERE id = " + id;

        try {
            Connectiondb.Delete(sql);
            System.out.println("Rendez-vous supprimé avec succès.");
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour éviter les erreurs SQL avec les apostrophes
    private String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }

    // Méthode pour rechercher des rendez-vous par client
    public ObservableList<Appointment> searchAppointmentsByClient(String clientName) {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM rendezvous WHERE client_name LIKE '%" + escapeString(clientName) + "%' ORDER BY date_rdv, heure_rdv";

        try {
            ResultSet rs = Connectiondb.Dispaly(sql);

            if (rs != null) {
                while (rs.next()) {
                    Appointment apt = new Appointment(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("client_phone"),
                            rs.getDate("date_rdv").toLocalDate(),
                            rs.getTime("heure_rdv").toLocalTime(),
                            rs.getString("opticien"),
                            rs.getString("service"),
                            rs.getString("statut"),
                            rs.getString("notes")
                    );
                    list.add(apt);
                }
                rs.close();
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // Méthode pour obtenir les rendez-vous d'une date spécifique
    public ObservableList<Appointment> getAppointmentsByDate(LocalDate date) {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM rendezvous WHERE date_rdv = '" + date.toString() + "' ORDER BY heure_rdv";

        try {
            ResultSet rs = Connectiondb.Dispaly(sql);

            if (rs != null) {
                while (rs.next()) {
                    Appointment apt = new Appointment(
                            rs.getInt("id"),
                            rs.getString("client_name"),
                            rs.getString("client_phone"),
                            rs.getDate("date_rdv").toLocalDate(),
                            rs.getTime("heure_rdv").toLocalTime(),
                            rs.getString("opticien"),
                            rs.getString("service"),
                            rs.getString("statut"),
                            rs.getString("notes")
                    );
                    list.add(apt);
                }
                rs.close();
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par date : " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // Méthode pour obtenir les statistiques
    public int getAppointmentCountByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM rendezvous WHERE statut = '" + escapeString(status) + "'";

        try {
            ResultSet rs = Connectiondb.Dispaly(sql);
            if (rs != null && rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                return count;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage : " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    // Méthode pour vérifier les conflits d'horaires
    public boolean hasTimeConflict(LocalDate date, LocalTime time, String optician, int excludeId) {
        String sql = String.format(
                "SELECT COUNT(*) as count FROM rendezvous WHERE date_rdv = '%s' AND heure_rdv = '%s' " +
                        "AND opticien = '%s' AND id != %d AND statut != 'Annulé'",
                date.toString(),
                time.toString(),
                escapeString(optician),
                excludeId
        );

        try {
            ResultSet rs = Connectiondb.Dispaly(sql);
            if (rs != null && rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                return count > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification des conflits : " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Méthode pour fermer la connexion (optionnelle car votre classe gère déjà la connexion)
    public void closeConnection() {
        // Votre classe Connectiondb gère la connexion de manière statique
        // Pas besoin de fermer explicitement
        System.out.println("Service fermé.");
    }
}