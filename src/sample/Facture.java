package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Facture {
    private final StringProperty numero;
    private final StringProperty date;
    private final StringProperty client;
    private final StringProperty montantHT;
    private final StringProperty tva;
    private final StringProperty montantTTC;
    private final StringProperty statut;
    private final StringProperty dateEcheance;

    // Constructeur
    public Facture(String numero, String date, String client, String montantHT,
                   String tva, String montantTTC, String statut, String dateEcheance) {
        this.numero = new SimpleStringProperty(numero);
        this.date = new SimpleStringProperty(date);
        this.client = new SimpleStringProperty(client);
        this.montantHT = new SimpleStringProperty(montantHT);
        this.tva = new SimpleStringProperty(tva);
        this.montantTTC = new SimpleStringProperty(montantTTC);
        this.statut = new SimpleStringProperty(statut);
        this.dateEcheance = new SimpleStringProperty(dateEcheance);
    }

    // Getters pour les propriétés (utilisés par PropertyValueFactory)
    public StringProperty numeroProperty() {
        return numero;
    }

    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty clientProperty() {
        return client;
    }

    public StringProperty montantHTProperty() {
        return montantHT;
    }

    public StringProperty tvaProperty() {
        return tva;
    }

    public StringProperty montantTTCProperty() {
        return montantTTC;
    }

    public StringProperty statutProperty() {
        return statut;
    }

    public StringProperty dateEcheanceProperty() {
        return dateEcheance;
    }

    // Getters pour les valeurs
    public String getNumero() {
        return numero.get();
    }

    public String getDate() {
        return date.get();
    }

    public String getClient() {
        return client.get();
    }

    public String getMontantHT() {
        return montantHT.get();
    }

    public String getTva() {
        return tva.get();
    }

    public String getMontantTTC() {
        return montantTTC.get();
    }

    public String getStatut() {
        return statut.get();
    }

    public String getDateEcheance() {
        return dateEcheance.get();
    }

    // Setters
    public void setNumero(String numero) {
        this.numero.set(numero);
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public void setClient(String client) {
        this.client.set(client);
    }

    public void setMontantHT(String montantHT) {
        this.montantHT.set(montantHT);
    }

    public void setTva(String tva) {
        this.tva.set(tva);
    }

    public void setMontantTTC(String montantTTC) {
        this.montantTTC.set(montantTTC);
    }

    public void setStatut(String statut) {
        this.statut.set(statut);
    }

    public void setDateEcheance(String dateEcheance) {
        this.dateEcheance.set(dateEcheance);
    }

    @Override
    public String toString() {
        return "Facture{" +
                "numero='" + getNumero() + '\'' +
                ", date='" + getDate() + '\'' +
                ", client='" + getClient() + '\'' +
                ", montantHT='" + getMontantHT() + '\'' +
                ", tva='" + getTva() + '\'' +
                ", montantTTC='" + getMontantTTC() + '\'' +
                ", statut='" + getStatut() + '\'' +
                ", dateEcheance='" + getDateEcheance() + '\'' +
                '}';
    }
}