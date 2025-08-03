package sample;

public class ProduitFacture {
    private String nom;
    private int quantite;
    private int quantiteMax; // Quantité maximale disponible dans la commande
    private double prixUnitaire;
    private double total;

    public ProduitFacture(String nom, int quantite, double prixUnitaire) {
        this.nom = nom;
        this.quantite = quantite;
        this.quantiteMax = quantite; // Par défaut, la quantité max est égale à la quantité
        this.prixUnitaire = prixUnitaire;
        this.total = quantite * prixUnitaire;
    }

    // Getters et Setters
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
        this.total = quantite * prixUnitaire; // Recalculer le total
    }

    public int getQuantiteMax() {
        return quantiteMax;
    }

    public void setQuantiteMax(int quantiteMax) {
        this.quantiteMax = quantiteMax;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        this.total = quantite * prixUnitaire; // Recalculer le total
    }

    public double getTotal() {
        return quantite * prixUnitaire; // Toujours calculer dynamiquement
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return String.format("%s (Qté: %d, Prix: %.2f DH)", nom, quantite, prixUnitaire);
    }
}