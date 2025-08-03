package sample;

import java.time.LocalDate;

public class Commande {
    private int id;
    private int idClient;
    private String dateCommande;
    private String idProduit;
    private int quantite;
    private double prixUnitaire;
    private double total;
    private String etat;
    private String priorite;
    private int delaiFabrication;
    private String dateLivraison;
    private String notes;
    private String nomClient;
    private String nomProduit;

    public Commande() {
        this.quantite = 1;
        this.etat = "En cours";
        this.priorite = "Normale";
        this.delaiFabrication = 1;
    }

    public Commande(int id, int idClient, String dateCommande, String idProduit) {
        this();
        this.id = id;
        this.idClient = idClient;
        this.dateCommande = dateCommande;
        this.idProduit = idProduit;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public String getDateCommande() { return dateCommande; }
    public void setDateCommande(String dateCommande) { this.dateCommande = dateCommande; }

    public String getIdProduit() { return idProduit; }
    public void setIdProduit(String idProduit) { this.idProduit = idProduit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) {
        this.quantite = quantite;
        calculerTotal();
    }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        calculerTotal();
    }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public int getDelaiFabrication() { return delaiFabrication; }
    public void setDelaiFabrication(int delaiFabrication) { this.delaiFabrication = delaiFabrication; }

    public String getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(String dateLivraison) { this.dateLivraison = dateLivraison; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    // Méthodes utilitaires
    private void calculerTotal() {
        this.total = this.quantite * this.prixUnitaire;
    }

    public void calculerDateLivraison() {
        if (dateCommande != null && delaiFabrication > 0) {
            LocalDate dateCmd = LocalDate.parse(dateCommande);
            this.dateLivraison = dateCmd.plusDays(delaiFabrication).toString();
        }
    }
    // Champs pour la facturation
    private double montantHT;
    private double tauxTVA = 20.0;
    private double montantTVA;
    private double montantTTC;

    // Méthodes de calcul
    public void calculerMontantsFacture() {
        this.montantHT = this.total;
        this.montantTVA = montantHT * (tauxTVA / 100);
        this.montantTTC = montantHT + montantTVA;
    }

    // Getters et setters
    public double getMontantHT() {
        return montantHT;
    }

    public double getTauxTVA() {
        return tauxTVA;
    }

    public double getMontantTVA() {
        return montantTVA;
    }

    public double getMontantTTC() {
        return montantTTC;
    }

    public void setTauxTVA(double tauxTVA) {
        this.tauxTVA = tauxTVA;
    }

    public boolean estUrgent() {
        return "Urgente".equals(this.priorite);
    }

    public boolean estEnRetard() {
        if (dateLivraison == null) return false;
        LocalDate dateLimit = LocalDate.parse(dateLivraison);
        return LocalDate.now().isAfter(dateLimit) && !"Livrée".equals(etat);
    }

    @Override
    public String toString() {
        return "Commande #" + id + " - " + nomClient + " - " + etat;
    }
}
