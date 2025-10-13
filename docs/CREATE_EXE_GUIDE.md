# 🚀 Guide de Création de l'Exécutable MatelasPro

## 📋 **Options Disponibles**

### **Option 1 : jpackage (Recommandée - Java 14+)**

**Avantages :**
- ✅ Intégré à Java (pas d'outil externe)
- ✅ Crée un installateur professionnel
- ✅ Gère automatiquement les dépendances
- ✅ Supporte les icônes et métadonnées

**Instructions :**

1. **Vérifier Java 14+ :**
   ```bash
   java -version
   jpackage --version
   ```

2. **Compiler l'application :**
   ```bash
   mvn clean package
   ```

3. **Créer l'exe :**
   ```bash
   # Utiliser le script automatique
   create-exe.bat
   
   # Ou manuellement :
   jpackage --input target --name "MatelasPro" --main-jar warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar --main-class com.warehouse.App --type exe --dest . --java-options "--module-path %JAVA_HOME%\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base" --java-options "-Dfile.encoding=UTF-8" --java-options "-Xmx1024m" --java-options "-Xms256m" --app-version "1.0.0" --vendor "MatelasPro" --description "Gestion d'Entrepot de Matelas" --win-dir-chooser --win-menu --win-shortcut
   ```

### **Option 2 : Launch4j (Alternative)**

**Avantages :**
- ✅ Simple à utiliser
- ✅ Interface graphique
- ✅ Configuration flexible
- ✅ Compatible avec toutes les versions de Java

**Instructions :**

1. **Télécharger Launch4j :**
   - Allez sur : https://launch4j.sourceforge.net/
   - Téléchargez la dernière version
   - Extrayez le fichier ZIP

2. **Compiler l'application :**
   ```bash
   mvn clean package
   ```

3. **Créer l'exe :**
   - Ouvrez Launch4j
   - Chargez le fichier `launch4j-config.xml`
   - Cliquez sur "Build wrapper"
   - L'exe sera créé : `MatelasPro.exe`

### **Option 3 : Maven Plugin (Automatique)**

**Avantages :**
- ✅ Intégré au build Maven
- ✅ Automatique
- ✅ Configuration dans pom.xml

**Instructions :**

1. **Ajouter le plugin au pom.xml (déjà fait)**
2. **Créer l'exe :**
   ```bash
   mvn clean package jpackage:jpackage
   ```

## 🎯 **Méthode Recommandée : jpackage**

### **Étape 1 : Préparation**
```bash
# Vérifier Java 14+
java -version
jpackage --version

# Compiler l'application
mvn clean package
```

### **Étape 2 : Création de l'Exe**
```bash
# Utiliser le script automatique
create-exe.bat

# Ou manuellement :
jpackage --input target --name "MatelasPro" --main-jar warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar --main-class com.warehouse.App --type exe --dest . --java-options "--module-path %JAVA_HOME%\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base" --java-options "-Dfile.encoding=UTF-8" --java-options "-Xmx1024m" --java-options "-Xms256m" --app-version "1.0.0" --vendor "MatelasPro" --description "Gestion d'Entrepot de Matelas" --win-dir-chooser --win-menu --win-shortcut
```

### **Étape 3 : Résultat**
- **Fichier créé :** `MatelasPro-1.0.exe`
- **Taille :** ~50-100 MB (avec JRE inclus)
- **Installation :** Double-clic pour installer
- **Désinstallation :** Via "Ajouter/Supprimer des programmes"

## 🔧 **Dépannage**

### **Problème : "jpackage not found"**
```bash
# Installer Java 14+ depuis :
# https://adoptium.net/
# ou
# https://www.oracle.com/java/technologies/downloads/
```

### **Problème : "JavaFX modules missing"**
```bash
# Vérifier que les modules JavaFX sont inclus
java --list-modules | grep javafx
```

### **Problème : "Permission denied"**
```bash
# Exécuter en tant qu'administrateur
# Ou utiliser Launch4j comme alternative
```

## 📦 **Distribution Finale**

### **Structure Recommandée :**
```
MatelasPro-Installation/
├── MatelasPro-1.0.exe
├── README.md
├── INSTALL.md
├── mysql_schema.sql
└── docs/
    ├── UserGuide.pdf
    └── AdminGuide.pdf
```

### **Instructions pour l'Utilisateur Final :**

1. **Installation :**
   - Double-cliquer sur `MatelasPro-1.0.exe`
   - Suivre l'assistant d'installation
   - Choisir le dossier d'installation

2. **Lancement :**
   - Raccourci sur le bureau
   - Menu Démarrer
   - Dossier d'installation

3. **Configuration :**
   - La base de données se crée automatiquement
   - Aucune configuration requise

## 🎉 **Avantages de l'Exe**

✅ **Installation professionnelle**  
✅ **Raccourcis automatiques**  
✅ **Désinstallation propre**  
✅ **Pas de Java requis** (JRE inclus)  
✅ **Interface native Windows**  
✅ **Gestion des mises à jour**  

**Votre application est maintenant prête pour une distribution professionnelle !** 🚀 