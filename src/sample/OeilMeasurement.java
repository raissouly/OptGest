package sample;

public class OeilMeasurement {
    private double sphere;
    private double cylindre;
    private double axe;
    private double addition;
    private double pd; // pupillary distance
    private double hauteurMontage;

    // Constructors
    public OeilMeasurement() {}

    public OeilMeasurement(double sphere, double cylindre, double axe, double addition) {
        this.sphere = sphere;
        this.cylindre = cylindre;
        this.axe = axe;
        this.addition = addition;
    }

    // Getters and Setters
    public double getSphere() { return sphere; }
    public void setSphere(double sphere) { this.sphere = sphere; }

    public double getCylindre() { return cylindre; }
    public void setCylindre(double cylindre) { this.cylindre = cylindre; }

    public double getAxe() { return axe; }
    public void setAxe(double axe) { this.axe = axe; }

    public double getAddition() { return addition; }
    public void setAddition(double addition) { this.addition = addition; }

    public double getPd() { return pd; }
    public void setPd(double pd) { this.pd = pd; }

    public double getHauteurMontage() { return hauteurMontage; }
    public void setHauteurMontage(double hauteurMontage) { this.hauteurMontage = hauteurMontage; }

    @Override
    public String toString() {
        return String.format("SPH: %.2f, CYL: %.2f, AXE: %.0f°, ADD: %.2f",
                sphere, cylindre, axe, addition);
    }
}