pipeline {
    agent any

    tools {
        jdk 'jdk17'          // Manage Jenkins > Tools: JDK named jdk17
        maven 'maven3'       // Manage Jenkins > Tools: Maven named maven3
    }

    environment {
        SCANNER_HOME   = tool 'sonar-scanner'                 // SonarQube Scanner tool name
        DOCKERHUB_USER = 'trainerstacks'
        IMAGE          = "${DOCKERHUB_USER}/trainerstacks-portfolio"
        TAG            = "${BUILD_NUMBER}"
        K8S_SERVER     = 'https://172.31.3.126:6443'          // your master private IP
        NAMESPACE      = 'webapps'
    }

    stages {

        stage('Git Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Trainerstacks/trainerstacks-portfolio.git'
            }
        }

        stage('Compile') {
            steps { sh 'mvn -B compile' }
        }

        stage('Test') {
            steps { sh 'mvn -B test' }
        }

        stage('Trivy File System Scan') {
            steps { sh 'trivy fs --format table -o trivy-fs-report.html .' }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {               // Manage Jenkins > System: SonarQube server named sonar
                    sh '''$SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectName=TrainerStacksPortfolio \
                        -Dsonar.projectKey=TrainerStacksPortfolio \
                        -Dsonar.java.binaries=target/classes'''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
                }
            }
        }

        stage('Build Package') {
            steps { sh 'mvn -B package -DskipTests' }
        }

        stage('Build & Tag Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                        sh "docker build -t ${IMAGE}:${TAG} -t ${IMAGE}:latest ."
                    }
                }
            }
        }

        stage('Trivy Image Scan') {
            steps { sh "trivy image --format table -o trivy-image-report.html ${IMAGE}:${TAG}" }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-cred', toolName: 'docker') {
                        sh "docker push ${IMAGE}:${TAG}"
                        sh "docker push ${IMAGE}:latest"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig(caCertificate: '', clusterName: 'kubernetes', contextName: '',
                               credentialsId: 'k8-cred', namespace: "${NAMESPACE}",
                               restrictKubeConfigAccess: false, serverUrl: "${K8S_SERVER}") {
                    // Swap the placeholder image for this build's image, then apply
                    sh """
                        sed 's|image: .*trainerstacks-portfolio:.*|image: ${IMAGE}:${TAG}|' k8s/deployment-service.yaml \
                          | kubectl apply -n ${NAMESPACE} -f -
                        kubectl rollout status deployment/portfolio-deployment -n ${NAMESPACE} --timeout=180s
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                withKubeConfig(caCertificate: '', clusterName: 'kubernetes', contextName: '',
                               credentialsId: 'k8-cred', namespace: "${NAMESPACE}",
                               restrictKubeConfigAccess: false, serverUrl: "${K8S_SERVER}") {
                    sh "kubectl get pods -o wide -n ${NAMESPACE}"
                    sh "kubectl get svc -n ${NAMESPACE}"
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'trivy-*.html', allowEmptyArchive: true
        }
    }
}
