package com.xspacesoft.jbreeze.lanterna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.xspacesoft.jbreeze.api.Daikin;
import com.xspacesoft.jbreeze.api.DaikinStatus;
import com.xspacesoft.jbreeze.api.options.FanDirection;
import com.xspacesoft.jbreeze.api.options.FanSpeed;
import com.xspacesoft.jbreeze.api.options.Mode;
import com.xspacesoft.jbreeze.api.options.Power;

public class Main {

	private Screen screen;
	private WindowBasedTextGUI textGUI;
	private List<Daikin> units;
	private Map<Daikin, DaikinStatus> statusMap;
	private File dataFile;
	private final int CONNECTION_TIMEOUT = 100;

	public Main() throws IOException {
		System.out.println("Drawing main window");
		Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
		screen = new TerminalScreen(terminal);
		screen.startScreen();
		units = new LinkedList<Daikin>();
		textGUI = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.CYAN));
		File folder = new File(new File(System.getProperty("user.home")), "JBreeze");
		if(!folder.exists())
			folder.mkdirs();
		dataFile = new File(folder, "config.txt");
		statusMap = new HashMap<Daikin, DaikinStatus>();
	}

	public static void main(String[] args) {
		try {
			Main main = new Main();
			main.loadData();
			main.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveData() throws IOException {
		if(!dataFile.exists())
			if(!dataFile.createNewFile())
				return;
		FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
		PrintWriter printWriter = new PrintWriter(fileOutputStream);
		for(Daikin unit : units) {
			printWriter.println(unit.getInetAddress().getHostName());
		}
		printWriter.close();
	}

	private void loadData() throws IOException, ClassNotFoundException {
		if(!dataFile.exists())
			return;
		Scanner scanner = new Scanner(dataFile);
		while(scanner.hasNextLine()) {
			try {
				Daikin daikin = new Daikin(InetAddress.getByName(scanner.nextLine()));
				units.add(daikin);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		scanner.close();
	}

	private void start() {
		Panel mainPanel = new Panel();
		Window window = new BasicWindow("JBreeze");
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		mainPanel.addComponent(new Label("Loading"));
		window.setComponent(mainPanel);
		textGUI.addWindow(window);
		System.out.println("Starting units polling");
		for(Daikin unit : units) {
			DaikinStatus daikinStatus = null;
			try {
				System.out.println("Connecting to unit " + unit.getInetAddress());
				if(unit.getInetAddress().isReachable(CONNECTION_TIMEOUT )) {
					System.out.println("Connected to " + unit.getInetAddress() + ", fetching data");
					daikinStatus = unit.getStatus();
					System.out.println("Connected to unit "
							+ unit.getInetAddress()
							+ " whith name "
							+ daikinStatus.getName());
				} else {
					System.out.println("Error connecting to " + unit.getInetAddress());
				}
			} catch (IOException e) { }
			statusMap.put(unit, daikinStatus);
		}

		window.close();
		showUnitList();
	}

	private void showUnitList() {
		System.out.println("Showing main menu");
		Window window = new BasicWindow("JBreeze");
		window.setHints(Arrays.asList(Window.Hint.CENTERED));
		Panel mainPanel = new Panel();
		Panel unitsPanel = new Panel();
		if(units.size()==0 ) {
			unitsPanel.addComponent(new Label("No units available"));
		} else {
			for(Daikin unit : units) {
				DaikinStatus status = statusMap.get(unit);
				Button unitButton;
				if(status!=null)
					unitButton = new Button(status.getName()
							+ " | " 
							+ status.getPower()
							+ " | "
							+ status.getMode()
							+ " | "
							+ status.getTemperature().getActual().toString(), () -> {
								unitDetail(unit);
							});
				else
					unitButton = new Button("Unreachable" 
							+ " (" 
							+ unit.getInetAddress().getHostAddress()
							+ ")", () -> {
								unitDetail(unit);
							});
				unitsPanel.addComponent(unitButton);
			}
		}
		mainPanel.addComponent(unitsPanel.withBorder(Borders.singleLine("Units")));
		Panel controls = new Panel();
		controls.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		Button addButton = new Button("Add unit", new Runnable() {

			@Override
			public void run() {
				addUnit();
				window.close();
			}
		});
		controls.addComponent(addButton);
		Button removeButton = new Button("Remove unit");
		controls.addComponent(removeButton);
		Button refreshButton = new Button("Refresh", () -> {
			window.close();
			start();
		});
		controls.addComponent(refreshButton);
		Button quitButton = new Button("Quit", () -> {
			System.exit(0);
		});


		controls.addComponent(quitButton);
		mainPanel.addComponent(controls.withBorder(Borders.singleLine("Controls")));
		window.setComponent(mainPanel);
		textGUI.addWindowAndWait(window);
	}

	private void addUnit() {
		Window window = new BasicWindow("Add unit");
		Panel panel = new Panel();
		Label hint = new Label("Enter unit's IP Address in the box:");
		panel.addComponent(hint);
		TextBox ip = new TextBox();
		Button confirm = new Button("Confirm", () -> {
			try {
				Daikin daikin = new Daikin(InetAddress.getByName(ip.getText()));
				units.add(daikin);
				DaikinStatus daikinStatus = daikin.getStatus();
				statusMap.put(daikin, daikinStatus);
				try {
					saveData();
				} catch (IOException e) {
					new MessageDialogBuilder()
					.setTitle("Error during data save")
					.setText(e.toString())
					.build()
					.showDialog(textGUI);
				}

			} catch (UnknownHostException e) {
				new MessageDialogBuilder()
				.setTitle("Error adding unit")
				.setText(e.toString())
				.build()
				.showDialog(textGUI);
			}
			window.close();
		});
		Button cancel = new Button("Cancel", new Runnable() {

			@Override
			public void run() {
				window.close();
			}
		});
		panel.addComponent(ip.withBorder(Borders.singleLine("Address")));
		Panel controls = new Panel();
		controls.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		controls.addComponent(confirm);
		controls.addComponent(cancel);
		panel.addComponent(controls);
		window.setComponent(panel);
		textGUI.addWindowAndWait(window);
	}


	private void unitDetail(Daikin daikin) {
		System.out.println("Opening unit detail for " + daikin.getInetAddress());
		boolean reachable = false;
		try {
			reachable = daikin.getInetAddress().isReachable(CONNECTION_TIMEOUT);
		} catch (IOException e) { }
		DaikinStatus daikinStatus = reachable?daikin.getStatus():null;
		if(daikinStatus==null) {
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
			Button autoModeButton = new Button("Auto", () -> {
				daikinStatus.setMode(Mode.AUTO);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button heatModeButton = new Button("Heat", () -> {
				daikinStatus.setMode(Mode.HEAT);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button coolModeButton = new Button("Cool", () -> {
				daikinStatus.setMode(Mode.COOL);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button fanModeButton = new Button("Fan", () -> {
				daikinStatus.setMode(Mode.FAN);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button dryModeButton = new Button("Dry", () -> {
				daikinStatus.setMode(Mode.DRY);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			modePanel.addComponent(autoModeButton);
			modePanel.addComponent(heatModeButton);
			modePanel.addComponent(coolModeButton);
			modePanel.addComponent(fanModeButton);
			modePanel.addComponent(dryModeButton);
			Panel temperaturePanel = new Panel();
			temperaturePanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
			StringBuilder currentTemperatureString = new StringBuilder();
			currentTemperatureString.append("Inside: " + daikinStatus.getTemperature().getActual());
			currentTemperatureString.append(" | ");
			currentTemperatureString.append("Outside: " + daikinStatus.getTemperature().getOutside());
			Label currentTemperatureDataLabel = new Label(currentTemperatureString.toString());
			Panel insertTemperaturePanel = new Panel();
			insertTemperaturePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
			Label targetTemperatureLabel = new Label("Target: ");
			TextBox targetTemperatureBox = new TextBox(getTargetTemperatureString(daikinStatus));
			Runnable invalidTemperatureMessage = () -> {
				new MessageDialogBuilder()
				.setTitle("Invalid temperature")
				.setText("Insert a corret value")
				.build()
				.showDialog(textGUI);
			};
			Button applyTargetTemperatureButton = new Button("Apply", () -> {
				if(targetTemperatureBox.getText().equals("M")||targetTemperatureBox.getText().equals("--")) {
					daikinStatus.getTemperature().setTarget(null);
				} else {
					try {
						Float newTarget = Float.parseFloat(targetTemperatureBox.getText());
						daikinStatus.getTemperature().setTarget(newTarget);
						daikin.setStatus(daikinStatus);
						window.close();
						unitDetail(daikin);
					} catch (NumberFormatException e) {
						invalidTemperatureMessage.run();
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
			Button autoFanButton = new Button("Auto", () -> {
				daikinStatus.setFanSpeed(FanSpeed.AUTO);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button silentFanButton = new Button("Silent", () -> {
				daikinStatus.setFanSpeed(FanSpeed.SILENCE);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button speed1 = new Button("1", () -> {
				daikinStatus.setFanSpeed(FanSpeed.SPEED_1);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button speed2 = new Button("2", () -> {
				daikinStatus.setFanSpeed(FanSpeed.SPEED_2);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button speed3 = new Button("3", () -> {
				daikinStatus.setFanSpeed(FanSpeed.SPEED_3);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button speed4 = new Button("4", () -> {
				daikinStatus.setFanSpeed(FanSpeed.SPEED_4);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			Button speed5 = new Button("5", () -> {
				daikinStatus.setFanSpeed(FanSpeed.SPEED_5);
				daikin.setStatus(daikinStatus);
				window.close();
				unitDetail(daikin);
			});
			fanSpeedPanel.addComponent(autoFanButton);
			fanSpeedPanel.addComponent(silentFanButton);
			fanSpeedPanel.addComponent(speed1);
			fanSpeedPanel.addComponent(speed2);
			fanSpeedPanel.addComponent(speed3);
			fanSpeedPanel.addComponent(speed4);
			fanSpeedPanel.addComponent(speed5);
			Panel windowControlPanel = new Panel();
			windowControlPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
			Button closeButton = new Button("Close", () -> {
				window.close();
			});
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
			mainPanel.addComponent(windowControlPanel);
			window.setComponent(mainPanel);
			textGUI.addWindow(window);
		}
	}

	private String getTargetTemperatureString(DaikinStatus daikinStatus) {
		Float target = daikinStatus.getTargetTemperature();
		if(target == null) {
			switch (daikinStatus.getMode()) {
			case DRY:
				return "M";
			case FAN:
				return "--";
			default:
				return "--";
			}
		} else {
			return target.toString();
		}
	}
}