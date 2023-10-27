package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TDRCalculator {

    public double getTDR(File directory) throws IOException {
        ArrayList<Long> list = new ArrayList<>(Arrays.asList(0L, 0L));
        getTDRFromTestFiles(directory, list);
        return (double) list.get(0) / list.get(1);
    }

    private void getTDRFromTestFiles(File directory, ArrayList<Long> valuesList) throws IOException {
        File[] files = directory.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                getTDRFromTestFiles(file, valuesList);
            }
            else if (file.getName().endsWith("Test.java")) {
                long[] values = analyzeFile(file);
                valuesList.set(0, valuesList.get(0) + values[0]);
                valuesList.set(1, valuesList.get(1) + values[1]);
            }
        }
    }

    private long[] analyzeFile(File file) throws IOException {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
        if (!parseResult.isSuccessful()) {
            throw new RuntimeException("Failed to parse the Java file: " + file.getAbsolutePath());
        }
        CompilationUnit compilationUnit = parseResult.getResult().orElseThrow(() ->
                new RuntimeException("Failed to get the CompilationUnit from: " + file.getAbsolutePath()));

        List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);

        long totalMethodsNotVoid = 0;
        long methodsWithReturnAndJavadoc = 0;
        for (MethodDeclaration method : methods) {
            if (!method.getType().asString().equals("void")) {
                totalMethodsNotVoid++;
                if (method.getJavadoc().isPresent() && method.getJavadoc().get().toText().contains("@return")) {
                    methodsWithReturnAndJavadoc++;
                }
            }
        }
        return new long[]{methodsWithReturnAndJavadoc, totalMethodsNotVoid};
    }
}

