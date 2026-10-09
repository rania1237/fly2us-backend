# FLY2US - Backend

![CI Backend](https://github.com/rania1237/fly2us-backend/actions/workflows/ci.yml/badge.svg)

Système de gestion de visas - Microservices Spring Boot.

## 🏗️ Architecture

| Service | Port | Description |
|---------|------|-------------|
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
