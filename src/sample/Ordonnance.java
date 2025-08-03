package sample;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Ordonnance {
    private int id;
    private int clientId;
    private int doctorId;
    private LocalDate date_Ordonnance;
    private String description;
    private String scan_path;

    // Right Eye (OD - Oculus Dexter)
    private String OD_Sph;
    private String OD_Cyl;
    private String OD_Axe;
    private String OD_Add;

    // Left Eye (OG - Oculus Gauche)
    private String OG_Sph;
    private String OG_Cyl;
    private String OG_Axe;
    private String OG_Add;

    private String distance_Pupillaire;
    private String hauteur_Montage;
    private String lens_type;
    private String priority_level;
    private LocalDate created_date;

    // Constructor with all parameters
    public Ordonnance(int id, int clientId, int doctorId, LocalDate date_Ordonnance, String description, String scan_path,
                      String OD_Sph, String OD_Cyl, String OD_Axe, String OD_Add,
                      String OG_Sph, String OG_Cyl, String OG_Axe, String OG_Add,
                      String distance_Pupillaire, String hauteur_Montage,
                      String lens_type, String priority_level) {
        this.id = id;
        this.clientId = clientId;
        this.doctorId = doctorId;
        this.date_Ordonnance = date_Ordonnance;
        this.description = description;
        this.scan_path = scan_path;
        this.OD_Sph = OD_Sph;
        this.OD_Cyl = OD_Cyl;
        this.OD_Axe = OD_Axe;
        this.OD_Add = OD_Add;
        this.OG_Sph = OG_Sph;
        this.OG_Cyl = OG_Cyl;
        this.OG_Axe = OG_Axe;
        this.OG_Add = OG_Add;
        this.distance_Pupillaire = distance_Pupillaire;
        this.hauteur_Montage = hauteur_Montage;
        this.lens_type = lens_type;
        this.priority_level = priority_level;
        this.created_date = LocalDate.now();
    }

    // Constructor without id (for new ordonnances)
    public Ordonnance(int clientId, int doctorId, LocalDate date_Ordonnance, String description, String scan_path,
                      String OD_Sph, String OD_Cyl, String OD_Axe, String OD_Add,
                      String OG_Sph, String OG_Cyl, String OG_Axe, String OG_Add,
                      String distance_Pupillaire, String hauteur_Montage,
                      String lens_type, String priority_level) {
        this(0, clientId, doctorId, date_Ordonnance, description, scan_path,
                OD_Sph, OD_Cyl, OD_Axe, OD_Add, OG_Sph, OG_Cyl, OG_Axe, OG_Add,
                distance_Pupillaire, hauteur_Montage, lens_type, priority_level);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate_Ordonnance() {
        return date_Ordonnance;
    }

    public String getDescription() {
        return description;
    }

    public String getScan_path() {
        return scan_path;
    }

    public String getScanPath() { // Alias for consistency
        return scan_path;
    }

    public String getOD_Sph() {
        return OD_Sph;
    }

    public String getOD_Cyl() {
        return OD_Cyl;
    }

    public String getOD_Axe() {
        return OD_Axe;
    }

    public String getOD_Add() {
        return OD_Add;
    }

    public String getOG_Sph() {
        return OG_Sph;
    }

    public String getOG_Cyl() {
        return OG_Cyl;
    }

    public String getOG_Axe() {
        return OG_Axe;
    }

    public String getOG_Add() {
        return OG_Add;
    }

    public String getDistance_Pupillaire() {
        return distance_Pupillaire;
    }

    public String getDistancePupillaire() { // Alias for consistency
        return distance_Pupillaire;
    }

    public String getHauteur_Montage() {
        return hauteur_Montage;
    }

    public String getHauteurMontage() { // Alias for consistency
        return hauteur_Montage;
    }

    public String getLens_type() {
        return lens_type;
    }

    public String getPriority_level() {
        return priority_level;
    }

    public LocalDate getCreated_date() {
        return created_date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public void setDate_Ordonnance(LocalDate date_Ordonnance) {
        this.date_Ordonnance = date_Ordonnance;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setScan_path(String scan_path) {
        this.scan_path = scan_path;
    }

    public void setOD_Sph(String OD_Sph) {
        this.OD_Sph = OD_Sph;
    }

    public void setOD_Cyl(String OD_Cyl) {
        this.OD_Cyl = OD_Cyl;
    }

    public void setOD_Axe(String OD_Axe) {
        this.OD_Axe = OD_Axe;
    }

    public void setOD_Add(String OD_Add) {
        this.OD_Add = OD_Add;
    }

    public void setOG_Sph(String OG_Sph) {
        this.OG_Sph = OG_Sph;
    }

    public void setOG_Cyl(String OG_Cyl) {
        this.OG_Cyl = OG_Cyl;
    }

    public void setOG_Axe(String OG_Axe) {
        this.OG_Axe = OG_Axe;
    }

    public void setOG_Add(String OG_Add) {
        this.OG_Add = OG_Add;
    }

    public void setDistance_Pupillaire(String distance_Pupillaire) {
        this.distance_Pupillaire = distance_Pupillaire;
    }

    public void setHauteur_Montage(String hauteur_Montage) {
        this.hauteur_Montage = hauteur_Montage;
    }

    public void setLens_type(String lens_type) {
        this.lens_type = lens_type;
    }

    public void setPriority_level(String priority_level) {
        this.priority_level = priority_level;
    }

    public void setCreated_date(LocalDate created_date) {
        this.created_date = created_date;
    }

    // Utility methods referenced in your controller
    public boolean hasRightEyePrescription() {
        return (OD_Sph != null && !OD_Sph.trim().isEmpty()) ||
                (OD_Cyl != null && !OD_Cyl.trim().isEmpty()) ||
                (OD_Axe != null && !OD_Axe.trim().isEmpty()) ||
                (OD_Add != null && !OD_Add.trim().isEmpty());
    }

    public boolean hasLeftEyePrescription() {
        return (OG_Sph != null && !OG_Sph.trim().isEmpty()) ||
                (OG_Cyl != null && !OG_Cyl.trim().isEmpty()) ||
                (OG_Axe != null && !OG_Axe.trim().isEmpty()) ||
                (OG_Add != null && !OG_Add.trim().isEmpty());
    }

    public String getRightEyeSummary() {
        StringBuilder summary = new StringBuilder();
        if (OD_Sph != null && !OD_Sph.trim().isEmpty()) {
            summary.append("Sph: ").append(OD_Sph);
        }
        if (OD_Cyl != null && !OD_Cyl.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Cyl: ").append(OD_Cyl);
        }
        if (OD_Axe != null && !OD_Axe.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Axe: ").append(OD_Axe);
        }
        if (OD_Add != null && !OD_Add.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Add: ").append(OD_Add);
        }
        return summary.length() > 0 ? summary.toString() : "Aucune prescription";
    }

    public String getLeftEyeSummary() {
        StringBuilder summary = new StringBuilder();
        if (OG_Sph != null && !OG_Sph.trim().isEmpty()) {
            summary.append("Sph: ").append(OG_Sph);
        }
        if (OG_Cyl != null && !OG_Cyl.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Cyl: ").append(OG_Cyl);
        }
        if (OG_Axe != null && !OG_Axe.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Axe: ").append(OG_Axe);
        }
        if (OG_Add != null && !OG_Add.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Add: ").append(OG_Add);
        }
        return summary.length() > 0 ? summary.toString() : "Aucune prescription";
    }

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", doctorId=" + doctorId +
                ", date_Ordonnance=" + date_Ordonnance +
                ", description='" + description + '\'' +
                ", lens_type='" + lens_type + '\'' +
                ", priority_level='" + priority_level + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ordonnance that = (Ordonnance) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}