import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;

public class GlobalKeyLogger implements NativeKeyListener {
    private static GlobalKeyLogger instance;

    GlobalKeyLogger() {
        new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                logKeyEvent("Pressed", e);
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                logKeyEvent("Released", e);
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                // Handle key typed event if needed
            }
        };
    }

    private void logKeyEvent(String action, NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode != NativeKeyEvent.VC_LEFT && keyCode != NativeKeyEvent.VC_RIGHT
                && keyCode != NativeKeyEvent.VC_UP && keyCode != NativeKeyEvent.VC_DOWN) {
            Logger.log(action + " key: " + NativeKeyEvent.getKeyText(keyCode));
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        logKeyEvent("Pressed", e);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        logKeyEvent("Released", e);
    }

    public void nativeMouseMoved(NativeMouseEvent e) {
        // Ignore mouse movement events
    }

    public void nativeMousePressed(NativeMouseEvent e) {
        // Ignore mouse pressed events
    }

    public void nativeMouseReleased(NativeMouseEvent e) {
        // Ignore mouse released events
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
        // Handle key typed event if needed
    }

    public static int[] waitForHotkey() {
        final Object lock = new Object();
        final int[] hotkey = new int[1];
        NativeKeyListener listener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                synchronized (lock) {
                    hotkey[0] = e.getModifiers() + e.getKeyCode();
                    lock.notify();
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                // Ignore
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                // Ignore
            }
        };

        GlobalScreen.addNativeKeyListener(listener);

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Logger.log("Interrupted while waiting for hotkey");
            }
        }

        GlobalScreen.removeNativeKeyListener(listener);

        return hotkey;
    }

    public static GlobalKeyLogger getInstance() {
        if (instance == null) {
            instance = new GlobalKeyLogger();
        }
        return instance;
    }

    public static String getHotkey() {
        GlobalKeyLogger.getInstance();
        int[] hotkey = GlobalKeyLogger.waitForHotkey();
        return NativeKeyEvent.getKeyText(hotkey[0]);
    }

    public static void waitForHotkeyNativeKeyReleased(int[] newHotkey, int i) {
        final Object lock = new Object();
        NativeKeyListener listener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                // Ignore
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                synchronized (lock) {
                    if (e.getModifiers() == newHotkey[0] && e.getKeyCode() == newHotkey[1]) {
                        lock.notify();
                    }
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                // Ignore
            }
        };

        GlobalScreen.addNativeKeyListener(listener);

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Logger.log("Interrupted while waiting for hotkey");
            }
        }

        GlobalScreen.removeNativeKeyListener(listener);
    }
}
