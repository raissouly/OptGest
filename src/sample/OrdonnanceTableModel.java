package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrdonnanceTableModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty clientName;
    private final SimpleStringProperty doctorName;
    private final SimpleStringProperty dateFormatted;
    private final SimpleStringProperty typeVerre;
    private final SimpleStringProperty priorite;
    private final SimpleStringProperty scanFileName;
    private Ordonnance originalOrdonnance;

    // Constructor that accepts String for date (matches controller usage)
    public OrdonnanceTableModel(int id, String clientName, String doctorName, String dateFormatted,
                                String typeVerre, String priorite, String scanFileName) {
        this.id = new SimpleIntegerProperty(id);
        this.clientName = new SimpleStringProperty(clientName);
        this.doctorName = new SimpleStringProperty(doctorName);
        this.dateFormatted = new SimpleStringProperty(dateFormatted);
        this.typeVerre = new SimpleStringProperty(typeVerre);
        this.priorite = new SimpleStringProperty(priorite);
        this.scanFileName = new SimpleStringProperty(scanFileName);
    }

    // Alternative constructor that accepts LocalDate
    public OrdonnanceTableModel(int id, String clientName, String doctorName, LocalDate date,
                                String typeVerre, String priorite, String scanFileName) {
        this.id = new SimpleIntegerProperty(id);
        this.clientName = new SimpleStringProperty(clientName);
        this.doctorName = new SimpleStringProperty(doctorName);
        this.dateFormatted = new SimpleStringProperty(
                date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Date non spécifiée"
        );
        this.typeVerre = new SimpleStringProperty(typeVerre);
        this.priorite = new SimpleStringProperty(priorite);
        this.scanFileName = new SimpleStringProperty(scanFileName);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getClientName() { return clientName.get(); }
    public String getDoctorName() { return doctorName.get(); }
    public String getDateFormatted() { return dateFormatted.get(); }
    public String getTypeVerre() { return typeVerre.get(); }
    public String getPriorite() { return priorite.get(); }
    public String getScanFileName() { return scanFileName.get(); }

    // Property getters for JavaFX binding
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty clientNameProperty() { return clientName; }
    public SimpleStringProperty doctorNameProperty() { return doctorName; }
    public SimpleStringProperty dateFormattedProperty() { return dateFormatted; }
    public SimpleStringProperty typeVerreProperty() { return typeVerre; }
    public SimpleStringProperty prioriteProperty() { return priorite; }
    public SimpleStringProperty scanFileNameProperty() { return scanFileName; }

    // Setters for updating values
    public void setClientName(String clientName) { this.clientName.set(clientName); }
    public void setDoctorName(String doctorName) { this.doctorName.set(doctorName); }
    public void setDateFormatted(String dateFormatted) { this.dateFormatted.set(dateFormatted); }
    public void setTypeVerre(String typeVerre) { this.typeVerre.set(typeVerre); }
    public void setPriorite(String priorite) { this.priorite.set(priorite); }
    public void setScanFileName(String scanFileName) { this.scanFileName.set(scanFileName); }

    // Original ordonnance reference
    public Ordonnance getOriginalOrdonnance() { return originalOrdonnance; }
    public void setOriginalOrdonnance(Ordonnance originalOrdonnance) { this.originalOrdonnance = originalOrdonnance; }
}