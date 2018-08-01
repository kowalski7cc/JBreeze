package com.xspacesoft.jbreeze.lanterna;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.xspacesoft.jbreeze.api.Daikin;
import com.xspacesoft.jbreeze.api.DaikinStatus;
import com.xspacesoft.jbreeze.api.options.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Main {

    private final static String OS = System.getProperty("os.name");
    private final int CONNECTION_TIMEOUT = 100;
    private WindowBasedTextGUI textGUI;
    private List<Daikin> units;
    private Map<Daikin, DaikinStatus> statusMap;
    private File dataFile;

    public Main() throws IOException {
        System.out.println("Starting JBreeze");
        System.out.println("Getting OS data");
        System.out.println("OS: " + OS);
        File home = getHomeDirectory();
        System.out.println("Home direcotry: " + home);
        System.out.println("Drawing main window");
        if (!home.exists())
            home.mkdirs();
        dataFile = new File(home, "config.txt");
        units = new LinkedList<>();
    }

    public static void main(String[] args) {
        try {
            Main main = new Main();
            main.loadData();
            main.pollUnits();
            main.startScreen();
            main.showUnitList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startScreen() throws IOException {
        Terminal terminal = null;
        DefaultTerminalFactory dtf = new DefaultTerminalFactory()
                .setTerminalEmulatorTitle("Jbreeze");
        try {
            terminal = dtf.createTerminal();
        } catch (IOException e) {
            if (OS.toLowerCase().contains("windows")) {
                try {
                    terminal = dtf.createTerminalEmulator();
                } catch (Exception e1) {
                    System.out.println(e1);
                    System.exit(1);
                }
            } else {
                System.exit(1);
            }
        }

        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
                new EmptySpace(TextColor.ANSI.CYAN));
    }

    private void saveData() throws IOException {
        if (!dataFile.exists() && !dataFile.createNewFile())
            return;
        PrintWriter printWriter = new PrintWriter(new FileOutputStream(dataFile));
        units.forEach(u -> printWriter.println(u.getInetAddress().getHostName()));
        printWriter.close();
    }

    private void loadData() throws IOException {
        if (!dataFile.exists())
            return;
        Scanner scanner = new Scanner(dataFile);
        while (scanner.hasNextLine()) {
            try {
                Daikin daikin = new Daikin(InetAddress.getByName(scanner.nextLine()));
                units.add(daikin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    private void pollUnits() {
        BatchControl batchControl = new BatchControl(units);
        statusMap = batchControl.getAllStatus();
        batchControl.close();
    }

    private void showUnitList() {
        System.out.println("Showing main menu");
        Window window = new BasicWindow("JBreeze");
        window.setHints(Collections.singletonList(Window.Hint.CENTERED));
        Panel mainPanel = new Panel();
        Panel unitsPanel = new Panel();

        if (units.size() == 0) {
            unitsPanel.addComponent(new Label("No units available"));
        } else {
            statusMap.forEach((unit, status) ->
                    unitsPanel.addComponent(newButtonFromStatus(unit, status, () ->
                            unitDetail(unit))));
        }

        mainPanel.addComponent(unitsPanel.withBorder(Borders.singleLine("Units")));

        Panel controls = new Panel();
        controls.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        controls.addComponent(new Button("Add unit", () -> {
            addUnit();
            window.close();
            pollUnits();
            showUnitList();
        }));

        controls.addComponent(new Button("Remove unit", () -> {
            removeUnit();
            window.close();
            pollUnits();
            showUnitList();
        }));

        controls.addComponent(new Button("Refresh", () -> {
            window.close();
            pollUnits();
            showUnitList();
        }));

        controls.addComponent(new Button("Quit", () -> System.exit(0)));

        mainPanel.addComponent(controls.withBorder(Borders.singleLine("Controls")));
        window.setComponent(mainPanel);
        textGUI.addWindowAndWait(window);
    }

    private Button newButtonFromStatus(Daikin unit, DaikinStatus status, Runnable action) {
        Objects.requireNonNull(unit);
        if (status == null)
            return new Button("Unreachable"
                    + " ("
                    + unit.getInetAddress().getHostAddress()
                    + ")", action);
        return new Button(status.getName()
                + " | "
                + status.getPower()
                + " | "
                + status.getMode()
                + " | "
                + status.getTemperature().getActual().toString(), action);
    }

    private void removeUnit() {
        Window window = new BasicWindow("Remove unit");
        Panel panel = new Panel();
        panel.addComponent(new Label("Select unit to discard"));
        Panel unitsPanel = new Panel();

        statusMap.forEach((unit, status) ->
                unitsPanel.addComponent(newButtonFromStatus(unit, status, () ->
                {
                    units.remove(unit);
                    statusMap.remove(unit);
                    try {
                        saveData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    window.close();
                    removeUnit();
                })));
        panel.addComponent(unitsPanel.withBorder(Borders.singleLine("Units")));

        Panel controls = new Panel();
        controls.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        controls.addComponent(new Button("Back", window::close));
        panel.addComponent(controls);
        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void addUnit() {
        Window window = new BasicWindow("Add unit");
        Panel panel = new Panel();
        Label hint = new Label("Enter unit's IP Address in the box:");
        panel.addComponent(hint);
        TextBox ip = new TextBox();
        panel.addComponent(ip.withBorder(Borders.singleLine("Address")));
        Panel controls = new Panel();
        controls.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        controls.addComponent(new Button("Confirm", () -> {
            try {
                Daikin daikin = new Daikin(InetAddress.getByName(ip.getText()));
                units.add(daikin);
                statusMap.put(daikin, daikin.getStatus());
                saveData();
                window.close();
            } catch (UnknownHostException e) {
                new MessageDialogBuilder()
                        .setTitle("Error adding unit")
                        .setText(e.toString())
                        .build()
                        .showDialog(textGUI);
            } catch (IOException e) {
                new MessageDialogBuilder()
                        .setTitle("Error during data save")
                        .setText(e.toString())
                        .build()
                        .showDialog(textGUI);
            }
        }));
        controls.addComponent(new Button("Cancel", window::close));
        panel.addComponent(controls);
        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void unitDetail(Daikin daikin) {
        System.out.println("Opening unit detail for " + daikin.getInetAddress());
        boolean reachable = false;

        try {
            reachable = daikin.getInetAddress().isReachable(CONNECTION_TIMEOUT);
        } catch (IOException ignore) {

        }

        DaikinStatus daikinStatus = reachable ? daikin.getStatus() : null;
        if (daikinStatus == null) {
            System.out.println("Showing alert for unreachable unit");
            new MessageDialogBuilder()
                    .setTitle("Error during unit connection")
                    .setText("Is unit connected and powered?")
                    .build()
                    .showDialog(textGUI);
        } else {
            String groupName = daikinStatus.getGroupName() == null ? "" : " - " + daikinStatus.getGroupName();
            Window window = new BasicWindow(daikinStatus.getName() + groupName);
            Button onButton = new Button("On", () -> {
                daikinStatus.setPower(Power.ON);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            });
            Button offButton = new Button("Off", () -> {
                daikinStatus.setPower(Power.OFF);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            });
            Panel powerPanel = new Panel();
            powerPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
            powerPanel.addComponent(onButton);
            powerPanel.addComponent(offButton);
            Panel modePanel = new Panel();
            modePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

            modePanel.addComponent(new Button("Auto", () -> {
                daikinStatus.setMode(Mode.AUTO);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            modePanel.addComponent(new Button("Heat", () -> {
                daikinStatus.setMode(Mode.HEAT);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            modePanel.addComponent(new Button("Cool", () -> {
                daikinStatus.setMode(Mode.COOL);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            modePanel.addComponent(new Button("Fan", () -> {
                daikinStatus.setMode(Mode.FAN);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            modePanel.addComponent(new Button("Dry", () -> {
                daikinStatus.setMode(Mode.DRY);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            Panel temperaturePanel = new Panel();
            temperaturePanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            String currentTemperatureString = "Inside: " +
                    daikinStatus.getTemperature().getActual() +
                    " | " +
                    "Outside: " +
                    daikinStatus.getTemperature().getOutside();

            Label currentTemperatureDataLabel = new Label(currentTemperatureString);
            Panel insertTemperaturePanel = new Panel();
            insertTemperaturePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

            Label targetTemperatureLabel = new Label("Target: ");
            TextBox targetTemperatureBox = new TextBox(getTargetTemperatureString(daikinStatus));

            Button applyTargetTemperatureButton = new Button("Apply", () -> {
                if (targetTemperatureBox.getText().equals("M") || targetTemperatureBox.getText().equals("--")) {
                    daikinStatus.getTemperature().setTarget(null);
                } else {
                    try {
                        Float newTarget = Float.parseFloat(targetTemperatureBox.getText());
                        daikinStatus.getTemperature().setTarget(newTarget);
                        daikin.setStatus(daikinStatus);
                        window.close();
                        unitDetail(daikin);
                    } catch (NumberFormatException e) {
                        new MessageDialogBuilder()
                                .setTitle("Invalid temperature")
                                .setText("Insert a corret value")
                                .build()
                                .showDialog(textGUI);
                    }
                }
            });
            insertTemperaturePanel.addComponent(targetTemperatureLabel);
            insertTemperaturePanel.addComponent(targetTemperatureBox);
            insertTemperaturePanel.addComponent(applyTargetTemperatureButton);
            temperaturePanel.addComponent(currentTemperatureDataLabel);
            temperaturePanel.addComponent(insertTemperaturePanel);
            Panel fanSpeedPanel = new Panel();
            fanSpeedPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

            fanSpeedPanel.addComponent(new Button("Auto", () -> {
                daikinStatus.setFanSpeed(FanSpeed.AUTO);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            fanSpeedPanel.addComponent(new Button("Silent", () -> {
                daikinStatus.setFanSpeed(FanSpeed.SILENCE);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            fanSpeedPanel.addComponent(new Button("1", () -> {
                daikinStatus.setFanSpeed(FanSpeed.SPEED_1);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            fanSpeedPanel.addComponent(new Button("2", () -> {
                daikinStatus.setFanSpeed(FanSpeed.SPEED_2);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            fanSpeedPanel.addComponent(new Button("3", () -> {
                daikinStatus.setFanSpeed(FanSpeed.SPEED_3);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            fanSpeedPanel.addComponent(new Button("4", () -> {
                daikinStatus.setFanSpeed(FanSpeed.SPEED_4);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            fanSpeedPanel.addComponent(new Button("5", () -> {
                daikinStatus.setFanSpeed(FanSpeed.SPEED_5);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            Panel specialModePanel = new Panel();
            specialModePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

            specialModePanel.addComponent(new Button("None", () -> {
                daikinStatus.setSpecialModeActive(false);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));
            specialModePanel.addComponent(new Button("Powerful", () -> {
                daikinStatus.setSpecialMode(SpecialMode.POWERFUL);
                daikinStatus.setSpecialModeActive(true);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));
            specialModePanel.addComponent(new Button("Economy", () -> {
                daikinStatus.setSpecialMode(SpecialMode.ECONOMY);
                daikinStatus.setSpecialModeActive(true);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            }));

            Panel windowControlPanel = new Panel();
            windowControlPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
            Button closeButton = new Button("Close", window::close);

            Button refreshButton = new Button("Refresh", () -> {
                window.close();
                unitDetail(daikin);
            });
            Button deleteUnitButton = new Button("Delete unit", () -> {
                units.remove(daikin);
                try {
                    saveData();
                } catch (IOException e) {
                    new MessageDialogBuilder()
                            .setTitle("Error during data save")
                            .setText(e.toString())
                            .build()
                            .showDialog(textGUI);
                } finally {
                    window.close();
                }
            });

            windowControlPanel.addComponent(closeButton);
            windowControlPanel.addComponent(refreshButton);
            windowControlPanel.addComponent(deleteUnitButton);


            Panel fanDirectionPanel = new Panel();
            fanDirectionPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
            Button noneDirectionButton = new Button("None", () -> {
                daikinStatus.setFanDirection(FanDirection.NONE);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            });
            Button verticalDirectionButton = new Button("Vertical", () -> {
                daikinStatus.setFanDirection(FanDirection.VERTICAL);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            });
            Button horizontalDirectionButton = new Button("Horizontal", () -> {
                daikinStatus.setFanDirection(FanDirection.HORIZONTAL);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            });
            Button verticalAndHorizontalDirectionButton = new Button("Vertical and Horizontal", () -> {
                daikinStatus.setFanDirection(FanDirection.VERTICAL_AND_HORIZONTAL);
                daikin.setStatus(daikinStatus);
                window.close();
                unitDetail(daikin);
            });
            fanDirectionPanel.addComponent(noneDirectionButton);
            fanDirectionPanel.addComponent(verticalDirectionButton);
            fanDirectionPanel.addComponent(horizontalDirectionButton);
            fanDirectionPanel.addComponent(verticalAndHorizontalDirectionButton);


            // Add subpanels to main panel
            Panel mainPanel = new Panel();
            mainPanel.addComponent(powerPanel.withBorder(Borders.singleLine("Power control | Actual status: " + daikinStatus.getPower())));
            mainPanel.addComponent(modePanel.withBorder(Borders.singleLine("Mode control | Actual mode: " + daikinStatus.getMode())));
            mainPanel.addComponent(temperaturePanel.withBorder(Borders.singleLine("Temperature control")));
            mainPanel.addComponent(fanSpeedPanel.withBorder(Borders.singleLine("Fan speed control | Acutal speed: " + daikinStatus.getFanSpeed())));
            mainPanel.addComponent(fanDirectionPanel.withBorder(Borders.singleLine("Fan direction control | Actual direction: " + daikinStatus.getFanDirection())));
            mainPanel.addComponent(specialModePanel.withBorder(Borders.singleLine("Special mode control | Actual: " + daikinStatus.getSpecialMode())));
            mainPanel.addComponent(windowControlPanel);
            window.setComponent(mainPanel);
            textGUI.addWindow(window);
        }
    }

    private String getTargetTemperatureString(DaikinStatus daikinStatus) {
        Float target = daikinStatus.getTargetTemperature();
        if (target == null) {
            switch (daikinStatus.getMode()) {
                case DRY:
                    return "M";
                case FAN:
                    return "--";
                default:
                    return "--";
            }
        }
        return target.toString();
    }

    private File getHomeDirectory() {
        if (OS.toLowerCase().contains("windows")) {
            return new File(new File(System.getenv("APPDATA")), "JBreeze");
        } else {
            return new File(new File(System.getProperty("user.home")), ".jbreeze");
        }
    }

}
