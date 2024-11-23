import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class HotKeyListener implements NativeKeyListener {

    private static final long TIMER_DELAY = 700; // 0.7 seconds
    private static String firstKey = null;
    private static String secondKey = null;
    private static Timer timer;

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (firstKey == null) {
            firstKey = NativeKeyEvent.getKeyText(e.getKeyCode());
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    firstKey = null;
                    secondKey = null;
                }
            }, TIMER_DELAY);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (firstKey != null) {
            secondKey = NativeKeyEvent.getKeyText(e.getKeyCode());
            // Debug: log hotkey
            // Logger.log(firstKey + " + " + secondKey);
            try {
                checkHotkey();
            } catch (InterruptedException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @SuppressWarnings("resource")
    public static void checkHotkey() throws InterruptedException, IOException {
        try {
            File file = new File("hotkey.inf");
            Scanner scanner = new Scanner(file);
            String line = scanner.nextLine();
            String[] hotkeys = line.split(" \\+ ");
            String expectedFirstKey = hotkeys[0];
            String expectedSecondKey = hotkeys[1];

            if (firstKey.equals(expectedFirstKey) && secondKey.equals(expectedSecondKey)) {
                SoundMuteApp.triggerHotkey();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error reading hotkey file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new HotKeyListener());
        } catch (NativeHookException e) {
            System.err.println("Error registering native hook: " + e.getMessage());
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Ignore key typed events as we only care about key presses
    }
}