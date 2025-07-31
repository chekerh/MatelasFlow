#!/bin/bash

echo "========================================"
echo "   MatelasPro - Gestion d'Entrepot"
echo "========================================"
echo ""
echo "Lancement de l'application..."
echo ""

# Vérifier si Java est installé
if ! command -v java &> /dev/null; then
    echo "ERREUR: Java n'est pas installé ou n'est pas dans le PATH"
    echo "Veuillez installer Java 17 ou supérieur"
    exit 1
fi

# Vérifier la version de Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERREUR: Java 17 ou supérieur est requis (version actuelle: $JAVA_VERSION)"
    exit 1
fi

# Lancer l'application avec les modules JavaFX
java --module-path "$JAVA_HOME/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -jar target/warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "ERREUR: Impossible de lancer l'application"
    echo "Vérifiez que le fichier JAR existe dans le dossier target/"
    echo "Ou essayez: java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar"
fi 