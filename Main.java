//1.tutor availability
//2.meal menu
//3.blood group

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;

// User Class for Authentication
class User implements Serializable {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}

// Student Class
class Student implements Serializable {
    private String id;
    private String name;
    private String contact;
    private int distance;
    private int merit;
    private int fatherMonthlyIncome;
    private String roomNumber;
    private String department;

    public Student(String id, String name, String contact, int distance, int merit, int fatherMonthlyIncome, String department) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.distance = distance;
        this.merit = merit;
        this.fatherMonthlyIncome = fatherMonthlyIncome;
        this.roomNumber = null;
        this.department = department;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public int getDistance() { return distance; }
    public int getMerit() { return merit; }
    public int getFatherMonthlyIncome() { return fatherMonthlyIncome; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}

// Room Class
class Room implements Serializable {
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
class Complaint implements Serializable {
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
class Appointment implements Serializable {
    private String appointmentId;
    private String studentId;
    private String authority;
    private String date;
    private String time;
    private boolean isApproved;

    public Appointment(String appointmentId, String studentId, String authority, String date, String time) {
        this.appointmentId = appointmentId;
        this.studentId = studentId;
        this.authority = authority;
        this.date = date;
        this.time = time;
        this.isApproved = false;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getStudentId() { return studentId; }
    public String getAuthority() { return authority; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { this.isApproved = approved; }
}

// HallManagementSystem Class
class HallManagementSystem {
    private List<User> users;
    private List<Student> students;
    private List<Room> rooms;
    private List<Complaint> complaints;
    private List<Appointment> appointments;
    private List<Runnable> dashboardUpdaters;

    public HallManagementSystem() {
        users = new ArrayList<>();
        students = new ArrayList<>();
        rooms = new ArrayList<>();
        complaints = new ArrayList<>();
        appointments = new ArrayList<>();
        dashboardUpdaters = new ArrayList<>();
        loadData();
    }

    public User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public void addUser(User user) {
        users.add(user);
        saveData();
    }

    public void addStudent(Student student) {
        students.add(student);
        saveData();
        notifyDashboard();
    }

    public void addDashboardUpdater(Runnable updater) {
        dashboardUpdaters.add(updater);
    }

    private void notifyDashboard() {
        for (Runnable updater : dashboardUpdaters) {
            updater.run();
        }
    }

    public void updateStudent(String id, Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(id)) {
                students.set(i, updatedStudent);
                saveData();
                notifyDashboard();
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
        users.removeIf(u -> u.getUsername().equals(id));
        saveData();
        notifyDashboard();
    }

    public Student getStudent(String id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Student> getStudents() { return students; }

    public void addRoom(Room room) {
        rooms.add(room);
        saveData();
        notifyDashboard();
    }

    public void deleteRoom(String roomNumber) {
        Room room = getRoom(roomNumber);
        if (room != null && !room.getOccupants().isEmpty()) {
            throw new IllegalStateException("Cannot delete room with occupants");
        }
        rooms.removeIf(r -> r.getRoomNumber().equals(roomNumber));
        saveData();
        notifyDashboard();
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
        notifyDashboard();
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
                notifyDashboard();
            }
        }
    }

    public void submitComplaint(Complaint complaint) {
        complaints.add(complaint);
        saveData();
        notifyDashboard();
    }

    public List<Complaint> getComplaints() { return complaints; }

    public List<Complaint> getStudentComplaints(String studentId) {
        return complaints.stream()
                .filter(c -> c.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public void submitAppointmentRequest(Appointment appointment) {
        appointments.add(appointment);
        saveData();
        notifyDashboard();
    }

    public void approveAppointment(String appointmentId) {
        for (Appointment a : appointments) {
            if (a.getAppointmentId().equals(appointmentId)) {
                a.setApproved(true);
                saveData();
                notifyDashboard();
                return;
            }
        }
        throw new IllegalArgumentException("Appointment not found");
    }

    public void rejectAppointment(String appointmentId) {
        appointments.removeIf(a -> a.getAppointmentId().equals(appointmentId));
        saveData();
        notifyDashboard();
    }

    public List<Appointment> getAppointments() { return appointments; }

    public List<Appointment> getStudentAppointments(String studentId) {
        return appointments.stream()
                .filter(a -> a.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    private void saveData() {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
                oos.writeObject(users);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("students.dat"))) {
                oos.writeObject(students);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("rooms.dat"))) {
                oos.writeObject(rooms);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("complaints.dat"))) {
                oos.writeObject(complaints);
            }
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
            File userFile = new File("users.dat");
            if (userFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFile))) {
                    users = (List<User>) ois.readObject();
                }
            } else {
                users.add(new User("admin", "admin123", "admin"));
                users.add(new User("S001", "pass123", "student"));
                users.add(new User("S002", "pass123", "student"));
            }
            File studentFile = new File("students.dat");
            if (studentFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(studentFile))) {
                    students = (List<Student>) ois.readObject();
                }
            } else {
                addStudent(new Student("S001", "John Doe", "123456789", 50, 500, 50000, "CSE"));
                addStudent(new Student("S002", "Jane Smith", "987654321", 30, 300, 30000, "EEE"));
            }
            File roomFile = new File("rooms.dat");
            if (roomFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(roomFile))) {
                    rooms = (List<Room>) ois.readObject();
                }
            } else {
                addRoom(new Room("R101", 2));
                addRoom(new Room("R102", 2));
            }
            File complaintFile = new File("complaints.dat");
            if (complaintFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(complaintFile))) {
                    complaints = (List<Complaint>) ois.readObject();
                }
            }
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

    public void exportData(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Students:");
            for (Student s : students) {
                writer.println(String.format("%s,%s,%s,%d,%d,%d,%s,%s",
                        s.getId(), s.getName(), s.getContact(), s.getDistance(), s.getMerit(),
                        s.getFatherMonthlyIncome(), s.getRoomNumber() != null ? s.getRoomNumber() : "None", s.getDepartment()));
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
                writer.println(String.format("%s,%s,%s,%s,%s,%b",
                        a.getAppointmentId(), a.getStudentId(), a.getAuthority(), a.getDate(), a.getTime(), a.isApproved()));
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
                        .thenComparingInt(Student::getMerit)
                        .thenComparingInt(Student::getFatherMonthlyIncome))
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

// Custom Table Cell Renderer for Modern Table Styling
class ModernTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(new Font("Roboto", Font.PLAIN, 14));
        if (isSelected) {
            c.setBackground(new Color(38, 166, 154)); // Teal
            c.setForeground(Color.WHITE);
        } else {
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 250)); // Alternating rows
            c.setForeground(Color.BLACK);
        }
        ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding
        return c;
    }
}

// Signup Panel
/*class SignupPanel extends JPanel {
    private HallManagementSystem system;
    private Runnable onSignupSuccess;

    public SignupPanel(HallManagementSystem system, Runnable onSignupSuccess) {
        this.system = system;
        this.onSignupSuccess = onSignupSuccess;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setBackground(new Color(245, 247, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("du_logo.png");
            Image scaledImage = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel.setText("DU Logo");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(logoLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel("Student Signup", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(38, 166, 154));
        gbc.gridy = 1;
        cardPanel.add(titleLabel, gbc);

        // Fields
        JLabel idLabel = new JLabel("Student ID (e.g., S003):");
        idLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        cardPanel.add(idLabel, gbc);

        JTextField idField = new JTextField(15);
        idField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(idField, gbc);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        cardPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(nameField, gbc);

        JLabel contactLabel = new JLabel("Contact (9-10 digits):");
        contactLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        cardPanel.add(contactLabel, gbc);

        JTextField contactField = new JTextField(15);
        contactField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(contactField, gbc);

        JLabel distanceLabel = new JLabel("Distance from Dhaka (km):");
        distanceLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 5;
        cardPanel.add(distanceLabel, gbc);

        JTextField distanceField = new JTextField(15);
        distanceField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(distanceField, gbc);

        JLabel meritLabel = new JLabel("Merit Position (1-8000):");
        meritLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 6;
        cardPanel.add(meritLabel, gbc);

        JTextField meritField = new JTextField(15);
        meritField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(meritField, gbc);

        JLabel incomeLabel = new JLabel("Father's Monthly Income (BDT):");
        incomeLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 7;
        cardPanel.add(incomeLabel, gbc);

        JTextField incomeField = new JTextField(15);
        incomeField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(incomeField, gbc);

        JLabel departmentLabel = new JLabel("Department:");
        departmentLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 8;
        cardPanel.add(departmentLabel, gbc);

        JTextField departmentField = new JTextField(15);
        departmentField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(departmentField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 9;
        cardPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(passwordField, gbc);

        JButton signupButton = new JButton("Signup");
        signupButton.setFont(new Font("Roboto", Font.BOLD, 16));
        signupButton.setBackground(new Color(38, 166, 154));
        signupButton.setForeground(Color.WHITE);
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        signupButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signupButton.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signupButton.setBackground(new Color(38, 166, 154));
            }
        });
        signupButton.addActionListener(e -> {
            try {
                String id = idField.getText();
                if (id == null || id.trim().isEmpty() || !id.matches("S\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid ID format (e.g., S003)");
                    return;
                }
                if (system.getStudent(id) != null) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists");
                    return;
                }
                String name = nameField.getText();
                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty");
                    return;
                }
                String contact = contactField.getText();
                if (contact == null || contact.trim().isEmpty() || !contact.matches("\\d{9,10}")) {
                    JOptionPane.showMessageDialog(this, "Invalid contact number (9-10 digits)");
                    return;
                }
                String distanceStr = distanceField.getText();
                if (distanceStr == null || distanceStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be empty");
                    return;
                }
                int distance = Integer.parseInt(distanceStr);
                if (distance < 0) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be negative");
                    return;
                }
                String meritStr = meritField.getText();
                if (meritStr == null || meritStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Merit position cannot be empty");
                    return;
                }
                int merit = Integer.parseInt(meritStr);
                if (merit < 1 || merit > 8000) {
                    JOptionPane.showMessageDialog(this, "Merit position must be between 1 and 8000");
                    return;
                }
                String incomeStr = incomeField.getText();
                if (incomeStr == null || incomeStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Income cannot be empty");
                    return;
                }
                int income = Integer.parseInt(incomeStr);
                if (income < 0) {
                    JOptionPane.showMessageDialog(this, "Income cannot be negative");
                    return;
                }
                String department = departmentField.getText();
                if (department == null || department.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Department cannot be empty");
                    return;
                }
                String password = new String(passwordField.getPassword());
                if (password == null || password.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password cannot be empty");
                    return;
                }
                system.addStudent(new Student(id, name, contact, distance, merit, income, department));
                system.addUser(new User(id, password, "student"));
                JOptionPane.showMessageDialog(this, "Signup successful! Please login with ID: " + id);
                onSignupSuccess.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for distance, merit, or income");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(signupButton, gbc);

        JButton loginButton = new JButton("Back to Login");
        loginButton.setFont(new Font("Roboto", Font.BOLD, 16));
        loginButton.setBackground(new Color(100, 100, 100));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(120, 120, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(100, 100, 100));
            }
        });
        loginButton.addActionListener(e -> onSignupSuccess.run());
        gbc.gridy = 11;
        cardPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardPanel, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(38, 166, 154), 0, getHeight(), Color.WHITE);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
*/
// Signup Panel
/*class SignupPanel extends JPanel {
    private HallManagementSystem system;
    private Runnable onSignupSuccess;

    public SignupPanel(HallManagementSystem system, Runnable onSignupSuccess) {
        this.system = system;
        this.onSignupSuccess = onSignupSuccess;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setBackground(new Color(245, 247, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("du_logo.png");
            Image scaledImage = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel.setText("DU Logo");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(logoLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel("Student Signup", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(38, 166, 154));
        gbc.gridy = 1;
        cardPanel.add(titleLabel, gbc);

        // Fields
        JLabel idLabel = new JLabel("Student ID (e.g., S003):");
        idLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        cardPanel.add(idLabel, gbc);

        JTextField idField = new JTextField(20); // Increased width to 20 columns
        idField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(idField, gbc);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        cardPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20); // Increased width to 20 columns
        nameField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(nameField, gbc);

        JLabel contactLabel = new JLabel("Contact (9-10 digits):");
        contactLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        cardPanel.add(contactLabel, gbc);

        JTextField contactField = new JTextField(20); // Increased width to 20 columns
        contactField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(contactField, gbc);

        JLabel distanceLabel = new JLabel("Distance from Dhaka (km):");
        distanceLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 5;
        cardPanel.add(distanceLabel, gbc);

        JTextField distanceField = new JTextField(20); // Increased width to 20 columns
        distanceField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(distanceField, gbc);

        JLabel meritLabel = new JLabel("Merit Position (1-8000):");
        meritLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 6;
        cardPanel.add(meritLabel, gbc);

        JTextField meritField = new JTextField(20); // Increased width to 20 columns
        meritField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(meritField, gbc);

        JLabel incomeLabel = new JLabel("Father's Monthly Income (BDT):");
        incomeLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 7;
        cardPanel.add(incomeLabel, gbc);

        JTextField incomeField = new JTextField(20); // Increased width to 20 columns
        incomeField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(incomeField, gbc);

        JLabel departmentLabel = new JLabel("Department:");
        departmentLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 8;
        cardPanel.add(departmentLabel, gbc);

        JTextField departmentField = new JTextField(20); // Increased width to 20 columns
        departmentField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(departmentField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 9;
        cardPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20); // Increased width to 20 columns
        passwordField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(passwordField, gbc);

        JButton signupButton = new JButton("Signup");
        signupButton.setFont(new Font("Roboto", Font.BOLD, 16));
        signupButton.setBackground(new Color(38, 166, 154));
        signupButton.setForeground(Color.WHITE);
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        signupButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signupButton.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signupButton.setBackground(new Color(38, 166, 154));
            }
        });
        signupButton.addActionListener(e -> {
            try {
                String id = idField.getText();
                if (id == null || id.trim().isEmpty() || !id.matches("S\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid ID format (e.g., S003)");
                    return;
                }
                if (system.getStudent(id) != null) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists");
                    return;
                }
                String name = nameField.getText();
                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty");
                    return;
                }
                String contact = contactField.getText();
                if (contact == null || contact.trim().isEmpty() || !contact.matches("\\d{9,10}")) {
                    JOptionPane.showMessageDialog(this, "Invalid contact number (9-10 digits)");
                    return;
                }
                String distanceStr = distanceField.getText();
                if (distanceStr == null || distanceStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be empty");
                    return;
                }
                int distance = Integer.parseInt(distanceStr);
                if (distance < 0) {
                    JOptionPane.showMessageDialog(this, "Distance cannot be negative");
                    return;
                }
                String meritStr = meritField.getText();
                if (meritStr == null || meritStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Merit position cannot be empty");
                    return;
                }
                int merit = Integer.parseInt(meritStr);
                if (merit < 1 || merit > 8000) {
                    JOptionPane.showMessageDialog(this, "Merit position must be between 1 and 8000");
                    return;
                }
                String incomeStr = incomeField.getText();
                if (incomeStr == null || incomeStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Income cannot be empty");
                    return;
                }
                int income = Integer.parseInt(incomeStr);
                if (income < 0) {
                    JOptionPane.showMessageDialog(this, "Income cannot be negative");
                    return;
                }
                String department = departmentField.getText();
                if (department == null || department.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Department cannot be empty");
                    return;
                }
                String password = new String(passwordField.getPassword());
                if (password == null || password.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password cannot be empty");
                    return;
                }
                system.addStudent(new Student(id, name, contact, distance, merit, income, department));
                system.addUser(new User(id, password, "student"));
                JOptionPane.showMessageDialog(this, "Signup successful! Please login with ID: " + id);
                onSignupSuccess.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for distance, merit, or income");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(signupButton, gbc);

        JButton loginButton = new JButton("Back to Login");
        loginButton.setFont(new Font("Roboto", Font.BOLD, 16));
        loginButton.setBackground(new Color(100, 100, 100));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(120, 120, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(100, 100, 100));
            }
        });
        loginButton.addActionListener(e -> onSignupSuccess.run());
        gbc.gridy = 11;
        cardPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardPanel, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(38, 166, 154), 0, getHeight(), Color.WHITE);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}*/
// Signup Panel
class SignupPanel extends JPanel {
    private HallManagementSystem system;
    private Runnable onSignupSuccess;

    public SignupPanel(HallManagementSystem system, Runnable onSignupSuccess) {
        this.system = system;
        this.onSignupSuccess = onSignupSuccess;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setBackground(new Color(245, 247, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("du_logo.png");
            Image scaledImage = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel.setText("DU Logo");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(logoLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel("Student Signup", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(38, 166, 154));
        gbc.gridy = 1;
        cardPanel.add(titleLabel, gbc);

        // Fields
        JLabel idLabel = new JLabel("Student ID (e.g., S003):");
        idLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        cardPanel.add(idLabel, gbc);

        JTextField idField = new JTextField(20);
        idField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(idField, gbc);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        cardPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(nameField, gbc);

        JLabel contactLabel = new JLabel("Contact (9-10 digits):");
        contactLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        cardPanel.add(contactLabel, gbc);

        JTextField contactField = new JTextField(20);
        contactField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(contactField, gbc);

        JLabel districtLabel = new JLabel("District:");
        districtLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 5;
        cardPanel.add(districtLabel, gbc);

        // District Dropdown with approximate distances from Dhaka University
        String[] districts = {
                "Dhaka", "Faridpur", "Gazipur", "Gopalganj", "Kishoreganj", "Madaripur", "Manikganj", "Munshiganj",
                "Narayanganj", "Narsingdi", "Rajbari", "Shariatpur", "Tangail", "Bagerhat", "Chuadanga", "Jessore",
                "Jhenaidah", "Khulna", "Kushtia", "Magura", "Meherpur", "Narail", "Satkhira", "Bogra", "Joypurhat",
                "Naogaon", "Natore", "Nawabganj", "Pabna", "Rajshahi", "Sirajganj", "Dinajpur", "Gaibandha", "Kurigram",
                "Lalmonirhat", "Nilphamari", "Panchagarh", "Rangpur", "Thakurgaon", "Barguna", "Barisal", "Bhola",
                "Jhalokati", "Patuakhali", "Pirojpur", "Bandarban", "Brahmanbaria", "Chandpur", "Chittagong", "Comilla",
                "Cox's Bazar", "Feni", "Khagrachhari", "Lakshmipur", "Noakhali", "Rangamati", "Habiganj", "Maulvibazar",
                "Sunamganj", "Sylhet"
        };
        int[] distances = {
                0, 120, 50, 90, 145, 150, 70, 40, 20, 60, 110, 130, 100, 300, 220, 180, 200, 250, 190, 210, 230, 240, 270,
                180, 250, 300, 200, 220, 150, 230, 140, 350, 280, 320, 290, 400, 310, 260, 380, 200, 150, 280, 240, 220,
                190, 400, 120, 180, 250, 200, 300, 150, 320, 260, 280, 350, 180, 240, 300, 280
        };
        JComboBox<String> districtCombo = new JComboBox<>(districts);
        districtCombo.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(districtCombo, gbc);

        JLabel meritLabel = new JLabel("Merit Position (1-8000):");
        meritLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 6;
        cardPanel.add(meritLabel, gbc);

        JTextField meritField = new JTextField(20);
        meritField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(meritField, gbc);

        JLabel incomeLabel = new JLabel("Father's Monthly Income (BDT):");
        incomeLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 7;
        cardPanel.add(incomeLabel, gbc);

        JTextField incomeField = new JTextField(20);
        incomeField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(incomeField, gbc);

        // Faculty Dropdown
        JLabel facultyLabel = new JLabel("Faculty:");
        facultyLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 8;
        cardPanel.add(facultyLabel, gbc);

        String[] faculties = {
                "Faculty of Arts",
                "Faculty of Social Sciences",
                "Faculty of Law",
                "Faculty of Fine Arts",
                "Faculty of Business Studies",
                "Faculty of Science",
                "Faculty of Pharmacy",
                "Faculty of Biological Sciences",
                "Faculty of Earth and Environmental Sciences",
                "Faculty of Engineering and Technology",
                "Faculty of Medicine",
                "Faculty of Postgraduate Medical Sciences and Research",
                "Institutes"
        };
        JComboBox<String> facultyCombo = new JComboBox<>(faculties);
        facultyCombo.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(facultyCombo, gbc);

        // Department Dropdown (initially empty, populated dynamically)
        JLabel departmentLabel = new JLabel("Department:");
        departmentLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 9;
        cardPanel.add(departmentLabel, gbc);

        JComboBox<String> departmentCombo = new JComboBox<>();
        departmentCombo.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(departmentCombo, gbc);

        // Populate department dropdown based on faculty selection
        facultyCombo.addActionListener(e -> {
            departmentCombo.removeAllItems();
            String selectedFaculty = (String) facultyCombo.getSelectedItem();
            switch (selectedFaculty) {
                case "Faculty of Arts":
                    addDepartments(departmentCombo, new String[]{"Bangla", "English", "Arabic", "Persian Language and Literature", "Urdu", "Sanskrit", "Pali", "Buddhist Studies", "Linguistics", "Philosophy", "History", "Islamic Studies", "Islamic History and Culture", "Information Science and Library Management", "World Religions and Culture", "Theatre and Performance studies", "Music", "Dance"});
                    break;
                case "Faculty of Social Sciences":
                    addDepartments(departmentCombo, new String[]{"Economics", "Political Science", "International Relations", "Anthropology", "Public Administration", "Mass Communication and Journalism", "Communication Disorders", "Printing and Publication Studies", "Television Film and Photography", "Sociology", "Development Studies", "Criminology", "Japanese Studies", "Women and Gender Studies", "Peace and Conflict Studies"});
                    break;
                case "Faculty of Law":
                    addDepartments(departmentCombo, new String[]{"Law"});
                    break;
                case "Faculty of Fine Arts":
                    addDepartments(departmentCombo, new String[]{"Ceramics", "Craft", "Drawing and Painting", "Graphic Design", "Oriental Art", "Printmaking", "Sculpture", "History of Art"});
                    break;
                case "Faculty of Business Studies":
                    addDepartments(departmentCombo, new String[]{"Accounting & Information Systems", "Management", "Marketing", "Finance", "Banking and Insurance", "Management Information Systems", "International Business", "Tourism and Hospitality Management", "Organization Strategy and Leadership"});
                    break;
                case "Faculty of Science":
                    addDepartments(departmentCombo, new String[]{"Mathematics", "Applied Mathematics", "Physics", "Chemistry", "Statistics", "Biomedical Physics and Biomedical technology", "Theoretical Physics", "Computational Chemistry"});
                    break;
                case "Faculty of Pharmacy":
                    addDepartments(departmentCombo, new String[]{"Pharmacy", "Clinical Pharmacy and Pharmacology", "Pharmaceutical Chemistry", "Pharmaceutical Technology"});
                    break;
                case "Faculty of Biological Sciences":
                    addDepartments(departmentCombo, new String[]{"Botany", "Zoology", "Biochemistry and Molecular Biology", "Microbiology", "Psychology", "Medical psychology", "Educational psychology", "Genetic Engineering and Biotechnology", "Soil, Water and Environment", "Fisheries"});
                    break;
                case "Faculty of Earth and Environmental Sciences":
                    addDepartments(departmentCombo, new String[]{"Geography and Environment", "Geology", "Oceanography", "Disaster Science and Climate Resilience", "Meteorology"});
                    break;
                case "Faculty of Engineering and Technology":
                    addDepartments(departmentCombo, new String[]{"Electrical and Electronic Engineering", "Applied Chemistry and Chemical Engineering", "Computer Science & Engineering", "Nuclear Engineering", "Robotics and Mechatronics Engineering"});
                    break;
                case "Faculty of Medicine":
                    addDepartments(departmentCombo, new String[]{"Medicine and Surgery"});
                    break;
                case "Faculty of Postgraduate Medical Sciences and Research":
                    addDepartments(departmentCombo, new String[]{"Virology"});
                    break;
                case "Institutes":
                    addDepartments(departmentCombo, new String[]{"Education and Research", "Statistical Research and Training", "Business Administration", "Social Welfare and Research", "Modern Languages", "Information Technology", "Energy", "Disaster Management and Vulnerability Studies", "Nutrition and Food Science", "Health Economics", "Leather Engineering and Technology"});
                    break;
            }
        });

        // Trigger initial population
        facultyCombo.setSelectedIndex(0);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 10;
        cardPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(passwordField, gbc);

        JButton signupButton = new JButton("Signup");
        signupButton.setFont(new Font("Roboto", Font.BOLD, 16));
        signupButton.setBackground(new Color(38, 166, 154));
        signupButton.setForeground(Color.WHITE);
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        signupButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signupButton.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signupButton.setBackground(new Color(38, 166, 154));
            }
        });
        signupButton.addActionListener(e -> {
            try {
                String id = idField.getText();
                if (id == null || id.trim().isEmpty() || !id.matches("S\\d+")) {
                    JOptionPane.showMessageDialog(this, "Invalid ID format (e.g., S003)");
                    return;
                }
                if (system.getStudent(id) != null) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists");
                    return;
                }
                String name = nameField.getText();
                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty");
                    return;
                }
                String contact = contactField.getText();
                if (contact == null || contact.trim().isEmpty() || !contact.matches("\\d{9,10}")) {
                    JOptionPane.showMessageDialog(this, "Invalid contact number (9-10 digits)");
                    return;
                }
                String district = (String) districtCombo.getSelectedItem();
                int distance = distances[districtCombo.getSelectedIndex()];
                String meritStr = meritField.getText();
                if (meritStr == null || meritStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Merit position cannot be empty");
                    return;
                }
                int merit = Integer.parseInt(meritStr);
                if (merit < 1 || merit > 8000) {
                    JOptionPane.showMessageDialog(this, "Merit position must be between 1 and 8000");
                    return;
                }
                String incomeStr = incomeField.getText();
                if (incomeStr == null || incomeStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Income cannot be empty");
                    return;
                }
                int income = Integer.parseInt(incomeStr);
                if (income < 0) {
                    JOptionPane.showMessageDialog(this, "Income cannot be negative");
                    return;
                }
                String department = (String) departmentCombo.getSelectedItem();
                if (department == null || department.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select a department");
                    return;
                }
                String password = new String(passwordField.getPassword());
                if (password == null || password.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password cannot be empty");
                    return;
                }
                system.addStudent(new Student(id, name, contact, distance, merit, income, department));
                system.addUser(new User(id, password, "student"));
                JOptionPane.showMessageDialog(this, "Signup successful! Please login with ID: " + id);
                onSignupSuccess.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for merit or income");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(signupButton, gbc);

        JButton loginButton = new JButton("Back to Login");
        loginButton.setFont(new Font("Roboto", Font.BOLD, 16));
        loginButton.setBackground(new Color(100, 100, 100));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(120, 120, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(100, 100, 100));
            }
        });
        loginButton.addActionListener(e -> onSignupSuccess.run());
        gbc.gridy = 12;
        cardPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardPanel, gbc);
    }

    private void addDepartments(JComboBox<String> comboBox, String[] departments) {
        for (String dept : departments) {
            comboBox.addItem(dept);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(38, 166, 154), 0, getHeight(), Color.WHITE);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
// Login Panel
class LoginPanel extends JPanel {
    private HallManagementSystem system;
    private Runnable onLoginSuccess;
    private Runnable onSignup;

    public LoginPanel(HallManagementSystem system, Runnable onLoginSuccess, Runnable onSignup) {
        this.system = system;
        this.onLoginSuccess = onLoginSuccess;
        this.onSignup = onSignup;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setBackground(new Color(245, 247, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("du_logo.png");
            Image scaledImage = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel.setText("DU Logo");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(logoLabel, gbc);

        JLabel titleLabel = new JLabel("Hall Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(38, 166, 154));
        gbc.gridy = 1;
        cardPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        cardPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        usernameField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        cardPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Roboto", Font.PLAIN, 14));
        gbc.gridx = 1;
        cardPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Roboto", Font.BOLD, 16));
        loginButton.setBackground(new Color(38, 166, 154));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(38, 166, 154));
            }
        });
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            User user = system.authenticate(username, password);
            if (user != null) {
                MainGUI.currentUser = user;
                onLoginSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(loginButton, gbc);

        JButton signupButton = new JButton("Signup as Student");
        signupButton.setFont(new Font("Roboto", Font.BOLD, 16));
        signupButton.setBackground(new Color(100, 100, 100));
        signupButton.setForeground(Color.WHITE);
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        signupButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signupButton.setBackground(new Color(120, 120, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signupButton.setBackground(new Color(100, 100, 100));
            }
        });
        signupButton.addActionListener(e -> onSignup.run());
        gbc.gridy = 5;
        cardPanel.add(signupButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardPanel, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(38, 166, 154), 0, getHeight(), Color.WHITE);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
// Admin Dashboard Panel
class AdminDashboardPanel extends JPanel {
    private HallManagementSystem system;
    private JLabel totalStudentsLabel;
    private JLabel occupiedRoomsLabel;
    private JLabel totalRoomsLabel;
    private JLabel complaintsLabel;
    private JLabel appointmentsLabel;

    public AdminDashboardPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("du_logo.png");
            Image scaledImage = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel.setText("DU Logo");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(logoLabel, gbc);

        JLabel titleLabel = new JLabel("Dhaka University Hall Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(38, 166, 154));
        gbc.gridy = 1;
        add(titleLabel, gbc);

        JPanel studentsCard = createMetricCard("Total Students", system.getStudents().size());
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(studentsCard, gbc);

        JPanel occupiedRoomsCard = createMetricCard("Occupied Rooms",
                system.getRooms().stream().filter(r -> !r.getOccupants().isEmpty()).count());
        gbc.gridx = 1;
        add(occupiedRoomsCard, gbc);

        JPanel totalRoomsCard = createMetricCard("Total Rooms", system.getRooms().size());
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(totalRoomsCard, gbc);

        JPanel complaintsCard = createMetricCard("Open Complaints",
                system.getComplaints().stream().filter(c -> !c.isResolved()).count());
        gbc.gridx = 1;
        add(complaintsCard, gbc);

        JPanel appointmentsCard = createMetricCard("Pending Appointments",
                system.getAppointments().stream().filter(a -> !a.isApproved()).count());
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(appointmentsCard, gbc);

        JButton exportButton = new JButton("Export Data");
        exportButton.setFont(new Font("Roboto", Font.BOLD, 16));
        exportButton.setBackground(new Color(38, 166, 154));
        exportButton.setForeground(Color.WHITE);
        exportButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        exportButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                exportButton.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exportButton.setBackground(new Color(38, 166, 154));
            }
        });
        exportButton.addActionListener(e -> {
            system.exportData("hall_management_export.txt");
            JOptionPane.showMessageDialog(this, "Data exported to hall_management_export.txt");
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(exportButton, gbc);

        totalStudentsLabel = (JLabel) studentsCard.getComponent(1);
        occupiedRoomsLabel = (JLabel) occupiedRoomsCard.getComponent(1);
        totalRoomsLabel = (JLabel) totalRoomsCard.getComponent(1);
        complaintsLabel = (JLabel) complaintsCard.getComponent(1);
        appointmentsLabel = (JLabel) appointmentsCard.getComponent(1);
    }

    private JPanel createMetricCard(String title, long value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        titleLabel.setForeground(new Color(38, 166, 154));
        card.add(titleLabel);

        JLabel valueLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valueLabel.setFont(new Font("Roboto", Font.PLAIN, 24));
        card.add(valueLabel);

        return card;
    }

    public void updateDashboard() {
        totalStudentsLabel.setText(String.valueOf(system.getStudents().size()));
        occupiedRoomsLabel.setText(String.valueOf(
                system.getRooms().stream().filter(r -> !r.getOccupants().isEmpty()).count()));
        totalRoomsLabel.setText(String.valueOf(system.getRooms().size()));
        complaintsLabel.setText(String.valueOf(
                system.getComplaints().stream().filter(c -> !c.isResolved()).count()));
        appointmentsLabel.setText(String.valueOf(
                system.getAppointments().stream().filter(a -> !a.isApproved()).count()));
    }
}

// Student Dashboard Panel
class StudentDashboardPanel extends JPanel {
    private HallManagementSystem system;
    private String studentId;

    public StudentDashboardPanel(HallManagementSystem system, String studentId) {
        this.system = system;
        this.studentId = studentId;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon("du_logo.png");
            Image scaledImage = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            logoLabel.setText("DU Logo");
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(logoLabel, gbc);

        JLabel titleLabel = new JLabel("Student Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(38, 166, 154));
        gbc.gridy = 1;
        add(titleLabel, gbc);

        Student student = system.getStudent(studentId);
        JPanel nameCard = createMetricCard("Name", student != null ? student.getName() : "Unknown");
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(nameCard, gbc);

        JPanel roomCard = createMetricCard("Room",
                student != null && student.getRoomNumber() != null ? student.getRoomNumber() : "Not Assigned");
        gbc.gridx = 1;
        add(roomCard, gbc);

        JPanel complaintsCard = createMetricCard("Complaints Submitted",
                system.getStudentComplaints(studentId).size());
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(complaintsCard, gbc);

        JPanel appointmentsCard = createMetricCard("Approved Appointments",
                system.getStudentAppointments(studentId).stream().filter(Appointment::isApproved).count());
        gbc.gridx = 1;
        add(appointmentsCard, gbc);

        JPanel seatRequestCard = createMetricCard("Seat Status",
                student != null && student.getRoomNumber() != null ? "Assigned" : "Pending/Request Seat");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(seatRequestCard, gbc);

        if (student != null && student.getRoomNumber() == null) {
            JButton requestSeatButton = new JButton("Request Seat");
            styleButton(requestSeatButton);
            requestSeatButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, "Seat request submitted. Admin will allocate a seat.");
            });
            gbc.gridx = 1;
            gbc.gridy = 4;
            add(requestSeatButton, gbc);
        }
    }

    private JPanel createMetricCard(String title, Object value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        titleLabel.setForeground(new Color(38, 166, 154));
        card.add(titleLabel);

        JLabel valueLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        valueLabel.setFont(new Font("Roboto", Font.PLAIN, 24));
        card.add(valueLabel);

        return card;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Student Profile Panel
class StudentProfilePanel extends JPanel {
    private HallManagementSystem system;
    private String studentId;

    public StudentProfilePanel(HallManagementSystem system, String studentId) {
        this.system = system;
        this.studentId = studentId;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Student student = system.getStudent(studentId);
        if (student != null) {
            JLabel idLabel = new JLabel("ID: " + student.getId());
            idLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(idLabel);

            JLabel nameLabel = new JLabel("Name: " + student.getName());
            nameLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(nameLabel);

            JLabel contactLabel = new JLabel("Contact: " + student.getContact());
            contactLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(contactLabel);

            JLabel distanceLabel = new JLabel("Distance: " + student.getDistance() + " km");
            distanceLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(distanceLabel);

            JLabel meritLabel = new JLabel("Merit Position: " + student.getMerit());
            meritLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(meritLabel);

            JLabel incomeLabel = new JLabel("Father's Income: " + student.getFatherMonthlyIncome() + " BDT");
            incomeLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(incomeLabel);

            JLabel departmentLabel = new JLabel("Department: " + student.getDepartment());
            departmentLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(departmentLabel);

            JLabel roomLabel = new JLabel("Room: " + (student.getRoomNumber() != null ? student.getRoomNumber() : "None"));
            roomLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(roomLabel);
        } else {
            JLabel errorLabel = new JLabel("Student not found");
            errorLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
            add(errorLabel);
        }
    }
}

// Student Complaint Panel
class StudentComplaintPanel extends JPanel {
    private HallManagementSystem system;
    private String studentId;
    private JTable complaintTable;
    private ComplaintTableModel tableModel;

    public StudentComplaintPanel(HallManagementSystem system, String studentId) {
        this.system = system;
        this.studentId = studentId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new ComplaintTableModel(system.getStudentComplaints(studentId));
        complaintTable = new JTable(tableModel);
        complaintTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        complaintTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        complaintTable.getTableHeader().setBackground(new Color(38, 166, 154));
        complaintTable.getTableHeader().setForeground(Color.WHITE);
        complaintTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        add(new JScrollPane(complaintTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = new JButton("Submit Complaint");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
                String description = JOptionPane.showInputDialog("Enter Complaint Description:");
                if (description == null || description.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Description cannot be empty");
                    return;
                }
                String complaintId = "C" + (system.getComplaints().size() + 1);
                system.submitComplaint(new Complaint(complaintId, studentId, description));
                tableModel.setComplaints(system.getStudentComplaints(studentId));
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Complaint submitted successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting complaint: " + ex.getMessage());
            }
        });
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Student Appointment Panel
class StudentAppointmentPanel extends JPanel {
    private HallManagementSystem system;
    private String studentId;
    private JTable appointmentTable;
    private AppointmentTableModel tableModel;

    public StudentAppointmentPanel(HallManagementSystem system, String studentId) {
        this.system = system;
        this.studentId = studentId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new AppointmentTableModel(system.getStudentAppointments(studentId));
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        appointmentTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        appointmentTable.getTableHeader().setBackground(new Color(38, 166, 154));
        appointmentTable.getTableHeader().setForeground(Color.WHITE);
        appointmentTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = new JButton("Request Appointment");
        styleButton(addButton);
        addButton.addActionListener(e -> {
            try {
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
                system.submitAppointmentRequest(new Appointment(appointmentId, studentId, authority, date, time));
                tableModel.setAppointments(system.getStudentAppointments(studentId));
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Appointment request submitted successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error submitting appointment request: " + ex.getMessage());
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
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Student Panel (Admin)
class StudentPanel extends JPanel {
    private HallManagementSystem system;
    private JTable studentTable;
    private StudentTableModel tableModel;

    public StudentPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new StudentTableModel(system.getStudents());
        studentTable = new JTable(tableModel);
        studentTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        studentTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        studentTable.getTableHeader().setBackground(new Color(38, 166, 154));
        studentTable.getTableHeader().setForeground(Color.WHITE);
        studentTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

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
                    String meritStr = JOptionPane.showInputDialog("Enter Merit Position (1-8000):", student.getMerit());
                    if (meritStr == null || meritStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Merit position cannot be empty");
                        return;
                    }
                    int merit = Integer.parseInt(meritStr);
                    if (merit < 1 || merit > 8000) {
                        JOptionPane.showMessageDialog(this, "Merit position must be between 1 and 8000");
                        return;
                    }
                    String department = JOptionPane.showInputDialog("Enter Department:", student.getDepartment());
                    if (department == null || department.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Department cannot be empty");
                        return;
                    }
                    Student updatedStudent = new Student(id, name, contact, student.getDistance(), merit, student.getFatherMonthlyIncome(), department);
                    updatedStudent.setRoomNumber(student.getRoomNumber());
                    system.updateStudent(id, updatedStudent);
                    tableModel.fireTableDataChanged();
                    JOptionPane.showMessageDialog(this, "Student updated successfully");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number format for merit");
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
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Student Table Model
class StudentTableModel extends AbstractTableModel {
    private List<Student> students;
    private String[] columns = {"ID", "Name", "Contact", "Distance", "Merit", "Father's Income", "Room", "Department"};

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
            case 5: return student.getFatherMonthlyIncome();
            case 6: return student.getRoomNumber() != null ? student.getRoomNumber() : "None";
            case 7: return student.getDepartment();
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) { return columns[column]; }
}

// Room Panel
class RoomPanel extends JPanel {
    private HallManagementSystem system;
    private JTable roomTable;
    private RoomTableModel tableModel;

    public RoomPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new RoomTableModel(system.getRooms());
        roomTable = new JTable(tableModel);
        roomTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        roomTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        roomTable.getTableHeader().setBackground(new Color(38, 166, 154));
        roomTable.getTableHeader().setForeground(Color.WHITE);
        roomTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
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
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Room Table Model
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

// Admin Complaint Panel
class AdminComplaintPanel extends JPanel {
    private HallManagementSystem system;
    private JTable complaintTable;
    private ComplaintTableModel tableModel;

    public AdminComplaintPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new ComplaintTableModel(system.getComplaints());
        complaintTable = new JTable(tableModel);
        complaintTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        complaintTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        complaintTable.getTableHeader().setBackground(new Color(38, 166, 154));
        complaintTable.getTableHeader().setForeground(Color.WHITE);
        complaintTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        add(new JScrollPane(complaintTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton resolveButton = new JButton("Resolve Complaint");
        styleButton(resolveButton);
        resolveButton.addActionListener(e -> {
            int row = complaintTable.getSelectedRow();
            if (row >= 0) {
                String complaintId = (String) tableModel.getValueAt(row, 0);
                for (Complaint c : system.getComplaints()) {
                    if (c.getComplaintId().equals(complaintId)) {
                        c.setResolved(true);
                        tableModel.fireTableDataChanged();
                        JOptionPane.showMessageDialog(this, "Complaint resolved successfully");
                        return;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a complaint to resolve");
            }
        });
        buttonPanel.add(resolveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Complaint Table Model
class ComplaintTableModel extends AbstractTableModel {
    private List<Complaint> complaints;
    private String[] columns = {"Complaint ID", "Student ID", "Description", "Resolved"};

    public ComplaintTableModel(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    public void setComplaints(List<Complaint> complaints) {
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

// Admin Appointment Panel
class AdminAppointmentPanel extends JPanel {
    private HallManagementSystem system;
    private JTable appointmentTable;
    private AppointmentTableModel tableModel;

    public AdminAppointmentPanel(HallManagementSystem system) {
        this.system = system;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tableModel = new AppointmentTableModel(system.getAppointments());
        appointmentTable = new JTable(tableModel);
        appointmentTable.setFont(new Font("Roboto", Font.PLAIN, 14));
        appointmentTable.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 14));
        appointmentTable.getTableHeader().setBackground(new Color(38, 166, 154));
        appointmentTable.getTableHeader().setForeground(Color.WHITE);
        appointmentTable.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton approveButton = new JButton("Approve Appointment");
        styleButton(approveButton);
        approveButton.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String appointmentId = (String) tableModel.getValueAt(row, 0);
                try {
                    system.approveAppointment(appointmentId);
                    tableModel.fireTableDataChanged();
                    JOptionPane.showMessageDialog(this, "Appointment approved successfully");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Appointment not found");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select an appointment to approve");
            }
        });
        buttonPanel.add(approveButton);

        JButton rejectButton = new JButton("Reject Appointment");
        styleButton(rejectButton);
        rejectButton.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String appointmentId = (String) tableModel.getValueAt(row, 0);
                system.rejectAppointment(appointmentId);
                tableModel.fireTableDataChanged();
                JOptionPane.showMessageDialog(this, "Appointment rejected successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Select an appointment to reject");
            }
        });
        buttonPanel.add(rejectButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setBackground(new Color(38, 166, 154));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(55, 200, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(38, 166, 154));
            }
        });
    }
}

// Appointment Table Model
class AppointmentTableModel extends AbstractTableModel {
    private List<Appointment> appointments;
    private String[] columns = {"Appointment ID", "Student ID", "Authority", "Date", "Time", "Approved"};

    public AppointmentTableModel(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
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
            case 5: return appointment.isApproved();
            default: return null;
        }
    }

    @Override
    public String getColumnName(int column) { return columns[column]; }
}

// Main GUI
class MainGUI extends JFrame {
    public static User currentUser;
    private HallManagementSystem system;
    private JPanel mainPanel;

    public MainGUI(HallManagementSystem system) {
        this.system = system;
        setTitle("Hall Management System - Dhaka University");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        mainPanel = new JPanel(new BorderLayout());
        showLoginPanel();
        add(mainPanel);
        setVisible(true);
    }

    private void showLoginPanel() {
        mainPanel.removeAll();
        JPanel loginPanel = new LoginPanel(system, this::loadInterface, this::showSignupPanel);
        mainPanel.add(loginPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showSignupPanel() {
        mainPanel.removeAll();
        JPanel signupPanel = new SignupPanel(system, this::showLoginPanel);
        mainPanel.add(signupPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void loadInterface() {
        mainPanel.removeAll();
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Roboto", Font.PLAIN, 16));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(new Color(38, 166, 154));

        if (currentUser.getRole().equals("admin")) {
            AdminDashboardPanel dashboardPanel = new AdminDashboardPanel(system);
            tabbedPane.addTab("Dashboard", dashboardPanel);
            tabbedPane.addTab("Students", new StudentPanel(system));
            tabbedPane.addTab("Rooms", new RoomPanel(system));
            tabbedPane.addTab("Complaints", new AdminComplaintPanel(system));
            tabbedPane.addTab("Appointments", new AdminAppointmentPanel(system));
            system.addDashboardUpdater(dashboardPanel::updateDashboard);
        } else {
            String studentId = currentUser.getUsername();
            tabbedPane.addTab("Dashboard", new StudentDashboardPanel(system, studentId));
            tabbedPane.addTab("Submit Complaint", new StudentComplaintPanel(system, studentId));
            tabbedPane.addTab("Request Appointment", new StudentAppointmentPanel(system, studentId));
            tabbedPane.addTab("Profile", new StudentProfilePanel(system, studentId));
        }

        tabbedPane.setSelectedIndex(0);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Roboto", Font.BOLD, 16));
        logoutButton.setBackground(new Color(100, 100, 100));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(new Color(120, 120, 120));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(new Color(100, 100, 100));
            }
        });
        logoutButton.addActionListener(e -> {
            currentUser = null;
            showLoginPanel();
        });
        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.add(logoutButton);
        contentPanel.add(logoutPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HallManagementSystem system = new HallManagementSystem();
            new MainGUI(system);
        });
    }
}