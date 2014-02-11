package ru.j0p.ladva;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

public class LineageClicker {
    private static final int MANA_Y       = 79;
    private static final int MANA_X_START = 20;
    private static final int MANA_X_END   = 169;

    private static final Color MANA_UP   = new Color(0, 113, 206);
    private static final Color MANA_DOWN = new Color(24, 65, 99);

    private static final int TESTPOINT_X       = 12;
    private static final int TESTPOINT_Y       = 78;
    private static final Color TESTPOINT_COLOR = new Color(24, 32, 57);

    private Robot robot;
    private Random random;

    public LineageClicker() throws AWTException {
        robot = new Robot();
        random = new Random(System.currentTimeMillis());
    }

    public Float getManaPercentage() {
        return getBarPercentage(
            MANA_X_START,
            MANA_X_END,
            MANA_Y,
            MANA_UP,
            MANA_DOWN
        );
    }

    private Float getBarPercentage(int leftX, int rightX, int barY, Color colorUp, Color colorDown) {
        if (!isLineageRunning()) {
            return null;
        }

        BufferedImage screen = robot.createScreenCapture(new Rectangle(leftX, barY, rightX - leftX, 1));

        int startX   = 0;
        int endX     = screen.getWidth();
        int currentX = endX;

        while (startX < endX - 5) { // we don't need such precision
            currentX = (startX + endX) / 2;
            if (screen.getRGB(currentX, 0) == colorDown.getRGB()) {
                endX = currentX;
            } else {
                startX = currentX;
            }
        }

        return ((float) currentX / screen.getWidth() * 100);
    }

    public void delay(int ms) {
        robot.delay(getGaussianDelay(ms));
    }

    public void click(int x, int y) {
        if (!isLineageRunning()) {
            return;
        }
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(getGaussianDelay(50));
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }


    private boolean isLineageRunning() {
        return robot.getPixelColor(TESTPOINT_X, TESTPOINT_Y).equals(TESTPOINT_COLOR);
    }

    private int getGaussianDelay(int ms) {
        double gaussian = random.nextGaussian();
        if (gaussian < -2) {
            gaussian = -2;
        }
        else if (gaussian > 2) {
            gaussian = 2;
        }
        gaussian *= .1;
        return (int) (ms * (1 + gaussian / 10)); // get ms +- 20%
    }
}
