package nl.tue.vrp.gui;

import javax.swing.*;

import edu.uci.ics.jung.visualization.*;
import nl.tue.vrp.gui.filter.YamlFileFilter;
import nl.tue.vrp.gui.input.code.CodeInput;
import nl.tue.vrp.gui.input.twoevrp.TwoEVRPInput;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("2E-VRP");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel headerPanel = getPanel(Color.RED);
        JPanel footerPanel = getPanel(Color.BLUE);

        TwoEVRPInput twoEVRPInput = new TwoEVRPInput(mainFrame);

        JTabbedPane p3 = new JTabbedPane();

        CodeInput input1 = new CodeInput("Algorithm 1", new HashMap<>() {{put("a", "b"); put("b", "c");}}, "default");
        input1.getPanel().setAlignmentX(Component.LEFT_ALIGNMENT);
        p3.add("Algorithm 1", input1.getPanel());
        CodeInput input2 = new CodeInput("Algorithm 2", new HashMap<>() {{put("a", "b"); put("b", "c");}}, "default");
        input2.getPanel().setAlignmentX(Component.LEFT_ALIGNMENT);
        p3.add("Algorithm 2", input2.getPanel());

        GridLayout gridLayout = new GridLayout(1,3);
        JPanel middlePanel = new JPanel(gridLayout);
        middlePanel.add(twoEVRPInput.getPanel());
        middlePanel.add(p3);

        JButton submitButton = new JButton("run");
        submitButton.addActionListener(e -> {
            String input = twoEVRPInput.get2EVRP();

            JFrame j = new JFrame("2E-VRP Result");
            JTextArea tx = new JTextArea();
            tx.setText(input);
            tx.setEnabled(false);
            j.add(tx);
            j.setSize(700,500);
            j.setVisible(true);
            j.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        });
        footerPanel.add(submitButton);

        JButton loadOutputButton = new JButton("load output");
        loadOutputButton.addActionListener(e -> {
        });
        footerPanel.add(loadOutputButton);

        mainFrame.add(headerPanel, BorderLayout.NORTH);
        mainFrame.add(middlePanel, BorderLayout.CENTER);
        mainFrame.add(footerPanel, BorderLayout.SOUTH);
        mainFrame.pack();

        mainFrame.setSize(700,500);
        mainFrame.setVisible(true);
    }
    private static JPanel getPanel(Color c)
    {
        JPanel result = new JPanel();
        result.setBorder(BorderFactory.createLineBorder(c));
        return result;
    }

}
