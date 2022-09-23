package nl.tue.vrp.gui.config;

import java.util.ArrayList;
import java.util.SortedMap;

public class GUIConfig {
    private SortedMap<String, SortedMap<String, String>> algorithmSource;
    private ArrayList<AlgorithmConfig> algorithmConfigs;

    public GUIConfig() {
    }

    public SortedMap<String, SortedMap<String, String>> getAlgorithmSource() {
        return algorithmSource;
    }

    public void setAlgorithmSource(SortedMap<String, SortedMap<String, String>> algorithmSource) {
        this.algorithmSource = algorithmSource;
    }

    public ArrayList<AlgorithmConfig> getAlgorithmConfigs() {
        return algorithmConfigs;
    }

    public void setAlgorithmConfigs(ArrayList<AlgorithmConfig> algorithmConfigs) {
        this.algorithmConfigs = algorithmConfigs;
    }
}
