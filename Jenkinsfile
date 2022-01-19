pipeline {
    agent any

    tools {
        maven "Maven"
    }

    stages {
        stage('Build') {
            steps {
                git 'https://github.com/camueller/SmartApplianceEnabler.git'
                sh "mvn package -B -Pweb"
            }
        }
        stage('Deploy') {
            steps {
                dir('docker') {
                    sh "cp ../target/SmartApplianceEnabler*.war sae-ci/SmartApplianceEnabler.war"
                    sh "docker build --tag=avanux/smartapplianceenabler-amd64:ci ./sae-ci"
                }
            }
        }
        stage('Launch') {
            steps {
                sh "docker stop sae"
                sh "docker volume rm -f sae"
                sh "docker volume create sae"
                sh "docker run -d --rm -v sae:/opt/sae/data -p 8081:8080 --name sae avanux/smartapplianceenabler-amd64:ci"
            }
        }
        stage('Test') {
            environment {
                BROWSERSTACK_USERNAME = credentials('BROWSERSTACK_USERNAME')
                BROWSERSTACK_ACCESS_KEY = credentials('BROWSERSTACK_ACCESS_KEY')
            }
            steps {
                dir('src/test/angular') {
                    sh "npm i"
                    sh "npm run test:chrome"
                }
            }
        }
    }
}
