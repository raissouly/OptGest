package sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {
    private Connection conn;

    public ProduitDAO(Connection conn) {
        this.conn = conn;
    }

    // جلب جميع المنتجات
    public List<Produit> getAllProduits() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produit p = new Produit(
                        rs.getString("reference"),
                        rs.getString("designation"),
                        rs.getString("taille"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getString("couleur"),
                        rs.getString("type"),
                        rs.getString("marque"),
                        rs.getInt("stock_minimal")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erreur dans getAllProduits : " + e.getMessage());
        }

        return list;
    }

    // إدخال منتج جديد
    public void insertProduit(Produit p) {
        String sql = "INSERT INTO produits(reference, designation, taille, quantite, prix_unitaire, couleur, type, marque, stock_minimal) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getReference());
            stmt.setString(2, p.getDesignation());
            stmt.setString(3, p.getTaille());
            stmt.setInt(4, p.getQuantite());
            stmt.setDouble(5, p.getPrixUnitaire());
            stmt.setString(6, p.getCouleur());
            stmt.setString(7, p.getType());
            stmt.setString(8, p.getMarque());
            stmt.setInt(9, p.getStockMinimal());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur dans insertProduit : " + e.getMessage());
        }
    }

    // تعديل منتج موجود
    public void updateProduit(Produit p) {
        String sql = "UPDATE produits "
                + "SET designation = ?, taille = ?, quantite = ?, prix_unitaire = ?, couleur = ?, type = ?, marque = ?, stock_minimal = ? "
                + "WHERE reference = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getDesignation());
            stmt.setString(2, p.getTaille());
            stmt.setInt(3, p.getQuantite());
            stmt.setDouble(4, p.getPrixUnitaire());
            stmt.setString(5, p.getCouleur());
            stmt.setString(6, p.getType());
            stmt.setString(7, p.getMarque());
            stmt.setInt(8, p.getStockMinimal());
            stmt.setString(9, p.getReference());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur dans updateProduit : " + e.getMessage());
        }
    }

    // حذف منتج
    public void deleteProduit(String reference) {
        String sql = "DELETE FROM produits WHERE reference = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reference);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur dans deleteProduit : " + e.getMessage());
        }
    }
}
