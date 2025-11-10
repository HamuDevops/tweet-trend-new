pipeline {
    agent {
        node { 
            label 'maven'
        }
    }

environment {
    PATH+MAVEN = '/opt/apache-maven-3.9.11/bin'
}


    stages {
        stage("build") {
            steps {
                sh 'mvn clean deploy'
            }
        }
    }
}
