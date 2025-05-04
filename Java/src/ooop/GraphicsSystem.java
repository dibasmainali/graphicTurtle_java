package ooop;

import java.awt.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;

import uk.ac.leedsbeckett.oop.LBUGraphics;

// Turtle graphics system extending LBUGraphics
public class GraphicsSystem extends LBUGraphics {
    // Constants for app configuration
    private static final String APP_TITLE = "Turtle Graphics";
    private static final List<String> VALID_COMMANDS = Arrays.asList(
        "clock", "equilateral", "penwidth", "image", "save", "load", "screenshot","triangle", "square", "pencolor", 
        "about", "drawOff", "drawOn", "help","left", "right", "forward", "move", "penup", "pendown", "reverse", 
        "green", "red", "white", "black", "reset", "clear"  
    );
    private static final int DEFAULT_WINDOW_WIDTH = 800;
    private static final int DEFAULT_WINDOW_HEIGHT = 450;
    
    // UI components and state
    private final JFrame mainFrame;
    private final List<String> commandHistory = new ArrayList<>();

    // Initialize graphics system
    public GraphicsSystem() {
        mainFrame = new JFrame(APP_TITLE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(this, BorderLayout.CENTER);
        configureWindow();
        createMenuBar();
        mainFrame.setVisible(true);
    }

    // Set up main window
    private void configureWindow() {
        mainFrame.setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                promptSaveOnExit();
            }
        });
    }

    // Create menu bar
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createSaveMenu());
        fileMenu.add(createLoadMenu());
        fileMenu.addSeparator();
        fileMenu.add(createExitMenuItem());
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createHelpMenuItem());
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        mainFrame.setJMenuBar(menuBar);
    }

    // Create save menu
    private JMenu createSaveMenu() {
        JMenu saveMenu = new JMenu("Save");
        JMenuItem saveCommandsItem = new JMenuItem("Save Commands");
        saveCommandsItem.addActionListener(e -> saveCommandsToFile());
        JMenuItem saveImageItem = new JMenuItem("Save Image");
        saveImageItem.addActionListener(e -> saveImageToFile());
        saveMenu.add(saveCommandsItem);
        saveMenu.add(saveImageItem);
        return saveMenu;
    }

    // Create load menu
    private JMenu createLoadMenu() {
        JMenu loadMenu = new JMenu("Load");
        JMenuItem loadCommandsItem = new JMenuItem("Load Commands");
        loadCommandsItem.addActionListener(e -> loadCommandsFromFile());
        JMenuItem loadImageItem = new JMenuItem("Load Image");
        loadImageItem.addActionListener(e -> loadImageFromFile());
        loadMenu.add(loadCommandsItem);
        loadMenu.add(loadImageItem);
        return loadMenu;
    }

    // Create exit menu item
    private JMenuItem createExitMenuItem() {
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> promptSaveOnExit());
        return exitItem;
    }

    // Create help menu item
    private JMenuItem createHelpMenuItem() {
        JMenuItem helpItem = new JMenuItem("Show Help");
        helpItem.addActionListener(e -> showHelpDialog());
        return helpItem;
    }

    // Prompt to save before exit
    private void promptSaveOnExit() {
        if (!commandHistory.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Do you want to save before exiting?",
                "Exit Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) showSaveOptionsDialog();
            if (confirm != JOptionPane.CANCEL_OPTION) System.exit(0);
        } else System.exit(0);
    }

    // Show save options dialog
    private void showSaveOptionsDialog() {
        Object[] options = {"Save Commands", "Save Image", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
                "What would you like to save?",
                "Save Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == JOptionPane.YES_OPTION) saveCommandsToFile();
        else if (choice == JOptionPane.NO_OPTION) saveImageToFile();
    }

    // Show load options dialog
    private void showLoadOptionsDialog() {
        Object[] options = {"Load Commands", "Load Image", "Cancel"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
                "What would you like to load?",
                "Load Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == JOptionPane.YES_OPTION) loadCommandsFromFile();
        else if (choice == JOptionPane.NO_OPTION) loadImageFromFile();
    }

    // Save commands to file
    private void saveCommandsToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Commands");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(Paths.get(fileChooser.getSelectedFile().getAbsolutePath()), commandHistory);
                showSuccessMessage("Commands saved successfully!");
            } catch (Exception ex) {
                showErrorMessage("Error saving commands: " + ex.getMessage());
            }
        }
    }

    // Load commands from file
    private void loadCommandsFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Commands");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reset();
                drawOn();
                try (Scanner scanner = new Scanner(Paths.get(fileChooser.getSelectedFile().getAbsolutePath()))) {
                    while (scanner.hasNextLine()) processCommand(scanner.nextLine());
                }
                showSuccessMessage("Commands loaded successfully!");
            } catch (Exception ex) {
                showErrorMessage("Error loading commands: " + ex.getMessage());
            }
        }
    }

    // Save image to file
    private void saveImageToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".png")) filePath += ".png";
                ImageIO.write(getBufferedImage(), "png", new File(filePath));
                showSuccessMessage("Image saved successfully!");
            } catch (Exception ex) {
                showErrorMessage("Error saving image: " + ex.getMessage());
            }
        }
    }

    // Load image from file
    private void loadImageFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Image");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                displayImageInNewWindow(fileChooser.getSelectedFile().getAbsolutePath(), 
                                      fileChooser.getSelectedFile().getName());
            } catch (Exception ex) {
                showErrorMessage("Error loading image: " + ex.getMessage());
            }
        }
    }

    // Display image in new window
    private void displayImageInNewWindow(String imagePath, String windowTitle) {
        JFrame imageFrame = new JFrame(windowTitle);
        imageFrame.setPreferredSize(new Dimension(800, 400));
        imageFrame.getContentPane().add(new JLabel(new ImageIcon(imagePath)));
        imageFrame.pack();
        imageFrame.setLocationRelativeTo(null);
        imageFrame.setVisible(true);
    }

    // Show success message
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Show error message
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Display about info
    @Override
    public void about() {
        super.about();
        getGraphicsContext().drawString("Dibas mainali", 200, 100);
    }

    // Draw equilateral triangle
    public void drawEquilateralTriangle(int sideLength) {
        validatePositiveParameter(sideLength, "side length");
        drawOn();
        for (int i = 0; i < 3; i++) {
            forward(sideLength);
            left(120);
        }
    }

    // Draw triangle with given sides
    public void drawTriangle(int sideA, int sideB, int sideC) {
        validatePositiveParameter(sideA, "first side");
        validatePositiveParameter(sideB, "second side");
        validatePositiveParameter(sideC, "third side");
        if (!isValidTriangle(sideA, sideB, sideC)) throw new IllegalArgumentException("Invalid triangle sides");
        double angleA = Math.toDegrees(Math.acos((sideB*sideB + sideC*sideC - sideA*sideA)/(2.0*sideB*sideC)));
        double angleB = Math.toDegrees(Math.acos((sideA*sideA + sideC*sideC - sideB*sideB)/(2.0*sideA*sideC)));
        double angleC = Math.toDegrees(Math.acos((sideA*sideA + sideB*sideB - sideC*sideC)/(2.0*sideA*sideB)));
        drawOn();
        forward(sideA);
        left((int)(180.0 - angleC));
        forward(sideB);
        left((int)(180.0 - angleA));
        forward(sideC);
        left((int)(180.0 - angleB));
    }

    // Check if sides can form valid triangle
    private boolean isValidTriangle(int a, int b, int c) {
        return a + b > c && a + c > b && b + c > a;
    }

    // Draw square
    public void drawSquare(int sideLength) {
        validatePositiveParameter(sideLength, "side length");
        drawOn();
        for(int i=0; i<4; i++) {
            forward(sideLength);
            left(90);
        }
    }

    // Set pen width
    public void setPenWidth(int width) {
        validatePositiveParameter(width, "pen width");
        this.setStroke(width);
    }

    // Set pen color (RGB)
    public void setPenColor(int red, int green, int blue) {
        validateColorComponent(red, "red");
        validateColorComponent(green, "green");
        validateColorComponent(blue, "blue");
        this.setPenColour(new Color(red, green, blue));
    }

    // Validate color component (0-255)
    private void validateColorComponent(int value, String componentName) {
        if (value < 0 || value > 255) throw new IllegalArgumentException(componentName + " must be between 0-255");
    }

    // Validate positive parameter
    private void validatePositiveParameter(int value, String paramName) {
        if (value <= 0) throw new IllegalArgumentException(paramName + " must be positive");
    }
    
    // Show help dialog
    private void showHelpDialog() {
        String detail = "about: Display the turtle dance moving round and the name of the author\n" +
                       "drawOff: Lifts the pen from the canvas, so that movement does not get shown\n" +
                       "drawOn: Places the pen down on the canvas so movement gets shown as a drawn line\n" +
                       "black: Make the pen color black\n" +
                       "green: Makes the pen color green\n" +
                       "red: Makes the pen color red\n" +
                       "white: Makes the pen color white\n" +
                       "clear: Clears the whole output screen\n" +
                       "reset: Resets the canvas to its initial state with turtle pointing down but does not clear the display\n" +
                       "save: It provides options to save command or to save image\n" +
                       "load: It provides options to load command or to load image\n" +
                       "pencolor <int><int><int>: It takes three diferent color value to make RGB color\n" +
                       "penwidth: It takes one parameter and sets the pen stroke\n" +
                       "help: Display this menu!\n" +
                       "\n" +
                       "DRAWINGS\n" +
                       "rectangle <BREADTH> AND <HEIGHT>: Draws a rectangle\n" +
                       "square <SIDE>: Draws a square with the same length of all sides\n" +
                       "triangle <int>: Equilateral triangle is drawn\n" +
                       "triangle <int><int><int>: Three parameter is passed it draws normal traingle\n" +
                       "\n" +
                       "LINES BY PASSING PARAMETERS\n" +
                       "forward <int>: Forwards the turtle by the units passed\n" +
                       "backward <int>: Trutle will move backwards by the units passed\n" +
                       "left <int>: Turn the turtle to right by degrees passed\n" +
                       "right <int>: Turn the turtle to left by degree passed\n";

        JOptionPane.showMessageDialog(null, detail);
    }
    
    
    // Process command string
    public void processCommand(String command) {

        if (command == null || command.trim().isEmpty()) return;
        
        String[] parts = command.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();
        
        try {
            if (VALID_COMMANDS.contains(commandName)) {
                System.out.println("> " + command);
                commandHistory.add(command);
                
                switch (commandName) {
                    case "forward":
                    case "move":
                        forward(parts);
                        break;
                    case "left":
                        turn(parts, true);
                        break;
                    case "right":
                        turn(parts, false);
                        break;
                    case "reverse":
                        reverse(parts);
                        break;
                    case "penup":
                    case "drawOff":
                        System.out.println("Pen up");
                        drawOff();
                        break;
                    case "pendown":
                    case "drawOn":
                        System.out.println("Pen down");
                        drawOn();
                        break;
                        
                    case "black":
                        System.out.println("Pen color set to black");
                        setPenColour(Color.BLACK);
                        break;
                    case "red":
                        System.out.println("Pen color set to red");
                        setPenColour(Color.RED);
                        break;
                    case "green":
                        System.out.println("Pen color set to green");
                        setPenColour(Color.GREEN);
                        break;
                    case "white":
                        System.out.println("Pen color set to white");
                        setPenColour(Color.WHITE);
                        break;
                    case "clear":
                        System.out.println("Canvas cleared");
                        clear();
                        break;
                    case "reset":
                        System.out.println("Canvas reset");
                        reset();
                        break;
                    case "about":
                        about();
                        break;
                    case "equilateral":
                        equilateral(parts);
                        break;
                    case "pencolor":
                        pencolor(parts);
                        break;
                    case "square":
                        square(parts);
                        break;
                    case "penwidth":
                        penwidth(parts);
                        break;
                    case "triangle":
                        triangle(parts);
                        break;
                    case "save":
                        showSaveOptionsDialog();
                        break;
                    case "load":
                        showLoadOptionsDialog();
                        break;
                    case "screenshot":
                        saveImageToFile();
                        break;
                    case "image":
                        loadImageFromFile();
                        break;
                    case "help":
                        showHelpDialog();
                        break;
                    
                }
            } else {
                String errorMsg = "Invalid command: " + commandName;
                System.err.println(errorMsg);
                showErrorMessage(errorMsg);
            }
        } catch (NumberFormatException nfe) {
            String errorMsg = "Invalid number in command: " + command;
            System.err.println(errorMsg);
            showErrorMessage(errorMsg);
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            showErrorMessage(iae.getMessage());
        } catch (Exception e) {
            String errorMsg = "Error executing command: " + e.getMessage();
            System.err.println(errorMsg);
            showErrorMessage(errorMsg);
        }}
    
    // Turtle angle tracking
    private double turtleAngle = 0;

    // Get turtle position
    private Point getTurtlePosition() {
        return new Point(getxPos(), getyPos());
    }

    // Get turtle angle
    private int getTurtleAngle() {
        return (int) turtleAngle;
    }

    // Move turtle forward
    private void forward(String[] parts) {
        if (parts.length < 2) throw new IllegalArgumentException("forward requires distance");
        int distance = Integer.parseInt(parts[1]);
        validatePositiveParameter(distance, "distance");
        Point currentPos = getTurtlePosition();
        double angleRad = Math.toRadians(getTurtleAngle());
        int newX = currentPos.x + (int)(distance * Math.sin(angleRad));
        int newY = currentPos.y - (int)(distance * Math.cos(angleRad));
        Rectangle bounds = new Rectangle(50, 50, getWidth() - 100, getHeight() - 100);
        if (!bounds.contains(newX, newY)) {
            showErrorMessage("Movement would take turtle out of bounds!");
            return;
        }
        forward(distance);
    }

    // Turn turtle left/right
    private void turn(String[] parts, boolean isLeft) {
        int angle = parts.length > 1 ? Integer.parseInt(parts[1]) : 90;
        validatePositiveParameter(angle, "angle");
        if (isLeft) left(angle); else right(angle);
    }
    
    // Move turtle backward
    private void reverse(String[] parts) {
        int distance = parts.length > 1 ? Integer.parseInt(parts[1]) : 90;
        validatePositiveParameter(distance, "distance");
        forward(-distance);
    }
    
    // Draw equilateral triangle
    private void equilateral(String[] parts) {
        if (parts.length < 2) throw new IllegalArgumentException("equilateral requires size");
        drawEquilateralTriangle(Integer.parseInt(parts[1]));
    }
    
    // Set pen color
    private void pencolor(String[] parts) {
        if (parts.length < 4) throw new IllegalArgumentException("pencolor requires RGB values");
        setPenColor(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
    
    // Draw square
    private void square(String[] parts) {
        if (parts.length < 2) throw new IllegalArgumentException("square requires size");
        drawSquare(Integer.parseInt(parts[1]));
    }
    
    // Set pen width
    private void penwidth(String[] parts) {
        if (parts.length < 2) throw new IllegalArgumentException("penwidth requires size");
        setPenWidth(Integer.parseInt(parts[1]));
    }
    
    // Draw triangle
    private void triangle(String[] parts) {
        if (parts.length < 4) throw new IllegalArgumentException("triangle requires 3 sides");
        drawTriangle(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }

    // Main method
    public static void main(String[] args) {
        new GraphicsSystem();
    }
}