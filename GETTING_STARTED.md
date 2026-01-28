# 🚀 Guide de Prise en Main - Product Trial

Ce guide vous accompagne dans la prise en main du projet Product Trial, une application e-commerce full-stack développée avec Angular et Spring Boot.

---

## 📋 Table des matières

1. [Présentation du projet](#présentation-du-projet)
2. [Versions des technologies](#versions-des-technologies)
3. [Prérequis](#prérequis)
4. [Installation et lancement manuel](#installation-et-lancement-manuel)
5. [Tests](#tests)
6. [Déploiement avec Docker](#déploiement-avec-docker)
7. [Utilisation de Postman](#utilisation-de-postman)
8. [Documentation API](#documentation-api)

---

## 📖 Présentation du projet

**Product Trial** est une application e-commerce complète permettant de :
- Gérer un catalogue de produits
- Gérer un panier d'achat (avec support utilisateur anonyme)
- Gérer une liste de souhaits (wishlist)
- Authentification utilisateur avec JWT
- Interface d'administration pour la gestion des produits

### Architecture

- **Backend** : Spring Boot 3.x (Java 21) - API REST
- **Frontend** : Angular 18 - Application SPA
- **Base de données** : H2 (en mémoire)
- **Sécurité** : JWT (JSON Web Tokens)
- **Documentation** : Swagger/OpenAPI 3.0

---

## 🔧 Versions des technologies

### Backend (Java/Spring Boot)

| Technologie | Version |
|------------|---------|
| **Java** | 21 |
| **Spring Boot** | 3.2.0 |
| **Spring Framework** | 6.1.x (via Spring Boot) |
| **Spring Data JPA** | 3.2.x |
| **Hibernate** | 6.4.x |
| **H2 Database** | 2.2.224 (géré par Spring Boot) |
| **JWT (jjwt)** | 0.12.3 |
| **SpringDoc OpenAPI** | 2.3.0 |
| **Lombok** | Version gérée par Spring Boot |
| **Maven** | Géré par Spring Boot Parent |

### Frontend (Angular/TypeScript)

| Technologie | Version |
|------------|---------|
| **Angular** | 18.0.2 |
| **Angular CLI** | 18.0.3 |
| **TypeScript** | 5.4.5 |
| **Node.js** | Recommandé 20.x |
| **PrimeNG** | 17.18.0 |
| **PrimeIcons** | 7.0.0 |
| **PrimeFlex** | 3.3.1 |
| **RxJS** | 7.8.1 |
| **Zone.js** | 0.14.7 |

### Outils de développement

| Outil | Version |
|-------|---------|
| **Docker** | 20.x ou supérieur |
| **Docker Compose** | 2.x ou supérieur |
| **Maven** | 3.8+ |
| **npm** | 9.x ou supérieur |

---

## ✅ Prérequis

### Pour le Backend

- **Java 21** (JDK) installé et configuré
- **Maven 3.8+** installé
- **IDE** (IntelliJ IDEA, Eclipse, VS Code) recommandé

Vérifier l'installation :
```bash
java -version  # Doit afficher version 21
mvn -version   # Doit afficher version 3.8+
```

### Pour le Frontend

- **Node.js 20.x** (ou supérieur) installé
- **npm 9.x** (ou supérieur) installé
- **Angular CLI 18.0.3** installé globalement

Vérifier l'installation :
```bash
node -v        # Doit afficher v20.x ou supérieur
npm -v         # Doit afficher 9.x ou supérieur
ng version     # Doit afficher Angular CLI 18.0.3
```

Installer Angular CLI globalement :
```bash
npm install -g @angular/cli@18.0.3
```

### Pour Docker (optionnel)

- **Docker Desktop** installé et démarré
- **Docker Compose** installé

Vérifier l'installation :
```bash
docker --version
docker-compose --version
```

---

## 🏃 Installation et lancement manuel

### 1. Cloner le projet

```bash
git clone <url-du-repo>
cd product-trial-master
```

### 2. Lancer le Backend

#### Étape 1 : Naviguer vers le dossier backend

```bash
cd back
```

#### Étape 2 : Compiler le projet

```bash
mvn clean install
```

#### Étape 3 : Lancer l'application

```bash
mvn spring-boot:run
```

Ou avec Java directement :
```bash
java -jar target/product-trial-1.0.0.jar
```

#### Étape 4 : Vérifier que le backend est démarré

- Le backend sera accessible sur : **http://localhost:8080**
- La console H2 sera accessible sur : **http://localhost:8080/h2-console**
  - URL JDBC : `jdbc:h2:mem:producttrial`
  - Username : `sa`
  - Password : (vide)
- Swagger UI sera accessible sur : **http://localhost:8080/swagger-ui.html**

#### Configuration backend

Le fichier `back/src/main/resources/application.properties` contient la configuration :
- Port : 8080
- Base de données H2 en mémoire
- JWT secret et expiration
- Email admin : `admin@admin.com`

### 3. Lancer le Frontend

#### Étape 1 : Naviguer vers le dossier frontend

```bash
cd front
```

#### Étape 2 : Installer les dépendances

```bash
npm install
```

#### Étape 3 : Lancer l'application en mode développement

```bash
ng serve
```

Ou avec npm :
```bash
npm start
```

#### Étape 4 : Vérifier que le frontend est démarré

- Le frontend sera accessible sur : **http://localhost:4200**
- L'application se rechargera automatiquement lors des modifications

#### Configuration frontend

Le fichier `front/src/environments/environment.ts` contient :
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Assurez-vous que l'URL de l'API correspond au port du backend.

### 4. Accéder à l'application

1. Ouvrir un navigateur à l'adresse : **http://localhost:4200**
2. L'application est prête à être utilisée !

#### Compte administrateur par défaut

Pour accéder aux fonctionnalités d'administration, vous devez créer un compte avec l'email :
- **Email** : `admin@admin.com`
- **Mot de passe** : (celui que vous choisissez lors de l'inscription)

### 5. Récapitulatif rapide des commandes

Depuis la **racine du projet** (`product-trial-master`) :

```bash
# Lancer uniquement le backend
cd back
mvn spring-boot:run

# Lancer uniquement le frontend (dans un autre terminal)
cd front
npm install        # première fois uniquement
npm start          # ou: ng serve
```

Ensuite :
- Backend : `http://localhost:8080`
- Frontend : `http://localhost:4200`

---

## 🎮 Parcours fonctionnel & URLs utiles

### 1. Pages principales (Frontend)

- **Accueil / Liste des produits**  
  - URL : `http://localhost:4200/home` (généralement redirigé depuis `/`)  
  - Contenu :  
    - Liste paginée des produits  
    - Recherche par texte  
    - Filtrage par catégorie  
    - Boutons pour ajouter au panier et aux favoris

- **Gestion admin des produits**  
  - URL : `http://localhost:4200/products/list`  
  - Accès : uniquement pour l'utilisateur `admin@admin.com` (authentifié)  
  - Actions :  
    - Créer un produit  
    - Modifier un produit  
    - Supprimer un produit  
    - Formulaire complet avec validations (Reactive Forms)

- **Panier**  
  - URL : `http://localhost:4200/cart`  
  - Fonctionnalités :  
    - Voir les produits ajoutés  
    - Modifier les quantités  
    - Supprimer des produits  
    - Support utilisateur non connecté via `localStorage`

- **Favoris (Wishlist)**  
  - URL : `http://localhost:4200/wishlist`  
  - Fonctionnalités :  
    - Voir les produits favoris  
    - Supprimer des favoris

- **Contact**  
  - URL : `http://localhost:4200/contact`  
  - Fonctionnalités :  
    - Formulaire email + message (limité à 300 caractères)  
    - Validation côté client (Reactive Forms)  
    - Appel à `POST /api/contact` côté backend

### 2. Authentification (Frontend)

- **Ouverture de la fenêtre de connexion**  
  - Dans l'en-tête (header), bouton **Connexion**  
  - Ouvre une boîte de dialogue avec :  
    - Mode **Connexion**  
    - Mode **Création de compte** (toggle)

- **Création de compte (Register)**  
  - Champs requis : `username`, `firstname`, `email`, `password`  
  - Appelle `POST /api/account`  
  - Pour un compte admin, utiliser l'email **`admin@admin.com`**

- **Connexion (Login)**  
  - Champs requis : `email`, `password`  
  - Appelle `POST /api/token`  
  - Le token JWT est stocké dans `localStorage`  
  - Le panier local est synchronisé avec le backend après connexion

- **Profil & Déconnexion**  
  - Dans l'en-tête, menu utilisateur (icône / email)  
  - Entrées :  
    - Affichage de l'email actuel  
    - Bouton **Déconnexion** (appelle `authService.logout()` et vide le panier serveur)

### 3. URLs Backend importantes

- API principale : `http://localhost:8080/api`
- **Auth** :  
  - `POST /api/account` : création de compte  
  - `POST /api/token` : login, retourne un JWT
- **Produits** :  
  - `GET /api/products` : liste (publique)  
  - `GET /api/products/{id}` : détail (public)  
  - `POST /api/products` : création (admin uniquement)  
  - `PATCH /api/products/{id}` : modification (admin uniquement)  
  - `DELETE /api/products/{id}` : suppression (admin uniquement)
- **Panier** (`Authorization: Bearer <token>` requis pour le mode connecté) :  
  - `GET /api/cart`  
  - `POST /api/cart/add`  
  - `PATCH /api/cart/{cartItemId}`  
  - `DELETE /api/cart/{cartItemId}`  
  - `DELETE /api/cart`
- **Wishlist** :  
  - `GET /api/wishlist`  
  - `POST /api/wishlist/add`  
  - `DELETE /api/wishlist/{wishlistItemId}`
- **Contact** :  
  - `POST /api/contact`

---

## 🧪 Tests

### Tests Backend

#### Lancer tous les tests

```bash
cd back
mvn test
```

#### Lancer uniquement les tests unitaires

```bash
mvn test -Dtest=*Test
```

#### Lancer uniquement les tests d'intégration

```bash
mvn test -Dtest=*IntegrationTest
```

#### Lancer un test spécifique

```bash
mvn test -Dtest=ProductServiceTest
```

#### Générer un rapport de couverture (si configuré)

```bash
mvn test jacoco:report
```

#### Structure des tests

- **Tests unitaires** : `back/src/test/java/com/alten/producttrial/service/`
- **Tests d'intégration** : `back/src/test/java/com/alten/producttrial/integration/`
- **Tests de mappers** : `back/src/test/java/com/alten/producttrial/mapper/`
- **Tests de sécurité** : `back/src/test/java/com/alten/producttrial/security/`

#### Profil de test

Les tests utilisent le profil `test` défini dans `back/src/test/resources/application-test.properties` :
- Base de données H2 en mémoire
- JWT secret de test
- Pas d'initialisation SQL

### Tests Frontend

Les tests frontend sont implémentés avec **Karma** et **Jasmine** (configuration dans `front/src/karma.conf.js` et `front/angular.json`). Les specs se trouvent à côté des fichiers sources (fichiers `*.spec.ts`).

#### Fichiers de test présents

- **`src/app/app.component.spec.ts`** : tests du composant racine (création, titre, ouverture/fermeture du dialogue de connexion).
- **`src/app/shared/data-access/auth.service.spec.ts`** : tests du service d’authentification (register, login, logout, `getAuthHeaders`, `isAuthenticated`).

Vous pouvez ajouter d’autres fichiers `*.spec.ts` pour les composants et services (par exemple `contact.service.spec.ts`, `cart.service.spec.ts`, etc.).

#### Prérequis

- Depuis le dossier **front** : `npm install` déjà exécuté (dépendances de test incluses : Karma, Jasmine, launchers, etc.).
- **Chrome** installé (utilisé par défaut par Karma) ; pour la CI, les tests peuvent tourner en **ChromeHeadless**.

#### Lancer les tests (mode watch, avec ouverture du navigateur)

```bash
cd front
npm test
```

Ou avec Angular CLI :

```bash
cd front
ng test
```

- Karma ouvre une fenêtre **Chrome** et exécute les specs.
- Les tests se relancent automatiquement à chaque modification des fichiers (mode watch).
- Pour arrêter : `Ctrl+C` dans le terminal.

#### Lancer les tests une seule fois (sans watch)

```bash
cd front
ng test --no-watch
```

#### Lancer les tests en mode CI (headless, une seule exécution)

```bash
cd front
npm run test:ci
```

Cette commande utilise la configuration **ci** (pas de watch) et lance Chrome en mode headless, adapté aux pipelines CI (Jenkins, GitLab CI, GitHub Actions, etc.).

#### Lancer les tests avec rapport de couverture

```bash
cd front
ng test --no-watch --code-coverage
```

- Le rapport est généré dans **`front/coverage/`** (sous-dossier selon le nom du projet, ex. `altenshop`).
- Ouvrir `coverage/altenshop/index.html` dans un navigateur pour consulter le rapport HTML.

#### Résumé des commandes (frontend)

| Action              | Commande |
|---------------------|----------|
| Tests + watch        | `npm test` ou `ng test` |
| Tests une fois       | `ng test --no-watch` |
| Tests CI (headless) | `npm run test:ci` |
| Tests + couverture  | `ng test --no-watch --code-coverage` |

---

## 🐳 Déploiement avec Docker

### Prérequis

- Docker Desktop installé et démarré
- Docker Compose installé

### Lancer l'application complète avec Docker Compose

#### Étape 1 : À la racine du projet

```bash
# À la racine du projet (product-trial-master)
docker-compose up --build
```

Cette commande va :
1. Construire les images Docker pour le backend et le frontend
2. Démarrer les deux conteneurs
3. Configurer le réseau entre les conteneurs

#### Étape 2 : Accéder à l'application

- **Frontend** : http://localhost:4200
- **Backend** : http://localhost:8080
- **Swagger UI** : http://localhost:8080/swagger-ui.html

### Commandes Docker utiles

#### Arrêter les conteneurs

```bash
docker-compose down
```

#### Arrêter et supprimer les volumes

```bash
docker-compose down -v
```

#### Voir les logs

```bash
# Tous les services
docker-compose logs

# Un service spécifique
docker-compose logs backend
docker-compose logs frontend

# Logs en temps réel
docker-compose logs -f
```

#### Redémarrer un service spécifique

```bash
docker-compose restart backend
docker-compose restart frontend
```

#### Reconstruire les images

```bash
docker-compose build --no-cache
```

#### Voir l'état des conteneurs

```bash
docker-compose ps
```

### Structure Docker

- **Backend Dockerfile** : `back/Dockerfile`
- **Frontend Dockerfile** : `front/Dockerfile`
- **Docker Compose** : `docker-compose.yml` (à la racine)

### Configuration Docker

Le fichier `docker-compose.yml` configure :
- **Backend** : Port 8080
- **Frontend** : Port 4200 (mappé sur le port 80 du conteneur)
- **Réseau** : `product-trial-network` (bridge)

---

## 📮 Utilisation de Postman

### Importer la collection Postman

#### Étape 1 : Ouvrir Postman

Lancer l'application Postman sur votre machine.

#### Étape 2 : Importer la collection

1. Cliquer sur **Import** dans Postman
2. Sélectionner le fichier : `back/Product_Trial_API.postman_collection.json`
3. La collection "Product Trial API" apparaîtra dans votre workspace

### Configurer l'environnement Postman

#### Créer un environnement

1. Cliquer sur **Environments** dans la barre latérale
2. Cliquer sur **+** pour créer un nouvel environnement
3. Nommer l'environnement : "Product Trial Local"
4. Ajouter les variables suivantes :

| Variable | Valeur initiale | Valeur actuelle |
|----------|----------------|-----------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | (vide) | (sera rempli automatiquement) |

5. Sauvegarder l'environnement
6. Sélectionner cet environnement dans le menu déroulant en haut à droite

### Utiliser la collection

#### 1. Authentification

**Créer un compte** :
- Requête : `POST /api/account`
- Body (JSON) :
```json
{
  "username": "testuser",
  "firstname": "Test",
  "email": "admin@admin.com",
  "password": "password123"
}
```

**Se connecter** :
- Requête : `POST /api/token`
- Body (JSON) :
```json
{
  "email": "admin@admin.com",
  "password": "password123"
}
```

**Important** : Après la connexion, copier le token de la réponse et le coller dans la variable d'environnement `token`.

#### 2. Utiliser le token automatiquement

La collection Postman est configurée pour utiliser automatiquement la variable `token` dans les requêtes authentifiées via le header :
```
Authorization: Bearer {{token}}
```

#### 3. Tester les endpoints

La collection contient des requêtes pour :
- **Authentication** : Register, Login
- **Products** : Get All, Get By ID, Create, Update, Delete
- **Cart** : Get Items, Add Item, Update Quantity, Remove Item, Clear Cart
- **Wishlist** : Get Items, Add Item, Remove Item
- **Contact** : Send Message

#### 4. Exécuter toute la collection

1. Cliquer sur la collection "Product Trial API"
2. Cliquer sur **Run** (bouton en haut à droite)
3. Sélectionner les requêtes à exécuter
4. Cliquer sur **Run Product Trial API**

**Note** : Assurez-vous d'exécuter d'abord "Register" ou "Login" pour obtenir un token avant d'exécuter les autres requêtes.

---

## 📚 Documentation API

### Swagger UI

Une fois le backend démarré, accédez à la documentation interactive :

**URL** : http://localhost:8080/swagger-ui.html

#### Fonctionnalités Swagger UI

- **Voir tous les endpoints** : Liste complète des API disponibles
- **Tester les endpoints** : Exécuter des requêtes directement depuis l'interface
- **Voir les modèles** : Schémas des objets utilisés
- **Authentification** : Cliquer sur "Authorize" et entrer le token JWT

#### Authentification dans Swagger

1. Cliquer sur le bouton **Authorize** en haut à droite
2. Dans le champ "bearerAuth", entrer votre token JWT (sans le préfixe "Bearer ")
3. Cliquer sur **Authorize**
4. Fermer la fenêtre
5. Les requêtes authentifiées sont maintenant disponibles

### OpenAPI Specification

L'API est documentée avec OpenAPI 3.0. Le fichier JSON est accessible à :
- **URL** : http://localhost:8080/v3/api-docs

Vous pouvez importer ce fichier dans d'autres outils comme :
- Postman
- Insomnia
- Redoc
- Stoplight


#### Base de données vide

La base de données H2 est en mémoire et se réinitialise à chaque redémarrage. Les données initiales sont chargées depuis `back/src/main/resources/data.sql`.

---

## 📝 Notes importantes

- **Base de données** : H2 est en mémoire, les données sont perdues au redémarrage
- **Admin** : Seul l'utilisateur avec l'email `admin@admin.com` peut gérer les produits
- **JWT** : Les tokens expirent après 24 heures (configurable dans `application.properties`)
- **Tests** : Les tests utilisent une base de données H2 séparée en mémoire

---

**Bon développement ! 🚀**
