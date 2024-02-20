def call(String rosdistro, String type, String user, String password) {
  // Generate random tag to avoid conflicts
  def tag = UUID.randomUUID().toString()
  def ubuntu_codename = getUbuntuCodename(rosdistro)
  sh 'find debs -name "*.deb" -exec curl -u ' + user + ':' + password + ' -XPOST -F file=@{} "https://aptly-staging.robotnik.ws/files/' + tag + '" \\;'
  sh 'curl -u ' + user + ':' + password + ' -XPOST "https://aptly-staging.robotnik.ws/repos/ros-' + rosdistro + '/file/' + tag + '"'
  sh 'curl -u ' + user + ':' + password + ' -XPUT -H \'Content-Type: application/json\' --data \'{\"ForceOverwrite\": true, \"Signing\": {\"Batch\": true, \"Passphrase\": \"\"}}\' "https://aptly-staging.robotnik.ws/publish/filesystem:publish:ros-' + rosdistro + '-' + type + '/' + ubuntu_codename + '"'
}
