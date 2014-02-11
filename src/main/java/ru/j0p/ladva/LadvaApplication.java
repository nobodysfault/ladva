package ru.j0p.ladva;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LadvaApplication {
    private static Runnable craftWorker = new Runnable() {
        @Override
        public void run() {
            LineageClicker lineageClicker = null;
            try {
                lineageClicker = new LineageClicker();
            } catch (AWTException e) {
                System.out.println("Cannot create LineageClicker, exiting");
                System.exit(1);
            }

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
    };
    private static Thread craftThread = new Thread(craftWorker);

    public static void main(String[] args) {
        System.out.println("LADVA");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.out.println("Cannot change LookAndFeel, exiting");
            System.exit(1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                init();
            }
        });
    }

    private static void init() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported, exiting");
            System.exit(1);
        }

        final SystemTray systemTray = SystemTray.getSystemTray();
        Dimension trayIconSize = systemTray.getTrayIconSize();
        Image iconImage = createImage("images/ladva.gif", "LADVA");
        iconImage = iconImage.getScaledInstance(
            (int) trayIconSize.getWidth(),
            (int) trayIconSize.getHeight(),
            Image.SCALE_SMOOTH
        );
        final TrayIcon trayIcon = new TrayIcon(
            iconImage,
            "LADVA"
        );
        trayIcon.setImageAutoSize(true);

        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem startItem = new JMenuItem("Start");
        JMenuItem stopItem  = new JMenuItem("Stop");
        JMenuItem exitItem  = new JMenuItem("Exit");

        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(aboutItem);
        popupMenu.addSeparator();
        popupMenu.add(startItem);
        popupMenu.add(stopItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.setLocation(e.getX(), e.getY());
                    popupMenu.setInvoker(popupMenu);
                    popupMenu.setVisible(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.setLocation(e.getX(), e.getY());
                    popupMenu.setInvoker(popupMenu);
                    popupMenu.setVisible(true);
                }
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "LADVA Lineage 2 autoclicker");
            }
        });

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                systemTray.remove(trayIcon);
                System.exit(0);
            }
        });

        startItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!craftThread.isAlive()) {
                    craftThread.start();
                }
            }
        });

        stopItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (craftThread.isAlive()) {
                    craftThread.stop();
                }
            }
        });

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Cannot add tray icon, exiting");
            System.exit(1);
        }


    }

    protected static Image createImage(String path, String description) {
        URL imageURL = ClassLoader.getSystemClassLoader().getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
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

