import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SoundController {
    private static boolean isMuted = false; // Toggle flag
    private static Map<String, Integer> originalVolumes = new HashMap<>(); // Store original volumes for restoration

    public static void triggerHotkey() {
        try {
            if (!isMuted) {
                // Step 1: Get the active window process ID using PowerShell
                String activeProcessId = getActiveWindowPID();
                
                if (activeProcessId == null) {
                    System.out.println("Error: Could not retrieve active window PID.");
                    return;
                }

                // Step 2: Mute all processes except active
                muteAllExcept(activeProcessId);
                
                // Mark as muted
                isMuted = true;
                System.out.println("Muted all except active window process: " + activeProcessId);
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return pid != null ? pid.trim() : null;
    }

    private static void muteAllExcept(String activeProcessId) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("svcl.exe", "/scomma", "");
        Process process = pb.start(); // Start the process to retrieve the list

        // Parse the output to mute all processes except the active window PID
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String currentMicrophone = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Capture")) {
                    currentMicrophone = line.split(",")[0];
                } else if (line.contains(".exe")) {
                    String processId = line.split(",")[0];
                    if (!processId.equals(activeProcessId) && !processId.equals(currentMicrophone)) {
                        // Get and store the original volume before muting
                        int originalVolume = getProcessVolume(processId);
                        originalVolumes.put(processId, originalVolume);
                        // Mute the process
                        new ProcessBuilder("svcl.exe", "/Mute", processId).start();
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
                return Integer.parseInt(volumeStr.trim());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Error parsing volume level for process ID: " + processId);
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
