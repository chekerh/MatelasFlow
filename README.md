# 🏪 Système de Gestion d'Entrepôt de Matelas

Un système de gestion d'entrepôt moderne et intuitif développé en JavaFX pour la gestion des matelas, transactions, et utilisateurs.

## ✨ Fonctionnalités

### 🔐 Authentification
- Interface de connexion sécurisée
- Gestion des rôles (Administrateur/Employé)
- Hachage des mots de passe avec BCrypt

### 🛏️ Gestion des Matelas
- **CRUD complet** : Ajouter, modifier, supprimer, consulter
- Gestion des stocks en temps réel
- Prix unitaire et quantité
- Types : Mousse, Ressort, Latex, etc.
- Tailles : 90x200, 140x190, etc.
- Marques : Simmons, Ikea, etc.

### 💰 Gestion des Transactions
- **Types de transactions** :
  - **Vente** : Vente directe à un client
  - **Prêt** : Prêt à un magasin avec date de retour
  - **Transfert** : Déplacement vers un autre entrepôt
  - **Retour** : Retour de matelas prêtés
- Suivi automatique des stocks
- Prix et quantités
- Notes et détails

### 🏪 Gestion des Propriétaires de Magasin
- Enregistrement des magasins partenaires
- Contact et informations
- Lié aux transactions de prêt/transfert

### 👥 Gestion des Utilisateurs (Admin uniquement)
- Création et gestion des comptes employés
- Attribution des rôles
- Sécurité d'accès

### 📊 Rapports et Statistiques
- Génération de rapports PDF
- Rapports quotidiens et mensuels
- Statistiques de vente et stock
- Historique des transactions

### 🎨 Interface Utilisateur
- **Design moderne** avec animations fluides
- **Interface en français** complète
- **Mode plein écran** pour une expérience immersive
- **Dialogs intégrés** (pas de fenêtres séparées)
- **Navigation animée** (centrée → gauche)
- **Couleurs vives** et design professionnel

## 🚀 Installation et Configuration

### Prérequis
- **Java 17+** (OpenJDK recommandé)
- **Maven 3.6+**
- **XAMPP** (pour MySQL)
- **IntelliJ IDEA** (recommandé)

### 1. Configuration de la Base de Données

#### A. Démarrer XAMPP
```bash
# Démarrer Apache et MySQL dans XAMPP Control Panel
```

#### B. Créer la Base de Données
1. Ouvrir **phpMyAdmin** (http://localhost/phpmyadmin)
2. Créer une nouvelle base de données : `warehouse_mattress`
3. Importer le fichier : `src/main/resources/mysql_schema.sql`

#### C. Créer l'Utilisateur Admin
```sql
-- Dans phpMyAdmin, exécuter :
INSERT INTO users (username, password_hash, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZkZo5eEe7JQpKqVqZqKqKqKqKqKqKqKqKqKqKq', 'admin');
```

**Note** : Le mot de passe par défaut est `admin123`. Pour changer :
1. Utiliser un générateur BCrypt en ligne
2. Remplacer le hash dans la base de données

### 2. Configuration du Projet

#### A. Cloner/Extraire le Projet
```bash
cd warehouse-mattress-app
```

#### B. Configuration IntelliJ IDEA
1. **Ouvrir le projet** dans IntelliJ
2. **Configurer les VM Options** :
   - Run → Edit Configurations
   - VM Options : `--module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml`
3. **Reload Maven** : View → Tool Windows → Maven → Reload

#### C. Vérifier les Dépendances
Le `pom.xml` contient toutes les dépendances nécessaires :
- JavaFX (controls, fxml, base, graphics)
- MySQL Connector
- BCrypt pour le hachage
- iText pour les PDF

### 3. Compilation et Exécution

#### A. Compiler le Projet
```bash
mvn clean compile
```

#### B. Exécuter l'Application
```bash
mvn javafx:run
```

**Ou depuis IntelliJ** :
- Run → Run 'App'

## 🧪 Guide de Test

### 1. Test de Connexion
1. **Lancer l'application**
2. **Se connecter** avec :
   - Username : `admin`
   - Password : `admin123`
3. **Vérifier** que le dashboard s'affiche en plein écran

### 2. Test des Matelas
1. **Cliquer sur "🛏 Matelas"**
2. **Ajouter un matelas** :
   - Type : "Mousse"
   - Taille : "90x200"
   - Marque : "Simmons"
   - Quantité : 10
   - Prix : 299.99
3. **Vérifier** que le matelas apparaît dans la liste
4. **Modifier** le matelas créé
5. **Supprimer** un matelas

### 3. Test des Transactions
1. **Cliquer sur "💶 Transactions"**
2. **Ajouter une transaction** :
   - Type : "Vente"
   - Matelas : Sélectionner un matelas
   - Quantité : 2
   - Prix : 599.98
3. **Vérifier** que le stock diminue automatiquement
4. **Tester les autres types** : Prêt, Transfert, Retour

### 4. Test des Propriétaires
1. **Cliquer sur "🏪 Propriétaires de magasin"**
2. **Ajouter un propriétaire** :
   - Nom : "Magasin Central"
   - Contact : "contact@magasin.com"
3. **Vérifier** qu'il apparaît dans les transactions

### 5. Test des Rapports
1. **Cliquer sur "📄 Rapports"**
2. **Générer un rapport PDF**
3. **Vérifier** que le fichier PDF est créé

### 6. Test de la Gestion Utilisateurs (Admin)
1. **Cliquer sur "👤 Utilisateurs"**
2. **Ajouter un employé** :
   - Username : "employe1"
   - Password : "password123"
   - Role : "employé"
3. **Se déconnecter et se reconnecter** avec le nouvel utilisateur

## 🔧 Résolution des Problèmes

### Erreur JavaFX
```
java: package javafx.fxml does not exist
```
**Solution** :
1. Vérifier que JavaFX est dans le classpath
2. Configurer les VM Options dans IntelliJ
3. Reload Maven

### Erreur de Connexion Base de Données
```
Communications link failure
```
**Solution** :
1. Vérifier que MySQL est démarré dans XAMPP
2. Vérifier les paramètres de connexion dans `DBUtil.java`
3. Vérifier que la base `warehouse_mattress` existe

### Erreur BCrypt
```
Invalid salt version
```
**Solution** :
1. Utiliser un générateur BCrypt en ligne
2. Mettre à jour le hash dans la base de données
3. Vérifier le format du hash

### Erreur FXML
```
javafx.fxml.LoadException
```
**Solution** :
1. Vérifier que tous les fichiers FXML sont dans `target/classes`
2. Vérifier les chemins des ressources
3. Clean et recompile le projet

### Problème de Plein Écran
**Solution** :
1. Vérifier que `setFullScreen(true)` est appelé
2. Vérifier les permissions système
3. Tester en mode fenêtré d'abord

## 📁 Structure du Projet

```
warehouse-mattress-app/
├── src/
│   ├── main/
│   │   ├── java/com/warehouse/
│   │   │   ├── controller/     # Contrôleurs JavaFX
│   │   │   ├── model/          # Modèles et DAO
│   │   │   └── util/           # Utilitaires (PDF, etc.)
│   │   └── resources/
│   │       ├── fxml/           # Interfaces utilisateur
│   │       ├── css/            # Styles CSS
│   │       ├── images/         # Images et logos
│   │       └── mysql_schema.sql # Schéma de base de données
│   └── test/                   # Tests unitaires
├── pom.xml                     # Configuration Maven
└── README.md                   # Ce fichier
```

## 🎯 Fonctionnalités Avancées

### Animations et Transitions
- Navigation fluide avec animations CSS
- Boutons avec effets hover
- Transitions entre vues

### Sécurité
- Hachage BCrypt des mots de passe
- Gestion des rôles et permissions
- Validation des données

### Performance
- Requêtes SQL optimisées
- Interface responsive
- Gestion mémoire efficace

### Extensibilité
- Architecture modulaire
- Séparation MVC claire
- Code réutilisable

## 📞 Support

Pour toute question ou problème :
1. Vérifier ce README
2. Consulter les logs de l'application
3. Vérifier la configuration de la base de données

## 🚀 Déploiement

### Version de Production
1. Compiler avec : `mvn clean package`
2. Créer un JAR exécutable
3. Distribuer avec les dépendances

### Configuration Serveur
1. Installer MySQL sur le serveur
2. Configurer les paramètres de connexion
3. Déployer l'application

---

**Développé avec ❤️ pour une gestion d'entrepôt moderne et efficace** 