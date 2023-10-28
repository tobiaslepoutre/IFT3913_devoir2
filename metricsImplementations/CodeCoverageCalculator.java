import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class CodeCoverageCalculator {

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

    private static boolean isTestMethodOf(MethodDeclaration codeMethod, MethodDeclaration testMethod) {
        return testMethod.getNameAsString().toLowerCase().contains(codeMethod.getNameAsString().toLowerCase());
    }

    private static CompilationUnit parseJavaFile(File file) throws FileNotFoundException {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(file);

        if (result.isSuccessful()) {
            return result.getResult().orElse(null);
        } else {
            return null;
        }
    }

    public static void checkCoverageInDirectory(File sourceDirectory, File testDirectory) {
        File[] sourceFiles = sourceDirectory.listFiles();

        if (sourceFiles != null) {
            for (File sourceFile : sourceFiles) {
                if (sourceFile.isDirectory()) {
                    checkCoverageInDirectory(sourceFile, new File(testDirectory, sourceFile.getName()));
                } else if (sourceFile.getName().endsWith(".java") && !sourceFile.getName().endsWith("Test.java")) {
                    String testName = sourceFile.getName().replace(".java", "Test.java");
                    File testFile = new File(testDirectory, testName);
                    if (testFile.exists()) {
                        try {
                            double coverage = calculateCoverage(sourceFile, testFile);
                            System.out.println(sourceFile.getName() + " has a coverage of " + coverage + "% by " + testName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        File sourceDirectory = new File("src/jfreechart/src/main/java/org/jfree");
        File testDirectory = new File("src/jfreechart/src/test/java/org/jfree");



        checkCoverageInDirectory(sourceDirectory, testDirectory);
    }
}
