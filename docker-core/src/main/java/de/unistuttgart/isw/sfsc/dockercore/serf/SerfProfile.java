package de.unistuttgart.isw.sfsc.dockercore.serf;

enum SerfProfile {
  LAN("lan"),
  LOCAL("local"),
  WAN("wan");

  private final String asCommandString;

  SerfProfile(String asCommandString) {
    this.asCommandString = asCommandString;
  }

  String getAsCommandString() {
    return asCommandString;
  }
}
