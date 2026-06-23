pipeline {
    agent any

    environment {
        DB_NAME               = 'mydb'
        DB_USER               = 'myuser'
        DB_PASSWORD           = credentials('DB_PASSWORD')
        ACCOUNT_PASSWORD      = credentials('ACCOUNT_PASSWORD')
        CASH_PASSWORD         = credentials('CASH_PASSWORD')
        TRANSFER_PASSWORD     = credentials('TRANSFER_PASSWORD')
        NOTIFICATION_PASSWORD = credentials('NOTIFICATION_PASSWORD')
        IMAGE_TAG             = "latest"
        ROOT_DIR              = '/home/user/java/github/bank-platform-microservices'
    }

    stages {
            stage('Build & Unit Tests') {
                parallel {
                    stage('Chassis') {
                        steps {
                            dir("${ROOT_DIR}/chassis") {
                                sh 'mvn clean test'
                            }
                        }
                    }
                    stage('Account Service') {
                        steps {
                            dir("${ROOT_DIR}/account-service") {
                                sh 'mvn clean test'
                            }
                        }
                    }
                    stage('Cash Service') {
                        steps {
                            dir("${ROOT_DIR}/cash-service") {
                                sh 'mvn clean test'
                            }
                        }
                    }
                   stage('Notification Service') {
                        steps {
                            dir("${ROOT_DIR}/notification-service") {
                                sh 'mvn clean test'
                            }
                        }
                   }
                   stage('Transfer Service') {
                        steps {
                            dir("${ROOT_DIR}/transfer-service") {
                                sh 'mvn clean test'
                            }
                        }
                   }
                }
            }

            stage('Build Docker Images') {
                steps {
                    dir(ROOT_DIR) {
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
            }

            stage('Install PostgreSQL to TEST') {
                steps {
                    sh """
                    helm upgrade --install postgres oci://registry-1.docker.io/bitnamicharts/postgresql \\
                    --namespace test --create-namespace \\
                    --set auth.database=${DB_NAME} \\
                    --set auth.username=${DB_USER} \\
                    --set auth.password=${DB_PASSWORD} \\
                    --set fullnameOverride=bank-secret-db
                    """
                }
            }

            stage('Create DB Secrets for TEST') {
                steps {
                    sh """
                    kubectl create secret generic bank-account-db-secret \\
                    --from-literal=password=${ACCOUNT_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic bank-cash-db-secret \\
                    --from-literal=password=${CASH_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic bank-transfer-db-secret \\
                    --from-literal=password=${TRANSFER_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic bank-notification-db-secret \\
                    --from-literal=password=${NOTIFICATION_PASSWORD} \\
                    -n test --dry-run=client -o yaml | kubectl apply -f -
                    """
                }
            }

            stage('Build Helm Chart') {
                steps {
                    dir(ROOT_DIR) {
                        sh """
                        helm dependency update ./helm/

                        helm dependency build ./helm/
                        """
                    }
                }
            }

            stage('Helm Deploy to TEST') {
                steps {
                    dir(ROOT_DIR) {
                        sh """
                        helm upgrade --install bank ./helm/ \\
                        --namespace test --create-namespace \\
                        --set kafka.enabled=true \\
                        \\
                        --set debezium-operator.enabled=true \\
                        \\
                        --set account-db.enabled=true \\
                        --set account-db.auth.database=accounts \\
                        --set account-db.auth.username=account_user \\
                        --set account-db.auth.password=${ACCOUNT_PASSWORD} \\
                        --set account-db.auth.postgresPassword=${ACCOUNT_PASSWORD} \\
                        \\
                        --set cash-db.enabled=true \\
                        --set cash-db.auth.database=cashes \\
                        --set cash-db.auth.username=cash_user \\
                        --set cash-db.auth.password=${CASH_PASSWORD} \\
                        --set cash-db.auth.postgresPassword=${CASH_PASSWORD} \\
                        \\
                        --set transfer-db.enabled=true \\
                        --set transfer-db.auth.database=transfers \\
                        --set transfer-db.auth.username=transfer_user \\
                        --set transfer-db.auth.password=${TRANSFER_PASSWORD} \\
                        --set transfer-db.auth.postgresPassword=${TRANSFER_PASSWORD} \\
                        \\
                        --set notification-db.enabled=true \\
                        --set notification-db.auth.database=notifications \\
                        --set notification-db.auth.username=notification_user \\
                        --set notification-db.auth.password=${NOTIFICATION_PASSWORD} \\
                        --set notification-db.auth.postgresPassword=${NOTIFICATION_PASSWORD}
                        """
                    }
                }
            }
    }
}