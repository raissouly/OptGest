package sample;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class HistoriqueLivraison {
    private int id;
    private String numCommande;
    private LocalDate dateCommande;
    private LocalDate dateLivraison;
    private LocalDate dateLivraisonPrevue;
    private int fournisseurId;
    private String fournisseurNom;
    private String produitsLivres;
    private int quantiteLivree;
    private double montantLivraison;
    private String statutLivraison;
    private String observations;
    private String transporteur;
    private String numBonLivraison;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Constructeur par défaut
    public HistoriqueLivraison() {
        this.quantiteLivree = 0;
        this.montantLivraison = 0.0;
        this.statutLivraison = "En attente";
    }

    // Constructeur avec paramètres principaux
    public HistoriqueLivraison(String numCommande, LocalDate dateCommande,
                               int fournisseurId, String fournisseurNom) {
        this();
        this.numCommande = numCommande;
        this.dateCommande = dateCommande;
        this.fournisseurId = fournisseurId;
        this.fournisseurNom = fournisseurNom;
    }

    // Getters
    public int getId() { return id; }
    public String getNumCommande() { return numCommande; }
    public LocalDate getDateCommande() { return dateCommande; }
    public LocalDate getDateLivraison() { return dateLivraison; }
    public LocalDate getDateLivraisonPrevue() { return dateLivraisonPrevue; }
    public int getFournisseurId() { return fournisseurId; }
    public String getFournisseurNom() { return fournisseurNom; }
    public String getProduitsLivres() { return produitsLivres; }
    public int getQuantiteLivree() { return quantiteLivree; }
    public double getMontantLivraison() { return montantLivraison; }
    public String getStatutLivraison() { return statutLivraison; }
    public String getObservations() { return observations; }
    public String getTransporteur() { return transporteur; }
    public String getNumBonLivraison() { return numBonLivraison; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNumCommande(String numCommande) { this.numCommande = numCommande; }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande = dateCommande; }
    public void setDateLivraison(LocalDate dateLivraison) { this.dateLivraison = dateLivraison; }
    public void setDateLivraisonPrevue(LocalDate dateLivraisonPrevue) { this.dateLivraisonPrevue = dateLivraisonPrevue; }
    public void setFournisseurId(int fournisseurId) { this.fournisseurId = fournisseurId; }
    public void setFournisseurNom(String fournisseurNom) { this.fournisseurNom = fournisseurNom; }
    public void setProduitsLivres(String produitsLivres) { this.produitsLivres = produitsLivres; }
    public void setQuantiteLivree(int quantiteLivree) { this.quantiteLivree = quantiteLivree; }
    public void setMontantLivraison(double montantLivraison) { this.montantLivraison = montantLivraison; }
    public void setStatutLivraison(String statutLivraison) { this.statutLivraison = statutLivraison; }
    public void setObservations(String observations) { this.observations = observations; }
    public void setTransporteur(String transporteur) { this.transporteur = transporteur; }
    public void setNumBonLivraison(String numBonLivraison) { this.numBonLivraison = numBonLivraison; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    // Méthodes utilitaires
    @Override
    public String toString() {
        return numCommande + " - " + fournisseurNom + " (" + statutLivraison + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HistoriqueLivraison that = (HistoriqueLivraison) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Méthodes de validation et calculs
    public boolean isValid() {
        return numCommande != null && !numCommande.trim().isEmpty() &&
                dateCommande != null &&
                fournisseurId > 0 &&
                fournisseurNom != null && !fournisseurNom.trim().isEmpty();
    }

    public boolean isLivree() {
        return "Livrée".equals(statutLivraison);
    }

    public boolean isEnAttente() {
        return "En attente".equals(statutLivraison);
    }

    public boolean isEnRetard() {
        if (dateLivraisonPrevue == null) return false;
        LocalDate today = LocalDate.now();
        return today.isAfter(dateLivraisonPrevue) && !isLivree();
    }

    public long getDelaiLivraison() {
        if (dateCommande == null || dateLivraison == null) return 0;
        return ChronoUnit.DAYS.between(dateCommande, dateLivraison);
    }

    public long getRetard() {
        if (dateLivraisonPrevue == null || dateLivraison == null) return 0;
        return ChronoUnit.DAYS.between(dateLivraisonPrevue, dateLivraison);
    }

    public String getStatutFormatted() {
        switch (statutLivraison) {
            case "Livrée":
                return "✅ Livrée";
            case "En attente":
                return "⏳ En attente";
            case "En cours":
                return "🚚 En cours";
            case "Retard":
                return "⚠️ En retard";
            case "Annulée":
                return "❌ Annulée";
            default:
                return statutLivraison;
        }
    }

    public String getMontantFormatted() {
        return String.format("%.2f MAD", montantLivraison);
    }

    public String getDelaiFormatted() {
        long delai = getDelaiLivraison();
        if (delai == 0) {
            return "Immédiat";
        } else if (delai == 1) {
            return "1 jour";
        } else {
            return delai + " jours";
        }
    }

    public String getRetardFormatted() {
        long retard = getRetard();
        if (retard <= 0) {
            return "À temps";
        } else if (retard == 1) {
            return "1 jour de retard";
        } else {
            return retard + " jours de retard";
        }
    }
}