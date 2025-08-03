package sample.dao;

import sample.Connectiondb;
import sample.ProduitFacture;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitFactureDAO {

    public static List<ProduitFacture> getProduitsByFactureNumero(String numeroFacture) {
        List<ProduitFacture> produits = new ArrayList<>();

        String query = "SELECT fl.designation, fl.quantite, fl.prix_unitaire " +
                "FROM factures f " +
                "JOIN factures_lignes fl ON f.id = fl.ID_Facture " +
                "WHERE f.numero_facture = ?";

        try (Connection conn = Connectiondb.Connection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, numeroFacture);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nom = rs.getString("designation");
                int quantite = rs.getInt("quantite");
                double prix = rs.getDouble("prix_unitaire");
                produits.add(new ProduitFacture(nom, quantite, prix));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }
}
