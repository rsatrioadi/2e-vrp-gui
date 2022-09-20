package nl.tue.vrp.gui.strategy;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CClassLoader {
    public static Object loadFromFile(String code, String filename, String className) {
        Object ret = null;

//        StringBuilder sb = new StringBuilder(64);
//        sb.append("package tmp;\n");
//        sb.append("import nl.tue.vrp.strategy.packagesearch.PackageSearch;\n");
//        sb.append("import nl.tue.vrp.model.PackageAvailability;\n");
//        sb.append("import nl.tue.vrp.model.Visit;\n");
//        sb.append("import java.util.List;\n");
//        sb.append("public class HelloWorld implements PackageSearch {\n");
//        sb.append("    @Override\n");
//        sb.append("    public PackageAvailability getNextPackage(Visit lastVisit, List<PackageAvailability> packages) {\n");
//        sb.append("        System.out.println(\"Hello world\");\n");
//        sb.append("        return null;\n");
//        sb.append("    }\n");
//        sb.append("}\n");

        File classJava = new File("tmp/" + filename);
        if (classJava.getParentFile().exists() || classJava.getParentFile().mkdirs()) {
            try {
                Writer writer = null;
                try {
                    writer = new FileWriter(classJava);
                    writer.write(code);
                    writer.flush();
                } finally {
                    try {
                        writer.close();
                    } catch (Exception e) {
                    }
                }

                /** Compilation Requirements *********************************************************************************************/
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

//                System.out.println(System.getProperty("java.class.path"));
//                System.out.println(System.getProperty("jdk.module.path"));

                // This sets up the class path that the compiler will use.
                // I've added the .jar file that contains the DoStuff interface within in it...
                List<String> optionList = new ArrayList<>();
                optionList.add("-classpath");
                optionList.add(System.getProperty("java.class.path")+ File.pathSeparator + System.getProperty("jdk.module.path"));

                Iterable<? extends JavaFileObject> compilationUnit
                        = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(classJava));
                JavaCompiler.CompilationTask task = compiler.getTask(
                        null,
                        fileManager,
                        diagnostics,
                        optionList,
                        null,
                        compilationUnit);
                /********************************************************************************************* Compilation Requirements **/
                if (task.call()) {
                    /** Load and execute *************************************************************************************************/
//                    System.out.println("Yipe");
                    // Create a new custom class loader, pointing to the directory that contains the compiled
                    // classes, this should point to the top of the package structure!
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
                    // Load the class from the classloader by name....
                    Class<?> loadedClass = classLoader.loadClass("tmp." + className);
                    // Create a new instance...
                    ret = loadedClass.getDeclaredConstructor().newInstance();
                    // Santity check
//                    if (obj instanceof DoStuff) {
//                        // Cast to the DoStuff interface
//                        DoStuff stuffToDo = (DoStuff) obj;
//                        // Run it baby
//                        stuffToDo.doStuff();
//                    }
                    /************************************************************************************************* Load and execute **/
                } else {
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        System.out.format("Error on line %d in %s: %s%n",
                                diagnostic.getLineNumber(),
                                diagnostic.getSource().toUri(), diagnostic.getMessage(Locale.US));
                    }
                }
                fileManager.close();
            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exp) {
                exp.printStackTrace();
            } catch (InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }
}
