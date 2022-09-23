package nl.tue.vrp.gui.output;

import nl.tue.vrp.output.Output;

import javax.swing.*;
import java.awt.*;

public class OutputGraph {

    public OutputGraph(Output output) {
        JFrame frame = new JFrame("2E-VRP Result");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        OutputGraphPanel graphPanel = new OutputGraphPanel(output);
        JPanel listPanel = new OutputTreeListPanel(output, e -> {
            graphPanel.highlight(e);
            return null;
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 2;
        gbc1.weighty = 1;
        frame.add(graphPanel, gbc1);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.weightx = 1;
        gbc2.weighty = 1;
        frame.add(listPanel, gbc2);
        frame.pack();

        frame.setSize(1000, 600);
        frame.setVisible(true);
    }
}
