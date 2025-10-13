# 📦 MatelasPro - Guide d'Installation Client

## 🎯 Guide Complet pour STE Habiba

Ce guide explique comment installer et configurer MatelasPro sur l'ordinateur du client.

---

## 📋 Prérequis

### **Logiciels Nécessaires**

1. **Java JDK 17 ou supérieur**
   - Télécharger: https://adoptium.net/
   - Version recommandée: Eclipse Temurin JDK 17

2. **XAMPP (Apache + MySQL)**
   - Télécharger: https://www.apachefriends.org/
   - Version recommandée: XAMPP 8.0 ou supérieur

3. **Maven (optionnel, pour recompiler)**
   - Télécharger: https://maven.apache.org/download.cgi
   - Seulement nécessaire si vous voulez modifier le code

---

## 🚀 Installation Étape par Étape

### **Étape 1: Installer Java**

1. Téléchargez et installez Java JDK 17
2. Vérifiez l'installation:
   ```cmd
   java -version
   ```
   Doit afficher: `openjdk version "17.x.x"`

3. Si la commande ne fonctionne pas:
   - Ajoutez Java au PATH système
   - Redémarrez l'ordinateur

### **Étape 2: Installer XAMPP**

1. Téléchargez XAMPP depuis https://www.apachefriends.org/
2. Installez dans: `C:\xampp` (chemin par défaut recommandé)
3. Lancez XAMPP Control Panel
4. Démarrez **Apache** et **MySQL**
5. Vérifiez:
   - Apache: Ouvrez http://localhost/ dans le navigateur
   - MySQL: Le service doit être "Running" (vert)

### **Étape 3: Configurer la Base de Données**

1. Ouvrez phpMyAdmin: http://localhost/phpmyadmin/
2. Créez une nouvelle base de données:
   - Nom: `warehouse_db`
   - Encodage: `utf8mb4_general_ci`

3. Importez le schéma (si fourni):
   - Cliquez sur "Import"
   - Sélectionnez le fichier `warehouse_schema.sql`
   - Cliquez "Go"

4. Créez un utilisateur admin:
   ```sql
   INSERT INTO users (username, password, role) 
   VALUES ('admin', '$2a$10$XB0EZLlBr2f1OT5g8jMvueOzUvWKCK8qvAaLkFPQhCN8y7XQhVUbm', 'admin');
   ```
   - Username: `admin`
   - Password: `admin123`

### **Étape 4: Installer MatelasPro**

1. **Copiez le dossier complet** `matress` sur le Bureau:
   ```
   C:\Users\[NomUtilisateur]\Desktop\matress\
   ```

2. **Structure du dossier**:
   ```
   matress/
   ├── target/
   │   └── warehouse-mattress-app-1.0-SNAPSHOT.jar
   ├── src/
   ├── docs/
   ├── start-matelas-pro.bat      ← Script de lancement
   ├── MatelasPro-Launcher.vbs    ← Raccourci sans console
   ├── pom.xml
   └── README.md
   ```

3. **Vérifiez que le fichier JAR existe**:
   ```
   matress\target\warehouse-mattress-app-1.0-SNAPSHOT.jar
   ```

### **Étape 5: Créer un Raccourci Bureau**

#### **Méthode 1: Raccourci Direct (Simple)**

1. Clic droit sur `MatelasPro-Launcher.vbs`
2. Cliquez "Créer un raccourci"
3. Déplacez le raccourci sur le Bureau
4. Renommez: "MatelasPro"
5. Changez l'icône (optionnel):
   - Clic droit → Propriétés → Changer l'icône
   - Sélectionnez une icône système ou personnalisée

#### **Méthode 2: Raccourci avec Icône Personnalisée**

1. Clic droit sur le Bureau → Nouveau → Raccourci
2. Emplacement:
   ```
   wscript.exe "C:\Users\Dell\Desktop\matress\MatelasPro-Launcher.vbs"
   ```
3. Nom: `MatelasPro`
4. Clic droit sur le raccourci → Propriétés
5. Changer l'icône → Parcourir vers une icône .ico

#### **Méthode 3: Raccourci Avancé (Batch Visible)**

Si vous voulez voir les logs de démarrage:
1. Clic droit sur le Bureau → Nouveau → Raccourci
2. Emplacement:
   ```
   C:\Users\Dell\Desktop\matress\start-matelas-pro.bat
   ```
3. Nom: `MatelasPro (avec logs)`

---

## ⚙️ Configuration

### **Fichier de Configuration: application.properties**

Emplacement: `src/main/resources/application.properties`

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/warehouse_db
db.username=root
db.password=

# Firebase Configuration (optionnel)
firebase.enabled=true
firebase.databaseURL=https://your-project.firebaseio.com
```

### **Modifier la Configuration**

Si votre MySQL a un mot de passe:
1. Ouvrez `application.properties`
2. Changez `db.password=` en `db.password=VotreMotDePasse`
3. Recompilez: `mvn clean package`

---

## 🎮 Utilisation

### **Démarrage Normal**

1. **Double-cliquez sur l'icône MatelasPro** sur le Bureau
2. Le script va automatiquement:
   - ✅ Démarrer Apache (si non démarré)
   - ✅ Démarrer MySQL (si non démarré)
   - ✅ Lancer MatelasPro
3. L'application s'ouvre en plein écran

### **Connexion**

- **Utilisateur**: `admin`
- **Mot de passe**: `admin123`
- Appuyez sur **Entrée** pour vous connecter

### **Arrêt**

- Cliquez sur "🚪 Déconnexion" dans l'application
- Fermez la fenêtre

**Important**: XAMPP reste actif en arrière-plan. Pour l'arrêter:
- Ouvrez XAMPP Control Panel
- Cliquez "Stop" sur Apache et MySQL

---

## 🔧 Dépannage

### **Problème: "Java n'est pas reconnu"**

**Solution**:
1. Vérifiez Java installé: `java -version`
2. Ajoutez Java au PATH:
   - Panneau de configuration → Système → Variables d'environnement
   - Ajoutez `C:\Program Files\Java\jdk-17\bin` au PATH
3. Redémarrez l'ordinateur

### **Problème: "Apache ne démarre pas"**

**Causes possibles**:
- Port 80 déjà utilisé (Skype, IIS, etc.)

**Solution**:
1. Ouvrez XAMPP Control Panel
2. Cliquez "Config" → "Apache (httpd.conf)"
3. Changez:
   ```
   Listen 80
   ServerName localhost:80
   ```
   En:
   ```
   Listen 8080
   ServerName localhost:8080
   ```
4. Redémarrez Apache

### **Problème: "MySQL ne démarre pas"**

**Causes possibles**:
- Port 3306 déjà utilisé

**Solution**:
1. Ouvrez XAMPP Control Panel
2. Cliquez "Config" → "my.ini"
3. Changez:
   ```
   port=3306
   ```
   En:
   ```
   port=3307
   ```
4. Mettez à jour `application.properties`:
   ```
   db.url=jdbc:mysql://localhost:3307/warehouse_db
   ```
5. Recompilez

### **Problème: "Impossible de se connecter à la base de données"**

**Solution**:
1. Vérifiez MySQL est démarré (XAMPP Control Panel)
2. Vérifiez le mot de passe MySQL dans `application.properties`
3. Vérifiez la base de données existe:
   - Ouvrez http://localhost/phpmyadmin/
   - La base `warehouse_db` doit exister

### **Problème: "L'application ne démarre pas"**

**Solution**:
1. Vérifiez le fichier JAR existe:
   ```
   matress\target\warehouse-mattress-app-1.0-SNAPSHOT.jar
   ```
2. Si absent, recompilez:
   ```cmd
   cd C:\Users\Dell\Desktop\matress
   mvn clean package
   ```
3. Vérifiez les logs dans la console

---

## 📂 Structure des Fichiers

```
matress/
├── src/                           # Code source
│   ├── main/
│   │   ├── java/                  # Fichiers Java
│   │   │   └── com/warehouse/
│   │   │       ├── controller/    # Contrôleurs
│   │   │       ├── model/         # Modèles de données
│   │   │       ├── util/          # Utilitaires
│   │   │       └── Main.java      # Point d'entrée
│   │   └── resources/
│   │       ├── css/               # Styles CSS
│   │       ├── fxml/              # Interfaces
│   │       ├── images/            # Images/Logos
│   │       └── application.properties
│   └── test/                      # Tests (optionnel)
│
├── target/                        # Fichiers compilés
│   └── warehouse-mattress-app-1.0-SNAPSHOT.jar  ← Application
│
├── docs/                          # Documentation
│   ├── ALL_FIXES_AND_IMPROVEMENTS.md
│   ├── INSTALLATION_CLIENT.md     ← Ce fichier
│   └── ...
│
├── start-matelas-pro.bat          # Script de lancement Windows
├── MatelasPro-Launcher.vbs        # Lanceur sans console
├── pom.xml                        # Configuration Maven
└── README.md                      # Lisez-moi principal
```

---

## 🔒 Sécurité

### **Bonnes Pratiques**

1. **Changez le mot de passe admin**:
   - Connectez-vous en tant qu'admin
   - Allez dans "👤 Utilisateurs"
   - Modifiez le mot de passe

2. **Sauvegarde régulière**:
   - Base de données: phpMyAdmin → Export
   - Fichiers: Copiez le dossier `matress/`

3. **Accès réseau**:
   - Par défaut, XAMPP n'est accessible que localement
   - Ne pas exposer MySQL au réseau public

4. **Permissions**:
   - Seul l'admin peut accéder aux fonctions sensibles
   - Les employés ont un accès limité

---

## 📊 Génération de Rapports

Les rapports PDF sont automatiquement enregistrés dans:
```
C:\Users\[NomUtilisateur]\Desktop\les raports de STE Habiba\
```

Types de rapports disponibles:
- 📅 Rapports quotidiens
- 📊 Statistiques mensuelles
- 📦 État des stocks
- 💰 Transactions

---

## 🆘 Support

### **En cas de problème**

1. **Consultez la documentation** dans `docs/`
2. **Vérifiez les logs** XAMPP:
   - `C:\xampp\apache\logs\error.log`
   - `C:\xampp\mysql\data\[hostname].err`
3. **Contactez le support technique**

### **Fichiers de Log**

- Application: `activity_log.txt`
- Apache: `C:\xampp\apache\logs\error.log`
- MySQL: `C:\xampp\mysql\data\*.err`

---

## ✅ Checklist d'Installation

- [ ] Java JDK 17 installé et dans le PATH
- [ ] XAMPP installé dans `C:\xampp`
- [ ] Apache et MySQL démarrés
- [ ] Base de données `warehouse_db` créée
- [ ] Utilisateur admin créé
- [ ] Dossier `matress` copié sur le Bureau
- [ ] Fichier JAR existe dans `target/`
- [ ] Raccourci Bureau créé
- [ ] Application démarre correctement
- [ ] Connexion admin fonctionne
- [ ] XAMPP démarre automatiquement avec l'app

---

## 🎉 Félicitations!

MatelasPro est maintenant installé et prêt à l'emploi!

**Premier lancement**:
1. Double-cliquez sur l'icône MatelasPro
2. Connectez-vous avec `admin` / `admin123`
3. Explorez les fonctionnalités

**Profitez de votre nouveau système de gestion!** 🚀

---

*Installation Guide - MatelasPro v1.0*  
*© 2025 STE Habiba - Tous droits réservés*
