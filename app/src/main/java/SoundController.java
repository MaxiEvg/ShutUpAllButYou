import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoundController {
    private static boolean isMuted = false; // Toggle flag
    private static Map<String, Integer> originalVolumes = new ConcurrentHashMap<>(); // Store original volumes for
                                                                                     // restoration

    public static void triggerHotkey() {
        try {
            if (!isMuted) {
                // Step 1: Get the active window process ID using PowerShell
                String activeProcessId = CurrentIDtoPName();

                if (activeProcessId == null) {
                    System.out.println("Error: Could not retrieve active window PID.");
                    return;
                }

                // Step 2: Mute all processes except active
                muteAllExcept(activeProcessId);

                // Mark as muted
                isMuted = true;
                System.out.println("Tried to mute all except active window process");
            } else {
                // Step 3: Restore all original volumes
                restoreVolumes();

                // Reset mute flag
                isMuted = false;
                System.out.println("Restored original volumes for all applications.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while toggling mute.");
        }
    }

    private static String getActiveWindowPID() {
        String pid = null;
        try {
            String command = "powershell -command \"(Get-Process | Where-Object { $_.MainWindowHandle -eq " +
                    "(Add-Type '[DllImport(\\\"user32.dll\\\")]public static extern IntPtr GetForegroundWindow();' " +
                    "-Name 'WinAPI' -Namespace 'WinAPI' -PassThru)::GetForegroundWindow() }).Id\"";

            ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-command", command);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                pid = reader.readLine();
                if (pid != null) {
                    System.out.println("Found PID: " + pid.trim());
                }
            }

            process.waitFor();
            if (process.exitValue() != 0) {
                System.out.println("Error: PowerShell command execution failed.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.out.println("Thread was interrupted, failed to complete operation");
        }
        Logger.log(pid != null ? pid.trim() : null + " - return of getActiveWindowPID");
        return pid != null ? pid.trim() : null;
    }

    private static String CurrentIDtoPName() throws IOException {
        String currid = getActiveWindowPID();

        if (currid == null) {
            System.out.println("Error: Could not retrieve active window PID.");
            return "err acurrid";
        }

        String command = "powershell -command \"(Get-Process | Where-Object { $_.Id -eq " + currid + " }).Name\"";

        ProcessBuilder processBuilder = new ProcessBuilder("powershell", "-command", command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            reader.close();
            process.waitFor();

            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            return "err process";
        }
    }

    private static void muteAllExcept(String activeProcessId) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("svcl.exe", "/scomma", "");
        Process process = pb.start(); // Start the process to retrieve the list

        // extract the process list and write it to a file
        System.out.println("Muting all applications except the active application...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            String processList = sb.toString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("sound_instances_to_a_comma_list.txt"))) {
                writer.write(processList);
            }
        }

        if (process.exitValue() != 0) {
            System.out.println("Error: Failed to retrieve sound instances.");
            return;
        }

        // Parse the output to mute all processes except the active window PID
        try (BufferedReader reader = new BufferedReader(new FileReader("sound_instances_to_a_comma_list.txt"))) {
            String currentMicrophone = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("BM800")) {
                    currentMicrophone = line.split(",")[0];
                    System.out.println("Found current microphone: " + currentMicrophone);
                } else if (line.contains(".exe")) {
                    String[] columns = line.split(",");
                    if (columns.length > 20) {
                        String processId = columns[20];
                        // Mute all processes except the active window PID and the current microphone
                        if (!processId.equals(activeProcessId)) {
                            System.out.println("Muting process ID: " + processId);
                            // Get and store the original volume before muting
                            int originalVolume = getProcessVolume(processId);
                            originalVolumes.put(processId, originalVolume);
                            // Mute the process
                            new ProcessBuilder("svcl.exe", "/Mute", processId).start();
                        } else {
                            System.out.println("Unmuting current app's process ID: " + processId);
                            // Unmute the current app's process ID
                            new ProcessBuilder("svcl.exe", "/Unmute", processId).start();

                        }
                    }
                }
            }
        }
    }

    private static int getProcessVolume(String processId) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("svcl.exe", "/GetVolume", processId);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String volumeStr = reader.readLine();
            if (volumeStr != null) {
                int volume = Integer.parseInt(volumeStr.trim());
                System.out.println("Retrieved volume level for process ID: " + processId + " = " + volume);
                return volume;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Error parsing volume level for process ID: " + processId);
            try (PrintWriter out = new PrintWriter(new FileWriter("error.log", true))) {
                out.println("Error parsing volume level for process ID: " + processId);
                out.flush();
            }
        }
        return 100; // Default volume if retrieval fails
    }

    private static void restoreVolumes() throws IOException {
        // Restore each process volume to its original state
        for (Map.Entry<String, Integer> entry : originalVolumes.entrySet()) {
            String processId = entry.getKey();
            int originalVolume = entry.getValue();
            // Restore the volume level to the original
            new ProcessBuilder("svcl.exe", "/SetVolume", processId, String.valueOf(originalVolume)).start();
            new ProcessBuilder("svcl.exe", "/Unmute", processId).start();
        }
        originalVolumes.clear(); // Clear stored volumes for the next mute cycle
    }
}
