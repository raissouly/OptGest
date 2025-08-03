package sample;

public class CommandeHistorique {
    private String date;
    private String produit;
    private int quantite;
    private double total;
    private String statut;

    public CommandeHistorique(String date, String produit, int quantite, double total, String statut) {
        this.date = date;
        this.produit = produit;
        this.quantite = quantite;
        this.total = total;
        this.statut = statut;
    }

    public String getDate() {
        return date;
    }

    public String getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getTotal() {
        return total;
    }

    public String getStatut() {
        return statut;
    }
}
