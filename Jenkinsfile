pipeline {
    agent any

    environment {
        IMAGE_TAG       = "latest"
    }

    stages {
            stage('Build & Unit Tests') {
                parallel {
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
                    stage('Chassis') {
                        steps {
                            dir('chassis') {
                                sh 'mvn clean test'
                            }
                        }
                    }
                   stage('Gateway') {
                        steps {
                            dir('gateway') {
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
                   sh """
                   docker build -t account-service:${IMAGE_TAG} account-service
                   docker build -t cash-service:${IMAGE_TAG} cash-service
                   docker build -t notification-service:${IMAGE_TAG} notification-service
                   docker build -t transfer-service:${IMAGE_TAG} transfer-service
                   docker build -t gateway:${IMAGE_TAG} gateway
                   """
            }
        }
    }
}