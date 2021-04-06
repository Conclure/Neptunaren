package me.conclure.neptunaren;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    // declare argument specs
    OptionParser parser =
        new OptionParser() {
          {
            this.accepts("token", "Bot token").withRequiredArg().required();
          }
        };

    OptionSet options = null;

    // parsing arguments
    try {
      options = parser.parse(args);
    } catch (OptionException e) {
      LOGGER.error("", e);
    }

    // start of bot
    Main.start(options);
  }

  private static void start(OptionSet optionSet) {
    try {
      Bot.init(optionSet);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}