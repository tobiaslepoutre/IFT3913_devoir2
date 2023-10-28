package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CycloCompCalculator {

    public double getCycloComp(File directory) throws IOException {
        HashMap<Integer, Number> map = new HashMap<>();
        map.put(0, 0.0);
        map.put(1, 0L);
        getCycloCompFromTestFiles(directory, map);
        return (double) map.get(0) / (long) map.get(1);
    }

    private void getCycloCompFromTestFiles(File directory, HashMap<Integer, Number> valuesMap) throws IOException {
        File[] files = directory.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                getCycloCompFromTestFiles(file, valuesMap);
            }
            else if (file.getName().endsWith("Test.java")) {
                HashMap<Integer, Number> values = analyzeFile(file);
                valuesMap.put(0, (double) valuesMap.get(0) + (double) values.get(0));
                valuesMap.put(1, (long) valuesMap.get(1) + (long) values.get(1));
            }
        }
    }

    public HashMap<Integer, Number> analyzeFile(File file) throws IOException {
        ParseResult<CompilationUnit> parseResult = new JavaParser().parse(file);
        if (!parseResult.isSuccessful()) {
            throw new RuntimeException("Failed to parse the Java file: " + file.getAbsolutePath());
        }
        CompilationUnit cu = parseResult.getResult().orElseThrow(() ->
                new RuntimeException("Failed to get the CompilationUnit from: " + file.getAbsolutePath()));

        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        long totalComplexity = 0;
        for (MethodDeclaration method : methods) {
            totalComplexity += calculateForMethod(method);
        }
        HashMap<Integer, Number> values = new HashMap<>();
        double fileComplexity = methods.isEmpty() ? 0 : (double) totalComplexity / methods.size();
        values.put(0, fileComplexity);
        values.put(1, (long) methods.size());
        return values;
    }

    public long calculateForMethod(MethodDeclaration method) {
        long complexity = 1;
        complexity += method.findAll(IfStmt.class).size();
        complexity += method.findAll(ForStmt.class).size();
        complexity += method.findAll(WhileStmt.class).size();
        complexity += method.findAll(DoStmt.class).size();
        complexity += method.findAll(SwitchStmt.class).size();
        complexity += method.findAll(CatchClause.class).size();
        return complexity;
    }
}

