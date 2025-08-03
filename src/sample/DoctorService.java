package sample;

import java.util.ArrayList;
import java.util.List;

public class DoctorService {
    private List<Doctor> doctors;

    public DoctorService() {
        this.doctors = new ArrayList<>();
        initializeDummyData();
    }

    private void initializeDummyData() {
        doctors.add(new Doctor(1, "BENNANI", "Youssef", "Ophtalmologie", "0512345678", "y.bennani@clinic.ma"));
        doctors.add(new Doctor(2, "TAZI", "Aicha", "Optométrie", "0523456789", "a.tazi@clinic.ma"));
        doctors.add(new Doctor(3, "RIFAI", "Hassan", "Ophtalmologie", "0534567890", "h.rifai@clinic.ma"));
    }

    public List<Doctor> getAllDoctors() {
        return new ArrayList<>(doctors);
    }

    public Doctor getDoctorById(int id) {
        return doctors.stream()
                .filter(doctor -> doctor.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public void updateDoctor(Doctor doctor) {
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getId() == doctor.getId()) {
                doctors.set(i, doctor);
                break;
            }
        }
    }

    public void deleteDoctor(int id) {
        doctors.removeIf(doctor -> doctor.getId() == id);
    }
}