import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class CodeCoverageCalculator {

    /**
     * Calculate the estimated code coverage based on method names.
     *
     * @param codeFile The Java file with the main code.
     * @param testFile The Java file with the tests.
     * @return The estimated coverage percentage.
     * @throws FileNotFoundException If one of the files is not found.
     */
    public static double calculateCoverage(File codeFile, File testFile) throws FileNotFoundException {
        CompilationUnit codeCU = parseJavaFile(codeFile);
        CompilationUnit testCU = parseJavaFile(testFile);

        List<MethodDeclaration> codeMethods = codeCU.findAll(MethodDeclaration.class);
        List<MethodDeclaration> testMethods = testCU.findAll(MethodDeclaration.class);

        int matchingMethods = 0;
        
        for (MethodDeclaration codeMethod : codeMethods) {
            for (MethodDeclaration testMethod : testMethods) {
                if (isTestMethodOf(codeMethod, testMethod)) {
                    matchingMethods++;
                    break;
                }
            }
        }

        return ((double) matchingMethods / codeMethods.size()) * 100;
    }

    /**
     * Check if the test method is likely testing the given code method.
     *
     * @param codeMethod The method from the main code.
     * @param testMethod The method from the test code.
     * @return true if the test method name contains the code method name, false otherwise.
     */
    private static boolean isTestMethodOf(MethodDeclaration codeMethod, MethodDeclaration testMethod) {
        return testMethod.getNameAsString().toLowerCase().contains(codeMethod.getNameAsString().toLowerCase());
    }

    /**
     * Load and parse a Java file into a CompilationUnit for further analysis.
     *
     * @param file The Java file to be parsed.
     * @return The CompilationUnit representing the structure of the Java file.
     * @throws FileNotFoundException If the file is not found.
     */
    private static CompilationUnit parseJavaFile(File file) throws FileNotFoundException {
    	JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(file);

        if (result.isSuccessful()) {
            return result.getResult().orElse(null);
        } else {
            // Gérer l'erreur de parsing ici si nécessaire.
            return null;
        }
    }
    
    public static void main(String[] args) {

        File sourceDirectory = new File("jfreechart/src/main/java/org/jfree/chart/title");
        File testDirectory = new File("jfreechart/src/test/java/org/jfree/chart/title");

        if (!sourceDirectory.isDirectory() || !testDirectory.isDirectory()) {
            System.out.println("Veuillez fournir des répertoires valides." + testDirectory);
            return;
        }

        File[] sourceFiles = sourceDirectory.listFiles((dir, name) -> name.endsWith(".java") && !name.endsWith("Test.java"));
        
        for (File sourceFile : sourceFiles) {
            String testName = sourceFile.getName().replace(".java", "Test.java");
            File testFile = new File(testDirectory, testName);
            
            if (testFile.exists()) {
                try {
                    double coverage = calculateCoverage(sourceFile, testFile);
                    System.out.println(sourceFile.getName() + " has a coverage of " + (coverage) + "% by " + testName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
