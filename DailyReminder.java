import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class DailyReminder {

    private static final String FILE_NAME = "reminders.txt";
    private static List<String> reminders = new ArrayList<>();
    private static int currentIndex = 0;
    private static String allReminders;
    private static boolean isUserInputActive = false;
    private static JFrame frame;
    private static JTextArea textArea;
    private static String intervalTextField;
    private static long delay = 0;
    private static Timer timer = new Timer();
    private static Integer counter = 0;

    public static void main(String[] args) {
        loadRemindersFromFile();
        setupGUI();
              
    }

    private static void setupGUI() {
        frame = new JFrame("Meeldetuletaja");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton addButton = new JButton("Lisa meeldetuletus");
        JButton deleteButton = new JButton("Kustuta meeldetuletus");

        // Add a label and text field for setting the time interval
        JButton intervalButton = new JButton("Intervall");

        JButton startButton = new JButton("Alusta");

        panel.add(startButton);
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(intervalButton);
        
        frame.add(panel, BorderLayout.SOUTH);
        
    
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String reminder = JOptionPane.showInputDialog("Sisesta uus meeldetuletus:");
                if (reminder != null && !reminder.trim().isEmpty()) {
                    textArea.setText("");
                    reminders.add(reminder);
                    textArea.append("\n" + "Meeldetuletus lisatus" + "\n") ;
                    for (int i = 0; i < reminders.size(); i++) {
                        String remindor = reminders.get(i);
                        int orderNumber = i + 1; // Adding 1 to the index to make it 1-based
                        textArea.append(orderNumber + ". " + remindor + "\n"); // Display order number and reminder
                        
                    }
                    saveRemindersToFile();
                }
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                timer.scheduleAtFixedRate(new ReminderTask(), 0, 10 * 60 * 1000);
                textArea.append("Meeldetuletaja käib intervalliga: " + 10 * 60 * 1000 + " ms" + "\n");
            }
        });

        intervalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                intervalTextField = JOptionPane.showInputDialog("Sisesta korduse intervall minutites:");
                if (intervalTextField != null && !intervalTextField.isEmpty() && intervalTextField.matches("[0-9]+")) {
                    long timeInterval = Long.parseLong(intervalTextField) * 60 * 1000;
                    timer.cancel();
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new ReminderTask(), timeInterval, timeInterval);
                    textArea.append("Intervall: " + intervalTextField + " min" + "\n");
                    
                } else {
                    JOptionPane.showMessageDialog(frame, "Sisesta norm väärtus");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String number = JOptionPane.showInputDialog("Sisesta kustutatava meeldetuletuse number:");
                try {
                    int indexToDelete = Integer.parseInt(number) - 1;
                    if (indexToDelete >= 0 && indexToDelete < reminders.size()) {
                        reminders.remove(indexToDelete);
                        textArea.setText(""); // Clear the text area
                        textArea.append("\n" + "Meeldetuletus kustutatud" + "\n") ;
                        for (int i = 0; i < reminders.size(); i++) {
                            String reminder = reminders.get(i);
                            int orderNumber = i + 1; // Adding 1 to the index to make it 1-based
                            textArea.append(orderNumber + ". " + reminder + "\n"); // Display order number and reminder
                            saveRemindersToFile();
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Sellist pole.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Kehtetu sisend.");
                }
            }
        });

        // add a UI component to set the time interval

        frame.setVisible(true);
    }

    static class ReminderTask extends TimerTask {
        @Override
        public void run() {
           
                           
                allReminders = ""; // Initialize to empty
                for (int i = 0; i < reminders.size(); i++) {
                    allReminders += (i + 1) + ". " + reminders.get(i) + "\n";
                }

                int letterCount = allReminders.length(); // Use the actual length of the string
                if (letterCount > 121) {
                    counter++;
                    if (counter < 2) {
                        displayGUINotification(allReminders);
                    }
                    for (int i = 0; i < letterCount; i += 121) {
                        int endIndex = Math.min(i + 121, allReminders.length());
                        String notificationContent = allReminders.substring(i, endIndex);
                        displayNotification(notificationContent);

                        try {
                            Thread.sleep(2000); // Wait for 5 seconds before the next notification
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    displayNotification(allReminders);
                    counter++;
                    if (counter < 2) {
                        displayGUINotification(allReminders);
                    }
                }

                Thread userInputThread = new Thread(() -> {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Enter a new reminder or 'Delete [number]' to remove a reminder:");
                    String input = scanner.nextLine();

                    if (input.toLowerCase().startsWith("delete ")) {
                        try {
                            int indexToDelete = Integer.parseInt(input.split(" ")[1]) - 1;
                            if (indexToDelete >= 0 && indexToDelete < reminders.size()) {
                                reminders.remove(indexToDelete);
                                System.out.println("Reminder deleted successfully.");
                            } else {
                                System.out.println("Invalid reminder number.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input format.");
                        }
                    } else {
                        reminders.add(input);
                        System.out.println("Reminder added successfully.");
                        currentIndex = 0;
                    }
                });
                userInputThread.start();

                try {
                    userInputThread.join(10000); // Wait for 10 seconds for the thread to finish
                } catch (InterruptedException e) {
                    // The thread was interrupted because the user didn't provide input within 10
                    // seconds
                }

                if (userInputThread.isAlive()) {
                    userInputThread.interrupt(); // Interrupt the thread if it's still running
                }

                saveRemindersToFile();
                currentIndex = 0;

            

        }
    }

    public static int countLetters(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c)) {
                count++;
            }
        }
        return count;
    }

    private static void loadRemindersFromFile() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    reminders.add(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading reminders from file: " + e.getMessage());
            }
        }
    }

    private static void saveRemindersToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            for (String reminder : reminders) {
                writer.write(reminder + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving reminders to file: " + e.getMessage());
        }
    }

    private static void displayGUINotification(String message) {
        textArea.append(message + "\n");
    }

    private static void displayNotification(String message) {

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); // You can provide an icon here
            TrayIcon trayIcon = new TrayIcon(image, "DailyReminder");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("DailyReminder App");
            tray.add(trayIcon);
            trayIcon.displayMessage("Reminder", message, MessageType.INFO);
            tray.remove(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
