package sample;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceService {

    private final Connection connection;

    public OrdonnanceService() {
        this.connection = Connectiondb.Connection();
    }

    public List<Ordonnance> getAllOrdonnances() {
        List<Ordonnance> list = new ArrayList<>();
        String sql = "SELECT * FROM ordonnances ORDER BY Date_Ordonnance DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToOrdonnance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getAllOrdonnances: " + e.getMessage());
        }
        return list;
    }

    public Ordonnance getOrdonnanceById(int id) {
        // Try different possible column names for ID
        String[] possibleIdColumns = {"ID", "id", "Id", "ordonnance_id", "ID_Ordonnance"};

        for (String idColumn : possibleIdColumns) {
            String sql = "SELECT * FROM ordonnances WHERE " + idColumn + " = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToOrdonnance(rs);
                    }
                }
            } catch (SQLException e) {
                // Continue to next possible column name
                continue;
            }
        }
        return null;
    }

    public boolean addOrdonnance(Ordonnance ord) {
        String sql = "INSERT INTO ordonnances (ID_Client, ID_Doctor, Date_Ordonnance, Description, " +
                "OD_Sph, OD_Cyl, OD_Axe, OD_Add, OG_Sph, OG_Cyl, OG_Axe, OG_Add, Distance_Pupillaire, " +
                "Hauteur_Montage, lens_type, priority_level, scan_path, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setOrdonnanceValues(ps, ord);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ord.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in addOrdonnance: " + e.getMessage());
        }
        return false;
    }

    public boolean updateOrdonnance(Ordonnance ord) {
        // Try different possible column names for the WHERE clause
        String[] possibleIdColumns = {"ID", "id", "Id", "ordonnance_id", "ID_Ordonnance"};

        for (String idColumn : possibleIdColumns) {
            String sql = "UPDATE ordonnances SET ID_Client=?, ID_Doctor=?, Date_Ordonnance=?, Description=?, " +
                    "OD_Sph=?, OD_Cyl=?, OD_Axe=?, OD_Add=?, OG_Sph=?, OG_Cyl=?, OG_Axe=?, OG_Add=?, " +
                    "Distance_Pupillaire=?, Hauteur_Montage=?, lens_type=?, priority_level=?, scan_path=? " +
                    "WHERE " + idColumn + "=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                setOrdonnanceValues(ps, ord);
                ps.setInt(18, ord.getId());
                int result = ps.executeUpdate();
                if (result > 0) {
                    return true;
                }
            } catch (SQLException e) {
                // Continue to next possible column name
                continue;
            }
        }
        return false;
    }

    public boolean deleteOrdonnance(int id) {
        // Try different possible column names for ID
        String[] possibleIdColumns = {"ID", "id", "Id", "ordonnance_id", "ID_Ordonnance"};

        for (String idColumn : possibleIdColumns) {
            String sql = "DELETE FROM ordonnances WHERE " + idColumn + " = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                int result = ps.executeUpdate();
                if (result > 0) {
                    return true;
                }
            } catch (SQLException e) {
                // Continue to next possible column name
                continue;
            }
        }
        return false;
    }

    public List<Ordonnance> getOrdonnancesByClient(int clientId) {
        return getOrdonnancesByField("ID_Client", clientId);
    }

    public List<Ordonnance> getOrdonnancesByDoctor(int doctorId) {
        return getOrdonnancesByField("ID_Doctor", doctorId);
    }

    public List<Ordonnance> getOrdonnancesByPriority(String priority) {
        List<Ordonnance> list = new ArrayList<>();
        String sql = "SELECT * FROM ordonnances WHERE priority_level = ? ORDER BY Date_Ordonnance DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, priority);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrdonnance(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getOrdonnancesByPriority: " + e.getMessage());
        }
        return list;
    }

    public List<Ordonnance> getOrdonnancesByDateRange(LocalDate start, LocalDate end) {
        List<Ordonnance> list = new ArrayList<>();
        String sql = "SELECT * FROM ordonnances WHERE Date_Ordonnance BETWEEN ? AND ? ORDER BY Date_Ordonnance DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrdonnance(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getOrdonnancesByDateRange: " + e.getMessage());
        }
        return list;
    }

    private List<Ordonnance> getOrdonnancesByField(String field, int value) {
        List<Ordonnance> list = new ArrayList<>();
        String sql = "SELECT * FROM ordonnances WHERE " + field + " = ? ORDER BY Date_Ordonnance DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrdonnance(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in getOrdonnancesByField: " + e.getMessage());
        }
        return list;
    }

    private void setOrdonnanceValues(PreparedStatement ps, Ordonnance o) throws SQLException {
        ps.setInt(1, o.getClientId());
        ps.setInt(2, o.getDoctorId());
        ps.setDate(3, Date.valueOf(o.getDate_Ordonnance()));
        ps.setString(4, o.getDescription());
        ps.setString(5, o.getOD_Sph());
        ps.setString(6, o.getOD_Cyl());
        ps.setString(7, o.getOD_Axe());
        ps.setString(8, o.getOD_Add());
        ps.setString(9, o.getOG_Sph());
        ps.setString(10, o.getOG_Cyl());
        ps.setString(11, o.getOG_Axe());
        ps.setString(12, o.getOG_Add());
        ps.setString(13, o.getDistance_Pupillaire());
        ps.setString(14, o.getHauteur_Montage());
        ps.setString(15, o.getLens_type());
        ps.setString(16, o.getPriority_level());
        ps.setString(17, o.getScan_path());
        ps.setDate(18, Date.valueOf(o.getCreated_date()));
    }

    private Ordonnance mapResultSetToOrdonnance(ResultSet rs) throws SQLException {
        // First, let's identify the correct ID column name
        int id = getIdFromResultSet(rs);

        return new Ordonnance(
                id,
                rs.getInt("ID_Client"),
                rs.getInt("ID_Doctor"),
                rs.getDate("Date_Ordonnance").toLocalDate(),
                rs.getString("Description"),
                rs.getString("scan_path"),
                rs.getString("OD_Sph"),
                rs.getString("OD_Cyl"),
                rs.getString("OD_Axe"),
                rs.getString("OD_Add"),
                rs.getString("OG_Sph"),
                rs.getString("OG_Cyl"),
                rs.getString("OG_Axe"),
                rs.getString("OG_Add"),
                rs.getString("Distance_Pupillaire"),
                rs.getString("Hauteur_Montage"),
                rs.getString("lens_type"),
                rs.getString("priority_level")
        );
    }

    /**
     * Helper method to get the ID from ResultSet, trying different possible column names
     */
    private int getIdFromResultSet(ResultSet rs) throws SQLException {
        String[] possibleIdColumns = {"ID", "id", "Id", "ordonnance_id", "ID_Ordonnance"};

        for (String columnName : possibleIdColumns) {
            try {
                return rs.getInt(columnName);
            } catch (SQLException e) {
                // Column doesn't exist, try next one
                continue;
            }
        }

        // If none of the expected column names work, print available columns for debugging
        ResultSetMetaData metaData = rs.getMetaData();
        System.out.println("Available columns in ordonnances table:");
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            System.out.println("Column " + i + ": " + metaData.getColumnName(i));
        }

        throw new SQLException("No valid ID column found in the result set");
    }

    public void closeConnection() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Debug method to show table structure
    public void debugTableStructure() {
        String sql = "SELECT * FROM ordonnances LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            System.out.println("=== ORDONNANCES TABLE STRUCTURE ===");
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println("Column " + i + ": " + metaData.getColumnName(i));
            }
            System.out.println("=====================================");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}