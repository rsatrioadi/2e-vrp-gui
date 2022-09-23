package nl.tue.vrp.gui.input.twoevrp;

import nl.tue.vrp.gui.filter.YamlFileFilter;
import nl.tue.vrp.model.Constraints;
import nl.tue.vrp.model.ConstraintsBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.EnumSet;

public class TwoEVRPInput {
    private final JPanel panel;
    private final JButton loadFileButton;

    private final JButton visualizeButton = null; // TODO
    private final JLabel inputLabel;
    private final JTextArea inputTextArea;

    private final JCheckBox box1;
    private final JCheckBox box2;
    private final JCheckBox box3;

    public TwoEVRPInput(JFrame mainFrame) {
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        inputLabel = new JLabel("Input 2E-VRP");
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inputLabel);

        box1 = new JCheckBox("capacity");
        box1.setSelected(true);
        box2 = new JCheckBox("time");
        box2.setSelected(true);
        box3 = new JCheckBox("fuel");
        panel.add(box1);
        panel.add(box2);
        panel.add(box3);

        inputTextArea = new JTextArea();
        inputTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        loadFileButton = new JButton("Load from File");
        loadFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
//            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(new YamlFileFilter());
            int result = fileChooser.showOpenDialog(mainFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
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

    public EnumSet<Constraints> getConstraints() {
        ConstraintsBuilder builder = new ConstraintsBuilder();
        if (box1.isSelected()) builder.addCapacityCheck();
        if (box2.isSelected()) builder.addTimeCheck();
        if (box3.isSelected()) builder.addFuelCheck();
        return builder.get();
    }
}
