import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;

// Student Class
class Student {
    private String id;
    private String name;
    private String contact;
    private int distance;
    private int merit;
    private String father;
    private String economic;
    private String roomNumber;

    public Student(String id, String name, String contact, int distance, int merit, String father, String economic) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.distance = distance;
        this.merit = merit;
        this.father = father;
        this.economic = economic;
        this.roomNumber = null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public int getDistance() { return distance; }
    public int getMerit() { return merit; }
    public String getFather() { return father; }
    public String getEconomic() { return economic; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}

// Room Class
class Room {
    private String roomNumber;
    private int capacity;
    private List<String> occupants;

    public Room(String roomNumber, int capacity) {
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupants = new ArrayList<>();
    }

    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public List<String> getOccupants() { return occupants; }

    public boolean isAvailable() {
        return occupants.size() < capacity;
    }

    public void addOccupant(String studentId) {
        if (isAvailable()) {
            occupants.add(studentId);
        } else {
            throw new IllegalStateException("Room is full");
        }
    }

    public void removeOccupant(String studentId) {
        occupants.remove(studentId);
    }
}

// Complaint Class
class Complaint {
    private String complaintId;
    private String studentId;
    private String description;
    private boolean isResolved;

    public Complaint(String complaintId, String studentId, String description) {
        this.complaintId = complaintId;
        this.studentId = studentId;
        this.description = description;
        this.isResolved = false;
    }

    public String getComplaintId() { return complaintId; }
    public String getStudentId() { return studentId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { this.isResolved = resolved; }
}

// Appointment Class
class Appointment {
    private String appointmentId;
    private String studentId;
    private String authority;
    private String date;
    private String time;

    public Appointment(String appointmentId, String studentId, String authority, String date, String time) {
        this.appointmentId = appointmentId;
        this.studentId = studentId;
        this.authority = authority;
        this.date = date;
        this.time = time;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getStudentId() { return studentId; }
    public String getAuthority() { return authority; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}

// HallManagementSystem Class
class HallManagementSystem {
    private List<Student> students;
    private List<Room> rooms;
    private List<Complaint> complaints;
    private List<Appointment> appointments;

    public HallManagementSystem() {
        students = new ArrayList<>();
        rooms = new ArrayList<>();
        complaints = new ArrayList<>();
        appointments = new ArrayList<>();
        loadData(); // Load data from files on startup
    }

    public void addStudent(Student student) { students.add(student); saveData(); }
    public void updateStudent(String id, Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(id)) {
                students.set(i, updatedStudent);
                saveData();
                return;
            }
        }
        throw new IllegalArgumentException("Student not found");
    }
    public void deleteStudent(String id) {
        Student student = getStudent(id);
        if (student != null && student.getRoomNumber() != null) {
            unassignSeat(id);
        }
        students.removeIf(s -> s.getId().equals(id));
        saveData();
    }
    public Student getStudent(String id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    public List<Student> getStudents() { return students; }

    public void addRoom(Room room) { rooms.add(room); saveData(); }
    public void deleteRoom(String roomNumber) {
        Room room = getRoom(roomNumber);
        if (room != null && !room.getOccupants().isEmpty()) {
            throw new IllegalStateException("Cannot delete room with occupants");
        }
        rooms.removeIf(r -> r.getRoomNumber().equals(roomNumber));
        saveData();
    }
    public Room getRoom(String roomNumber) {
        return rooms.stream().filter(r -> r.getRoomNumber().equals(roomNumber)).findFirst().orElse(null);
    }
    public List<Room> getRooms() { return rooms; }

    public void assignSeat(String studentId, String roomNumber) {
        Student student = getStudent(studentId);
        Room room = getRoom(roomNumber);
        if (student == null) throw new IllegalArgumentException("Student not found");
        if (room == null) throw new IllegalArgumentException("Room not found");
        if (!room.isAvailable()) throw new IllegalStateException("Room is full");
        if (student.getRoomNumber() != null) throw new IllegalStateException("Student already assigned to a room");
        room.addOccupant(studentId);
        student.setRoomNumber(roomNumber);
        saveData();
    }

    public void unassignSeat(String studentId) {
        Student student = getStudent(studentId);
        if (student == null) throw new IllegalArgumentException("Student not found");
        String roomNumber = student.getRoomNumber();
        if (roomNumber != null) {
            Room room = getRoom(roomNumber);
            if (room != null) {
                room.removeOccupant(studentId);
                student.setRoomNumber(null);
                saveData();
            }
        }
    }

    public void submitComplaint(Complaint complaint) { complaints.add(complaint); saveData(); }
    public List<Complaint> getComplaints() { return complaints; }

    public void scheduleAppointment(Appointment appointment) { appointments.add(appointment); saveData(); }
    public List<Appointment> getAppointments() { return appointments; }

    // Data Persistence
    private void saveData() {
        try {
            // Save students
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("students.dat"))) {
                oos.writeObject(students);
            }
            // Save rooms
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("rooms.dat"))) {
                oos.writeObject(rooms);
            }
            // Save complaints
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("complaints.dat"))) {
                oos.writeObject(complaints);
            }
            // Save appointments
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("appointments.dat"))) {
                oos.writeObject(appointments);
            }
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            // Load students
            File studentFile = new File("students.dat");
            if (studentFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(studentFile))) {
                    students = (List<Student>) ois.readObject();
                }
            } else {
                // Sample data if no file exists
                addStudent(new Student("S001", "John Doe", "123456789", 50, 85, "Engineer", "Middle"));
                addStudent(new Student("S002", "Jane Smith", "987654321", 30, 90, "Teacher", "Low"));
            }
            // Load rooms
            File roomFile = new File("rooms.dat");
            if (roomFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(roomFile))) {
                    rooms = (List<Room>) ois.readObject();
                }
            } else {
                addRoom(new Room("R101", 2));
                addRoom(new Room("R102", 2));
            }
            // Load complaints
            File complaintFile = new File("complaints.dat");
            if (complaintFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(complaintFile))) {
                    complaints = (List<Complaint>) ois.readObject();
                }
            }
            // Load appointments
            File appointmentFile = new File("appointments.dat");
            if (appointmentFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(appointmentFile))) {
                    appointments = (List<Appointment>) ois.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    // Export data to text file
    public void exportData(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Students:");
            for (Student s : students) {
                writer.println(String.format("%s,%s,%s,%d,%d,%s,%s,%s",
                        s.getId(), s.getName(), s.getContact(), s.getDistance(), s.getMerit(),
                        s.getFather(), s.getEconomic(), s.getRoomNumber() != null ? s.getRoomNumber() : "None"));
            }
            writer.println("\nRooms:");
            for (Room r : rooms) {
                writer.println(String.format("%s,%d,%s",
                        r.getRoomNumber(), r.getCapacity(), r.getOccupants()));
            }
            writer.println("\nComplaints:");
            for (Complaint c : complaints) {
                writer.println(String.format("%s,%s,%s,%b",
                        c.getComplaintId(), c.getStudentId(), c.getDescription(), c.isResolved()));
            }
            writer.println("\nAppointments:");
            for (Appointment a : appointments) {
                writer.println(String.format("%s,%s,%s,%s,%s",
                        a.getAppointmentId(), a.getStudentId(), a.getAuthority(), a.getDate(), a.getTime()));
            }
        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }
}

// Seat Allocator
class SeatAllocator {
    public static void allocateSeats(HallManagementSystem system) {
        List<Student> unassignedStudents = system.getStudents().stream()
                .filter(s -> s.getRoomNumber() == null)
                .sorted(Comparator.comparingInt(Student::getDistance).reversed()
                        .thenComparingInt(Student::getMerit).reversed()
                        .thenComparing(Student::getFather)
                        .thenComparing(Student::getEconomic))
                .collect(Collectors.toList());

        for (Student student : unassignedStudents) {
            for (Room room : system.getRooms()) {
                try {
                    system.assignSeat(student.getId(), room.getRoomNumber());
                    break;
                } catch (IllegalStateException ignored) {
                    // Room is full, try next room
                }
            }
        }
    }
}

// GUI Classes
class MainGUI extends JFrame {
    public MainGUI(HallManagementSystem system) {
        setTitle("Hall Management System");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 248, 255)); // Light blue background

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        tabbedPane.addTab("Dashboard", new DashboardPanel(system));
        tabbedPane.addTab("Students", new StudentPanel(system));
        tabbedPane.addTab("Rooms", new RoomPanel(system));
        tabbedPane.addTab("Complaints", new ComplaintPanel(system));
        tabbedPane.addTab("Appointments", new AppointmentPanel(system));

        add(tabbedPane);
        setVisible(true);
    }
}

class DashboardPanel extends JPanel {
    public DashboardPanel(HallManagementSystem system) {
        setLayout(new GridLayout(4, 2, 10, 10));
        setBackground(new Color(245, 245, 220)); // Beige background
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel totalStudentsLabel = new JLabel("Total Students: " + system.getStudents().size());
        totalStudentsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(totalStudentsLabel);

        JLabel occupiedRoomsLabel = new JLabel("Occupied Rooms: " +
                system.getRooms().stream().filter(r -> !r.getOccupants().isEmpty()).count());
        occupiedRoomsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(occupiedRoomsLabel);

        JLabel totalRoomsLabel = new JLabel("Total Rooms: " + system.getRooms().size());
        totalRoomsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(totalRoomsLabel);

        JLabel complaintsLabel = new JLabel("Open Complaints: " +
                system.getComplaints().stream().filter(c -> !c.isResolved()).count());
        complaintsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(complaintsLabel);

        JLabel appointmentsLabel = new JLabel("Scheduled Appointments: " + system.getAppointments().size());
        appointmentsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(appointmentsLabel);

        JButton exportButton = new JButton("Export Data");
        exportButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exportButton.setBackground(new Color(100, 149, 237));
        exportButton.setForeground(Color.WHITE);
        exportButton.addActionListener(e -> {
            system.exportData("hall_management_export.txt");
            JOptionPane.showMessageDialog(this, "Data exported to hall_management_export.txt");
        });
        add(exportButton);
    }
}

class StudentPanel extends JPanel {
    private HallManagementSystem system;
    private JTable studentTable;
    private StudentTableModel tableModel;

    public StudentPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));

        tableModel = new StudentTableModel(system.getStudents());
        studentTable = new JTable(tableModel);
        studentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        studentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton addButton = new JButton("Add Student");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String id = JOptionPane.showInputDialog("Enter Student ID (e.g., S003):");
                if (id == null || id.trim().isEmpty() || !id.matches("S\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid ID format (e.g., S003)");
                    return;
                }
                if (system.getStudent(id) != null) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists");
                    return;
                }
                String name = JOptionPane.showInputDialog("Enter Name:");
                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty");
                    return;
                }
                String contact = JOptionPane.showInputDialog("Enter Contact (e.g., 123456789):");
                if (contact == null || contact.trim().isEmpty() || !contact.matches("\\d{9,10}")) {
                    JOptionPane.showMessageDialog(this, "Invalid contact number (9-10 digits)");
                    return;
                }
                String distanceStr = JOptionPane.showInputDialog("Enter Distance from Dhaka (km):");
                if (distanceStr == null || distanceStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be empty");
                    return;
                }
                int distance = Integer.parseInt(distanceStr);
                if (distance < 0) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be negative");
                    return;
                }
                String meritStr = JOptionPane.showInputDialog("Enter Merit Score (0-100):");
                if (meritStr == null || meritStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Merit score cannot be empty");
                    return;
                }
                int merit = Integer.parseInt(meritStr);
                if (merit < 0 || merit > 100) {
                    JOptionPane.showMessageDialog(this, "Merit score must be between 0 and 100");
                    return;
                }
                String father = JOptionPane.showInputDialog("Enter Father's Occupation:");
                if (father == null || father.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Father's occupation cannot be empty");
                    return;
                }
                String economic = JOptionPane.showInputDialog("Enter Economic Situation (e.g., Low, Middle, High):");
                if (economic == null || economic.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Economic situation cannot be empty");
                    return;
                }
                system.addStudent(new Student(id, name, contact, distance, merit, father, economic));
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Student added successfully");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for distance or merit");
            }
        });
        buttonPanel.add(addButton);

        JButton updateButton = new JButton("Update Student");
        styleButton(updateButton);
        updateButton.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row >= 0) {
                try {
                    String id = (String) tableModel.getValueAt(row, 0);
                    Student student = system.getStudent(id);
                    String name = JOptionPane.showInputDialog("Enter Name:", student.getName());
                    if (name == null || name.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Name cannot be empty");
                        return;
                    }
                    String contact = JOptionPane.showInputDialog("Enter Contact:", student.getContact());
                    if (contact == null || contact.trim().isEmpty() || !contact.matches("\\d{9,10}")) {
                        JOptionPane.showMessageDialog(this, "Invalid contact number (9-10 digits)");
                        return;
                    }
                    student.setName(name);
                    student.setContact(contact);
                    system.updateStudent(id, student);
                    tableModel.fireTableDataChanged();
                    JOptionPane.showMessageDialog(this, "Student updated successfully");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating student: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a student to update");
            }
        });
        buttonPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete Student");
        styleButton(deleteButton);
        deleteButton.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String id = (String) tableModel.getValueAt(row, 0);
                    system.deleteStudent(id);
                    tableModel.fireTableDataChanged();
                    JOptionPane.showMessageDialog(this, "Student deleted successfully");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a student to delete");
            }
        });
        buttonPanel.add(deleteButton);

        JButton allocateSeatsButton = new JButton("Allocate Seats");
        styleButton(allocateSeatsButton);
        allocateSeatsButton.addActionListener(e -> {
            try {
                SeatAllocator.allocateSeats(system);
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Seats allocated successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error allocating seats: " + ex.getMessage());
            }
        });
        buttonPanel.add(allocateSeatsButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
}

class StudentTableModel extends AbstractTableModel {
    private List<Student> students;
    private String[] columns = {"ID", "Name", "Contact", "Distance", "Merit", "Father", "Economic", "Room"};

    public StudentTableModel(List<Student> students) {
        this.students = students;
    }

    @Override
    public int getRowCount() { return students.size(); }
    @Override
    public int getColumnCount() { return columns.length; }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student student = students.get(rowIndex);
        switch (columnIndex) {
            case 0: return student.getId();
            case 1: return student.getName();
            case 2: return student.getContact();
            case 3: return student.getDistance();
            case 4: return student.getMerit();
            case 5: return student.getFather();
            case 6: return student.getEconomic();
            case 7: return student.getRoomNumber() != null ? student.getRoomNumber() : "None";
            default: return null;
        }
    }
    @Override
    public String getColumnName(int column) { return columns[column]; }
}

class RoomPanel extends JPanel {
    private HallManagementSystem system;
    private JTable roomTable;
    private RoomTableModel tableModel;

    public RoomPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));

        tableModel = new RoomTableModel(system.getRooms());
        roomTable = new JTable(tableModel);
        roomTable.setFont(new Font("Arial", Font.PLAIN, 12));
        roomTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton addButton = new JButton("Add Room");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String roomNumber = JOptionPane.showInputDialog("Enter Room Number (e.g., R103):");
                if (roomNumber == null || roomNumber.trim().isEmpty() || !roomNumber.matches("R\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid room number format (e.g., R103)");
                    return;
                }
                if (system.getRoom(roomNumber) != null) {
                    JOptionPane.showMessageDialog(this, "Room number already exists");
                    return;
                }
                String capacityStr = JOptionPane.showInputDialog("Enter Capacity (1-10):");
                if (capacityStr == null || capacityStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Capacity cannot be empty");
                    return;
                }
                int capacity = Integer.parseInt(capacityStr);
                if (capacity < 1 || capacity > 10) {
                    JOptionPane.showMessageDialog(this, "Capacity must be between 1 and 10");
                    return;
                }
                system.addRoom(new Room(roomNumber, capacity));
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Room added successfully");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for capacity");
            }
        });
        buttonPanel.add(addButton);

        JButton deleteButton = new JButton("Delete Room");
        styleButton(deleteButton);
        deleteButton.addActionListener(e -> {
            int row = roomTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this room?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        String roomNumber = (String) tableModel.getValueAt(row, 0);
                        system.deleteRoom(roomNumber);
                        tableModel.fireTableDataChanged();
                        JOptionPane.showMessageDialog(this, "Room deleted successfully");
                    } catch (IllegalStateException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a room to delete");
            }
        });
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
}

class RoomTableModel extends AbstractTableModel {
    private List<Room> rooms;
    private String[] columns = {"Room Number", "Capacity", "Occupants"};

    public RoomTableModel(List<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public int getRowCount() { return rooms.size(); }
    @Override
    public int getColumnCount() { return columns.length; }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Room room = rooms.get(rowIndex);
        switch (columnIndex) {
            case 0: return room.getRoomNumber();
            case 1: return room.getCapacity();
            case 2: return room.getOccupants().toString();
            default: return null;
        }
    }
    @Override
    public String getColumnName(int column) { return columns[column]; }
}

class ComplaintPanel extends JPanel {
    private HallManagementSystem system;
    private JTable complaintTable;
    private ComplaintTableModel tableModel;

    public ComplaintPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));

        tableModel = new ComplaintTableModel(system.getComplaints());
        complaintTable = new JTable(tableModel);
        complaintTable.setFont(new Font("Arial", Font.PLAIN, 12));
        complaintTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(complaintTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton addButton = new JButton("Submit Complaint");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String studentId = JOptionPane.showInputDialog("Enter Student ID (e.g., S001):");
                if (studentId == null || studentId.trim().isEmpty() || !studentId.matches("S\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid student ID format (e.g., S001)");
                    return;
                }
                if (system.getStudent(studentId) == null) {
                    JOptionPane.showMessageDialog(this, "Student not found");
                    return;
                }
                String description = JOptionPane.showInputDialog("Enter Complaint Description:");
                if (description == null || description.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Description cannot be empty");
                    return;
                }
                String complaintId = "C" + (system.getComplaints().size() + 1);
                system.submitComplaint(new Complaint(complaintId, studentId, description));
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Complaint submitted successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting complaint: " + ex.getMessage());
            }
        });
        buttonPanel.add(addButton);

        JButton resolveButton = new JButton("Resolve Complaint");
        styleButton(resolveButton);
        resolveButton.addActionListener(e -> {
            int row = complaintTable.getSelectedRow();
            if (row >= 0) {
                String complaintId = (String) tableModel.getValueAt(row, 0);
                Complaint complaint = system.getComplaints().stream()
                        .filter(c -> c.getComplaintId().equals(complaintId))
                        .findFirst().orElse(null);
                if (complaint != null) {
                    complaint.setResolved(true);
                    tableModel.fireTableDataChanged();
                    JOptionPane.showMessageDialog(this, "Complaint resolved successfully");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a complaint to resolve");
            }
        });
        buttonPanel.add(resolveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
}

class ComplaintTableModel extends AbstractTableModel {
    private List<Complaint> complaints;
    private String[] columns = {"Complaint ID", "Student ID", "Description", "Resolved"};

    public ComplaintTableModel(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    @Override
    public int getRowCount() { return complaints.size(); }
    @Override
    public int getColumnCount() { return columns.length; }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Complaint complaint = complaints.get(rowIndex);
        switch (columnIndex) {
            case 0: return complaint.getComplaintId();
            case 1: return complaint.getStudentId();
            case 2: return complaint.getDescription();
            case 3: return complaint.isResolved();
            default: return null;
        }
    }
    @Override
    public String getColumnName(int column) { return columns[column]; }
}

class AppointmentPanel extends JPanel {
    private HallManagementSystem system;
    private JTable appointmentTable;
    private AppointmentTableModel tableModel;

    public AppointmentPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 248, 255));

        tableModel = new AppointmentTableModel(system.getAppointments());
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        appointmentTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255));
        JButton addButton = new JButton("Schedule Appointment");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String studentId = JOptionPane.showInputDialog("Enter Student ID (e.g., S001):");
                if (studentId == null || studentId.trim().isEmpty() || !studentId.matches("S\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid student ID format (e.g., S001)");
                    return;
                }
                if (system.getStudent(studentId) == null) {
                    JOptionPane.showMessageDialog(this, "Student not found");
                    return;
                }
                String authority = JOptionPane.showInputDialog("Enter Authority (e.g., Provost):");
                if (authority == null || authority.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Authority cannot be empty");
                    return;
                }
                String date = JOptionPane.showInputDialog("Enter Date (YYYY-MM-DD):");
                if (date == null || date.trim().isEmpty() || !isValidDate(date)) {
                    JOptionPane.showMessageDialog(this, "Invalid date format (YYYY-MM-DD)");
                    return;
                }
                String time = JOptionPane.showInputDialog("Enter Time (HH:MM):");
                if (time == null || time.trim().isEmpty() || !time.matches("\\d{2}:\\d{2}")) {
                    JOptionPane.showMessageDialog(this, "Invalid time format (HH:MM)");
                    return;
                }
                String appointmentId = "A" + (system.getAppointments().size() + 1);
                system.scheduleAppointment(new Appointment(appointmentId, studentId, authority, date, time));
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Appointment scheduled successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error scheduling appointment: " + ex.getMessage());
            }
        });
        buttonPanel.add(addButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
}

class AppointmentTableModel extends AbstractTableModel {
    private List<Appointment> appointments;
    private String[] columns = {"Appointment ID", "Student ID", "Authority", "Date", "Time"};

    public AppointmentTableModel(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    @Override
    public int getRowCount() { return appointments.size(); }
    @Override
    public int getColumnCount() { return columns.length; }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Appointment appointment = appointments.get(rowIndex);
        switch (columnIndex) {
            case 0: return appointment.getAppointmentId();
            case 1: return appointment.getStudentId();
            case 2: return appointment.getAuthority();
            case 3: return appointment.getDate();
            case 4: return appointment.getTime();
            default: return null;
        }
    }
    @Override
    public String getColumnName(int column) { return columns[column]; }
}

// Main Class
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI(new HallManagementSystem()));
    }
}