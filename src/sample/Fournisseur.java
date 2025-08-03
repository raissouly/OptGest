package sample;

import java.time.LocalDateTime;

public class Fournisseur {
    private int id;
    private String nom;
    private String codeFournisseur;
    private String telephone;
    private String fax;
    private String email;
    private String siteWeb;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String statut;
    private String notes;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Constructeur par défaut
    public Fournisseur() {

    }
    public Fournisseur(int id, String nom, String ville) {
        this.id = id;
        this.nom = nom;
        this.ville = ville;
    }

    // Constructeur avec paramètres
    public Fournisseur(String nom, String codeFournisseur, String telephone, String email) {
        this.nom = nom;
        this.codeFournisseur = codeFournisseur;
        this.telephone = telephone;
        this.email = email;
        this.pays = "Maroc";
        this.statut = "Actif";
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getCodeFournisseur() { return codeFournisseur; }
    public String getTelephone() { return telephone; }
    public String getFax() { return fax; }
    public String getEmail() { return email; }
    public String getSiteWeb() { return siteWeb; }
    public String getAdresse() { return adresse; }
    public String getVille() { return ville; }
    public String getCodePostal() { return codePostal; }
    public String getPays() { return pays; }
    public String getStatut() { return statut; }
    public String getNotes() { return notes; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setCodeFournisseur(String codeFournisseur) { this.codeFournisseur = codeFournisseur; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setFax(String fax) { this.fax = fax; }
    public void setEmail(String email) { this.email = email; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setVille(String ville) { this.ville = ville; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    public void setPays(String pays) { this.pays = pays; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    // Méthodes utilitaires
    @Override
    public String toString() {
        return nom + " (" + codeFournisseur + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fournisseur that = (Fournisseur) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Méthodes de validation
    public boolean isValid() {
        return nom != null && !nom.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                codeFournisseur != null && !codeFournisseur.trim().isEmpty();
    }

    public boolean isActif() {
        return "Actif".equals(statut);
    }
}