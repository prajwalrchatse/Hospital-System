import java.io.*;
import java.util.*;

/**
 * Hospital Appointment Management System
 *
 * Console-based Java application with file-based storage.
 * All classes are kept in a single file for simplicity.
 *
 * To compile:
 *   javac HospitalAppointmentSystem.java
 * To run:
 *   java HospitalAppointmentSystem
 */
public class HospitalAppointmentSystem {

    // ---------- CONFIG: FILE PATHS ----------
    private static final String PATIENT_FILE = "patients.txt";
    private static final String DOCTOR_FILE = "doctors.txt";
    private static final String APPOINTMENT_FILE = "appointments.txt";

    // ---------- MAIN ----------
    public static void main(String[] args) {
        ensureDataFilesExist();

        PatientRepository patientRepo = new PatientRepository(PATIENT_FILE);
        DoctorRepository doctorRepo = new DoctorRepository(DOCTOR_FILE);
        AppointmentRepository appointmentRepo = new AppointmentRepository(APPOINTMENT_FILE);

        PatientService patientService = new PatientService(patientRepo);
        DoctorService doctorService = new DoctorService(doctorRepo);
        AppointmentService appointmentService = new AppointmentService(appointmentRepo, patientRepo, doctorRepo);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMainMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    patientService.addPatient(scanner);
                    break;
                case "2":
                    patientService.listPatients();
                    break;
                case "3":
                    doctorService.addDoctor(scanner);
                    break;
                case "4":
                    doctorService.listDoctors();
                    break;
                case "5":
                    appointmentService.bookAppointment(scanner);
                    break;
                case "6":
                    appointmentService.listAppointments();
                    break;
                case "7":
                    appointmentService.cancelAppointment(scanner);
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting system. Goodbye.");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option (0-7).");
            }

            System.out.println();
        }

        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("========================================");
        System.out.println("   HOSPITAL APPOINTMENT MANAGEMENT");
        System.out.println("========================================");
        System.out.println("1. Add Patient");
        System.out.println("2. List Patients");
        System.out.println("3. Add Doctor");
        System.out.println("4. List Doctors");
        System.out.println("5. Book Appointment");
        System.out.println("6. List Appointments");
        System.out.println("7. Cancel Appointment");
        System.out.println("0. Exit");
        System.out.println("========================================");
    }

    private static void ensureDataFilesExist() {
        try {
            File p = new File(PATIENT_FILE);
            if (!p.exists()) p.createNewFile();

            File d = new File(DOCTOR_FILE);
            if (!d.exists()) d.createNewFile();

            File a = new File(APPOINTMENT_FILE);
            if (!a.exists()) a.createNewFile();
        } catch (IOException e) {
            System.out.println("Warning: Could not create data files. " + e.getMessage());
        }
    }

    // =========================================================
    // MODEL CLASSES
    // =========================================================

    static class Patient {
        private int id;
        private String name;
        private int age;
        private String gender;
        private String phone;

        public Patient(int id, String name, int age, String gender, String phone) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.phone = phone;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        public String toFileString() {
            return id + "|" + escape(name) + "|" + age + "|" + escape(gender) + "|" + escape(phone);
        }

        public static Patient fromFileString(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 5) return null;
            try {
                int id = Integer.parseInt(parts[0]);
                String name = unescape(parts[1]);
                int age = Integer.parseInt(parts[2]);
                String gender = unescape(parts[3]);
                String phone = unescape(parts[4]);
                return new Patient(id, name, age, gender, phone);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return String.format("ID: %d | Name: %s | Age: %d | Gender: %s | Phone: %s",
                    id, name, age, gender, phone);
        }
    }

    static class Doctor {
        private int id;
        private String name;
        private String specialization;

        public Doctor(int id, String name, String specialization) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSpecialization() { return specialization; }

        public String toFileString() {
            return id + "|" + escape(name) + "|" + escape(specialization);
        }

        public static Doctor fromFileString(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 3) return null;
            try {
                int id = Integer.parseInt(parts[0]);
                String name = unescape(parts[1]);
                String specialization = unescape(parts[2]);
                return new Doctor(id, name, specialization);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return String.format("ID: %d | Name: %s | Specialization: %s",
                    id, name, specialization);
        }
    }

    static class Appointment {
        private int id;
        private int patientId;
        private int doctorId;
        private String date;     // stored as string, e.g. "2025-11-24"
        private String timeSlot; // stored as string, e.g. "10:00-10:15"
        private String status;   // "BOOKED" or "CANCELLED"

        public Appointment(int id, int patientId, int doctorId,
                           String date, String timeSlot, String status) {
            this.id = id;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.date = date;
            this.timeSlot = timeSlot;
            this.status = status;
        }

        public int getId() { return id; }
        public int getPatientId() { return patientId; }
        public int getDoctorId() { return doctorId; }
        public String getDate() { return date; }
        public String getTimeSlot() { return timeSlot; }
        public String getStatus() { return status; }

        public void setStatus(String status) {
            this.status = status;
        }

        public String toFileString() {
            return id + "|" + patientId + "|" + doctorId + "|" +
                    escape(date) + "|" + escape(timeSlot) + "|" + escape(status);
        }

        public static Appointment fromFileString(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 6) return null;
            try {
                int id = Integer.parseInt(parts[0]);
                int patientId = Integer.parseInt(parts[1]);
                int doctorId = Integer.parseInt(parts[2]);
                String date = unescape(parts[3]);
                String timeSlot = unescape(parts[4]);
                String status = unescape(parts[5]);
                return new Appointment(id, patientId, doctorId, date, timeSlot, status);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return String.format(
                    "Appointment ID: %d | PatientID: %d | DoctorID: %d | Date: %s | Time: %s | Status: %s",
                    id, patientId, doctorId, date, timeSlot, status);
        }
    }

    // =========================================================
    // REPOSITORIES (FILE HANDLING)
    // =========================================================

    static class PatientRepository {
        private final String filePath;

        public PatientRepository(String filePath) {
            this.filePath = filePath;
        }

        public List<Patient> findAll() {
            List<Patient> list = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    Patient p = Patient.fromFileString(line);
                    if (p != null) list.add(p);
                }
            } catch (IOException e) {
                System.out.println("Error reading patients file: " + e.getMessage());
            }
            return list;
        }

        public void saveAll(List<Patient> patients) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
                for (Patient p : patients) {
                    pw.println(p.toFileString());
                }
            } catch (IOException e) {
                System.out.println("Error writing patients file: " + e.getMessage());
            }
        }

        public int getNextId() {
            List<Patient> list = findAll();
            int max = 0;
            for (Patient p : list) {
                if (p.getId() > max) max = p.getId();
            }
            return max + 1;
        }

        public Patient findById(int id) {
            for (Patient p : findAll()) {
                if (p.getId() == id) return p;
            }
            return null;
        }
    }

    static class DoctorRepository {
        private final String filePath;

        public DoctorRepository(String filePath) {
            this.filePath = filePath;
        }

        public List<Doctor> findAll() {
            List<Doctor> list = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    Doctor d = Doctor.fromFileString(line);
                    if (d != null) list.add(d);
                }
            } catch (IOException e) {
                System.out.println("Error reading doctors file: " + e.getMessage());
            }
            return list;
        }

        public void saveAll(List<Doctor> doctors) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
                for (Doctor d : doctors) {
                    pw.println(d.toFileString());
                }
            } catch (IOException e) {
                System.out.println("Error writing doctors file: " + e.getMessage());
            }
        }

        public int getNextId() {
            List<Doctor> list = findAll();
            int max = 0;
            for (Doctor d : list) {
                if (d.getId() > max) max = d.getId();
            }
            return max + 1;
        }

        public Doctor findById(int id) {
            for (Doctor d : findAll()) {
                if (d.getId() == id) return d;
            }
            return null;
        }
    }

    static class AppointmentRepository {
        private final String filePath;

        public AppointmentRepository(String filePath) {
            this.filePath = filePath;
        }

        public List<Appointment> findAll() {
            List<Appointment> list = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    Appointment a = Appointment.fromFileString(line);
                    if (a != null) list.add(a);
                }
            } catch (IOException e) {
                System.out.println("Error reading appointments file: " + e.getMessage());
            }
            return list;
        }

        public void saveAll(List<Appointment> appointments) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
                for (Appointment a : appointments) {
                    pw.println(a.toFileString());
                }
            } catch (IOException e) {
                System.out.println("Error writing appointments file: " + e.getMessage());
            }
        }

        public int getNextId() {
            List<Appointment> list = findAll();
            int max = 0;
            for (Appointment a : list) {
                if (a.getId() > max) max = a.getId();
            }
            return max + 1;
        }

        public Appointment findById(int id) {
            for (Appointment a : findAll()) {
                if (a.getId() == id) return a;
            }
            return null;
        }
    }

    // =========================================================
    // SERVICES (BUSINESS LOGIC)
    // =========================================================

    static class PatientService {
        private final PatientRepository repository;

        public PatientService(PatientRepository repository) {
            this.repository = repository;
        }

        public void addPatient(Scanner scanner) {
            try {
                System.out.print("Enter patient name: ");
                String name = scanner.nextLine().trim();
                System.out.print("Enter age: ");
                int age = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Enter gender: ");
                String gender = scanner.nextLine().trim();
                System.out.print("Enter phone number: ");
                String phone = scanner.nextLine().trim();

                int id = repository.getNextId();
                Patient p = new Patient(id, name, age, gender, phone);

                List<Patient> all = repository.findAll();
                all.add(p);
                repository.saveAll(all);

                System.out.println("Patient added successfully with ID: " + id);
            } catch (NumberFormatException e) {
                System.out.println("Invalid age. Patient not added.");
            }
        }

        public void listPatients() {
            List<Patient> all = repository.findAll();
            if (all.isEmpty()) {
                System.out.println("No patients found.");
                return;
            }
            System.out.println("---- Patient List ----");
            for (Patient p : all) {
                System.out.println(p);
            }
        }
    }

    static class DoctorService {
        private final DoctorRepository repository;

        public DoctorService(DoctorRepository repository) {
            this.repository = repository;
        }

        public void addDoctor(Scanner scanner) {
            System.out.print("Enter doctor name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter specialization: ");
            String specialization = scanner.nextLine().trim();

            int id = repository.getNextId();
            Doctor d = new Doctor(id, name, specialization);

            List<Doctor> all = repository.findAll();
            all.add(d);
            repository.saveAll(all);

            System.out.println("Doctor added successfully with ID: " + id);
        }

        public void listDoctors() {
            List<Doctor> all = repository.findAll();
            if (all.isEmpty()) {
                System.out.println("No doctors found.");
                return;
            }
            System.out.println("---- Doctor List ----");
            for (Doctor d : all) {
                System.out.println(d);
            }
        }
    }

    static class AppointmentService {
        private final AppointmentRepository appointmentRepository;
        private final PatientRepository patientRepository;
        private final DoctorRepository doctorRepository;

        public AppointmentService(AppointmentRepository ar,
                                  PatientRepository pr,
                                  DoctorRepository dr) {
            this.appointmentRepository = ar;
            this.patientRepository = pr;
            this.doctorRepository = dr;
        }

        public void bookAppointment(Scanner scanner) {
            try {
                System.out.print("Enter patient ID: ");
                int patientId = Integer.parseInt(scanner.nextLine().trim());
                Patient patient = patientRepository.findById(patientId);
                if (patient == null) {
                    System.out.println("Invalid patient ID.");
                    return;
                }

                System.out.print("Enter doctor ID: ");
                int doctorId = Integer.parseInt(scanner.nextLine().trim());
                Doctor doctor = doctorRepository.findById(doctorId);
                if (doctor == null) {
                    System.out.println("Invalid doctor ID.");
                    return;
                }

                System.out.print("Enter appointment date (e.g., 2025-11-24): ");
                String date = scanner.nextLine().trim();
                System.out.print("Enter time slot (e.g., 10:00-10:15): ");
                String timeSlot = scanner.nextLine().trim();

                // Check for conflicts
                List<Appointment> all = appointmentRepository.findAll();
                for (Appointment a : all) {
                    if (a.getDoctorId() == doctorId &&
                            a.getDate().equalsIgnoreCase(date) &&
                            a.getTimeSlot().equalsIgnoreCase(timeSlot) &&
                            a.getStatus().equalsIgnoreCase("BOOKED")) {
                        System.out.println("Error: This time slot is already booked for the selected doctor.");
                        return;
                    }
                }

                int id = appointmentRepository.getNextId();
                Appointment newAppt = new Appointment(id, patientId, doctorId, date, timeSlot, "BOOKED");
                all.add(newAppt);
                appointmentRepository.saveAll(all);

                System.out.println("Appointment booked successfully with ID: " + id);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Appointment not booked.");
            }
        }

        public void listAppointments() {
            List<Appointment> all = appointmentRepository.findAll();
            if (all.isEmpty()) {
                System.out.println("No appointments found.");
                return;
            }

            // Preload patients and doctors into maps for quick lookup
            Map<Integer, Patient> patientMap = new HashMap<>();
            for (Patient p : patientRepository.findAll()) {
                patientMap.put(p.getId(), p);
            }
            Map<Integer, Doctor> doctorMap = new HashMap<>();
            for (Doctor d : doctorRepository.findAll()) {
                doctorMap.put(d.getId(), d);
            }

            System.out.println("---- Appointment List ----");
            for (Appointment a : all) {
                Patient p = patientMap.get(a.getPatientId());
                Doctor d = doctorMap.get(a.getDoctorId());

                String pName = (p != null) ? p.getName() : "UnknownPatient(" + a.getPatientId() + ")";
                String dName = (d != null) ? d.getName() : "UnknownDoctor(" + a.getDoctorId() + ")";

                System.out.printf(
                        "ApptID: %d | Patient: %s (ID:%d) | Doctor: %s (ID:%d) | Date: %s | Time: %s | Status: %s%n",
                        a.getId(), pName, a.getPatientId(), dName, a.getDoctorId(),
                        a.getDate(), a.getTimeSlot(), a.getStatus()
                );
            }
        }

        public void cancelAppointment(Scanner scanner) {
            try {
                System.out.print("Enter appointment ID to cancel: ");
                int apptId = Integer.parseInt(scanner.nextLine().trim());

                List<Appointment> all = appointmentRepository.findAll();
                Appointment target = null;
                for (Appointment a : all) {
                    if (a.getId() == apptId) {
                        target = a;
                        break;
                    }
                }

                if (target == null) {
                    System.out.println("Appointment not found.");
                    return;
                }

                if (target.getStatus().equalsIgnoreCase("CANCELLED")) {
                    System.out.println("Appointment is already cancelled.");
                    return;
                }

                target.setStatus("CANCELLED");
                appointmentRepository.saveAll(all);
                System.out.println("Appointment cancelled successfully.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid appointment ID.");
            }
        }
    }

    // =========================================================
    // UTILITY METHODS
    // =========================================================

    // Very simple escaping to avoid breaking file format with pipes and newlines.
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("|", "\\p")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        // reverse of escape, careful with order
        return s.replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\p", "|")
                .replace("\\\\", "\\");
    }
}