# DevSecOps Challenge - Mibanco

Plataforma de CI/CD segura sobre GitHub Actions y Azure, implementada como parte del reto técnico de Especialista DevSecOps. Automatiza el ciclo completo desde el código fuente hasta el despliegue en Kubernetes (AKS), con controles de seguridad integrados en cada etapa del pipeline.

## Diagrama de arquitectura

![Diagrama de arquitectura del flujo E2E](docs/diagrama-arquitectura.png)

*(Diagrama editable disponible en `docs/diagrama-arquitectura.drawio`, abrir con [app.diagrams.net](https://app.diagrams.net))*

## Arquitectura general

El proyecto se compone de **dos repositorios**:

| Repositorio | Propósito |
|---|---|
| [`devsecops-challenge`](.) (este repo) | Código del microservicio, Dockerfile, Helm chart, y los 3 workflows que disparan el pipeline por ambiente |
| [`devsecops-shared-workflows-challenge`](https://github.com/anakarina1928/devsecops-shared-workflows-challenge) | Templates reutilizables (`workflow_call`) que encapsulan la lógica de build, scan y deploy, más una Action propia en JavaScript |

## El microservicio

Aplicación Spring Boot mínima, con dos endpoints:

| Endpoint | Descripción |
|---|---|
| `GET /hello` | Devuelve un saludo que incluye un secreto obtenido de Azure Key Vault en tiempo de despliegue |
| `GET /actuator/health` | Health check usado por Kubernetes (liveness/readiness probes) |

## Estrategia de ramas (GitFlow)

```
feature/*  →  Pull Request  →  develop  →  release/*  →  main  →  hotfix/*
                                  │             │            │
                                  ▼             ▼            ▼
                                 dev            qa          prod
                            (automático)  (automático)   (manual)
```

| Rama | Dispara | Ambiente | Escaneo de seguridad |
|---|---|---|---|
| `develop` | `develop.yml` (automático) | `dev` | SAST (CodeQL) |
| `release/*` | `release.yml` (automático) | `qa` | Container Scan (Trivy) |
| `main` | `production.yml` (**manual**, `workflow_dispatch`) | `prod` | Ninguno — se promueve la misma imagen ya validada en `dev`/`qa` (*build once, promote everywhere*) |

El despliegue a `prod` requiere:
1. Disparo manual explícito, indicando el `image_tag` (SHA) a promover
2. Aprobación de un revisor (GitHub Environment `prod` con Required Reviewer)

## Pipeline (orquestador)

Cada uno de los 3 workflows del microservicio (`develop.yml`, `release.yml`, `production.yml`) **solo llama** al `template-orchestrator.yml` del repo de templates, sin lógica propia. El orquestador encadena:

```
Preflight (verifica permisos Azure/ACR/AKS)
   → Build & Test (Maven)
      → Docker Build & Push (a ACR)
         → Security Scan (SAST | Container Scan | ninguno, según ambiente)
            → Deploy (Helm upgrade hacia AKS)
```

## Controles de seguridad implementados

- **SAST**: CodeQL (rama `develop`)
- **SCA**: Dependabot (activo a nivel de repositorio)
- **Secret Scanning**: GitHub Secret Scanning + Push Protection
- **Container Scanning**: Trivy (rama `release/*`), con Security Gate que bloquea si hay vulnerabilidades `CRITICAL`
- **Autenticación sin secretos estáticos**: OIDC (Federated Credentials) entre GitHub Actions y Azure
- **Mínimo privilegio en contenedores**: usuario no-root en el Dockerfile y `securityContext` en el Helm chart
- **Permisos explícitos**: cada workflow declara únicamente los permisos que necesita
- **Pinning por SHA**: todas las Actions de terceros referenciadas por el SHA exacto del commit, no por tag mutable

## Gestión de variables y secretos

| Tipo | Dónde vive | Ejemplos |
|---|---|---|
| **Secrets** (Repository) | GitHub Secrets | `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` |
| **Variables** (Repository) | GitHub Actions Variables | `ACR_REGISTRY`, `IMAGE_NAME`, `RESOURCE_GROUP`, `AKS_CLUSTER_NAME` |
| **Secreto de aplicación** | Azure Key Vault | Obtenido en tiempo de despliegue vía una Action propia en JavaScript (`keyvault-secret-fetch`), inyectado como Kubernetes Secret |

## Estrategia de rollback

1. `helm rollback <release> <revision>` directo en el cluster
2. Re-ejecutar `production.yml` (Actions → "Re-run jobs") sobre un run anterior exitoso, reutilizando su mismo `image_tag`
3. Ambas opciones para `prod` requieren la misma aprobación manual que cualquier despliegue

## Estructura del repositorio

```
.
├── .github/workflows/
│   ├── develop.yml       # dispara pipeline hacia dev
│   ├── release.yml       # dispara pipeline hacia qa
│   └── production.yml    # dispara pipeline hacia prod (manual)
├── src/                  # código fuente del microservicio (Spring Boot)
├── helm/microservicio/    # Helm chart (Deployment, Service, Ingress, Secret)
├── Dockerfile             # build multi-stage, usuario no-root
└── docs/
    ├── diagrama-arquitectura.png
    └── diagrama-arquitectura.drawio
```

## Cómo probarlo localmente

```bash
mvn compile
mvn test
```

## Cómo verificar un despliegue

```bash
kubectl get pods -n dev    # o qa / prod
kubectl port-forward -n dev svc/microservicio 8080:80
curl http://localhost:8080/hello
```

## Mejoras identificadas (no implementadas en esta iteración)

- Integración de HashiCorp Vault para secretos de aplicación en runtime (se optó por Azure Key Vault, más simple de integrar en el tiempo disponible)
- Patrón GitOps con repositorio de manifiestos separado (ArgoCD/Flux), para desacoplar el pipeline de build del de despliegue
<img width="1896" height="901" alt="diagrama-reto-tecnico drawio" src="https://github.com/user-attachments/assets/6977634f-d4d4-495f-99ad-8b8431e4dc3a" />
