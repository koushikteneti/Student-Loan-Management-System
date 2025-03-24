package studentLoanManagement;

import java.sql.*;
import java.util.Scanner;

public class StudentLoanManagementSystem {

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/student_loan_management";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Koushik@2002";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Student Loan Management System!");
        System.out.println("1. Student Login");
        System.out.println("2. Admin Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                studentLogin(scanner);
                break;
            case 2:
                adminLogin(scanner);
                break;
            case 3:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid choice!");
        }

        scanner.close();
    }

    // Student login and functionalities
    private static void studentLogin(Scanner scanner) {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM students WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("password_hash");
                if (PasswordHasher.verifyPassword(password, storedPasswordHash)) {
                    System.out.println("Login successful!");
                    int studentId = resultSet.getInt("student_id");
                    studentMenu(scanner, studentId);
                } else {
                    System.out.println("Invalid password!");
                }
            } else {
                System.out.println("Student not found!");
            }
        } catch (SQLException e) {
            System.err.println("Error during student login.");
            e.printStackTrace();
        }
    }

    // Student menu
    private static void studentMenu(Scanner scanner, int studentId) {
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. Apply for a Loan");
            System.out.println("2. View Loan Status");
            System.out.println("3. Make a Repayment");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    applyForLoan(scanner, studentId);
                    break;
                case 2:
                    viewLoanStatus(studentId);
                    break;
                case 3:
                    makeRepayment(scanner, studentId);
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Apply for a loan
    private static void applyForLoan(Scanner scanner, int studentId) {
        System.out.print("Enter loan amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "INSERT INTO loans (student_id, amount) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            preparedStatement.setDouble(2, amount);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Loan application submitted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error applying for loan.");
            e.printStackTrace();
        }
    }

    // View loan status
    private static void viewLoanStatus(int studentId) {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM loans WHERE student_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println("\nLoan ID: " + resultSet.getInt("loan_id"));
                System.out.println("Amount: $" + resultSet.getDouble("amount"));
                System.out.println("Status: " + resultSet.getString("status"));
                System.out.println("Applied At: " + resultSet.getTimestamp("applied_at"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching loan status.");
            e.printStackTrace();
        }
    }

    // Make a repayment
    private static void makeRepayment(Scanner scanner, int studentId) {
        System.out.print("Enter loan ID: ");
        int loanId = scanner.nextInt();
        System.out.print("Enter repayment amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "INSERT INTO repayments (loan_id, amount) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, loanId);
            preparedStatement.setDouble(2, amount);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Repayment successful!");
            }
        } catch (SQLException e) {
            System.err.println("Error making repayment.");
            e.printStackTrace();
        }
    }

    // Admin login and functionalities
    private static void adminLogin(Scanner scanner) {
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();

        // Hardcoded admin credentials for simplicity
        if (username.equals("admin") && password.equals("admin123")) {
            System.out.println("Admin login successful!");
            adminMenu(scanner);
        } else {
            System.out.println("Invalid admin credentials!");
        }
    }

    // Admin menu
    private static void adminMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Approve/Reject Loans");
            System.out.println("2. View All Loans");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    approveRejectLoans(scanner);
                    break;
                case 2:
                    viewAllLoans();
                    break;
                case 3:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Approve or reject loans
    private static void approveRejectLoans(Scanner scanner) {
        System.out.print("Enter loan ID: ");
        int loanId = scanner.nextInt();
        System.out.print("Enter status (APPROVED/REJECTED): ");
        String status = scanner.next().toUpperCase();
        scanner.nextLine(); // Consume newline

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "UPDATE loans SET status = ? WHERE loan_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, loanId);

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Loan status updated successfully!");
            } else {
                System.out.println("Loan not found!");
            }
        } catch (SQLException e) {
            System.err.println("Error updating loan status.");
            e.printStackTrace();
        }
    }

    // View all loans
    private static void viewAllLoans() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM loans";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                System.out.println("\nLoan ID: " + resultSet.getInt("loan_id"));
                System.out.println("Student ID: " + resultSet.getInt("student_id"));
                System.out.println("Amount: $" + resultSet.getDouble("amount"));
                System.out.println("Status: " + resultSet.getString("status"));
                System.out.println("Applied At: " + resultSet.getTimestamp("applied_at"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all loans.");
            e.printStackTrace();
        }
    }
}

// Utility class for password hashing
class PasswordHasher {
    public static String hashPassword(String password) {
        // Use a secure hashing algorithm like BCrypt or Argon2 in a real application
        return Integer.toString(password.hashCode()); // Simple hash for demonstration
    }

    public static boolean verifyPassword(String password, String storedHash) {
        return hashPassword(password).equals(storedHash);
    }
}