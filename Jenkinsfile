pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        IMAGE_NAME = 'chungnam-team3-be'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'
        DISCORD_WEBHOOK = credentials('discord-webhook-url')
    }

    stages {
        stage('준비') {
            steps {
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    // 브랜치별 환경 설정
                    if (env.BRANCH_NAME == 'main') {
                        env.TARGET_ENV = 'prod'
                        env.DOCKER_TAG = "${env.BUILD_NUMBER}"
                    } else if (env.BRANCH_NAME == 'develop') {
                        env.TARGET_ENV = 'dev'
                        env.DOCKER_TAG = "dev-${env.BUILD_NUMBER}"
                    } else {
                        env.TARGET_ENV = 'skip'
                        echo "❌ ${env.BRANCH_NAME} 브랜치는 배포하지 않습니다."
                    }
                }
            }
        }

        stage('테스트') {
            when {
                expression { env.TARGET_ENV != 'skip' }
            }
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean test
                '''
            }
        }

        stage('빌드') {
            when {
                expression { env.TARGET_ENV != 'skip' }
            }
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker 이미지 생성') {
            when {
                expression { env.TARGET_ENV != 'skip' }
            }
            steps {
                script {
                    def imageTag = "${DOCKER_REGISTRY}/${IMAGE_NAME}:${env.DOCKER_TAG}"
                    def latestTag = "${DOCKER_REGISTRY}/${IMAGE_NAME}:${env.TARGET_ENV}-latest"

                    sh """
                        docker build -t ${imageTag} .
                        docker tag ${imageTag} ${latestTag}
                    """

                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo \$DOCKER_PASS | docker login ${DOCKER_REGISTRY} -u \$DOCKER_USER --password-stdin
                            docker push ${imageTag}
                            docker push ${latestTag}
                        """
                    }

                    sh "docker rmi ${imageTag} ${latestTag} || true"
                }
            }
        }

        stage('배포') {
            when {
                expression { env.TARGET_ENV != 'skip' }
            }
            steps {
                script {
                    build job: 'Deploy-Team3-App', parameters: [
                        string(name: 'ENVIRONMENT', value: env.TARGET_ENV),
                        string(name: 'IMAGE_TAG', value: env.DOCKER_TAG),
                        string(name: 'GIT_COMMIT', value: env.GIT_COMMIT_SHORT)
                    ]
                }
            }
        }
    }

    post {
        success {
            script {
                if (env.TARGET_ENV != 'skip') {
                    discordSend(
                        title: "✅ 배포 성공!",
                        description: """
                        **환경:** ${env.TARGET_ENV}
                        **브랜치:** ${env.BRANCH_NAME}
                        **이미지:** ${IMAGE_NAME}:${env.DOCKER_TAG}
                        **커밋:** ${env.GIT_COMMIT_SHORT}
                        **URL:** ${env.TARGET_ENV == 'prod' ? 'https://api.your-domain.com' : 'https://dev-api.your-domain.com'}
                        """,
                        webhookURL: env.DISCORD_WEBHOOK
                    )
                }
            }
        }

        failure {
            script {
                if (env.TARGET_ENV != 'skip') {
                    discordSend(
                        title: "❌ 배포 실패!",
                        description: """
                        **환경:** ${env.TARGET_ENV}
                        **브랜치:** ${env.BRANCH_NAME}
                        **실패 단계:** ${env.STAGE_NAME}
                        **커밋:** ${env.GIT_COMMIT_SHORT}
                        """,
                        webhookURL: env.DISCORD_WEBHOOK
                    )
                }
            }
        }

        always {
            cleanWs()
        }
    }
}