package nl.tue.vrp.gui.input.twoevrp;

import nl.tue.vrp.gui.filter.YamlFileFilter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;

public class TwoEVRPInput {
    private final JFrame mainFrame;
    private final JPanel panel;
    private final JButton loadFileButton;

    private final JButton visualizeButton = null; // TODO
    private final JLabel inputLabel;
    private final JTextArea inputTextArea;

    public TwoEVRPInput(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        inputLabel = new JLabel("Input 2E-VRP");
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inputLabel);

        inputTextArea = new JTextArea();
        inputTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        loadFileButton = new JButton("Load from File");
        loadFileButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(System.getProperty("user.home")));
            fc.setFileFilter(new YamlFileFilter());
            int result = fc.showOpenDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fc.getSelectedFile();
                try {
                    String content = Files.readString(selectedFile.toPath(), Charset.defaultCharset());
                    inputTextArea.setText(content);
                } catch (final Exception exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(Arrays.toString(exception.getStackTrace()));
                    JOptionPane.showMessageDialog(mainFrame, String.format("Failed to open file %s, err: %s", selectedFile.getAbsolutePath(), exception.getMessage()), "error opening file", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(loadFileButton);
        JScrollPane scrollPane1 = new JScrollPane(inputTextArea);
        scrollPane1.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(scrollPane1);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String get2EVRP() {
        return inputTextArea.getText();
    }
}
