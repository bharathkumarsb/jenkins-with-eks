# Pipeline Migration Guide

This document describes the step-by-step process of migrating from a **single monolithic Jenkins pipeline** to **modular pipelines** that give app teams autonomy while ensuring platform stability.

---

## 1. Current State

### Existing Pipeline (Monolith)

* Checkout SCM
* Initialize pipeline
* Pull base Docker image
* Prepare environment
* Reset/Launch EKS environment
* Build & publish Docker image
* Deploy to EKS
* Run deployment health checks

### Challenges

* All steps run together → tightly coupled
* Long runtimes (infra + app build + deploy)
* Platform concerns (EKS setup, base image) mixed with service delivery
* App teams cannot deploy independently
* Rollbacks require rerunning the entire pipeline

---

## 2. Target State (Modular Pipelines)

### Platform Pipelines

* **eks-env-manage** → Manage EKS clusters, namespaces, quotas
* **base-image-build** → Build and publish shared base images

### Per-Service Pipelines

* **svc-ci** → Build, test, scan, push service image
* **svc-deploy** → Deploy a tagged image to an environment via Helm
* **svc-preview** (optional) → Ephemeral PR-based environments

### Benefits

* Teams deploy independently with minimal cross-dependencies
* Immutable image promotion (build once, deploy many)
* Faster feedback: CI builds separate from deploy jobs
* Shared library centralizes common logic
* RBAC: platform vs. service ownership separation

---

## 3. Migration Phases

### Phase 1 — Discovery & Baselining

* Inventory current Jenkins jobs and stage runtimes
* Identify reusable steps (init, docker build/push, helm deploy, rollout checks)
* Document standards:

  * Image tags = commit SHA7
  * Helm values per environment
  * Rollout checks = `kubectl rollout status` + `/healthz`

**Deliverable:** Current state report + standards doc

---

### Phase 2 — Target Design & Templates

* Create **Jenkins Shared Library** (`initPipeline`, `dockerBuildPush`, `withEksAuth`, `helmDeploy`, `waitForRollout`)
* Author **template Jenkinsfiles**:

  * `Jenkinsfile.ci` → build/test/scan/push
  * `Jenkinsfile.deploy` → helm deploy + health checks
* Define **folders & RBAC** in Jenkins:

  * `/platform/` for infra jobs
  * `/services/<svc>/` for app jobs

**Deliverable:** Shared library repo + template Jenkinsfiles

---

### Phase 3 — Pilot Migration

* Pick one service (`hello-svc`)
* Implement `svc-ci` and `svc-deploy`
* Test dev deployment:

  * Validate image push to ECR
  * Helm deploy with values-dev.yaml
  * Health check passes
* Add QA/Prod deploy with approval (`input` step)

**Deliverable:** Pilot service migrated to modular pipelines

---

### Phase 4 — Rollout to All Services

* Onboard remaining services using templates
* Automate job creation (Job DSL / JCasC)
* Switch off monolithic pipeline once all migrated

**Deliverable:** All services live on modular pipelines

---

### Phase 5 — Hardening & Optimization

* Add SBOM (Syft) + vuln gates (Trivy)
* Add notifications (Slack/Teams)
* Implement canary/blue-green for sensitive apps
* Enforce image retention & preview env cleanup policies

**Deliverable:** Hardened, production-grade pipeline ecosystem

---

## 4. Migration Mapping

| Old Monolithic Stage              | New Modular Pipeline        |
| --------------------------------- | --------------------------- |
| Checkout SCM, Initialize, Prepare | Shared lib → `initPipeline` |
| Docker pull base image            | `base-image-build`          |
| Reset/Launch EKS environment      | `eks-env-manage`            |
| Build & Publish Docker image      | `svc-ci`                    |
| Deploy to EKS, Health checks      | `svc-deploy`                |

---

## 5. Governance & Ownership

* **Platform team**: Own `/platform/` pipelines, shared library, IAM roles, EKS clusters
* **App teams**: Own `/services/<svc>/` CI & deploy pipelines
* **Security**: Define scanning gates, IAM guardrails
* **Release Mgmt**: Approve prod deploys

---

## 6. Rollback Strategy

* Re-run `svc-deploy` with previous image tag
* Or `helm rollback <release> <revision>`

---

## 7. Success Metrics

* Each service deploys independently
* Build once, promote many (no rebuilds per env)
* CI runtime faster than monolith build stage
* Prod deploys gated & traceable (commit → image → helm release → cluster)
* MTTR for rollback < 5 minutes

---

## 8. Risks & Mitigations

| Risk                      | Mitigation                                |
| ------------------------- | ----------------------------------------- |
| Credential sprawl         | Centralize via assume-role per namespace  |
| Drift between Helm values | Pin chart versions + periodic `helm diff` |
| Rollback untested         | Validate rollback flow in QA              |
| App team learning curve   | Templates + onboarding documentation      |

---

## 9. Next Steps

1. Stand up shared library repo
2. Configure `/platform/eks-env-manage` + `/platform/base-image-build`
3. Pilot one service (`hello-svc`) with `Jenkinsfile.ci` + `Jenkinsfile.deploy`
4. Validate in dev → qa → prod
5. Roll out to all services, retire monolith

---

📌 *This migration guide ensures smooth transition to modular pipelines, balancing autonomy for app teams with centralized governance by platform teams.*
