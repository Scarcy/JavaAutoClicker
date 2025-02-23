import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

public class AutoClicker extends JFrame implements NativeKeyListener, NativeMouseListener {
    private JTextField location1Field, location2Field, delayField, hotkeyField;
    private JButton pickLocation1Button, pickLocation2Button, startStopButton, setHotkeyButton;
    private JLabel statusLabel;
    private JCheckBox doubleClickCheckBox, randomDelayCheckBox;
    private JToolTip randomDelayToolTip;
    private JSpinner randomDelayMax;
    private Point point1, point2;
    private boolean clicking = false;
    private Thread clickThread;
    private boolean pickingLocation = false; // Flag to track if user is selecting a location
    private int pickingFor = 0; // 1 for first location, 2 for second location
    private int startStopKey = NativeKeyEvent.VC_F6; // Default: F6
    private Logger log;
    private Random rand;

    public AutoClicker() {
        setTitle("Ellefsen AutoClicker");
        setSize(300, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        this.log = Logger.getLogger(AutoClicker.class.getName());
        rand = new Random();
        //setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/mouse.png")));

        // Location 1
        JLabel location1Label = new JLabel("Location 1:");
        location1Label.setBounds(10, 10, 80, 25);
        location1Field = new JTextField();
        location1Field.setBounds(100, 10, 100, 25);
        location1Field.setEditable(false);
        pickLocation1Button = new JButton("Pick");
        pickLocation1Button.setBounds(210, 10, 60, 25);
        pickLocation1Button.addActionListener(e -> startPickingLocation(1));

        // Location 2
        JLabel location2Label = new JLabel("Location 2:");
        location2Label.setBounds(10, 40, 80, 25);
        location2Field = new JTextField();
        location2Field.setBounds(100, 40, 100, 25);
        location2Field.setEditable(false);
        pickLocation2Button = new JButton("Pick");
        pickLocation2Button.setBounds(210, 40, 60, 25);
        pickLocation2Button.addActionListener(e -> startPickingLocation(2));

        // Delay Field
        JLabel delayLabel = new JLabel("Delay (ms):");
        delayLabel.setBounds(10, 70, 80, 25);
        delayField = new JTextField("200");
        delayField.setBounds(100, 70, 100, 25);


        // Set Hotkey
        JLabel hotkeyLabel = new JLabel("Hotkey:");
        hotkeyLabel.setBounds(10, 100, 80, 25);
        hotkeyField = new JTextField("F6");
        hotkeyField.setBounds(100, 100, 100, 25);
        hotkeyField.setEditable(false);
        setHotkeyButton = new JButton("Set");
        setHotkeyButton.setBounds(210, 100, 60, 25);
        setHotkeyButton.addActionListener(e -> startHotkeySelection());

        // Double Click Toggle
        doubleClickCheckBox = new JCheckBox("Double Click");
        doubleClickCheckBox.setBounds(10, 130, 125, 25);
        //doubleClickCheckBox.setToolTipText("Sometimes single");
        JLabel doubleClickInfoLabel = new JLabel("ⓘ");
        doubleClickInfoLabel.setBounds(135, 130, 20, 25);

        // Create custom tooltip (JPanel)
        JPopupMenu tooltipPopup = new JPopupMenu();
        tooltipPopup.add(new JLabel("<html><div style='padding: 5px'>" +
                "Sometimes double clicks are necessary when" +
                "<br>" +
                "the clicker clicks on different windows</div></html>"));

        // Show tooltip when mouse hovers over the label
        doubleClickInfoLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                tooltipPopup.show(doubleClickInfoLabel, 0, doubleClickInfoLabel.getHeight());
            }

            public void mouseExited(MouseEvent e) {
                tooltipPopup.setVisible(false);
            }
        });

        // Random Delay
        randomDelayCheckBox = new JCheckBox("+/- Random Delay");
        randomDelayCheckBox.setBounds(10, 160, 160, 25);
        randomDelayCheckBox.setToolTipText("Random delay may help to avoid bot detections in games");
        randomDelayCheckBox.addActionListener(e -> toggleRandomDelay());

        SpinnerModel smodel = new SpinnerNumberModel(100, 1, 500, 5);
        randomDelayMax = new JSpinner(smodel);
        randomDelayMax.setBounds(165, 160, 100, 25);
        randomDelayMax.setEnabled(false);

        // Start/Stop Button
        startStopButton = new JButton("Start");
        startStopButton.setBounds(10, 190, 260, 30);
        startStopButton.addActionListener(e -> toggleClicking());

        // Status Label
        statusLabel = new JLabel("Status: Idle");
        statusLabel.setBounds(10, 225, 260, 25);

        // Add components to the frame
        add(location1Label);
        add(location1Field);
        add(pickLocation1Button);
        add(location2Label);
        add(location2Field);
        add(pickLocation2Button);
        add(delayLabel);
        add(delayField);
        add(doubleClickCheckBox);
        add(hotkeyLabel);
        add(hotkeyField);
        add(setHotkeyButton);
        add(startStopButton);
        add(statusLabel);
        add(randomDelayCheckBox);
        add(doubleClickInfoLabel);
        add(randomDelayMax);
        // Disable JNativeHook logging
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        // Register global key listener
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
        } catch (Exception e) {
            log.warning("Exception when registering Mouse/Key listeners: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Can't register Mouse/Keyboard events.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void toggleRandomDelay() {
        boolean isEnabled = randomDelayCheckBox.isSelected();
        randomDelayMax.setEnabled(isEnabled);
    }

    private void startPickingLocation(int location) {
        pickingLocation = true;
        pickingFor = location;
        statusLabel.setText("Click anywhere to set Location " + location + "...");
    }

    private void startHotkeySelection() {
        //JOptionPane.showMessageDialog(this, "Press a key to set as the hotkey.");
        hotkeyField.setText("Press a key...");
        setHotkeyButton.setEnabled(false);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!setHotkeyButton.isEnabled()) {
            startStopKey = e.getKeyCode();
            hotkeyField.setText(NativeKeyEvent.getKeyText(startStopKey));
            setHotkeyButton.setEnabled(true);
            statusLabel.setText("Status: Idle");
        } else if (e.getKeyCode() == startStopKey) {
            toggleClicking();
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if (pickingFor == 0) return; // Only record if user is picking a location

        Point clickedPoint = new Point(e.getX(), e.getY());

        if (pickingFor == 1) {
            point1 = clickedPoint;
            location1Field.setText(point1.x + ", " + point1.y);
        } else if (pickingFor == 2) {
            point2 = clickedPoint;
            location2Field.setText(point2.x + ", " + point2.y);
        }

        pickingFor = 0; // Reset selection mode
        statusLabel.setText("Status: Idle");
    }

    private void toggleClicking() {
        if (!clicking) {
            if (point1 == null || point2 == null) {
                JOptionPane.showMessageDialog(this, "Please select both click locations first!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int delay = Integer.parseInt(delayField.getText());
                int randomDelay = (int) randomDelayMax.getValue();
                clicking = true;
                startStopButton.setText("Stop");
                statusLabel.setText("Status: Running...");

                this.requestFocusInWindow(); // Removes focus from input fields

                clickThread = new Thread(() -> clickLoop(delay, randomDelay));
                clickThread.start();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            clicking = false;
            startStopButton.setText("Start");
            statusLabel.setText("Status: Idle");
            if (clickThread != null) clickThread.interrupt();
        }
    }
    private int getDelay(int const_delay, int randomDelay, boolean withRandom){
        if (!withRandom){
            return const_delay;
        }
        int extra = rand.nextInt(randomDelay);
        return const_delay + extra;
    }
    private void clickLoop(int delay, int randomDelay) {
        try {
            Robot robot = new Robot();
            boolean doubleClick = doubleClickCheckBox.isSelected();
            boolean withRandomDelay = randomDelayCheckBox.isSelected();

            while (clicking) {
                performClick(robot, point1, doubleClick);
                Thread.sleep(getDelay(delay, randomDelay, withRandomDelay));

                performClick(robot, point2, doubleClick);
                Thread.sleep(getDelay(delay, randomDelay, withRandomDelay));
            }
        } catch (InterruptedException e){
            clicking = false;
            statusLabel.setText("Status: Stopped");
        }
        catch (Exception e) {
            clicking = false;
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void performClick(Robot robot, Point point, boolean doubleClick) {
        robot.mouseMove(point.x, point.y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        if (doubleClick) {
            try {
                Thread.sleep(50);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } catch (InterruptedException e) {

            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AutoClicker::new);
    }
}
