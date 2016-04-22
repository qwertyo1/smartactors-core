package info.smart_tools.smartactors.core.create_new_instance_strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import org.mdkt.compiler.CompiledCode;
import org.mdkt.compiler.DynamicClassLoader;
import org.mdkt.compiler.SourceCode;
/**
 * Class for compile string with java code to byte code
 * in memory
 */
final class InMemoryCodeCompiler {

    /**
     * System java compiler
     */
    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    /**
     * Constructor - prohibited
     */
    private InMemoryCodeCompiler() {
    }

    /**
     * Compile {@link String} with custom class to java byte code and represent
     * compiled class
     * @param classPath location of classes
     * @param className full name of future class
     * @param sourceCodeInText code source
     * @return compiled class
     * @throws Exception if any errors occurred
     */
    public static Class<?> compile(final String classPath, final String className, final String sourceCodeInText)
            throws Exception {
        try {
            List<String> optionList = new ArrayList<>();
            // set compiler's classpath to be same as the runtime's
            optionList.addAll(Arrays.asList("-classpath", classPath));

            SourceCode sourceCode = new SourceCode(className, sourceCodeInText);
            CompiledCode compiledCode = new CompiledCode(className);
            List compilationUnits = Collections.singletonList(sourceCode);
            DynamicClassLoader cl = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
            ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(
                    javac.getStandardFileManager(null, null, null), compiledCode, cl
            );

            CompilationTask task = javac.getTask(
                    null,
                    fileManager,
                    null,
                    optionList,
                    null,
                    compilationUnits
            );
            task.call();
            return cl.loadClass(className);
        } catch (Error e) {
            throw new Exception(e);
        }
    }
}
