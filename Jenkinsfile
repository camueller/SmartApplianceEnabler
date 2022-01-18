pipeline {
    agent any

    tools {
        // Install the Maven version configured and add it to the path.
        maven "Maven"
    }

    stages {
        stage('Build') {
            steps {
                //git 'https://github.com/camueller/SmartApplianceEnabler.git'
                //sh "mvn package -B -Pweb"
                sh "echo Build"
            }
        }
        stage('Deploy') {
            steps {
                dir('docker') {
                    sh "cp ../target/SmartApplianceEnabler*.war sae-ci/SmartApplianceEnabler.war"
                    // sh "docker build --tag=avanux/smartapplianceenabler-amd64:ci ./sae-ci"
                }
            }
        }
        stage('Launch') {
            steps {
                sh "Launched"
                // sh "docker run -d --rm -v sae:/opt/sae/data -p 8081:8080 --name sae avanux/smartapplianceenabler-amd64:ci"
            }
        }
    }
}
