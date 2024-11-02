
//
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SoundMuteApp {

    public static void GUIwindow() { // ---------------------------- GUI BODY ---------------------------- \\

        JFrame frame = new JFrame("Sound Mute App"); // Create new window with title "Sound Mute App"
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set default operation on close to exit the application
        frame.setSize(450, 200); // Setting standart window sise
        frame.setMinimumSize(new Dimension(450, 200)); // Set minimum size
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

        // Create a label to display the current hotkey
        final JLabel currentHotkeyLabel = new JLabel("Current Hotkey: " + currentHotkey);
        borderPanel.add(currentHotkeyLabel, BorderLayout.NORTH);

        // Create a panel with a GridLayout
        JPanel gridPanel = new JPanel(new GridLayout(2, 2));

        // Create buttons
        JButton CaptureBTN = new JButton("Hotkey capture");
        CaptureBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Capture button action
                System.out.println("Capture button clicked!");
            }
        });

        JButton DelayHtkBTN = new JButton("Trigger hotkey with 5s delay");
        DelayHtkBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Delay button action
                System.out.println("Delay button clicked!");
            }
        });

        JButton MoveToTrayBTN = new JButton("Move program to tray");
        MoveToTrayBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Move to Tray button action
                System.out.println("Move to Tray button clicked!");
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
                    } else {
                        System.out.println("Failed to delete the file");
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
        }
    }

    public static void main(String[] args) {
        GUIwindow();
    }
}
