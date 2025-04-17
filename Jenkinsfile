pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'miriama13/foyer-app'
        DOCKER_TAG = 'latest'
        SONARQUBE_URL = 'http://172.20.99.98:9000'
        NEXUS_URL = 'http://172.20.99.98:8081/repository/maven-releases/'
        ARTIFACT_VERSION = "0.0.1-${BUILD_NUMBER}"
        ARTIFACT_NAME = 'Foyer'
        ARTIFACT_PATH = "tn/esprit/spring/${ARTIFACT_NAME}/${ARTIFACT_VERSION}/${ARTIFACT_NAME}-${ARTIFACT_VERSION}.jar"
    }

    stages {
        stage('Nettoyer Workspace') {
            steps {
                cleanWs()
                sh 'rm -rf .git || true'
            }
        }

        stage('Checkout Code') {
            steps {
                script {
                    try {
                        retry(3) {
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: 'Mariemtl-clean']],
                                userRemoteConfigs: [[
                                    url: 'https://github.com/Miriama130/devop.git',
                                    credentialsId: 'TOKEN',
                                    timeout: 30
                                ]],
                                extensions: [
                                    [$class: 'CloneOption', 
                                     depth: 1, 
                                     noTags: true, 
                                     shallow: true,
                                     timeout: 60],
                                    [$class: 'CleanBeforeCheckout'],
                                    [$class: 'LocalBranch', localBranch: 'Mariemtl-clean']
                                ],
                                gitTool: 'Default'
                            ])
                        }
                    } catch (Exception e) {
                        echo "Échec du checkout standard, tentative de réparation Git..."
                        sh '''
                            rm -rf *
                            git init
                            git remote add origin https://github.com/Miriama130/devop.git
                            git config --global http.postBuffer 524288000
                            git config --global http.sslVerify false
                            git fetch --depth=1 origin Mariemtl-clean
                            git checkout Mariemtl-clean
                        '''
                    }
                }
                
                sh 'git lfs pull || true'
            }
        }

        stage('Clean Docker Environment') {
            steps {
                sh '''
                    docker-compose -f docker-compose.yml down --remove-orphans --volumes || true
                    docker rm -f spring-foyer mysql-container || true
                    docker rmi -f ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                    docker system prune -f
                '''
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonarqubetoken', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=FoyerApp \
                        -Dsonar.host.url=${SONARQUBE_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )]) {
                        sh """
                            mvn deploy \
                            -DaltDeploymentRepository=nexus-releases::default::${NEXUS_URL} \
                            -DrepositoryId=nexus-releases \
                            -s settings.xml
                        """
                    }
                }
            }
        }

        stage('Download Artifact from Nexus') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'nexus',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS'
                    )]) {
                        sh 'mkdir -p target'
                        sh """
                            curl -u ${NEXUS_USER}:${NEXUS_PASS} \
                            -o target/${ARTIFACT_NAME}-${ARTIFACT_VERSION}.jar \
                            "${NEXUS_URL}${ARTIFACT_PATH}"
                        """
                        sh 'ls -l target/'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh 'ls -l target/*.jar'
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker images | grep ${DOCKER_IMAGE}"
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockercredentials', 
                    usernameVariable: 'DOCKER_USERNAME', 
                    passwordVariable: 'DOCKER_PASSWORD'  
                )]) {
                    sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh "docker logout"
                }
            }
        }

        stage('Prepare Ports') {
            steps {
                script {
                    sh 'docker-compose -f docker-compose.yml down || true'
                    sh 'docker volume rm dockerimage_mysql_data || true'
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline exécuté avec succès!"
            echo "Artéfacts déployés sur Nexus: ${NEXUS_URL}"
            echo "Image Docker: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo "Application disponible à: http://172.20.99.98:8082/Foyer"
        }
        failure {
            echo "Échec du pipeline. Vérifiez les logs pour les erreurs."
            echo "Problèmes possibles:"
            echo "1. Problèmes de connexion Git"
            echo "2. Échec des tests ou build Maven"
            echo "3. Problèmes d'authentification Docker/Nexus"
        }
    }
}
