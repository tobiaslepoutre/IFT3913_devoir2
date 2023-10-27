package org.example;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: Main <path_to_directory>");
            return;
        }
        File directory = new File(args[0]);
        if (!directory.isDirectory()) {
            System.out.println("The provided path is not a directory.");
            return;
        }

        TDRCalculator tdrc = new TDRCalculator();
        TDSTCalculator tdstc = new TDSTCalculator();
        double tdst = tdstc.getTDSD(directory);
        double tdr = tdrc.getTDR(directory);
        System.out.printf("Taux de méthodes avec javaDoc pour tous les fichiers de " +
                "test du répertoire '%s' : %.2f%%\n", directory.getName(), tdst * 100);
        System.out.printf("Taux de méthode non void avec documentation @return " +
                "pour tous les fichiers de test du répertoire '%s' : %.2f%%", directory.getName(), tdr * 100);
    }
}
