package sample;

import java.time.LocalDateTime;

public class ProduitFournisseur {
    private int id;
    private int fournisseurId;
    private String produitId;
    private String nomProduit;
    private String categorie;
    private double prixFournisseur;
    private int delaiLivraison;
    private int qteMinCommande;
    private String statut;
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public ProduitFournisseur() {
        this.qteMinCommande = 1;
        this.statut = "Disponible";
    }

    // Constructeur avec paramètres principaux
    public ProduitFournisseur(int fournisseurId, String produitId, String nomProduit,
                              String categorie, double prixFournisseur, int delaiLivraison) {
        this();
        this.fournisseurId = fournisseurId;
        this.produitId = produitId;
        this.nomProduit = nomProduit;
        this.categorie = categorie;
        this.prixFournisseur = prixFournisseur;
        this.delaiLivraison = delaiLivraison;
    }

    // Getters
    public int getId() { return id; }
    public int getFournisseurId() { return fournisseurId; }
    public String getProduitId() { return produitId; }
    public String getNomProduit() { return nomProduit; }
    public String getCategorie() { return categorie; }
    public double getPrixFournisseur() { return prixFournisseur; }
    public int getDelaiLivraison() { return delaiLivraison; }
    public int getQteMinCommande() { return qteMinCommande; }
    public String getStatut() { return statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFournisseurId(int fournisseurId) { this.fournisseurId = fournisseurId; }
    public void setProduitId(String produitId) { this.produitId = produitId; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setPrixFournisseur(double prixFournisseur) { this.prixFournisseur = prixFournisseur; }
    public void setDelaiLivraison(int delaiLivraison) { this.delaiLivraison = delaiLivraison; }
    public void setQteMinCommande(int qteMinCommande) { this.qteMinCommande = qteMinCommande; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    // Méthodes utilitaires
    @Override
    public String toString() {
        return nomProduit + " (" + produitId + ") - " + prixFournisseur + " MAD";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProduitFournisseur that = (ProduitFournisseur) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Méthodes de validation et utilitaires
    public boolean isValid() {
        return produitId != null && !produitId.trim().isEmpty() &&
                nomProduit != null && !nomProduit.trim().isEmpty() &&
                prixFournisseur > 0 &&
                delaiLivraison >= 0 &&
                qteMinCommande > 0;
    }

    public boolean isDisponible() {
        return "Disponible".equals(statut);
    }

    public boolean isDiscontinue() {
        return "Discontinu".equals(statut);
    }

    public String getStatutFormatted() {
        switch (statut) {
            case "Disponible":
                return "✅ Disponible";
            case "Indisponible":
                return "❌ Indisponible";
            case "Discontinu":
                return "⛔ Discontinu";
            default:
                return statut;
        }
    }

    public String getDelaiLivraisonFormatted() {
        if (delaiLivraison == 0) {
            return "Immédiat";
        } else if (delaiLivraison == 1) {
            return "1 jour";
        } else {
            return delaiLivraison + " jours";
        }
    }

    public String getPrixFormatted() {
        return String.format("%.2f MAD", prixFournisseur);
    }
}