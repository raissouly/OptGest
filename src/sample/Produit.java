package sample;

public class Produit {
    private String reference;
    private String designation;
    private String taille;
    private int quantite;
    private double prixUnitaire;
    private String couleur;
    private String type;
    private String marque;
    private int stockMinimal;
    private Integer fournisseurId;
    private String fournisseurNom;
    public Produit() {
        // يمكنك تركه فارغًا أو تعيين قيم افتراضية إذا أحببت
    }



    public Produit(String reference, String designation, int quantite, int stockMinimal,Integer fournisseurId, String fournisseurNom) {
        this.reference = reference;
        this.designation = designation;
        this.quantite = quantite;
        this.stockMinimal = stockMinimal;
        this.fournisseurId = fournisseurId;
        this.fournisseurNom = fournisseurNom;


    }

    public Integer getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Integer fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public String getFournisseurNom() {
        return fournisseurNom;
    }

    public void setFournisseurNom(String fournisseurNom) {
        this.fournisseurNom = fournisseurNom;
    }

    public Produit(String reference, String designation, String taille, int quantite,
                   double prixUnitaire, String couleur, String type, String marque, int stockMinimal) {
        this.reference = reference;
        this.designation = designation;
        this.taille = taille;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.couleur = couleur;
        this.type = type;
        this.marque = marque;
        this.stockMinimal = stockMinimal;



    }

    // Getters and Setters
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getTaille() {
        return taille;
    }

    public void setTaille(String taille) {
        this.taille = taille;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public int getStockMinimal() {
        return stockMinimal;
    }

    public void setStockMinimal(int stockMinimal) {
        this.stockMinimal = stockMinimal;
    }

    // Enhanced methods for inventory management

    /**
     * Checks if the product is out of stock
     * @return true if quantity is 0
     */
    public boolean isOutOfStock() {
        return quantite == 0;
    }

    /**
     * Checks if the product has low stock
     * @return true if quantity is below or equal to minimum stock level
     */
    public boolean isLowStock() {
        return quantite > 0 && quantite <= stockMinimal;
    }

    /**
     * Gets the stock status as a string
     * @return "Out of Stock", "Low Stock", or "In Stock"
     */
    public String getStockStatus() {
        if (isOutOfStock()) {
            return "Out of Stock";
        } else if (isLowStock()) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }

    /**
     * Calculates the total value of this product in inventory
     * @return quantity * unit price
     */
    public double getTotalValue() {
        return quantite * prixUnitaire;
    }

    /**
     * Checks if the product needs restocking
     * @return true if out of stock or low stock
     */
    public boolean needsRestock() {
        return isOutOfStock() || isLowStock();
    }

    /**
     * Calculates how many units are needed to reach minimum stock
     * @return number of units needed, or 0 if already above minimum
     */
    public int getUnitsNeededForMinStock() {
        if (quantite >= stockMinimal) {
            return 0;
        }
        return stockMinimal - quantite;
    }

    /**
     * Safely adds stock (prevents negative quantities)
     * @param amount amount to add
     * @return true if successful, false if amount is negative
     */
    public boolean addStock(int amount) {
        if (amount < 0) {
            return false;
        }
        this.quantite += amount;
        return true;
    }

    /**
     * Safely removes stock (prevents negative quantities)
     * @param amount amount to remove
     * @return true if successful, false if insufficient stock or negative amount
     */
    public boolean removeStock(int amount) {
        if (amount < 0 || amount > this.quantite) {
            return false;
        }
        this.quantite -= amount;
        return true;
    }

    /**
     * Gets a formatted string representation of the product
     * @return formatted product information
     */
    public String getFormattedInfo() {
        return String.format("%s - %s (%s %s) - Qty: %d - $%.2f - %s",
                reference, designation, couleur != null ? couleur : "",
                taille != null ? taille : "", quantite, prixUnitaire, getStockStatus());
    }

    /**
     * Checks if product matches search criteria
     * @param searchTerm search term to match against
     * @return true if any field contains the search term (case-insensitive)
     */
    public boolean matchesSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }

        String term = searchTerm.toLowerCase().trim();
        return (reference != null && reference.toLowerCase().contains(term)) ||
                (designation != null && designation.toLowerCase().contains(term)) ||
                (couleur != null && couleur.toLowerCase().contains(term)) ||
                (marque != null && marque.toLowerCase().contains(term)) ||
                (type != null && type.toLowerCase().contains(term)) ||
                (taille != null && taille.toLowerCase().contains(term));
    }

    /**
     * Validates that all required fields are present
     * @return true if product is valid
     */
    public boolean isValid() {
        return reference != null && !reference.trim().isEmpty() &&
                designation != null && !designation.trim().isEmpty() &&
                quantite >= 0 && prixUnitaire >= 0 && stockMinimal >= 0;
    }

    /**
     * Creates a copy of this product
     * @return new Produit instance with same values
     */
    public Produit copy() {
        return new Produit(reference, designation, taille, quantite,
                prixUnitaire, couleur, type, marque, stockMinimal);
    }

    @Override
    public String toString() {
        return String.format("Produit{ref='%s', designation='%s', qty=%d, price=%.2f, stock=%s}",
                reference, designation, quantite, prixUnitaire, getStockStatus());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Produit produit = (Produit) obj;
        return reference != null ? reference.equals(produit.reference) : produit.reference == null;
    }

    @Override
    public int hashCode() {
        return reference != null ? reference.hashCode() : 0;
    }
}