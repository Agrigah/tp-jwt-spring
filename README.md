# 🛡️ Sécurisation d'une API REST par Tokens JWT – Spring Security & Java 21

**Aya** – Projet de démonstration d'une authentification *stateless* basée sur JSON Web Tokens (JWT) avec Spring Boot 4, Spring Security et JJWT.

> 📌 **Note** : Les images de ce README sont des images fictives de démonstration. Dans le dépôt réel, elles sont remplacées par les captures Postman authentiques dans le dossier `screenshots/`.

---

## 📑 Table des matières

1. [Vue d'ensemble & Architecture](#1-vue-densemble--architecture)
2. [Principes fondamentaux : Architecture sans état](#2-principes-fondamentaux--architecture-sans-état-stateless)
3. [Structure ternaire d'un Jeton JWT](#3-structure-ternaire-dun-jeton-jwt)
4. [Démarrage rapide](#4-démarrage-rapide)
5. [Cahier de recette – Tests & Validation](#5-cahier-de-recette--tests--validation)
6. [Audit navigateur – Console H2](#6-audit-navigateur--console-h2)
7. [Matrice de recette & Checklist finale](#7-matrice-de-recette--checklist-finale)

---

## 1. Vue d'ensemble & Architecture

Ce projet implémente un système complet d'authentification et d'autorisation sécurisé pour une API REST en utilisant **Spring Boot 4**, **Java 21 LTS** et **JJWT 0.13.0**.

Contrairement aux architectures basées sur les sessions HTTP (`JSESSIONID`), cette solution adopte une approche **strictement stateless** :

- **Mémoire serveur neutre** : aucun état de session n'est conservé côté backend.
- **Autonomie cryptographique** : chaque requête porte son propre contexte d'habilitation via l'en-tête `Authorization: Bearer <token>`.
- **Scalabilité horizontale native** : n'importe quelle instance backend peut valider instantanément un token sans interroger une session partagée.

---

## 2. Principes fondamentaux : Architecture sans état (Stateless)

1. L'utilisateur s'inscrit (`POST /auth/register`) ou se connecte (`POST /auth/login`).
2. Le serveur vérifie les identifiants et génère un **JWT signé** (HMAC-SHA).
3. Le client stocke le token et l'envoie dans l'en-tête `Authorization` pour chaque requête protégée.
4. Un filtre Spring Security (`JwtFilter`) intercepte la requête, valide la signature et peuple le `SecurityContextHolder`.
5. Si le token est valide, la requête est autorisée ; sinon, elle est rejetée (`403 Forbidden`).

---

## 3. Structure ternaire d'un Jeton JWT

Un JWT (RFC 7519) se compose de **trois segments** séparés par des points (`.`) :

```text
Token = Base64URL(Header) . Base64URL(Payload) . Signature
```

- **Header** : métadonnées (algorithme, type).
- **Payload** : assertions (claims) – `sub`, `iat`, `exp`, etc.
- **Signature** : empreinte HMAC-SHA calculée avec une clé secrète.

**Exemple de Header** :

```json
{ "alg": "HS384" }
```

**Exemple de Payload** :

```json
{
  "sub": "aya@example.com",
  "iat": 1792086056,
  "exp": 1792089656
}
```

La signature garantit l'intégrité et l'authenticité : toute modification du header ou du payload invalide la signature.

---

## 4. Démarrage rapide

### Prérequis

- Java Development Kit (**JDK 21** ou supérieur).
- Apache Maven 3.9+ (ou le wrapper `./mvnw` inclus).
- Postman (pour les tests).
- Un navigateur web moderne (pour la console H2).

### Lancement de l'application

```bash
./mvnw spring-boot:run
```

Sous Windows :

```cmd
mvnw.cmd spring-boot:run
```

**Vérification** : les logs doivent afficher le démarrage de Tomcat sur le port `8080` et la console H2 disponible à l'adresse `/h2-console`.

---

## 5. Cahier de recette – Tests & Validation

> Les images ci-dessous sont des **placeholders**. Dans le dépôt réel, elles sont remplacées par les captures Postman authentiques.

### Test 1 : Inscription d'un utilisateur (`POST /auth/register`)

**Objectif** : créer un compte avec un mot de passe haché via `BCryptPasswordEncoder` avant persistance en base H2.

**Configuration Postman** :

- Méthode : `POST`
- URL : `http://localhost:8080/auth/register`
- Headers : `Content-Type: application/json`
- Body (raw JSON) :

```json
{
  "email": "aya@example.com",
  "password": "aya"
}
```

**Résultat attendu** : `200 OK` avec le corps `"Utilisateur cree"`.

![Figure 1 - Aya - Register 200 OK](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+POST+%2Fauth%2Fregister+200+OK)

*Figure 1 : Envoi de la requête d'inscription et confirmation HTTP 200 OK.*

---

### Test 2 : Authentification & récupération du token (`POST /auth/login`)

**Objectif** : soumettre les identifiants pour validation et générer un JWT signé.

**Configuration Postman** :

- Méthode : `POST`
- URL : `http://localhost:8080/auth/login`
- Body (raw JSON) :

```json
{
  "email": "aya@example.com",
  "password": "aya"
}
```

**Résultat attendu** : `200 OK` avec un JSON contenant le champ `token`.

![Figure 2 - Aya - Login Token JWT](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+POST+%2Fauth%2Flogin+-+Token+JWT)

*Figure 2 : Authentification réussie et génération du jeton JWT.*

---

### Test 3 : Décodage & analyse cryptographique du token

**Objectif** : inspecter la structure du token sur [jwt.io](https://jwt.io) pour valider les claims.

**Analyse** :

- Header : `{"alg": "HS384"}`
- Payload : `{"sub": "aya@example.com", "iat": ..., "exp": ...}`
- Durée de validité : 1 heure.

![Figure 3 - Aya - jwt.io HS384](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+jwt.io+-+HS384+Decoded)

*Figure 3 : Décomposition du jeton sur le débogueur jwt.io.*

---

### Test 4 : Accès autorisé à une ressource protégée (`GET /api/profile`)

**Objectif** : vérifier que le filtre `JwtFilter` valide le token et alimente le contexte de sécurité.

**Configuration Postman** :

- Méthode : `GET`
- URL : `http://localhost:8080/api/profile`
- Authorization : Bearer Token → `{{token}}`

**Résultat attendu** : `200 OK` avec `"Utilisateur connecte : aya@example.com"`.

![Figure 4 - Aya - Profile 200 OK](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+GET+%2Fapi%2Fprofile+200+OK)

*Figure 4 : Accès autorisé à l'endpoint sécurisé `/api/profile`.*

---

### Test 5 : Accès autorisé au point d'entrée global (`GET /api/hello`)

**Objectif** : valider l'accès à une deuxième route protégée sous `/api/**`.

**Configuration Postman** :

- Méthode : `GET`
- URL : `http://localhost:8080/api/hello`
- Authorization : Bearer Token → `{{token}}`

**Résultat attendu** : `200 OK` avec `"Bienvenue dans l'API securisee"`.

![Figure 5 - Aya - Hello 200 OK](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+GET+%2Fapi%2Fhello+200+OK)

*Figure 5 : Accès confirmé au service sécurisé `/api/hello`.*

---

### Test 6 : Rejet en cas d'absence de token (`403 Forbidden`)

**Objectif** : éprouver la barrière de sécurité lorsque aucun token valide n'est fourni.

**Configuration Postman** :

- Méthode : `GET`
- URL : `http://localhost:8080/api/hello`
- Authorization : Bearer Token non configuré (ou vide).

**Résultat attendu** : `403 Forbidden`.

![Figure 6 - Aya - 403 No Token](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+403+Forbidden+-+No+Token)

*Figure 6 : Rejet catégorique `403 Forbidden` suite à l'absence de token.*

---

### Test 7 : Rejet lors d'une altération / falsification de signature

**Objectif** : démontrer la protection mathématique contre la falsification. Si un caractère du token est modifié, la signature recalculée ne correspond plus et la requête est rejetée.

**Démarche** : prendre un token valide, modifier le dernier caractère de la signature, puis l'envoyer.

**Résultat attendu** : `403 Forbidden` avec un corps JSON d'erreur.

![Figure 7 - Aya - 403 Tampered Token](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+403+Forbidden+-+Tampered+Token)

*Figure 7 : Blocage instantané d'un jeton altéré.*

---

## 6. Audit navigateur – Console H2

### Accès et paramètres de connexion

L'application intègre une base **H2 en mémoire** accessible via une console web.

1. Ouvrir : `http://localhost:8080/h2-console`
2. Paramètres :
   - **Driver Class** : `org.h2.Driver`
   - **JDBC URL** : `jdbc:h2:mem:securitydb`
   - **User Name** : `sa`
   - **Password** : *(laisser vide)*

![Figure 8 - Aya - H2 Console](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+H2+Console+-+securitydb)

*Figure 8 : Interface de connexion à la console H2.*

### Vérification du hachage BCrypt en base

Exécuter la requête SQL suivante :

```sql
SELECT * FROM USERS;
```

**Analyse** : la colonne `PASSWORD` doit contenir un hash commençant par `$2a$10$`, ce qui certifie l'utilisation de **BCrypt** avec un facteur de coût de 10. Le mot de passe en clair n'est jamais stocké.

![Figure 9 - Aya - USERS BCrypt](https://placehold.co/1200x600/0f172a/ffffff?text=Aya+-+SELECT+USERS+-+BCrypt+Hash)

*Figure 9 : Persistance de l'utilisateur avec son empreinte BCrypt dans la table `USERS`.*

---

## 7. Matrice de recette & Checklist finale

### Synthèse des points de terminaison

| Méthode | URI | Accès sécurité | Description | Code attendu |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | `permitAll()` | Inscription | `200 OK` |
| `POST` | `/auth/login` | `permitAll()` | Authentification & token | `200 OK` |
| `GET` | `/api/profile` | `authenticated()` | Profil connecté | `200 OK` |
| `GET` | `/api/hello` | `authenticated()` | Message sécurisé | `200 OK` |
| `GET` | `/api/hello` | *sans token* | Rejet | `403 Forbidden` |
| `GET` | `/api/hello` | *token altéré* | Rejet | `403 Forbidden` |
| `GET` | `/h2-console/**` | `permitAll()` | Console H2 | `200 OK` |

### Checklist de validation finale

- [x] L'application compile et démarre sans erreur (Tomcat sur le port 8080).
- [x] L'utilisateur `aya@example.com` est créé via `POST /auth/register`.
- [x] `POST /auth/login` génère un JWT valide (HMAC).
- [x] Le token est inspecté sur jwt.io ; les claims `sub`, `iat`, `exp` sont conformes.
- [x] `GET /api/profile` avec Bearer Token retourne `200 OK` et l'email.
- [x] `GET /api/hello` avec Bearer Token confirme l'accès autorisé.
- [x] `GET /api/hello` sans token retourne `403 Forbidden`.
- [x] La falsification de la signature déclenche un `403 Forbidden`.
- [x] La console H2 est accessible sur `jdbc:h2:mem:securitydb`.
- [x] `SELECT * FROM USERS;` confirme la présence de l'utilisateur avec hash BCrypt `$2a$10$...`.
- [x] Les captures d'écran réelles sont sauvegardées dans le répertoire `screenshots/`.

---

## 📂 Structure du projet (extrait)

```text
tp-jwt-spring/
├── src/main/java/ma/enset/tpjwtspring/
│   ├── config/          # Configuration Spring Security
│   ├── controller/      # Endpoints REST
│   ├── dto/             # Objets de transfert
│   ├── entity/          # Entités JPA (User)
│   ├── repository/      # Interfaces Spring Data
│   ├── security/        # Filtres & services JWT
│   └── service/         # Logique métier
├── screenshots/         # Captures d'écran des tests
├── pom.xml
└── README.md
```

---

## 👤 Auteur

**Aya** – Projet réalisé dans le cadre d'un TP sur la sécurisation d'API REST par JWT.
