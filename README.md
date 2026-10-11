[![CI Backend](https://github.com/rania1237/fly2us-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/rania1237/fly2us-backend/actions/workflows/ci.yml)
[![SonarCloud Backend](https://github.com/rania1237/fly2us-backend/actions/workflows/sonar.yml/badge.svg)](https://github.com/rania1237/fly2us-backend/actions/workflows/sonar.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=rania1237_fly2us-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=rania1237_fly2us-backend)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=rania1237_fly2us-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=rania1237_fly2us-backend)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=rania1237_fly2us-backend&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=rania1237_fly2us-backend)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=rania1237_fly2us-backend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=rania1237_fly2us-backend)

# FLY2US - Backend

Système de gestion de visas - Microservices Spring Boot.

## 🏗️ Architecture

| Service | Port | Description |
|---|---|---|
| Eureka Server | 8761 | Service Discovery |
| Config Server | 8888 | Configuration centralisée |
| Gateway | 8080 | API Gateway |
| User Service | 8081 | Authentification |
| Dossier Service | 8082 | Gestion des dossiers |
| Payment Service | 8083 | Paiements |
| Visa Service | 8086 | Gestion des visas |

## 🚀 Démarrage

```bash
# Compiler tous les services
./mvnw clean install