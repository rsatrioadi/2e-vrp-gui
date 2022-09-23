package nl.tue.vrp.gui;

import nl.tue.vrp.gui.config.AlgorithmConfig;
import nl.tue.vrp.gui.config.GUIConfig;
import nl.tue.vrp.gui.input.code.CodeInput;
import nl.tue.vrp.gui.input.twoevrp.TwoEVRPInput;
import nl.tue.vrp.gui.output.OutputGraph;
import nl.tue.vrp.gui.strategy.CClassLoader;
import nl.tue.vrp.model.Constraints;
import nl.tue.vrp.output.Output;
import nl.tue.vrp.solver.Solver;
import nl.tue.vrp.strategy.packageassignment.PackageAssignment;
import nl.tue.vrp.strategy.packagenodeavailability.PackageNodeAvailability;
import nl.tue.vrp.strategy.packagesearch.PackageSearch;
import nl.tue.vrp.strategy.selectvehicle.SelectVehicle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static final String CustomerPackageAssignmentAlgorithm = "CustomerPackageAssignmentAlgorithm";
    private static final String E2SelectVehicleAlgorithm = "E2SelectVehicleAlgorithm";
    private static final String E2PackageSearchAlgorithm = "E2PackageSearchAlgorithm";
    private static final String SatellitePackageAvailabilityAlgorithm = "SatellitePackageAvailabilityAlgorithm";
    private static final String E1SelectVehicleAlgorithm = "E1SelectVehicleAlgorithm";
    private static final String E1PackageSearchAlgorithm = "E1PackageSearchAlgorithm";

    private static GUIConfig loadConfig() throws FileNotFoundException {
        Yaml yaml = new Yaml(new Constructor(GUIConfig.class));
        return yaml.load(new FileInputStream("config.yaml"));
    }

    public static void main(String[] args) {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            GUIConfig guiConfig = loadConfig();

            JFrame mainFrame = new JFrame("2E-VRP");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JPanel headerPanel = getPanel(Color.RED);
            JPanel footerPanel = getPanel(Color.BLUE);

            TwoEVRPInput twoEVRPInput = new TwoEVRPInput(mainFrame);

            JTabbedPane p3 = new JTabbedPane();

            Map<String, Map<String, String>> algoMap = new HashMap<>();
            Map<String, String> defaultAlgoMap = new HashMap<>();
            for (String algoGroup : guiConfig.getAlgorithmSource().keySet()) {
                Map<String, String> algoContentMap = new HashMap<>();
                for (String algoName : guiConfig.getAlgorithmSource().get(algoGroup).keySet()) {
                    String filePath = guiConfig.getAlgorithmSource().get(algoGroup).get(algoName);
                    try {
                        InputStream inputStream = new FileInputStream(filePath);
                        if (algoName.equals("Custom")) {
                            defaultAlgoMap.put(algoGroup, new BufferedReader(new InputStreamReader(inputStream))
                                    .lines().collect(Collectors.joining("\n")));
                        } else {
                            algoContentMap.put(algoName, new BufferedReader(new InputStreamReader(inputStream))
                                    .lines().collect(Collectors.joining("\n")));
                        }
                    } catch (Exception e) {
                        System.out.printf("file '%s' not found, err: %s \n", filePath, e.getMessage());
                    }
                }
                if (!algoContentMap.isEmpty()) {
                    algoMap.put(algoGroup, algoContentMap);
                }
            }

            Map<String, CodeInput> codeInputMap = new HashMap<>();
            for (AlgorithmConfig algorithmConfig : guiConfig.getAlgorithmConfigs()) {
                Map<String, String> algoContentMap = algoMap.get(algorithmConfig.getAlgorithm());

                CodeInput input = new CodeInput(algorithmConfig.getName(), algoContentMap, defaultAlgoMap.get(algorithmConfig.getAlgorithm()));
                codeInputMap.put(algorithmConfig.getKey(), input);
                input.getPanel().setAlignmentX(Component.LEFT_ALIGNMENT);
                p3.add(algorithmConfig.getName(), input.getPanel());
            }

            GridLayout gridLayout = new GridLayout(1, 2);
            JPanel middlePanel = new JPanel(gridLayout);
            middlePanel.add(twoEVRPInput.getPanel());
            middlePanel.add(p3);

            JButton submitButton = new JButton("run");
            submitButton.addActionListener(e -> {
                String input = twoEVRPInput.get2EVRP();
                Map<String, Object> algoObjMap = new HashMap<>();
                for (Map.Entry<String, CodeInput> entry : codeInputMap.entrySet()) {
                    System.out.println();
                    String name = entry.getValue().getSelectedAlgoName();
                    String code = entry.getValue().getSelectedAlgo();
                    Object obj = CClassLoader.loadFromFile(code, "%s.java".formatted(name), entry.getValue().getSelectedAlgoName());
                    if (entry.getValue().getSelectedAlgo() == null)
                        throw new RuntimeException("NULL object " + entry.getKey());
                    System.out.printf("%s %s %s %s\n", entry.getKey(), entry.getValue().getSelectedAlgoName(), entry.getValue(), obj);
                    algoObjMap.put(entry.getKey(), obj);
                }

                try {
//                Yaml yaml = new Yaml(new Constructor(Output.class));
//                InputStream inputStream = new FileInputStream("res/example2.yml");
//                Output output = yaml.load(inputStream);

                    EnumSet<Constraints> constraints = twoEVRPInput.getConstraints();
                    Solver solver = new Solver(
                            input,
                            constraints,
                            (PackageAssignment) algoObjMap.get(CustomerPackageAssignmentAlgorithm),
                            (SelectVehicle) algoObjMap.get(E2SelectVehicleAlgorithm),
                            (PackageSearch) algoObjMap.get(E2PackageSearchAlgorithm),
                            (PackageNodeAvailability) algoObjMap.get(SatellitePackageAvailabilityAlgorithm),
                            (SelectVehicle) algoObjMap.get(E1SelectVehicleAlgorithm),
                            (PackageSearch) algoObjMap.get(E1PackageSearchAlgorithm)
                    );

                    Output output = solver.getOutput();
                    OutputGraph o = new OutputGraph(output);
                } catch (FileNotFoundException ex) {
                    System.err.println(ex.getMessage());
                    throw new RuntimeException(ex);
                }
            });
            footerPanel.add(submitButton);

            JButton loadOutputButton = new JButton("load output");
            loadOutputButton.addActionListener(e -> {
//            Yaml yaml = new Yaml(new Constructor(Output.class));
//            InputStream inputStream = new FileInputStream("res/example2.yml");
//            Output output = yaml.load(inputStream);
            });
//        footerPanel.add(loadOutputButton);

//        mainFrame.add(headerPanel, BorderLayout.NORTH);
            mainFrame.add(middlePanel, BorderLayout.CENTER);
            mainFrame.add(footerPanel, BorderLayout.SOUTH);
            mainFrame.pack();

            mainFrame.setSize(700, 500);
            mainFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static JPanel getPanel(Color c) {
        JPanel result = new JPanel();
        result.setBorder(BorderFactory.createLineBorder(c));
        return result;
    }

}
