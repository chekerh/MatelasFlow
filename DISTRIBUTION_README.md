# 🚀 Guide de Distribution - MatelasPro

## 📦 Création de l'Application Exécutable

### **Étape 1 : Compilation et Packaging**

```bash
# Dans le dossier warehouse-mattress-app
mvn clean package
```

Cela créera plusieurs fichiers dans le dossier `target/` :
- `warehouse-mattress-app-1.0-SNAPSHOT.jar` (JAR standard)
- `warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar` (JAR avec toutes les dépendances)
- `warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar` (JAR complet)

### **Étape 2 : Test du JAR**

```bash
# Windows
java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar

# Linux/Mac
java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar
```

## 🎯 Options de Distribution

### **Option 1 : JAR Exécutable (Recommandée)**

**Avantages :**
- ✅ Simple à distribuer
- ✅ Fonctionne sur tous les systèmes avec Java 17+
- ✅ Contient toutes les dépendances
- ✅ Pas d'installation requise

**Instructions pour l'utilisateur :**
1. Installer Java 17 ou supérieur
2. Double-cliquer sur le fichier JAR ou utiliser la commande `java -jar`
3. Utiliser les scripts `launch.bat` (Windows) ou `launch.sh` (Linux/Mac)

### **Option 2 : Package avec JRE Inclus**

**Utilisation de jlink pour créer un runtime personnalisé :**

```bash
# Créer un runtime Java personnalisé
jlink --module-path $JAVA_HOME/jmods --add-modules java.base,java.desktop,java.sql,java.naming,java.management,java.security.jgss,java.instrument --output runtime

# Créer un script de lancement
echo '#!/bin/bash' > matelaspro
echo 'runtime/bin/java -jar warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar' >> matelaspro
chmod +x matelaspro
```

### **Option 3 : Installateur Windows (Advanced)**

**Utilisation de Launch4j + Inno Setup :**

1. **Launch4j** pour créer un .exe
2. **Inno Setup** pour créer un installateur
3. **NSIS** pour des options avancées

### **Option 4 : Package macOS**

```bash
# Créer un .app bundle
mkdir -p MatelasPro.app/Contents/MacOS
mkdir -p MatelasPro.app/Contents/Resources
cp warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar MatelasPro.app/Contents/Resources/
echo '#!/bin/bash' > MatelasPro.app/Contents/MacOS/MatelasPro
echo 'java -jar "$(dirname "$0")/../Resources/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar"' >> MatelasPro.app/Contents/MacOS/MatelasPro
chmod +x MatelasPro.app/Contents/MacOS/MatelasPro
```

## 📋 Checklist de Distribution

### **✅ Prérequis Système**
- [ ] Java 17 ou supérieur installé
- [ ] Base de données MySQL configurée (optionnel, SQLite par défaut)
- [ ] Permissions d'écriture dans le dossier d'installation

### **✅ Fichiers à Inclure**
- [ ] `warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar`
- [ ] `launch.bat` (Windows)
- [ ] `launch.sh` (Linux/Mac)
- [ ] `README.md` avec instructions
- [ ] `mysql_schema.sql` (si utilisation MySQL)

### **✅ Documentation**
- [ ] Guide d'installation
- [ ] Guide d'utilisation
- [ ] Guide de configuration de la base de données
- [ ] Guide de dépannage

## 🛠️ Scripts de Build Automatisés

### **Script Windows (build.bat)**
```batch
@echo off
echo Building MatelasPro...
mvn clean package
if %errorlevel% equ 0 (
    echo Build successful!
    echo JAR created in target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar
) else (
    echo Build failed!
)
pause
```

### **Script Linux/Mac (build.sh)**
```bash
#!/bin/bash
echo "Building MatelasPro..."
mvn clean package
if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "JAR created in target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar"
else
    echo "Build failed!"
fi
```

## 🎯 Distribution Finale

### **Structure Recommandée :**
```
MatelasPro-Distribution/
├── MatelasPro.jar
├── launch.bat
├── launch.sh
├── README.md
├── INSTALL.md
├── mysql_schema.sql
└── docs/
    ├── UserGuide.pdf
    └── AdminGuide.pdf
```

### **Instructions pour l'Utilisateur Final :**

1. **Installation :**
   - Télécharger le package
   - Extraire dans un dossier
   - Installer Java 17+ si nécessaire

2. **Lancement :**
   - **Windows :** Double-cliquer sur `launch.bat`
   - **Linux/Mac :** Double-cliquer sur `launch.sh` ou `java -jar MatelasPro.jar`

3. **Configuration :**
   - La base de données SQLite se crée automatiquement
   - Pour MySQL, exécuter `mysql_schema.sql`

## 🔧 Dépannage

### **Problèmes Courants :**

1. **"Java not found"**
   - Installer Java 17+
   - Vérifier la variable PATH

2. **"Permission denied"**
   - Donner les permissions d'exécution : `chmod +x launch.sh`

3. **"Database connection failed"**
   - Vérifier la configuration MySQL
   - Utiliser SQLite par défaut

4. **"JavaFX not found"**
   - Le JAR shaded contient déjà JavaFX
   - Vérifier la version de Java

## 🚀 Lancement Rapide

```bash
# 1. Compiler
mvn clean package

# 2. Tester
java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar

# 3. Distribuer
cp target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar MatelasPro.jar
```

**Votre application est maintenant prête pour la distribution !** 🎉 