package sample;

import javafx.beans.property.*;

public class LigneFacture {
    private final StringProperty designation;
    private final IntegerProperty quantite;
    private final DoubleProperty prixUnitaire;
    private final DoubleProperty total;

    public LigneFacture() {
        this.designation = new SimpleStringProperty();
        this.quantite = new SimpleIntegerProperty();
        this.prixUnitaire = new SimpleDoubleProperty();
        this.total = new SimpleDoubleProperty();

        // Calcul automatique du total
        quantite.addListener((obs, oldVal, newVal) -> calculerTotal());
        prixUnitaire.addListener((obs, oldVal, newVal) -> calculerTotal());
    }

    private void calculerTotal() {
        total.set(quantite.get() * prixUnitaire.get());
    }

    // Getters et setters
    public StringProperty designationProperty() { return designation; }
    public String getDesignation() { return designation.get(); }
    public void setDesignation(String designation) { this.designation.set(designation); }

    public IntegerProperty quantiteProperty() { return quantite; }
    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int quantite) { this.quantite.set(quantite); }

    public DoubleProperty prixUnitaireProperty() { return prixUnitaire; }
    public double getPrixUnitaire() { return prixUnitaire.get(); }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire.set(prixUnitaire); }

    public DoubleProperty totalProperty() { return total; }
    public double getTotal() { return total.get(); }
}