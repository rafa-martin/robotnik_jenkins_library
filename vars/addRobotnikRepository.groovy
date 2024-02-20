def call(String rosdistro, String type, String arch = 'amd64') {
  def ubuntu_codename = getUbuntuCodename(rosdistro)
  def base_repository = 'https://aptly-staging.robotnik.ws'
  def keyring = '/usr/share/keyrings/robotnik-robotnik.gpg'
  // Add rosdep repository
  sh 'echo "yaml https://raw.githubusercontent.com/RobotnikAutomation/robotnik-packages/main/' + rosdistro + '/rosdep.yaml" | sudo tee -a /etc/ros/rosdep/sources.list.d/20-default.list'
  // Add public key to apt
  sh 'curl ' + base_repository + '/public.key | sudo gpg --dearmor -o ' + keyring
  // Add debian repository to sources.list
  sh 'echo "deb [signed-by=' + keyring + ' arch=' + arch + '] ' + base_repository + '/ros-' + rosdistro + '-' + type + ' ' + ubuntu_codename + ' main" | sudo tee -a /etc/apt/sources.list'
}
