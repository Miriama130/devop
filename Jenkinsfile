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
          stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
       stage('Checkout Code') {
         
    steps {
        git branch: 'Mariemtl-clean',
            credentialsId: 'TOKEN',
            url: 'https://github.com/Miriama130/devop.git',
            extensions: [[$class: 'GitLFSPull'], [$class: 'CloneOption', depth: 1, noTags: true, shallow: true]],
            config: ['http.postBuffer': '524288000'] // Augmente la taille du buffer
        
    }
}

        stage('Clean Docker Environment') {
    steps {
        sh '''
            # Stop and remove all containers from the compose file
            docker-compose -f docker-compose.yml down --remove-orphans --volumes || true
            
            # Remove specific containers by name if they still exist
            docker rm -f spring-foyer mysql-container || true
            
            # Remove old images
            docker rmi -f ${DOCKER_IMAGE}:${DOCKER_TAG} || true
            
            # Clean up any dangling resources
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
                    sh 'docker images | grep ${DOCKER_IMAGE}'
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
                    // Just ensure no containers are using the ports
                    sh 'docker-compose -f docker-compose.yml down || true'
                    
                    // Clean up any existing volumes if needed
                    sh 'docker volume rm dockerimage_mysql_data || true'
                }
            }
        }

       
    }

    post {
        success {
            echo "Pipeline executed successfully!"
            echo "Artifacts deployed to Nexus: ${NEXUS_URL}"
            echo "Docker Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo "Application deployed at: http://172.20.99.98:8082/Foyer"
        }
        failure {
            echo "Pipeline failed. Check the logs for errors."
        }
    }
}
