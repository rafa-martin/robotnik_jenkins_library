def call(String rosdistro) {
  if (rosdistro == "kinetic") {
    return "xenial"
  } else if (rosdistro == "melodic") {
    return "bionic"
  } else if (rosdistro == "noetic") {
    return "focal"
  } else if (rosdistro == "foxy") {
    return "focal"
  } else if (rosdistro == "humble") {
    return "jammy"
  } else if (rosdistro == "iron") {
    return "jammy"
  } else if (rosdistro == "rolling") {
    return "jammy"
  } else {
    return "unknown"
  }
}