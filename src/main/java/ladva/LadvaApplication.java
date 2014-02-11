package ladva;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.AWTException;

import ladva.lineage.LineageClicker;

public class LadvaApplication {
    private static LineageClicker lineageClicker;

    public static void main(String[] args) {
        System.out.println("LADVA clicker");

        try {
            lineageClicker = new LineageClicker();
        } catch (AWTException e) {
            e.printStackTrace();
            System.exit(1);
        }

        eventCycle();
    }

    private static void eventCycle() {
        while (true) {
            Float manaPercentage = lineageClicker.getManaPercentage();
            while (manaPercentage != null && manaPercentage > 20) {
                lineageClicker.click(1382, 651);
                lineageClicker.delay(200);
                manaPercentage = lineageClicker.getManaPercentage();
            }
            lineageClicker.click(153, 491);
            lineageClicker.delay(200);
        }
    }


// get current window title
    private void getWindowTitle() {
        if(Platform.isLinux()) {  // Possibly most of the Unix systems will work here too, e.g. FreeBSD
            final X11 x11 = X11.INSTANCE;
            final XLib xlib= XLib.INSTANCE;
            X11.Display display = x11.XOpenDisplay(null);
            X11.Window window=new X11.Window();
            xlib.XGetInputFocus(display, window,Pointer.NULL);
            X11.XTextProperty name=new X11.XTextProperty();
            x11.XGetWMName(display, window, name);
            System.out.println(name.toString());
        }
    }

    public interface Psapi extends StdCallLibrary {
        Psapi INSTANCE = (Psapi) Native.loadLibrary("Psapi", Psapi.class);

        WinDef.DWORD GetModuleBaseNameW(Pointer hProcess, Pointer hModule, byte[] lpBaseName, int nSize);
    }

    public interface XLib extends StdCallLibrary {
        XLib INSTANCE = (XLib) Native.loadLibrary("XLib", XLib.class);

        int XGetInputFocus(X11.Display display, X11.Window focus_return, Pointer revert_to_return);
    }
}

