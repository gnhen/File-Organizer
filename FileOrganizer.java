import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileOrganizer extends JFrame {
    private final JTextField directoryField;
    private final JTextArea logArea;
    private final JTable fileTypesTable;
    private final DefaultTableModel tableModel;
    private final HashMap<String, String[]> fileTypes; // Maps extension to [mainCategory, subCategory]
    private final JProgressBar progressBar;

    // Define main categories and their associated file extensions
    private final Map<String, Set<String>> categoryDefinitions = new HashMap<>() {
        {
            put("IMAGES", new HashSet<>(Arrays.asList(
                    "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "svg", "ico", "raw")));
            put("VIDEOS", new HashSet<>(Arrays.asList(
                    "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "mpeg", "mpg")));
            put("AUDIO", new HashSet<>(Arrays.asList(
                    "mp3", "wav", "m4a", "flac", "aac", "wma", "ogg", "mid", "midi")));
            put("DOCUMENTS", new HashSet<>(Arrays.asList(
                    "pdf", "doc", "docx", "txt", "rtf", "odt", "xls", "xlsx", "ppt", "pptx")));
            put("ARCHIVES", new HashSet<>(Arrays.asList(
                    "zip", "rar", "7z", "tar", "gz", "bz2", "iso")));
            put("CODE", new HashSet<>(Arrays.asList(
                    "java", "py", "cpp", "c", "h", "js", "html", "css", "php", "rb")));
        }
    };

    public FileOrganizer() {
        fileTypes = new HashMap<>();

        setTitle("File Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Directory Selection Panel
        JPanel directoryPanel = new JPanel(new BorderLayout(5, 0));
        directoryField = new JTextField();
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> browseDirectory());
        directoryPanel.add(directoryField, BorderLayout.CENTER);
        directoryPanel.add(browseButton, BorderLayout.EAST);

        // File Types Panel
        JPanel fileTypesPanel = new JPanel(new BorderLayout(5, 5));
        fileTypesPanel.setBorder(BorderFactory.createTitledBorder("File Type Mappings"));

        String[] columnNames = { "Extension", "Main Category", "Sub Category" };
        tableModel = new DefaultTableModel(columnNames, 0);
        fileTypesTable = new JTable(tableModel);

        JPanel fileTypeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton scanTypesButton = new JButton("Scan Types");
        scanTypesButton.addActionListener(e -> scanFileTypes());
        fileTypeButtonPanel.add(scanTypesButton);

        fileTypesPanel.add(new JScrollPane(fileTypesTable), BorderLayout.CENTER);
        fileTypesPanel.add(fileTypeButtonPanel, BorderLayout.SOUTH);

        // Log Panel
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        // Organize Button
        JButton organizeButton = new JButton("Organize Files");
        organizeButton.addActionListener(e -> organizeFiles());

        // Layout assembly
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(directoryPanel, BorderLayout.NORTH);
        topPanel.add(fileTypesPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(organizeButton, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String determineMainCategory(String extension) {
        for (Map.Entry<String, Set<String>> category : categoryDefinitions.entrySet()) {
            if (category.getValue().contains(extension.toLowerCase())) {
                return category.getKey();
            }
        }
        return "OTHER";
    }

    private void scanFileTypes() {
        String directoryPath = directoryField.getText();
        if (directoryPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a directory first.");
            return;
        }

        File folder = new File(directoryPath);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            log("No files found in directory.");
            return;
        }

        fileTypes.clear();
        Set<String> foundExtensions = new HashSet<>();

        // Scan for unique file extensions
        for (File file : files) {
            if (file.isFile()) {
                String extension = getFileExtension(file);
                if (!extension.isEmpty()) {
                    foundExtensions.add(extension);
                }
            }
        }

        // Categorize files
        for (String extension : foundExtensions) {
            String mainCategory = determineMainCategory(extension);
            String subCategory = extension.toUpperCase();
            fileTypes.put(extension, new String[] { mainCategory, subCategory });
        }

        updateFileTypesTable();
        log("Found " + foundExtensions.size() + " unique file types");
    }

    private void updateFileTypesTable() {
        tableModel.setRowCount(0);
        for (Map.Entry<String, String[]> entry : fileTypes.entrySet()) {
            tableModel.addRow(new Object[] {
                    entry.getKey(),
                    entry.getValue()[0],
                    entry.getValue()[1]
            });
        }
    }

    private void browseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            directoryField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void organizeFiles() {
        String directoryPath = directoryField.getText();
        if (directoryPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a directory first.");
            return;
        }

        setComponentsEnabled(false);

        new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                File folder = new File(directoryPath);
                File[] files = folder.listFiles();

                if (files == null || files.length == 0) {
                    log("No files to organize.");
                    return null;
                }

                // Create main category directories and their subdirectories
                Set<String> mainCategories = new HashSet<>();
                for (String[] categories : fileTypes.values()) {
                    mainCategories.add(categories[0]);
                }

                for (String mainCategory : mainCategories) {
                    File mainDir = new File(folder, mainCategory);
                    mainDir.mkdirs();

                    // Create subdirectories for this main category
                    for (Map.Entry<String, String[]> entry : fileTypes.entrySet()) {
                        if (entry.getValue()[0].equals(mainCategory)) {
                            new File(mainDir, entry.getValue()[1]).mkdirs();
                        }
                    }
                }

                int totalFiles = files.length;
                int processedFiles = 0;

                // Move files to appropriate directories
                for (File file : files) {
                    if (file.isFile()) {
                        String extension = getFileExtension(file);
                        String[] categories = fileTypes.get(extension);
                        if (categories != null) {
                            Path mainCategoryPath = new File(folder, categories[0]).toPath();
                            Path targetDir = mainCategoryPath.resolve(categories[1]);
                            moveFile(file.toPath(), targetDir);
                            log("Moved: " + file.getName() + " to " + categories[0] + "/" + categories[1]);
                        }
                    }
                    processedFiles++;
                    publish((processedFiles * 100) / totalFiles);
                }

                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                setComponentsEnabled(true);
                progressBar.setValue(100);
                log("File organization completed.");
                JOptionPane.showMessageDialog(FileOrganizer.this, "Files organized successfully!");
            }
        }.execute();
    }

    private void setComponentsEnabled(boolean enabled) {
        directoryField.setEnabled(enabled);
        fileTypesTable.setEnabled(enabled);
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JButton) {
                comp.setEnabled(enabled);
            }
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOfDot = name.lastIndexOf('.');
        return (lastIndexOfDot == -1) ? "" : name.substring(lastIndexOfDot + 1).toLowerCase();
    }

    private void moveFile(Path source, Path targetDir) {
        try {
            Files.move(source, targetDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log("Error moving file " + source.getFileName() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new FileOrganizer().setVisible(true);
        });
    }
}