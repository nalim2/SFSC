package de.unistuttgart.isw.sfsc.dockercore.serf;

enum SerfOption {
  NODE {
    @Override
    String getCommandString(String value) {
      return "-node=" + value;
    }
  },
  DISCOVER {
    @Override
    String getCommandString(String value) {
      return "-discover=" + value;
    }
  },
  BIND_ADDRESS {
    @Override
    String getCommandString(String value) {
      return "-bind=" + value;
    }
  },
  JOIN_ADDRESS {
    @Override
    String getCommandString(String value) {
      return "-join=" + value;
    }
  },
  TAG_PORT {
    @Override
    String getCommandString(String value) {
      return "-tag=port=\"" + value + "\"";
    }
  };

  abstract String getCommandString(String value);
}
