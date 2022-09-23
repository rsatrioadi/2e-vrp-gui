package nl.tue.vrp.gui.filter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class YamlFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        return f.isFile()
                && (f.getName().endsWith(".yaml") || f.getName().endsWith(".yml"));
    }

    @Override
    public String getDescription() {
        return "YAML file (*.yaml, *.yml)";
    }
}
