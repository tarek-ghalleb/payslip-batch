# Demo Spring Batch - Générateur Automatique de Fiches de Paie

Application démo Spring Batch pour la génération automatisée de fiches de paie mensuelles avec archivage sur MinIO/S3.

## Description

- **Planification automatique** : Génération le 1er de chaque mois à 2h
- **Calcul métier basique** : Salaire brut, charges sociales (22%), impôts (10%), net...
- **PDFs professionnels** : Génération avec iText7
- **Archivage S3** : Stockage automatique sur MinIO (compatible AWS S3)

## Stack Technique

**Backend**
- Java 21
- Spring Boot 3.5.9
- Spring Batch 5.2.4 (chunk-oriented processing)
- Spring Data JPA
- Lombok

**Génération PDF**
- iText 8.0.3

**Stockage**
- MinIO (S3-compatible)
- H2 Database


**Build & Deploy**
- Maven
- Docker Compose

## Architecture
```
Reader (JDBC) → Processor (Calculs) → Processor (PDF) → Writer (MinIO)
     ↓                  ↓                    ↓               ↓
 Employés DB      Salaire Net          Fichier PDF      S3 Bucket
```

## Quick Start
```bash
# 1. Démarrer MinIO
docker-compose up -d

# 2. Lancer l'application
mvn spring-boot:run

# 3. Déclencher manuellement
curl -X POST "http://localhost:8080/api/batch/payslips/generate?month=2026-01"

# 4. Accéder à MinIO
http://localhost:9001 (minioadmin / minioadmin)
```

## Planification

**Automatique** : 1er du mois à 2h (Europe/Paris)
```properties
payslip.generation.cron=0 0 2 1 * ?
```

## API REST

**Génération manuelle** :
```bash
POST /api/batch/payslips/generate?month=2026-01
```

**Statut du scheduler** :
```bash
GET /api/batch/status
```


## Résultat

Les PDFs générés sont stockés dans MinIO :
```
s3://payslips/
  └── 2026-01/
      ├── EMP001_payslip_2026-01.pdf
      ├── EMP002_payslip_2026-01.pdf
      └── ...
```

## Concepts Spring Batch démontrés

- **@Scheduled** - Planification automatique sans Quartz
- **JdbcCursorItemReader** - Lecture performante depuis DB
- **CompositeItemProcessor** - Enchaînement de processors
- **Custom ItemWriter** - Writer personnalisé pour MinIO
- **Chunk-oriented processing** - Traitement par lots
- **JobExplorer** - Vérification des exécutions passées

