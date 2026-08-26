pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        ECR_REGISTRY = "844669502065.dkr.ecr.us-east-1.amazonaws.com"
        ECR_REPO     = "dev"
        IMAGE_TAG    = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        SONAR_PROJECT_KEY = "Ecommerce-X"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh 'mvn -B clean package'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Static Code Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh """
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.projectName=${SONAR_PROJECT_KEY} \
                          -Dsonar.host.url=\$SONAR_HOST_URL \
                          -Dsonar.token=\$SONAR_AUTH_TOKEN
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG} -t ${ECR_REGISTRY}/${ECR_REPO}:latest ."
            }
        }

        stage('Container Vulnerability Scan') {
            steps {
                sh """
                    trivy image --exit-code 0 --severity HIGH,CRITICAL \
                      --format template --template '@/usr/local/share/trivy/templates/html.tpl' \
                      -o trivy-report.html ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}
                """
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: '.',
                        reportFiles: 'trivy-report.html',
                        reportName: 'Trivy Vulnerability Report',
                        keepAll: true,
                        alwaysLinkToLastBuild: true
                    ])
                }
            }
        }

        stage('Push to ECR') {
            steps {
                sh """
                    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    docker push ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}
                    docker push ${ECR_REGISTRY}/${ECR_REPO}:latest
                """
            }
        }

        stage('Update GitOps Manifest') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-creds', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh """
                        sed -i "s|image: .*|image: ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}|" k8s/deployment.yaml
                        git config user.email "jenkins@commercex.local"
                        git config user.name "Jenkins CI"
                        git add k8s/deployment.yaml
                        git commit -m "Deploy image ${IMAGE_TAG} [ci skip]" || echo "No changes to commit"
                        git push https://\${GIT_USER}:\${GIT_TOKEN}@github.com/Yaswanth-Mostedge/Ecommerce-X.git HEAD:Main
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded — image ${IMAGE_TAG} pushed and manifest updated. Argo CD will sync automatically."
        }
        failure {
            echo "Pipeline failed at stage: ${env.STAGE_NAME}"
        }
        always {
            cleanWs()
        }
    }
}