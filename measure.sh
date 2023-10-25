#!/bin/bash

# Naviguer vers le dossier du projet JFreeChart
cd src/

# Exécuter les tests et générer le rapport de couverture JaCoCo
mvn clean test jacoco:report

# Utilisation de cloc pour mesurer le nombre de tests par classe/module
cloc --by-file . > cloc_output.txt

# Utilisation de ckjm pour mesurer la complexité cyclomatique (facultatif, mais peut être utile)
java -jar /chemin/vers/ckjm.jar . > ckjm_output.txt
