# 📦 MatelasFlow - Guide d'Installation
**Guide Simple pour l'Installation sur Ordinateur Windows**

---

## ⚡ Installation Rapide (5 Minutes)

### **Étape 1: Installer XAMPP (Base de Données)** ⏱️ 2 min

1. **Télécharger XAMPP**:
   - Aller sur: https://www.apachefriends.org/download.html
   - Cliquer sur "Download" pour Windows
   - Fichier: `xampp-windows-x64-installer.exe` (~150 MB)

2. **Installer XAMPP**:
   - Double-cliquer sur le fichier téléchargé
   - Cliquer sur "Next" plusieurs fois
   - **IMPORTANT**: Installer dans `C:\xampp` (ne pas changer!)
   - Cocher:
     - ✅ Apache
     - ✅ MySQL
     - ✅ phpMyAdmin
   - Décocher le reste
   - Cliquer sur "Next" puis "Finish"

3. **Démarrer MySQL**:
   - Ouvrir "XAMPP Control Panel" (dans le menu Démarrer)
   - Cliquer sur "Start" à côté de "MySQL"
   - Attendre que ça devienne vert avec "Running"
   - ✅ MySQL est maintenant actif!

---

### **Étape 2: Créer la Base de Données** ⏱️ 2 min

1. **Ouvrir phpMyAdmin**:
   - Dans XAMPP Control Panel, cliquer sur "Admin" à côté de MySQL
   - Votre navigateur s'ouvre sur http://localhost/phpmyadmin

2. **Créer la base de données**:
   - Cliquer sur "New" (Nouveau) dans la barre de gauche
   - Nom de la base: `warehouse_db`
   - Cliquer sur "Create" (Créer)

3. **Importer les données**:
   - Cliquer sur `warehouse_db` dans la barre de gauche
   - Cliquer sur l'onglet "Import" en haut
   - Cliquer sur "Choose File" (Choisir un fichier)
   - Sélectionner le fichier `COMPLETE_SCHEMA.sql` (fourni avec l'application)
   - Faire défiler vers le bas et cliquer sur "Go"
   - ✅ Message de succès: "Import has been successfully finished"

---

### **Étape 3: Installer MatelasFlow** ⏱️ 1 min

**Option A: Si vous avez le fichier .exe (Recommandé)**

1. Double-cliquer sur `MatelasFlow-1.0.exe`
2. Suivre l'assistant d'installation:
   - Accepter la licence
   - Choisir l'emplacement (laisser par défaut)
   - Cocher "Create desktop shortcut" (raccourci bureau)
   - Cliquer sur "Install"
3. Cliquer sur "Finish"
4. ✅ Installation terminée!

**Option B: Si vous avez le fichier .jar**

1. Copier le dossier entier `matress` sur votre ordinateur
2. Double-cliquer sur `start-matelasflow.bat`
3. ✅ L'application démarre!

---

### **Étape 4: Premier Lancement** ⏱️ 30 sec

1. **IMPORTANT**: Vérifier que MySQL est démarré
   - Ouvrir XAMPP Control Panel
   - MySQL doit être vert avec "Running"
   - Si non, cliquer sur "Start"

2. **Lancer MatelasFlow**:
   - Double-cliquer sur l'icône sur le bureau
   - OU: Menu Démarrer → MatelasFlow

3. **Se connecter**:
   - Nom d'utilisateur: `admin`
   - Mot de passe: `admin123`
   - Cliquer sur "Login"

4. ✅ **Vous êtes connecté!**

---

## 🔒 Sécurité - À Faire Immédiatement

### **Changer le mot de passe admin**:

1. Dans l'application, cliquer sur "Gestion des Utilisateurs"
2. Sélectionner l'utilisateur "admin"
3. Cliquer sur "Modifier"
4. Entrer un nouveau mot de passe fort
5. Cliquer sur "Enregistrer"
6. ✅ Votre compte est maintenant sécurisé!

### **Créer d'autres utilisateurs**:

1. Aller dans "Gestion des Utilisateurs"
2. Cliquer sur "Ajouter"
3. Remplir les informations:
   - Nom d'utilisateur
   - Mot de passe
   - Rôle (admin ou employé)
4. Cliquer sur "Enregistrer"

---

## ⚙️ Démarrage Automatique de MySQL (Optionnel)

Pour ne pas avoir à démarrer MySQL manuellement à chaque fois:

### **Méthode 1: Configuration XAMPP**

1. Ouvrir XAMPP Control Panel
2. Cliquer sur "Config" (bouton en haut à droite)
3. Cocher "MySQL" dans "Autostart modules"
4. Cliquer sur "Save"
5. ✅ MySQL démarrera automatiquement avec Windows!

### **Méthode 2: Service Windows** (Avancé)

1. Faire un clic droit sur `install-mysql-service.bat`
2. Choisir "Exécuter en tant qu'administrateur"
3. Attendre la fin de l'installation
4. ✅ MySQL est maintenant un service Windows!

---

## 🆘 Problèmes Courants et Solutions

### **Problème 1: "Cannot connect to database"**

**Cause**: MySQL n'est pas démarré

**Solution**:
1. Ouvrir XAMPP Control Panel
2. Cliquer sur "Start" à côté de MySQL
3. Attendre que ça devienne vert
4. Relancer MatelasFlow

---

### **Problème 2: "Port 3306 already in use"**

**Cause**: Un autre MySQL est déjà installé sur l'ordinateur

**Solution Option 1** (Recommandé):
1. Ouvrir le Gestionnaire des tâches (Ctrl+Shift+Esc)
2. Trouver tous les processus "mysqld"
3. Faire clic droit → Arrêter le processus
4. Redémarrer XAMPP MySQL

**Solution Option 2**:
1. Désinstaller l'autre MySQL
2. Redémarrer l'ordinateur
3. Installer XAMPP

---

### **Problème 3: "Application won't start"**

**Cause**: Java n'est pas installé

**Solution**:
1. Télécharger Java depuis: https://adoptium.net/
2. Choisir "JRE 17" ou supérieur
3. Installer Java
4. Redémarrer l'ordinateur
5. Relancer MatelasFlow

---

### **Problème 4: "phpMyAdmin won't open"**

**Cause**: Apache n'est pas démarré

**Solution**:
1. Dans XAMPP Control Panel
2. Cliquer sur "Start" à côté d'Apache
3. Attendre que ça devienne vert
4. Réessayer: http://localhost/phpmyadmin

---

### **Problème 5: "Import failed" lors de l'import SQL**

**Cause**: Fichier SQL corrompu ou trop gros

**Solution**:
1. Vérifier que le fichier `COMPLETE_SCHEMA.sql` n'est pas vide
2. Réessayer l'import
3. Si ça ne marche pas:
   - Ouvrir `COMPLETE_SCHEMA.sql` avec Notepad++
   - Copier tout le contenu
   - Dans phpMyAdmin, onglet "SQL"
   - Coller le contenu
   - Cliquer sur "Go"

---

## 📝 Utilisation Quotidienne

### **Démarrage Normal**:

1. Ouvrir XAMPP Control Panel
2. Démarrer MySQL (si pas en auto-start)
3. Double-cliquer sur MatelasFlow (icône bureau)
4. Se connecter
5. ✅ Prêt à travailler!

### **Fermeture**:

1. Fermer MatelasFlow normalement
2. (Optionnel) Arrêter MySQL dans XAMPP
3. (Optionnel) Fermer XAMPP Control Panel

---

## 💾 Sauvegarde des Données

**IMPORTANT**: Faire des sauvegardes régulières!

### **Méthode Manuelle** (Recommandé chaque semaine):

1. Ouvrir phpMyAdmin
2. Cliquer sur `warehouse_db`
3. Cliquer sur l'onglet "Export"
4. Laisser les options par défaut
5. Cliquer sur "Go"
6. Un fichier `warehouse_db.sql` est téléchargé
7. Sauvegarder ce fichier sur une clé USB ou cloud

### **Restauration d'une Sauvegarde**:

1. Ouvrir phpMyAdmin
2. Cliquer sur `warehouse_db`
3. Onglet "Import"
4. Choisir le fichier de sauvegarde
5. Cliquer sur "Go"
6. ✅ Données restaurées!

---

## 📊 Configuration Système Requise

### **Minimum**:
- Windows 10 ou supérieur
- 4 GB RAM
- 500 MB espace disque
- Connexion Internet (pour installation)

### **Recommandé**:
- Windows 11
- 8 GB RAM
- 1 GB espace disque
- Écran 1920x1080

---

## 📞 Support Technique

Si vous rencontrez des problèmes:

1. **Vérifier cette section de dépannage** ci-dessus
2. **Vérifier les logs**:
   - XAMPP logs: `C:\xampp\mysql\data\mysql_error.log`
   - Application logs: dans le dossier de l'application

3. **Redémarrer tout**:
   - Fermer MatelasFlow
   - Arrêter MySQL dans XAMPP
   - Redémarrer l'ordinateur
   - Réessayer

4. **Contacter le support**: [Vos coordonnées ici]

---

## ✅ Checklist d'Installation

Avant de dire que c'est terminé, vérifier:

- [ ] XAMPP est installé dans `C:\xampp`
- [ ] MySQL démarre et devient vert dans XAMPP
- [ ] Base de données `warehouse_db` est créée
- [ ] Fichier `COMPLETE_SCHEMA.sql` est importé sans erreur
- [ ] MatelasFlow est installé (ou le dossier est copié)
- [ ] L'application se lance sans erreur
- [ ] Login fonctionne avec admin/admin123
- [ ] Mot de passe admin a été changé
- [ ] Premier test de création de matelas fonctionne
- [ ] Première transaction fonctionne
- [ ] Sauvegarde de test effectuée

✅ **Si tout est coché, l'installation est réussie!**

---

## 🎉 Félicitations!

Vous avez installé avec succès MatelasFlow!

**Prochaines étapes**:
1. Créer vos utilisateurs
2. Ajouter vos matelas dans l'inventaire
3. Ajouter vos propriétaires de magasins
4. Commencer à enregistrer vos transactions

**Bon travail!** 🚀

---

*Guide créé pour MatelasFlow v1.0 - SuperMousse 2025*
