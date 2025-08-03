package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ProduitFournisseurFormController implements Initializable {

    @FXML private TextField referenceField;
    @FXML private TextField designationField;
    @FXML private TextField tailleField;
    @FXML private TextField quantiteField;
    @FXML private TextField prixUnitaireField;
    @FXML private TextField couleurField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField marqueField;
    @FXML private TextField stockMinimalField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<Fournisseur> fournisseurCombo;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnReinitialiser;
    @FXML private Label titleLabel;

    private Produit produitToEdit;
    private boolean isEditing = false;
    private Runnable refreshCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupValidation();
        loadFournisseurs();
    }

    private void setupComboBoxes() {
        // Types de produits
        typeCombo.setItems(FXCollections.observableArrayList(
                 "Accessoire",
                 "Lentilles", "Lunettes", "Verres", "Autre"
        ));

        // Statuts
        statutCombo.setItems(FXCollections.observableArrayList(
                "Actif", "Inactif", "Discontinu", "En rupture"
        ));
        statutCombo.setValue("Actif");

        // Configuration du ComboBox fournisseur pour afficher le nom mais retourner l'objet
        fournisseurCombo.setCellFactory(param -> new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " - " + item.getVille());
                }
            }
        });

        fournisseurCombo.setButtonCell(new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionner un fournisseur");
                } else {
                    setText(item.getNom() + " - " + item.getVille());
                }
            }
        });
    }

    private void loadFournisseurs() {
        ObservableList<Fournisseur> fournisseurs = FXCollections.observableArrayList();
        try {
            String query = "SELECT id, nom, ville FROM fournisseurs ORDER BY nom";
            ResultSet rs = Connectiondb.Dispaly(query);
            while (rs.next()) {
                Fournisseur fournisseur = new Fournisseur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("ville")
                );
                fournisseurs.add(fournisseur);
            }
            fournisseurCombo.setItems(fournisseurs);
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des fournisseurs: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger la liste des fournisseurs.");
        }
    }

    private void setupValidation() {
        // Validation numérique pour les champs de quantité et prix
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantiteField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        stockMinimalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockMinimalField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        prixUnitaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                prixUnitaireField.setText(oldValue);
            }
        });
    }

    public void setProduitToEdit(Produit produit) {
        this.produitToEdit = produit;
        this.isEditing = true;
        titleLabel.setText("Modifier le produit");
        btnEnregistrer.setText("Mettre à jour");
        fillForm();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void fillForm() {
        if (produitToEdit != null) {
            referenceField.setText(produitToEdit.getReference());
            designationField.setText(produitToEdit.getDesignation());
            tailleField.setText(produitToEdit.getTaille());
            quantiteField.setText(String.valueOf(produitToEdit.getQuantite()));
            prixUnitaireField.setText(String.valueOf(produitToEdit.getPrixUnitaire()));
            couleurField.setText(produitToEdit.getCouleur());
            typeCombo.setValue(produitToEdit.getType());
            marqueField.setText(produitToEdit.getMarque());
            stockMinimalField.setText(String.valueOf(produitToEdit.getStockMinimal()));

            // Sélectionner le fournisseur correspondant
            if (produitToEdit.getFournisseurId() != null) {
                for (Fournisseur f : fournisseurCombo.getItems()) {
                    if (f.getId() == produitToEdit.getFournisseurId()) {
                        fournisseurCombo.setValue(f);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleEnregistrer(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            String reference = referenceField.getText().trim();
            String designation = designationField.getText().trim();
            String taille = tailleField.getText().trim();
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            double prixUnitaire = Double.parseDouble(prixUnitaireField.getText().trim());
            String couleur = couleurField.getText().trim();
            String type = typeCombo.getValue();
            String marque = marqueField.getText().trim();
            int stockMinimal = Integer.parseInt(stockMinimalField.getText().trim());

            // Récupérer l'ID du fournisseur sélectionné
            Integer fournisseurId = null;
            if (fournisseurCombo.getValue() != null) {
                fournisseurId = fournisseurCombo.getValue().getId();
            }

            String query;
            if (isEditing) {
                query = "UPDATE produits SET " +
                        "designation = '" + designation + "', " +
                        "taille = '" + taille + "', " +
                        "quantite = " + quantite + ", " +
                        "prix_unitaire = " + prixUnitaire + ", " +
                        "couleur = '" + couleur + "', " +
                        "type = '" + type + "', " +
                        "marque = '" + marque + "', " +
                        "stock_minimal = " + stockMinimal + ", " +
                        "fournisseur_id = " + (fournisseurId != null ? fournisseurId : "NULL") + " " +
                        "WHERE reference = '" + reference + "'";
                Connectiondb.Upadate(query);

                // Mettre à jour ou créer l'entrée dans produits_fournisseurs
                updateProduitFournisseur(reference, fournisseurId, designation, type, prixUnitaire);

                showAlert("Succès", "Produit mis à jour avec succès!");
            } else {
                // Vérifier si la référence existe déjà
                if (referenceExists(reference)) {
                    showAlert("Erreur", "Cette référence existe déjà!");
                    return;
                }

                query = "INSERT INTO produits (reference, designation, taille, quantite, prix_unitaire, couleur, type, marque, stock_minimal, fournisseur_id) VALUES (" +
                        "'" + reference + "', " +
                        "'" + designation + "', " +
                        "'" + taille + "', " +
                        quantite + ", " +
                        prixUnitaire + ", " +
                        "'" + couleur + "', " +
                        "'" + type + "', " +
                        "'" + marque + "', " +
                        stockMinimal + ", " +
                        (fournisseurId != null ? fournisseurId : "NULL") + ")";
                Connectiondb.Insert(query);

                // Ajouter l'entrée dans produits_fournisseurs si un fournisseur est sélectionné
                if (fournisseurId != null) {
                    insertProduitFournisseur(reference, fournisseurId, designation, type, prixUnitaire);
                }

                showAlert("Succès", "Produit ajouté avec succès!");
            }

            if (refreshCallback != null) {
                refreshCallback.run();
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides pour la quantité, le prix et le stock minimal.");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateProduitFournisseur(String reference, Integer fournisseurId, String designation, String type, double prixUnitaire) {
        if (fournisseurId == null) return;

        try {
            // Vérifier si l'entrée existe déjà
            String checkQuery = "SELECT COUNT(*) as count FROM produits_fournisseurs WHERE produit_id = '" + reference + "' AND fournisseur_id = " + fournisseurId;
            ResultSet rs = Connectiondb.Dispaly(checkQuery);

            if (rs.next() && rs.getInt("count") > 0) {
                // Mettre à jour l'entrée existante
                String updateQuery = "UPDATE produits_fournisseurs SET " +
                        "nom_produit = '" + designation + "', " +
                        "categorie = '" + type + "', " +
                        "prix_fournisseur = " + prixUnitaire + " " +
                        "WHERE produit_id = '" + reference + "' AND fournisseur_id = " + fournisseurId;
                Connectiondb.Upadate(updateQuery);
            } else {
                // Créer une nouvelle entrée
                insertProduitFournisseur(reference, fournisseurId, designation, type, prixUnitaire);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour de produits_fournisseurs: " + e.getMessage());
        }
    }

    private void insertProduitFournisseur(String reference, Integer fournisseurId, String designation, String type, double prixUnitaire) {
        try {
            String insertQuery = "INSERT INTO produits_fournisseurs (fournisseur_id, produit_id, nom_produit, categorie, prix_fournisseur, delai_livraison, qte_min_commande, statut) VALUES (" +
                    fournisseurId + ", " +
                    "'" + reference + "', " +
                    "'" + designation + "', " +
                    "'" + type + "', " +
                    prixUnitaire + ", " +
                    "5, " +  // délai de livraison par défaut
                    "1, " +  // quantité minimale par défaut
                    "'Disponible')";
            Connectiondb.Insert(insertQuery);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'insertion dans produits_fournisseurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void handleReinitialiser(ActionEvent event) {
        clearForm();
    }

    private boolean referenceExists(String reference) {
        try {
            String query = "SELECT COUNT(*) as count FROM produits WHERE reference = '" + reference + "'";
            java.sql.ResultSet rs = Connectiondb.Dispaly(query);
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de la référence: " + e.getMessage());
        }
        return false;
    }

    private void clearForm() {
        referenceField.clear();
        designationField.clear();
        tailleField.clear();
        quantiteField.clear();
        prixUnitaireField.clear();
        couleurField.clear();
        typeCombo.setValue(null);
        marqueField.clear();
        stockMinimalField.clear();
        descriptionArea.clear();
        statutCombo.setValue("Actif");
        fournisseurCombo.setValue(null);
    }

    private boolean validateForm() {
        if (referenceField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La référence est obligatoire.");
            referenceField.requestFocus();
            return false;
        }
        if (designationField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La désignation est obligatoire.");
            designationField.requestFocus();
            return false;
        }
        if (quantiteField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La quantité est obligatoire.");
            quantiteField.requestFocus();
            return false;
        }
        if (prixUnitaireField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le prix unitaire est obligatoire.");
            prixUnitaireField.requestFocus();
            return false;
        }
        if (stockMinimalField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le stock minimal est obligatoire.");
            stockMinimalField.requestFocus();
            return false;
        }
        if (typeCombo.getValue() == null) {
            showAlert("Erreur", "Le type de produit est obligatoire.");
            typeCombo.requestFocus();
            return false;
        }

        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite < 0) {
                showAlert("Erreur", "La quantité ne peut pas être négative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer une quantité valide.");
            return false;
        }

        try {
            double prix = Double.parseDouble(prixUnitaireField.getText().trim());
            if (prix < 0) {
                showAlert("Erreur", "Le prix ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un prix valide.");
            return false;
        }

        try {
            int stockMin = Integer.parseInt(stockMinimalField.getText().trim());
            if (stockMin < 0) {
                showAlert("Erreur", "Le stock minimal ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un stock minimal valide.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }


}