import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.*;
import javax.swing.JOptionPane;

public class UserForm extends Frame {
    private Label firstNameLabel, lastNameLabel, dobLabel, emailLabel, phoneLabel;
    private TextField firstNameField, lastNameField, dobField, emailField, phoneField;
    private TextArea outputArea;
    private Button submitButton;

    // Database connection parameters
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/user_form_data";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private Connection connection;

    public UserForm() {
        setTitle("User Form");
        setSize(400, 400);
        setBackground(Color.LIGHT_GRAY);
        setLayout(null); // Using absolute positioning for simplicity
        
        // Connect to the MySQL database
        try {
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Failed to connect to the database.");
            return; // Return if connection fails
        }
        
        // Initialize UI components
        initComponents();
        
        setVisible(true);
    }

    private void initComponents() {
        // First Name
        firstNameLabel = new Label("First Name*:");
        firstNameLabel.setBounds(50, 50, 100, 20);
        add(firstNameLabel);
        firstNameField = new TextField();
        firstNameField.setBounds(160, 50, 200, 20);
        add(firstNameField);

        // Last Name
        lastNameLabel = new Label("Last Name*:");
        lastNameLabel.setBounds(50, 80, 100, 20);
        add(lastNameLabel);
        lastNameField = new TextField();
        lastNameField.setBounds(160, 80, 200, 20);
        add(lastNameField);

        // Date of Birth
        dobLabel = new Label("Date of Birth (MM/DD/YYYY):");
        dobLabel.setBounds(50, 110, 150, 20);
        add(dobLabel);
        dobField = new TextField();
        dobField.setBounds(210, 110, 150, 20);
        add(dobField);

        // Email
        emailLabel = new Label("Email*:");
        emailLabel.setBounds(50, 140, 100, 20);
        add(emailLabel);
        emailField = new TextField();
        emailField.setBounds(160, 140, 200, 20);
        add(emailField);

        // Phone
        phoneLabel = new Label("Phone*:");
        phoneLabel.setBounds(50, 170, 100, 20);
        add(phoneLabel);
        phoneField = new TextField();
        phoneField.setBounds(160, 170, 200, 20);
        add(phoneField);

        // Submit Button
        submitButton = new Button("Submit");
        submitButton.setBounds(160, 200, 100, 30);
        submitButton.setBackground(Color.RED);
        add(submitButton);
        
        // Set cursor to hand pointer on hover
        submitButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });
        
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validateForm()) {
                    // Process form submission
                    String firstName = firstNameField.getText();
                    String lastName = lastNameField.getText();
                    String dob = dobField.getText();
                    String email = emailField.getText();
                    String phone = phoneField.getText();

                    // Format the date of birth
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        // Parse the input date string into Date object using the input format
                        java.util.Date date = inputFormat.parse(dob);
                        // Format the Date object into the desired output format
                        dob = outputFormat.format(date);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                        showMessage("Failed to parse date.");
                        return; // Exit method if date parsing fails
                    }

                    // Insert data into the database
                    try {
                        PreparedStatement statement = connection.prepareStatement("INSERT INTO users (first_name, last_name, dob, email, phone) VALUES (?, ?, ?, ?, ?)");
                        statement.setString(1, firstName);
                        statement.setString(2, lastName);
                        statement.setString(3, dob);
                        statement.setString(4, email);
                        statement.setString(5, phone);
                        int rowsInserted = statement.executeUpdate();
                        if (rowsInserted > 0) {
                            showMessage("User data inserted successfully.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showMessage("Failed to insert user data into the database.");
                    }

                    // Display output in TextArea
                    outputArea.append("First Name: " + firstName + "\n");
                    outputArea.append("Last Name: " + lastName + "\n");
                    outputArea.append("Date of Birth: " + dob + "\n");
                    outputArea.append("Email: " + email + "\n");
                    outputArea.append("Phone: " + phone + "\n\n");

                    // Clear fields after submission
                    clearFields();
                }
            }
        });

        // Output TextArea
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setBounds(50, 240, 310, 100);
        add(outputArea);

        // Window Listener
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private boolean validateForm() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showMessage("Please fill in all mandatory fields.");
            return false;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address.");
            return false;
        }

        if (!isValidPhoneNumber(phone)) {
            showMessage("Please enter a valid phone number.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidPhoneNumber(String phone) {
        // Simple validation assuming a phone number consists of digits only and has a length between 7 and 15
        return phone.matches("\\d{7,15}");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        dobField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }

    public static void main(String[] args) {
        new UserForm();
    }
}
