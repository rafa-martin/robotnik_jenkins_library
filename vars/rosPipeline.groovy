def call(Map params = [:]) {
  String rosdistro = ['noetic']
  String upstreamProjects = params.upstreamProjects
  boolean upload = params.containsKey('upload') ? params.upload : false

  pipeline {
    agent none
    options {
      checkoutToSubdirectory('src')
      disableConcurrentBuilds()
      retry(1)
      timeout(time: 1, unit: 'HOURS')
      timestamps()
    }
    triggers {
      githubPush()
      upstream(upstreamProjects: upstreamProjects, threshold: hudson.model.Result.SUCCESS)
      pollSCM('H 8 * * 1')
    }
    stages {
      stage('main'){
        matrix {
          axes {
            axis {
              name 'ROS_DISTRO'
              values 'melodic', 'noetic'
            }
          }
          agent {
            kubernetes {
              yaml """
                apiVersion: v1
                kind: Pod
                metadata:
                  labels:
                    ros-distro: ${ROS_DISTRO}
                spec:
                  containers:
                  - name: builder
                    image: registry.robotnik.ws/library/ros:${ROS_DISTRO}-builder
                """
              retries 1
            }
          }
          when { anyOf {
            // Only build if env.ROS_DISTRO is contained in the list
            expression { rosdistro.contains(env.ROS_DISTRO) }
          } }
          stages {
            stage('configure') {
              steps {
                container('builder') {
                  addRobotnikRepository("${ROS_DISTRO}", 'staging', 'amd64')
                }
              }
            }
            stage('install-deps') {
              steps {
                container('builder') {
                  sh 'sudo apt-get update'
                  sh 'rosdep update --include-eol-distros --rosdistro="${ROS_DISTRO}"'
                  sh 'rosdep install --from-paths src --ignore-src --rosdistro="${ROS_DISTRO}" -y'
                }
              }
            }
            stage('build') {
              steps {
                container('builder') {
                  sh 'compile_workspace.sh'
                }
              }
            }
            stage('packaging') {
              steps {
                container('builder') {
                  sh 'generate_debs.sh'
                }
              }
            }
            stage('upload') {
              when {
                expression {
                  return upload
                }
              }
              steps {
                container('builder') {
                  withCredentials([usernamePassword(credentialsId: 'aptly-staging-jenkins', usernameVariable: 'APTLY_USER', passwordVariable: 'APTLY_PASS')]) {
                    uploadAllDebs("${ROS_DISTRO}", 'staging', '${APTLY_USER}', '${APTLY_PASS}')
                  }
                }
              }
            }
          }
        }
      }
    }
    post {
      success {
        archiveArtifacts artifacts: 'debs/*.deb'
      }
      always {
        archiveArtifacts artifacts: 'logs/**'
      }
    }
  }
}
