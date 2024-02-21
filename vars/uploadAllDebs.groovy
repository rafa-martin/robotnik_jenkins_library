def call(String rosdistro, String type, String user, String password) {
  def api_base_repository = 'https://aptly-staging.robotnik.ws/api'
  // Generate random tag to avoid conflicts
  def tag = UUID.randomUUID().toString()
  def ubuntu_codename = getUbuntuCodename(rosdistro)
  sh 'find debs -name "*.deb" -exec curl -u ' + user + ':' + password + ' -XPOST -F file=@{} "' + api_base_repository + '/files/' + tag + '" \\;'
  sh 'curl -u ' + user + ':' + password + ' -XPOST "' + api_base_repository + '/repos/ros-' + rosdistro + '/file/' + tag + '"'
  sh 'curl -u ' + user + ':' + password + ' -XPUT -H \'Content-Type: application/json\' --data \'{\"ForceOverwrite\": true, \"Signing\": {\"Batch\": true, \"Passphrase\": \"\"}}\' "' + api_base_repository + '/publish/filesystem:publish:ros-' + rosdistro + '-' + type + '/' + ubuntu_codename + '"'
}
