package sample;

public class Doctor {
    private int id;
    private String nom;
    private String prenom;
    private String specialite;
    private String telephone;
    private String email;

    public Doctor() {}

    public Doctor(int id, String nom, String prenom, String specialite, String telephone, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.telephone = telephone;
        this.email = email;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "Dr. " + getFullName() + " (" + specialite + ")";
    }
}