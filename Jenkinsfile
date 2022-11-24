library identifier: 'icheko-jenkins-shared-lib@master',
        retriever: modernSCM([
            $class: 'GitSCMSource',
            id: '13ebda5f-2be5-4751-83d4-4d4500603cc5',
            remote: 'https://github.com/camueller/jenkins-shared-lib',
            traits: [[$class: 'jenkins.plugins.git.traits.BranchDiscoveryTrait']]
        ]) _

pipeline {
    agent any

    tools {
        maven "Maven"
    }

    parameters {
        booleanParam(name: 'DOCKER_PUSH', defaultValue: false, description: 'Push docker image to Dockerhub?')
        booleanParam(name: 'BETA_RELEASE', defaultValue: false, description: 'Is this a beta release?')
    }

    environment {
        VERSION = readMavenPom().getVersion()
        BROWSERSTACK_USERNAME = credentials('BROWSERSTACK_USERNAME')
        BROWSERSTACK_ACCESS_KEY = credentials('BROWSERSTACK_ACCESS_KEY')
        DOCKER_TAG = "${env.BETA_RELEASE == "true" ? "beta" : "latest"}"
    }

    stages {
        stage('Build') {
            steps {
                sh "mvn clean -B -Pweb"
                sh "mvn package -B -Pweb"
            }
        }
        stage('Dockerize') {
            steps {
                dir('docker') {
                    sh "cp ../target/SmartApplianceEnabler*.war sae-ci/SmartApplianceEnabler.war"
                    sh "docker build --tag=avanux/smartapplianceenabler-amd64:ci ./sae-ci"
                }
            }
        }
        stage('Chrome') {
            steps {
                sh "docker stop sae || true"
                sh "docker volume rm -f sae"
                sh "docker volume create sae"
                sh "docker run -d --rm -v sae:/opt/sae/data -p 8081:8080 --name sae avanux/smartapplianceenabler-amd64:ci"
                dir('src/test/angular') {
                    sh "npm i"
                    sh "npm run test:chrome"
                }
            }
        }
        stage('Firefox') {
            steps {
                sh "docker stop sae || true"
                sh "docker volume rm -f sae"
                sh "docker volume create sae"
                sh "docker run -d --rm -v sae:/opt/sae/data -p 8081:8080 --name sae avanux/smartapplianceenabler-amd64:ci"
                dir('src/test/angular') {
                    sh "npm run test:firefox"
                }
            }
        }
        stage('Safari') {
            steps {
                sh "docker stop sae || true"
                sh "docker volume rm -f sae"
                sh "docker volume create sae"
                sh "docker run -d --rm -v sae:/opt/sae/data -p 8081:8080 --name sae avanux/smartapplianceenabler-amd64:ci"
                dir('src/test/angular') {
                    sh "npm run test:safari"
                }
            }
        }
        stage('Stop') {
            steps {
                sh "docker stop sae || true"
            }
        }
        stage('Publish') {
            when {
                expression { params.DOCKER_PUSH }
            }
            steps {
                dir('docker') {
                    sh "cp ../target/SmartApplianceEnabler*.war sae-amd64/"
                    sh "sed -i 's#@project.version@#'\"$VERSION\"'#' ./sae-amd64/Dockerfile"
                    sh "docker build --tag=avanux/smartapplianceenabler-amd64:$VERSION ./sae-amd64"
                    sh "docker tag avanux/smartapplianceenabler-amd64:$VERSION avanux/smartapplianceenabler-amd64:$DOCKER_TAG"
                }
                withCredentials([usernamePassword(credentialsId: 'docker', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh "echo $PASSWORD | docker login --username $USERNAME --password-stdin"
                    dir('docker') {
                        sh "docker push avanux/smartapplianceenabler-amd64:$VERSION"
                        sh "docker push avanux/smartapplianceenabler-amd64:$DOCKER_TAG"
                    }
                }
                sh 'scp target/SmartApplianceEnabler-"$VERSION".war jenkins@raspi2:/home/jenkins/'
                build 'SmartApplianceEnabler-arm32'
            }
        }
    }
}
