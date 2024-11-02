
//
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;

public class SoundMuteApp {

    public static void GUIwindow() {

        // ------------------------------------------------------------------ \\
        // ---------------------------- GUI BODY ---------------------------- \\
        // ------------------------------------------------------------------ \\

        JFrame frame = new JFrame("Sound Mute App"); // Create new window with title "Sound Mute App"
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set default operation on close to exit the application
        frame.setSize(450, 150); // Setting standart window sise
        frame.setMinimumSize(new Dimension(450, 120)); // Set minimum size
        frame.setVisible(true); // Set visibility

        // buttons: hotkey capture, trigger hotkey with delay, move program to tray,
        // clear hotkey

        // Create a panel with a BorderLayout
        JPanel borderPanel = new JPanel(new BorderLayout());

        // Read the current hotkey from the file
        String currentHotkey = readFile("hotkey.inf");

        // If file is not found, create a new file and set default hotkey
        if (currentHotkey.isEmpty()) {
            currentHotkey = "Right Ctrl + Left Ctrl";
            writeFile("hotkey.inf", currentHotkey);
        }

        // Create a panel with a GridLayout
        JPanel gridPanel = new JPanel(new GridLayout(2, 2));

        // Create a panel with a FlowLayout to center the label
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Create a label to display the current hotkey
        final JLabel currentHotkeyLabel = new JLabel("Current Hotkey: " + currentHotkey);
        labelPanel.add(currentHotkeyLabel);

        // Add the label panel to the north of the border panel
        borderPanel.add(labelPanel, BorderLayout.NORTH);

        // Add the grid panel to the center of the border panel
        borderPanel.add(gridPanel, BorderLayout.CENTER);

        // ------------------------------------------------------------------ \\
        // ----------------------- MOUSE INTERACTIONS ----------------------- \\
        // ------------------------------------------------------------------ \\

        // Create buttons
        JButton CaptureBTN = new JButton("Hotkey capture");
        CaptureBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Capture button action
                System.out.println("Capture button clicked!");
                System.out.println("!-------------------!");
            }
        });

        JButton DelayHtkBTN = new JButton("Trigger hotkey with 5s delay");
        DelayHtkBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Delay button action
                System.out.println("Delay button clicked!");
                System.out.println("!-------------------!");
            }
        });

        JButton MoveToTrayBTN = new JButton("Move program to tray");
        MoveToTrayBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Move to Tray button action
                System.out.println("Move to Tray button clicked!");
                System.out.println("!-------------------!");
            }
        });

        JButton ClearHtkBTN = new JButton("Clear hotkey");
        ClearHtkBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Clear button action
                File file = new File("hotkey.inf");
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("Hotkey file deleted!");
                        currentHotkeyLabel.setText("Current Hotkey: None");
                        System.out.println("!-------------------!");
                    } else {
                        System.out.println("Failed to delete the file");
                        System.out.println("!-------------------!");
                    }
                }
            }
        });

        // Add buttons to the grid panel
        gridPanel.add(CaptureBTN);
        gridPanel.add(DelayHtkBTN);
        gridPanel.add(MoveToTrayBTN);
        gridPanel.add(ClearHtkBTN);

        // Add the grid panel to the bottom of the border panel
        borderPanel.add(gridPanel, BorderLayout.SOUTH);

        // Add the border panel to the frame
        frame.add(borderPanel);
        frame.setVisible(true);

        // ------------------------------------------------- \\
        // ------------------- TRAY ZONE ------------------- \\
        // ------------------------------------------------- \\

        // Create a system tray
        SystemTray tray = SystemTray.getSystemTray();

        // Create a tray icon
        Image image = Toolkit.getDefaultToolkit().getImage("SUABY.png"); // Replace with your icon path
        TrayIcon trayIcon = new TrayIcon(image, "Sound Mute App");

        // Create a popup menu for the tray icon
        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(e -> {
            frame.setVisible(true);
            frame.toFront();
        });
        popup.add(showItem);
        trayIcon.setPopupMenu(popup);

        // Add a listener to handle left-double-click and right-single-click events
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    frame.setVisible(true);
                    frame.toFront();
                } else if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                    frame.setVisible(true);
                }
            }
        });

        // Add the tray icon to the system tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            System.out.println("!-------------------!");
        }

        // Add a listener to the "Move program to tray" button
        MoveToTrayBTN.addActionListener(e -> {
            frame.setVisible(false);
            trayIcon.displayMessage("Sound Mute App", "The program has been moved to the system tray.",
                    TrayIcon.MessageType.INFO);
        });

        // Add a listener to the "Trigger hotkey with 5s delay" button
        DelayHtkBTN.addActionListener(e -> {
            // Code for Delay button action
            System.out.println("Delay button clicked!");
            System.out.println("!-------------------!");
            // Trigger the hotkey with a 5-second delay
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            triggerHotkey();
        });

    }

    // Method to trigger the hotkey
    public static void triggerHotkey() {
        // Code to trigger the hotkey
        System.out.println("Hotkey triggered!");
        System.out.println("!-------------------!");
    }

    // Method to read the contents of a file
    public static String readFile(String filename) {
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            String contents = scanner.nextLine();
            scanner.close();
            return contents;
        } catch (FileNotFoundException e) {
            System.out.println("File not found! Created new hotkey.inf with defalut of right 'ctrl + left ctrl'");
            System.out.println("!-------------------!");
            return "";
        }
    }

    // write file method
    public static void writeFile(String filename, String contents) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to file!");
            System.out.println("!-------------------!");
        }
    }

    public static void main(String[] args) {
        GUIwindow();
    }
}
