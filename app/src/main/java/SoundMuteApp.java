
//
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.GlobalScreen;

public class SoundMuteApp {
    public static void GUIwindow() throws NativeHookException {

        // ------------------------------------------------------------------ \\
        // ---------------------------- GUI BODY ---------------------------- \\
        // ------------------------------------------------------------------ \\

        JFrame frame = new JFrame("Sound Mute App"); // Create new window with title "Sound Mute App"
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set default operation on close to exit the application
        frame.setSize(450, 150); // Setting standart window sise
        frame.setMinimumSize(new Dimension(450, 120)); // Set minimum size
        frame.setVisible(true); // Set visibility
        // Center the window on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2 - 100;
        frame.setLocation(x, y);

        // buttons: hotkey capture, trigger hotkey with delay, move program to tray,
        // clear hotkey

        // Create a panel with a BorderLayout
        JPanel borderPanel = new JPanel(new BorderLayout());

        // ------------------------------------------------------------------ \\
        // ---------------------------- FILE WORK --------------------------- \\
        // ------------------------------------------------------------------ \\

        // Read the current hotkey from the file
        String currentHotkey = readFile("hotkey.inf");

        // If file is not found, create a new file and set default hotkey
        if (currentHotkey.isBlank()) {
            currentHotkey = "Ctrl + Back Slash";
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
        // ----------------------- BUTTON INTERACTIONS ---------------------- \\
        // ------------------------------------------------------------------ \\

        // Create buttons
        JButton CaptureBTN = new JButton("Hotkey capture");
        CaptureBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Capture button action
                Logger.log("Capture button clicked!");
                Logger.log("!-------------------!");

                // record new hotkey and write it in file hotkey.inf
                // update label, hotkey should be key1 + key2

                // start timer to record hotkey
                Timer timer = new Timer(2400, e1 -> {
                    // stop native hook
                    try {
                        GlobalScreen.unregisterNativeHook();
                    } catch (NativeHookException ex) {
                        Logger.log("Error unregistering native hook: " + ex);
                    }
                });
                timer.setRepeats(false);
                timer.start();

                // start native hook to record hotkey
                try {
                    GlobalScreen.registerNativeHook();
                } catch (NativeHookException ex) {
                    Logger.log("Error registering native hook: " + ex);
                }
                GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                    private String hotkey = "";
                    public int key1 = 0;
                    public int key2 = 0;

                    @Override
                    public void nativeKeyPressed(NativeKeyEvent e) {
                        if (key1 == 0) {
                            key1 = e.getKeyCode();
                        } else if (key2 == 0) {
                            key2 = e.getKeyCode();
                            hotkey = NativeKeyEvent.getKeyText(key1) + " + "
                                    + NativeKeyEvent.getKeyText(key2);
                            writeFile("hotkey.inf", hotkey);
                            currentHotkeyLabel.setText("Current Hotkey: " + hotkey);
                            try {
                                GlobalScreen.unregisterNativeHook();
                                System.out.println("Unregistered native hook, please restart program");
                            } catch (NativeHookException ex) {
                                Logger.log("Error unregistering native hook: " + ex);
                            }
                        }
                    }

                    @Override
                    public void nativeKeyReleased(NativeKeyEvent e) {
                        // ignore
                    }

                    @Override
                    public void nativeKeyTyped(NativeKeyEvent e) {
                        // ignore
                    }
                });
            }
        });

        // Delay trigger button logic
        JButton DelayHtkBTN = new JButton("Trigger hotkey with 5s delay");
        DelayHtkBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Delay button action
                Logger.log(" ^ with Delay button clicked!");
                Logger.log("!-------------------!");
            }
        });

        // Move to tray button logic
        JButton MoveToTrayBTN = new JButton("Move program to tray");
        MoveToTrayBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Move to Tray button action
                Logger.log("Move to Tray button clicked!");
                Logger.log("!-------------------!");
            }
        });

        // Clear button logic
        JButton ClearHtkBTN = new JButton("Clear hotkey");
        ClearHtkBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code for Clear button action
                File file = new File("hotkey.inf");
                if (file.exists()) {
                    if (file.delete()) {
                        Logger.log("Hotkey file deleted!");
                        currentHotkeyLabel.setText("Current Hotkey: None");
                        Logger.log("!-------------------!");
                    } else {
                        Logger.log("Failed to delete the file");
                        Logger.log("!-------------------!");
                    }
                }
            }
        });

        // Add a listener to the "Trigger hotkey with 5s delay" button
        DelayHtkBTN.addActionListener(e -> {
            // Code for Delay button action
            Logger.log("Delay button triggered!");
            Logger.log("!-------------------!");
            // Trigger the hotkey with a 5-second delay
            Timer timer = new Timer(1000, new ActionListener() {
                int seconds = 5;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (seconds > 0) {
                        seconds--;
                        SwingUtilities
                                .invokeLater(() -> DelayHtkBTN.setText("Waiting " + seconds + "s"));
                    } else {
                        triggerHotkey();
                        SwingUtilities.invokeLater(() -> DelayHtkBTN.setText("Trigger hotkey with 5s delay"));
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            timer.setRepeats(true);
            timer.start();
            SwingUtilities.invokeLater(() -> DelayHtkBTN.setText("Waiting 5s"));
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
        Image image = Toolkit.getDefaultToolkit().getImage("SUABY.png");
        TrayIcon trayIcon = new TrayIcon(image, "Sound Mute App");

        // Create a popup menu for the tray icon
        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(e -> {
            frame.setVisible(true);
            frame.toFront();
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            // Exit the program
            Logger.log("! Closing program with tray popup option !");
            System.exit(0);
        });
        popup.add(showItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        // Add a listener to handle left-double-click and right-single-click events
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    frame.setVisible(true);
                    frame.toFront();
                } else if (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
                    frame.setVisible(false);
                } else if (e.getClickCount() == 1 && SwingUtilities.isMiddleMouseButton(e)) {
                    // Log
                    Logger.log("Delay button triggered with mouse wheel button from tray!");
                    Logger.log("!-------------------!");

                    // Trigger the hotkey with a 5-second delay
                    Timer timer = new Timer(1000, new ActionListener() {
                        int seconds = 5;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (seconds > 0) {
                                seconds--;
                                SwingUtilities
                                        .invokeLater(() -> DelayHtkBTN.setText("Waiting " + seconds + "s"));
                            } else {
                                triggerHotkey();
                                SwingUtilities.invokeLater(() -> DelayHtkBTN.setText("Trigger hotkey with 5s delay"));
                                ((Timer) e.getSource()).stop();
                            }
                        }
                    });
                    timer.setRepeats(true);
                    timer.start();
                    SwingUtilities.invokeLater(() -> DelayHtkBTN.setText("Waiting 5s"));
                }
            }
        });

        // Add the tray icon to the system tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Logger.log("TrayIcon could not be added.");
            Logger.log("!-------------------!");
        }

        // Add a listener to the "Move program to tray" button
        MoveToTrayBTN.addActionListener(e -> {
            frame.setVisible(false);
            trayIcon.displayMessage("Sound Mute App", "The program has been moved to the system tray.",
                    TrayIcon.MessageType.INFO);
        });

    }

    // Method to trigger the hotkey
    public static void triggerHotkey() {
        // Code to trigger the hotkey
        Logger.log("Hotkey triggered!");
        Logger.log("!-------------------!");

        // Step 1: Get Active Window PID
        int activePID = getActiveWindowPID();

        if (activePID == -1) {
            System.out.println("Failed to get active window PID.");
            return;
        }

        // Step 2: List audio sessions and find the session matching the active PID
        String audioSessionsJson = listAudioSessions();
        if (audioSessionsJson == null) {
            System.out.println("Failed to list audio sessions.");
            return;
        }

        // Parse JSON and find app name for the matching PID (pseudo-code)
        String appName = findAppNameByPID(audioSessionsJson, activePID);
        if (appName == null) {
            System.out.println("No audio session found for active window PID.");
            return;
        }

        // Step 3: Toggle mute for the application
        toggleMuteApplication(appName);

    }

    public static String findAppNameByPID(String json, int pid) {
        // // Parse JSON and search for entry with matching PID, then return app name
        // JsonElement jelement = new JsonParser().parse(json);
        // JsonObject jObject = jelement.getAsJsonObject();
        // JsonArray jArray = jObject.get("playback").getAsJsonArray();

        // for (JsonElement e : jArray) {
        // JsonObject app = e.getAsJsonObject();
        // if (app.get("pid").getAsInt() == pid) {
        // return app.get("name").getAsString();
        // }
        // }
        return null;
    }

    // Method to read the contents of a file
    public static String readFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                Logger.log("File not found! Created new hotkey.inf with defalut of c'Ctrl + Back Slash'");
                Logger.log("!-------------------!");
                return "";
            }
            Scanner scanner = new Scanner(file);
            if (scanner.hasNextLine()) {
                String contents = scanner.nextLine();
                Logger.log("!File found, reading line!");
                Logger.log("!-------------!");

                scanner.close();
                return contents;
            } else {
                Logger.log("!------WARN-------!");
                Logger.log("!!!FOUND FILE, BUT IT SEEMS EMPTY!!!");
                Logger.log("Overwriten hotkey.inf with defalut of 'Ctrl + Back Slash' ");
                Logger.log("!-------------!");
                scanner.close();
                return "";
            }
        } catch (FileNotFoundException e) {
            Logger.log("Error: " + e.getMessage());
            Logger.log("!-------------------!");
        } catch (NullPointerException e) {
            Logger.log("Error: " + e.getMessage());
            Logger.log("!-------------------!");
        }
        return "";
    }

    // write file method
    public static void writeFile(String filename, String contents) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.close();
        } catch (IOException e) {
            Logger.log("Error writing to file!");
            Logger.log("!-------------------!");
        }
    }

    // ------------------------------------------------- \
    // --------------- MUTE TOGGLE ZONE ---------------- \
    // ------------------------------------------------- \

    // Getting current active window
    public static int getActiveWindowPID() {
        try {
            String command = "powershell (Get-Process -Id (Get-Process | " +
                    "Where-Object { .MainWindowHandle -eq (Get-Process -Id " +
                    "[System.Diagnostics.Process]::GetCurrentProcess().Id).MainWindowHandle }).Id).Id";

            Process process = new ProcessBuilder("cmd.exe", "/c", command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String pidString = reader.readLine();

            return (pidString != null) ? Integer.parseInt(pidString.trim()) : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    // Method to list all audio sessions
    public static String listAudioSessions() {
        try {
            String svclPath = "svcl.exe";
            String command = svclPath + " /sjson output.json"; // Exports data in JSON format

            Process process = new ProcessBuilder("cmd.exe", "/c", command).start();
            process.waitFor();

            return new String(Files.readAllBytes(Paths.get("output.json")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to mute/unmute an application
    public static void toggleMuteApplication(String appName) {
        try {
            String svclPath = "svcl.exe";
            String command = svclPath + " /MuteToggle \"" + appName + "\"";

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main body
    public static void main(String[] args) {
        try {
            GUIwindow();
            HotKeyListener.main(null);
        } catch (NativeHookException e) {
            Logger.log("Error installing native hook: " + e.getMessage());
        }
    }
}
