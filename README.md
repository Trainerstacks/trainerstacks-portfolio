# TrainerStacks Portfolio

Senthil's trainer portfolio website, built as a full DevSecOps demo project:
**Spring Boot → Maven tests → SonarQube → Trivy → Docker Hub → Kubernetes (kubeadm)**, automated by Jenkins.

The home page shows **which pod and node served the request**, so students can watch Kubernetes load-balance across replicas live.

## 1. Make it yours

| File | What to change |
|---|---|
| `src/main/resources/application.properties` | email, phone, LinkedIn (GitHub already set) |
| `Jenkinsfile` | `K8S_SERVER` only if your master private IP changes |

Already configured: Docker Hub `trainerstacks/trainerstacks-portfolio`, GitHub `https://github.com/Trainerstacks/trainerstacks-portfolio`.

Page text (courses, journey, projects) is in `src/main/resources/templates/index.html`.

## 2. Run locally

```bash
mvn spring-boot:run
# open http://localhost:8080
# JSON:  http://localhost:8080/api/info
```

## 3. Push to your GitHub

```bash
git init
git add .
git commit -m "TrainerStacks portfolio"
git branch -M main
git remote add origin https://github.com/Trainerstacks/trainerstacks-portfolio.git
git push -u origin main
```

## 4. Build and push the image manually (without Jenkins)

```bash
docker login
docker build -t trainerstacks/trainerstacks-portfolio:v1 -t trainerstacks/trainerstacks-portfolio:latest .
docker push trainerstacks/trainerstacks-portfolio:v1
docker push trainerstacks/trainerstacks-portfolio:latest
```

## 5. Deploy to the kubeadm cluster (on master)

```bash
kubectl create ns webapps
kubectl apply -f k8s/deployment-service.yaml -n webapps
kubectl get pods -o wide -n webapps
kubectl get svc -n webapps
```

Open `http://<WORKER-PUBLIC-IP>:30080` (allow port **30080** in the worker security group).

## 6. Live demo script for class

```bash
# Watch requests land on different pods
for i in $(seq 1 10); do curl -s http://<WORKER-IP>:30080/api/info; echo; done

# Scale up and down, then refresh the page / click "Ask again"
kubectl scale deployment portfolio-deployment --replicas=5 -n webapps
kubectl get pods -o wide -n webapps

# Self-healing: delete a pod and watch a new one replace it
kubectl delete pod <pod-name> -n webapps
kubectl get pods -n webapps -w

# Rolling update: change app.version in application.properties, rebuild as :v2, then
kubectl set image deployment/portfolio-deployment portfolio=trainerstacks/trainerstacks-portfolio:v2 -n webapps
kubectl rollout status deployment/portfolio-deployment -n webapps
kubectl rollout undo deployment/portfolio-deployment -n webapps   # rollback
```

## 7. Jenkins setup

**Plugins:** Eclipse Temurin installer, Pipeline Maven Integration, SonarQube Scanner, Docker Pipeline, Kubernetes CLI, Pipeline: Stage View.

**Tools (Manage Jenkins → Tools):** JDK `jdk17`, Maven `maven3`, SonarQube Scanner `sonar-scanner`, Docker `docker`.

**SonarQube server (Manage Jenkins → System):** name `sonar`, token credential `sonar-token`.

**Credentials:**

| ID | Type | Value |
|---|---|---|
| `docker-cred` | Username + password | Docker Hub username + access token |
| `sonar-token` | Secret text | SonarQube token |
| `k8-cred` | Secret text | Token from `k8s/jenkins-rbac.yaml` (see comments in file) |

The Jenkins server also needs `docker`, `trivy` and `kubectl` installed.

## Project layout

```
├── Dockerfile                 multi-stage build, non-root runtime
├── Jenkinsfile                full CI/CD pipeline
├── pom.xml
├── k8s/
│   ├── deployment-service.yaml   3 replicas + NodePort 30080 + health probes
│   └── jenkins-rbac.yaml         service account for Jenkins deploys
└── src/
    ├── main/java/com/trainerstacks/portfolio/
    │   ├── PortfolioApplication.java
    │   ├── HomeController.java    renders the page
    │   ├── InfoController.java    /api/info JSON
    │   └── RuntimeInfo.java       reads POD_NAME / NODE_NAME
    ├── main/resources/
    │   ├── application.properties
    │   ├── templates/index.html
    │   └── static/css/style.css
    └── test/java/.../PortfolioApplicationTests.java
```
