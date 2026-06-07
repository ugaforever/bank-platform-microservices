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
                    kubectl create secret generic account-service-account-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic cash-service-cash-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic transfer-service-transfer-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic notification-service-notification-db \\
                    --from-literal=password=${DB_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -
                    """
                }
            }

            stage('Helm Deploy to TEST') {
                steps {
                    sh """
                    helm upgrade --install account-service helm/charts/account-service \\
                      --namespace test --create-namespace \\
                      --set image.repository=account-service \\
                      --set image.tag=${IMAGE_TAG} \\
                      --set ingress.enabled=true \\
                      --set ingress.hosts[0].host=account.test.local \\
                      --set ingress.hosts[0].paths[0].path="/" \\
                      --set ingress.hosts[0].paths[0].pathType="ImplementationSpecific"

                    helm upgrade --install cash-service helm/charts/cash-service \\
                      --namespace test --create-namespace \\
                      --set image.repository=cash-service \\
                      --set image.tag=${IMAGE_TAG} \\
                      --set ingress.enabled=true \\
                      --set ingress.hosts[0].host=cash.test.local \\
                      --set ingress.hosts[0].paths[0].path="/" \\
                      --set ingress.hosts[0].paths[0].pathType="ImplementationSpecific"

                    helm upgrade --install transfer-service helm/charts/transfer-service \\
                      --namespace test --create-namespace \\
                      --set image.repository=transfer-service \\
                      --set image.tag=${IMAGE_TAG} \\
                      --set ingress.enabled=true \\
                      --set ingress.hosts[0].host=transfer.test.local \\
                      --set ingress.hosts[0].paths[0].path="/" \\
                      --set ingress.hosts[0].paths[0].pathType="ImplementationSpecific"

                    helm upgrade --install notification-service helm/charts/notification-service \\
                      --namespace test --create-namespace \\
                      --set image.repository=notification-service \\
                      --set image.tag=${IMAGE_TAG} \\
                      --set ingress.enabled=true \\
                      --set ingress.hosts[0].host=notification.test.local \\
                      --set ingress.hosts[0].paths[0].path="/" \\
                      --set ingress.hosts[0].paths[0].pathType="ImplementationSpecific"
                    """
                }
            }
        }
    }
}