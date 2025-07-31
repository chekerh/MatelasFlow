#!/bin/bash

echo "========================================"
echo "   Build MatelasPro - Gestion d'Entrepot"
echo "========================================"
echo ""

echo "Compilation et packaging..."
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build réussi !"
    echo ""
    echo "Fichiers créés dans target/ :"
    echo "- warehouse-mattress-app-1.0-SNAPSHOT.jar"
    echo "- warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar (RECOMMANDÉ)"
    echo "- warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar"
    echo ""
    echo "Pour tester l'application :"
    echo "java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar"
    echo ""
    echo "Ou utiliser le script : ./launch.sh"
else
    echo ""
    echo "❌ Build échoué !"
    echo "Vérifiez les erreurs ci-dessus."
fi 