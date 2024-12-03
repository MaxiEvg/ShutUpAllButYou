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

    @SuppressWarnings("null")
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
            if (process.waitFor() != 0) {
                System.out.println("Error: PowerShell command execution failed.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.out.println("Thread was interrupted, failed to complete operation");
        }
        System.out.println("Return of getActiveWindowPID:" + pid != null ? pid.trim() : null);
        return pid != null ? pid.trim() : null;

    }

    private static String CurrentIDtoPName() throws IOException {
        String currid = getActiveWindowPID();

        if (currid == null) {
            System.out.println("Error: Could not retrieve active window PID.");
            return "err acurrid";
        }

        String command = "Get-Process -Id " + currid + " | Select-Object -ExpandProperty Path";

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

            Logger.log("Converted PID:" + currid + " to name: " + output);
            return output.toString();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            return "err process";
        }
    }

    private static void muteAllExcept(String activeProcessId) throws IOException {
        if (activeProcessId == null || activeProcessId.isEmpty()) {
            throw new IllegalArgumentException("activeProcessId cannot be null or empty");
        }

        ProcessBuilder pb = new ProcessBuilder("svcl.exe", "/scomma", "");
        Process process = pb.start();

        // Extract the process list and write it to a file
        System.out.println("Muting all applications except the active application...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder strbl = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                strbl.append(line).append("\n");
            }

            String processList = strbl.toString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("sound_instances_to_a_comma_list.txt"))) {
                writer.write(processList);
            }
        }

        if (process.exitValue() != 0) {
            System.out.println("Error: Failed to retrieve sound instances.");
            return;
        }

        // Real work here
        String currentMicrophone = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("sound_instances_to_a_comma_list.txt"))) {
            String line;
            while (((line = reader.readLine()) != null) && !(line.contains("Name, Type, Direction"))) {
                if (line.contains(
                        "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\MMDevices\\Audio\\Capture\\")) {
                    try {
                        currentMicrophone = line.split(",")[0];
                        System.out.println("Found current microphone: " + currentMicrophone);
                        new ProcessBuilder("svcl.exe", "/Unmute", currentMicrophone).start();

                        if (currentMicrophone == null) {
                            System.out.println("Couldn't find your mic");
                        }

                    } catch (Exception e) {
                        System.out.println("Something wrong just happened getting microphone" + e.getMessage());
                    }
                }
                if (line.contains(".exe")) {
                    String[] columns = line.split(",");
                    if (columns.length >= 1 && !columns[20].equals(currentMicrophone)
                            && !columns[20].equals(activeProcessId)) {
                        String processId = columns[20];
                        // Get and store the original volume before muting
                        Integer originalVolume;
                        originalVolume = getProcessVolume(processId);

                        originalVolumes.put(processId, originalVolume);

                        if (line.contains("steam") || line.contains("Discord") || line.contains("telegram")) {
                            System.out.println(
                                    "Got Steam or Discord or Telegram process, unmuting mic just in case. PID: "
                                            + processId);
                            new ProcessBuilder("svcl.exe", "/Mute", processId).start();
                            new ProcessBuilder("svcl.exe", "/UnMute", currentMicrophone).start();
                        } else {
                            // Mute the process
                            new ProcessBuilder("svcl.exe", "/Mute", processId).start();
                            new ProcessBuilder("svcl.exe", "/UnMute", currentMicrophone).start();
                        }
                        if (line.contains(
                                "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\MMDevices\\Audio\\Capture")) {
                            new ProcessBuilder("svcl.exe", "/UnMute", currentMicrophone).start();
                        }

                    } else {
                        System.out.println("Unmuting current app's process ID: " + activeProcessId);
                        // Unmute the current app's process ID
                        new ProcessBuilder("svcl.exe", "/UnMute", activeProcessId).start();
                        new ProcessBuilder("svcl.exe", "/UnMute", currentMicrophone).start();
                    }
                    try {
                        // Logger.log("muting" + line);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                }
            }
        }
    }

    private static Integer getProcessVolume(String processId) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("svcl.exe", "/Stdout", "/GetPercent", processId);
        Process process = pb.start();
        // Debug: System.out.println("Retrieving volume level for process ID: " +
        // processId);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String volumeStr = reader.readLine();
            if (volumeStr != null) {
                try {
                    float volumeFloat = Float.parseFloat(volumeStr.trim());
                    if (volumeFloat >= 99.5) {
                        int volume = 100;
                        return volume;
                    } else {
                        int volume = Math.round(volumeFloat);
                        // System.out.println("Retrieved volume level for process ID: " + processId + "=
                        // " + volume);
                        return volume;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing volume level");
                    try (PrintWriter out = new PrintWriter(new FileWriter("error.log", true))) {
                        out.println("Error parsing volume level for process ID: " + processId + "\n " + e.getMessage());
                        out.flush();
                    }
                }
            }
        }
        return null;
    }

    private static void restoreVolumes() {
        try {
            // Restore each process volume to its original state
            for (Map.Entry<String, Integer> entry : originalVolumes.entrySet()) {
                String processId = entry.getKey();
                int originalVolume = entry.getValue();
                // Restore the volume level to the original
                new ProcessBuilder("svcl.exe", "/SetVolume", processId, String.valueOf(originalVolume)).start();
                new ProcessBuilder("svcl.exe", "/Unmute", processId).start();
            }
            originalVolumes.clear(); // Clear stored volumes for the next mute cycle
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
