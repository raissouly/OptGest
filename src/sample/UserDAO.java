package sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static boolean addUser(User user) {
        String sql = "INSERT INTO user (username, password, full_name, email, phone, role, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // تأكد من التشفير
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getRole());
            pstmt.setBoolean(7, user.isActive());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(User user) {
        String sql = "UPDATE user SET username = ?, full_name = ?, email = ?, phone = ?, role = ?, is_active = ?";

        // إضافة تحديث كلمة المرور إذا تم توفيرها
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            sql += ", password = ?";
        }

        sql += " WHERE id = ?";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            pstmt.setString(paramIndex++, user.getUsername());
            pstmt.setString(paramIndex++, user.getFullName());
            pstmt.setString(paramIndex++, user.getEmail());
            pstmt.setString(paramIndex++, user.getPhone());
            pstmt.setString(paramIndex++, user.getRole());
            pstmt.setBoolean(paramIndex++, user.isActive());

            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                pstmt.setString(paramIndex++, user.getPassword());
            }

            pstmt.setInt(paramIndex, user.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM user WHERE id = ?";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY username";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("is_active"));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static boolean isUsernameExists(String username, int excludeId) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ? AND id != ?";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, excludeId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}