# 🚀 Guide de Déploiement

**Prérequis:** Java 17+ | XAMPP (MySQL) | Maven

**Installation:** (1) Démarrer XAMPP MySQL → phpMyAdmin → Créer base `warehouse_db` → Importer `COMPLETE_SCHEMA.sql` (2) Vérifier `src/main/resources/config.properties` (3) Compiler: `mvn clean package` (4) Créer EXE (Windows): `mvn jpackage:jpackage@win` → Résultat dans `target/dist/MatelasPro.exe` (5) Créer APP (macOS): `mvn jpackage:jpackage@mac` → Résultat dans `target/dist/MatelasPro.app`

**Informations importantes:** Mot de passe pour créer compte: `MATELASPRO-ADMIN` | Admin par défaut: `admin`/`admin123` | **Clé de licence:** `T236-ZTZU-W4BU-UA99-383C` (utiliser cette clé pour activer l'application sur un nouvel ordinateur)

**Distribution:** Copier l'exécutable (EXE/APP) + `COMPLETE_SCHEMA.sql` + instructions pour installer XAMPP et créer la base de données.
