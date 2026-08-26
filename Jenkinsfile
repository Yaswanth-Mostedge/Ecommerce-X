pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.9-eclipse-temurin-17
    command: ['cat']
    tty: true
    resources:
      requests: { cpu: 250m, memory: 512Mi }
      limits: { cpu: 500m, memory: 768Mi }
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command: ['/busybox/cat']
    tty: true
    volumeMounts:
    - name: docker-config
      mountPath: /kaniko/.docker
    resources:
      requests: { cpu: 250m, memory: 512Mi }
      limits: { cpu: 500m, memory: 768Mi }
  - name: trivy
    image: aquasec/trivy:latest
    command: ['cat']
    tty: true
    resources:
      requests: { cpu: 100m, memory: 256Mi }
      limits: { cpu: 300m, memory: 512Mi }
  - name: awscli
    image: amazon/aws-cli:2.15.0
    command: ['cat']
    tty: true
    volumeMounts:
    - name: docker-config
      mountPath: /kaniko/.docker
  volumes:
  - name: docker-config
    emptyDir: {}
"""
        }
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        ECR_REGISTRY = "844669502065.dkr.ecr.us-east-1.amazonaws.com"
        ECR_REPO     = "dev"
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
                container('maven') {
                    sh 'mvn -B clean package'
                }
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
                container('maven') {
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
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Prepare Registry Auth') {
            steps {
                container('awscli') {
                    sh """
                        TOKEN=\$(aws ecr get-login-password --region us-east-1)
                        AUTH=\$(echo -n "AWS:\$TOKEN" | base64 -w0)
                        mkdir -p /kaniko/.docker
                        echo "{\\"auths\\":{\\"${ECR_REGISTRY}\\":{\\"auth\\":\\"\$AUTH\\"}}}" > /kaniko/.docker/config.json
                    """
                }
            }
        }

        stage('Build & Push Image (Kaniko)') {
            steps {
                container('kaniko') {
                    sh """
                        /kaniko/executor --context `pwd` \
                          --destination=${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG} \
                          --destination=${ECR_REGISTRY}/${ECR_REPO}:latest
                    """
                }
            }
        }

        stage('Container Vulnerability Scan') {
            steps {
                container('trivy') {
                    sh "trivy image --exit-code 0 --severity HIGH,CRITICAL ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
                }
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