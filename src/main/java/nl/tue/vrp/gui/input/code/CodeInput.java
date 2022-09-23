package nl.tue.vrp.gui.input.code;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeInput {
    private final String name;
    private final JLabel titleLabel;
    private final JPanel panel;
    private final List<JRadioButton> radioButtonList;
    private final ButtonGroup algoOptionsGroup;
    private final JTextArea textArea;

    public CodeInput(String name, Map<String, String> optionsConfig, String defaultTemplate) {
        this.name = name;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        titleLabel = new JLabel(name);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        algoOptionsGroup = new ButtonGroup();

        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        radioButtonList = new ArrayList<>();
        boolean first = true;
        for (var entry : optionsConfig.entrySet()) {
            JRadioButton option = new JRadioButton(entry.getKey());
            option.setActionCommand(entry.getKey());
            option.setAlignmentX(Component.LEFT_ALIGNMENT);
            radioButtonList.add(option);
            algoOptionsGroup.add(option);
            panel.add(option);
            if (first) {
                option.setSelected(true);
                textArea.setText(entry.getValue());
                textArea.setEditable(false);
                textArea.setBackground(new Color(240, 240, 240));
                first = false;
            }
            option.addActionListener(e -> {
                textArea.setText(entry.getValue());
                textArea.setEditable(false);
                textArea.setBackground(new Color(240, 240, 240));
            });
        }
        JRadioButton customOption = new JRadioButton("Custom");
        customOption.setActionCommand("Custom");
        customOption.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioButtonList.add(customOption);
        algoOptionsGroup.add(customOption);
        customOption.addActionListener(e -> {
            textArea.setText(defaultTemplate);
            textArea.setEditable(true);
            textArea.setBackground(Color.WHITE);
        });
        panel.add(customOption);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollPane);
    }

    public String getName() {
        return name;
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getSelectedAlgoName() {
        return algoOptionsGroup.getSelection().getActionCommand();
    }

    public String getSelectedAlgo() {
        return textArea.getText();
    }
}
