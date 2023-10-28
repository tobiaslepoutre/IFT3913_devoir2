import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestCounter {

    public static int countTestsInFile(File testFile) throws IOException {
        CompilationUnit cu = parseJavaFile(testFile);

        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        int count = 0;

        for (MethodDeclaration method : methods) {
            for (AnnotationExpr annotation : method.getAnnotations()) {
                if (annotation.getNameAsString().equals("Test")) {
                    count++;
                }
            }
        }

        return count;
    }

    private static CompilationUnit parseJavaFile(File file) throws IOException {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
        return parseResult.getResult().orElseThrow(() -> new IllegalArgumentException("Failed to parse file: " + file));
    }

    public static void searchForTestFiles(File directory) {
        File[] allFiles = directory.listFiles();
        if (allFiles == null) return;

        for (File file : allFiles) {
            if (file.isDirectory()) {
                searchForTestFiles(file); // Recursive call
            } else if (file.getName().endsWith("Test.java")) {
                try {
                    int numberOfTests = countTestsInFile(file);
                    System.out.println(file.getName() + " has " + numberOfTests + " test(s).");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        File rootDirectory = new File("."); // Current directory

        searchForTestFiles(rootDirectory);
    }
}