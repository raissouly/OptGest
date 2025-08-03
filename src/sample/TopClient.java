package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TopClient {
    private final StringProperty nom;
    private final StringProperty montant;

    // Constructeur
    public TopClient(String nom, String montant) {
        this.nom = new SimpleStringProperty(nom);
        this.montant = new SimpleStringProperty(montant);
    }

    // Getters pour les propriétés (utilisés par PropertyValueFactory)
    public StringProperty nomProperty() {
        return nom;
    }

    public StringProperty montantProperty() {
        return montant;
    }

    // Getters pour les valeurs
    public String getNom() {
        return nom.get();
    }

    public String getMontant() {
        return montant.get();
    }

    // Setters
    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public void setMontant(String montant) {
        this.montant.set(montant);
    }

    @Override
    public String toString() {
        return "TopClient{" +
                "nom='" + getNom() + '\'' +
                ", montant='" + getMontant() + '\'' +
                '}';
    }
}