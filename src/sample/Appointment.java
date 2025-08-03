package sample;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private IntegerProperty id;
    private StringProperty clientName;
    private StringProperty clientPhone;
    private ObjectProperty<LocalDate> date;
    private ObjectProperty<LocalTime> time;
    private StringProperty optician;
    private StringProperty service;
    private StringProperty status;
    private StringProperty notes;

    // Constructeur avec ID (pour les données de la base)
    public Appointment(int id, String clientName, String clientPhone, LocalDate date, LocalTime time,
                       String optician, String service, String status, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.clientName = new SimpleStringProperty(clientName);
        this.clientPhone = new SimpleStringProperty(clientPhone);
        this.date = new SimpleObjectProperty<>(date);
        this.time = new SimpleObjectProperty<>(time);
        this.optician = new SimpleStringProperty(optician);
        this.service = new SimpleStringProperty(service);
        this.status = new SimpleStringProperty(status);
        this.notes = new SimpleStringProperty(notes);
    }

    // Constructeur sans ID (pour les nouveaux rdv)
    public Appointment(String clientName, String clientPhone, LocalDate date, LocalTime time,
                       String optician, String service, String status, String notes) {
        this(0, clientName, clientPhone, date, time, optician, service, status, notes);
    }

    // Getters et setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getClientName() { return clientName.get(); }
    public void setClientName(String clientName) { this.clientName.set(clientName); }
    public StringProperty clientNameProperty() { return clientName; }

    public String getClientPhone() { return clientPhone.get(); }
    public void setClientPhone(String clientPhone) { this.clientPhone.set(clientPhone); }
    public StringProperty clientPhoneProperty() { return clientPhone; }

    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public LocalTime getTime() { return time.get(); }
    public void setTime(LocalTime time) { this.time.set(time); }
    public ObjectProperty<LocalTime> timeProperty() { return time; }

    public String getOptician() { return optician.get(); }
    public void setOptician(String optician) { this.optician.set(optician); }
    public StringProperty opticianProperty() { return optician; }

    public String getService() { return service.get(); }
    public void setService(String service) { this.service.set(service); }
    public StringProperty serviceProperty() { return service; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Appointment)) return false;
        Appointment other = (Appointment) obj;
        return this.getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }

    @Override
    public String toString() {
        return String.format("Appointment{id=%d, client='%s', date=%s, time=%s}",
                getId(), getClientName(), getDate(), getTime());
    }
}