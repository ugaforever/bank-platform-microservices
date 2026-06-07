pipeline {
    agent any

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
    }
}