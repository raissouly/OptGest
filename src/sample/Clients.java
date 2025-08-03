package sample;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Clients {
    // Properties for JavaFX TableView compatibility
    private IntegerProperty id;
    private StringProperty nom;
    private StringProperty prenom;
    private StringProperty telephone;
    private StringProperty email;
    private StringProperty entreprise;
    private StringProperty ville;
    private StringProperty chiffreAffaires;

    // Constructor - updated to match your controller usage
    public Clients(int id, String nom, String prenom, String entreprise, String email, String telephone, String ville, String chiffreAffaires) {
        this.id = new SimpleIntegerProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.prenom = new SimpleStringProperty(prenom);
        this.entreprise = new SimpleStringProperty(entreprise);
        this.email = new SimpleStringProperty(email);
        this.telephone = new SimpleStringProperty(telephone);
        this.ville = new SimpleStringProperty(ville);
        this.chiffreAffaires = new SimpleStringProperty(chiffreAffaires);
    }

    // Original constructor for backward compatibility
    public Clients(int id, String nom, String prenom, String telephone, String email) {
        this(id, nom, prenom, "", email, telephone, "", "0");
    }

    // Property methods for JavaFX TableView
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public StringProperty prenomProperty() {
        return prenom;
    }

    public StringProperty telephoneProperty() {
        return telephone;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty entrepriseProperty() {
        return entreprise;
    }

    public StringProperty villeProperty() {
        return ville;
    }

    public StringProperty chiffreAffairesProperty() {
        return chiffreAffaires;
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public String getNom() {
        return nom.get();
    }

    public String getPrenom() {
        return prenom.get();
    }

    public String getTelephone() {
        return telephone.get();
    }

    public String getEmail() {
        return email.get();
    }

    // New getters to match controller usage
    public String getEntreprise() {
        return entreprise.get();
    }

    public String getVille() {
        return ville.get();
    }

    public String getChiffreAffaires() {
        return chiffreAffaires.get();
    }

    // Setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public void setPrenom(String prenom) {
        this.prenom.set(prenom);
    }

    public void setTelephone(String telephone) {
        this.telephone.set(telephone);
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public void setEntreprise(String entreprise) {
        this.entreprise.set(entreprise);
    }

    public void setVille(String ville) {
        this.ville.set(ville);
    }

    public void setChiffreAffaires(String chiffreAffaires) {
        this.chiffreAffaires.set(chiffreAffaires);
    }

    @Override
    public String toString() {
        return "Clients{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", entreprise='" + getEntreprise() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", telephone='" + getTelephone() + '\'' +
                ", ville='" + getVille() + '\'' +
                ", chiffreAffaires='" + getChiffreAffaires() + '\'' +
                '}';
    }
}