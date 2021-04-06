package me.conclure.neptunaren;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import joptsimple.OptionSet;
import me.conclure.neptunaren.command.CommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Bot {
  private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

  private final SchedulerAdapter adapter;
  private final JDA jda;

  private Bot(String token) throws Exception {
    this.adapter = new SchedulerAdapter();
    this.jda = JDABuilder.createDefault(token)
        .setCallbackPool(this.adapter.async())
        .setAutoReconnect(true)
        .setEventManager(new AnnotatedEventManager())
        .build()
        .awaitReady();
    this.eventListeners().forEach(this.jda::addEventListener);
  }

  private Stream<?> eventListeners() {
    return Stream.of(
        new CommandHandler(this)
    );
  }

  public void shutdown() {
    this.jda.shutdown();

    if (!this.adapter.shutdownScheduler(1, TimeUnit.MINUTES)) {
      this.adapter.terminateScheduler();
    }

    //

    this.jda.shutdownNow();

    if (!this.adapter.shutdownSchedulerWorker(1, TimeUnit.MINUTES)) {
      this.adapter.terminateSchedulerWorker();
    }

    if (!this.adapter.shutdownSyncExecutor(1, TimeUnit.MINUTES)) {
      this.adapter.terminateSyncExecutor();
    }

    if (!this.adapter.shutdownAsyncExecutor(1, TimeUnit.MINUTES)) {
      this.adapter.terminateAsyncExecutor();
    }

    System.exit(0);
  }

  static void init(OptionSet optionSet) {
    try {
      // getting token from arguments
      String token = (String) optionSet.valueOf("token");
      // load bot on new thread
      new Bot(token);
    } catch (Exception e) {
      LOGGER.error("Failed bootstrap", e);
    }
  }
}
