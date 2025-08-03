package sample;

public class ProduitItem {
    private String reference;
    private String nom;
    private String categorie;

    public ProduitItem(String reference, String nom) {
        this.reference = reference;
        this.nom = nom;
        this.categorie = "";
    }

    public ProduitItem(String reference, String nom, String categorie) {
        this.reference = reference;
        this.nom = nom;
        this.categorie = categorie;
    }

    public String getReference() {
        return reference;
    }

    public String getNom() {
        return nom;
    }

    public String getCategorie() {
        return categorie;
    }

    @Override
    public String toString() {
        return nom;
    }
}
