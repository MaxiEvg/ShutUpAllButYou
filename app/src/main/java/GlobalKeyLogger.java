import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;

public class GlobalKeyLogger implements NativeKeyListener {

    // In GlobalKeyLogger.java
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode != NativeKeyEvent.VC_LEFT && keyCode != NativeKeyEvent.VC_RIGHT
                && keyCode != NativeKeyEvent.VC_UP && keyCode != NativeKeyEvent.VC_DOWN) {
            Logger.log("Pressed key: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode != NativeKeyEvent.VC_LEFT && keyCode != NativeKeyEvent.VC_RIGHT
                && keyCode != NativeKeyEvent.VC_UP && keyCode != NativeKeyEvent.VC_DOWN) {
            Logger.log("Released key: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'nativeKeyTyped'");
    }

}
