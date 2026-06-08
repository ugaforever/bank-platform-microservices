pipeline {
    agent any

    environment {
        DB_NAME         = 'mydb'
        DB_USER         = 'myuser'
        DB_PASSWORD     = credentials('DB_PASSWORD')
        IMAGE_TAG       = "latest"
    }

    stages {
            stage('Build & Unit Tests') {
                parallel {
                    stage('Chassis') {
                        steps {
                            dir('chassis') {
                                sh 'mvn clean test'
                            }
                        }
                    }
                    stage('Account Service') {
                        steps {
                            dir('account-service') {
                                sh 'mvn clean test'
                            }
                        }
                    }
                    stage('Cash Service') {
                        steps {
                            dir('cash-service') {
                                sh 'mvn clean test'
                            }
                        }
                    }
                   stage('Notification Service') {
                        steps {
                            dir('notification-service') {
                                sh 'mvn clean test'
                            }
                        }
                   }
                   stage('Transfer Service') {
                        steps {
                            dir('transfer-service') {
                                sh 'mvn clean test'
                            }
                        }
                   }
                }
            }

            stage('Build Docker Images') {
                steps {
                    dir('/home/user/java/github/bank-platform-microservices') {
                                        sh '''
                                            echo "Building Docker images from: $(pwd)"

                                            docker build -t account-service:${IMAGE_TAG} -f account-service/Dockerfile .
                                            docker build -t notification-service:${IMAGE_TAG} -f notification-service/Dockerfile .
                                            docker build -t cash-service:${IMAGE_TAG} -f cash-service/Dockerfile .
                                            docker build -t transfer-service:${IMAGE_TAG} -f transfer-service/Dockerfile .

                                            echo "All images built successfully!"
                                            docker images | grep -E "account|notification|cash|transfer|gateway"
                                        '''
                                    }
            }

            stage('Install PostgreSQL to TEST') {
                steps {
                    sh """
                    helm upgrade --install postgres oci://registry-1.docker.io/bitnamicharts/postgresql \\
                    --namespace test --create-namespace \\
                    --set auth.database=${DB_NAME} \\
                    --set auth.username=${DB_USER} \\
                    --set auth.password=${DB_PASSWORD}
                    """
                }
            }

            stage('Create DB Secrets for TEST') {
                steps {
                    sh """
                    kubectl create secret generic account-service-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic cash-service-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic transfer-service-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic notification-service-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -
                    """
                }
            }

            stage('Helm Deploy to TEST') {
                steps {
                    sh """
                    helm upgrade --install bank helm/ \\
                      --namespace test --create-namespace \\
                      --set kafka.enabled=true \\
                      --set account-db.enabled=true \\
                      --set cash-db.enabled=true \\
                      --set transfer-db.enabled=true \\
                      --set notification-db.enabled=true
                    """
                }
            }
        }
    }
}