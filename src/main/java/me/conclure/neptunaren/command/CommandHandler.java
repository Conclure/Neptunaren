package me.conclure.neptunaren.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import me.conclure.neptunaren.Bot;
import me.conclure.neptunaren.BotInfo;
import me.conclure.neptunaren.command.commands.PingCommand;
import me.conclure.neptunaren.command.commands.ShutdownCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

  private final Map<String, CommandExecutor> commandMap;
  private final Bot bot;

  public CommandHandler(Bot bot) {
    this.bot = bot;
    this.commandMap = new HashMap<>();

    this.registerCommandExecutors().forEach(executor -> {
      this.register(executor.getName(),executor);
      String[] aliases = executor.getAliases();

      if (aliases == null) {
        return;
      }

      if (aliases.length == 0) {
        return;
      }

      for (String alias : aliases) {
        this.register(alias,executor);
      }
    });
  }

  private void register(String string, CommandExecutor executor) {
    this.commandMap.put(string.toLowerCase(),executor);
  }

  private Stream<CommandExecutor> registerCommandExecutors() {
    return Stream.of(
        new PingCommand(),
        new ShutdownCommand()
    );
  }

  @SubscribeEvent
  public void onMessageReceived(GuildMessageReceivedEvent event) {

    //ensures sender is not bot
    if (event.getAuthor().isBot()) {
      return;
    }

    //ensures message is not a webhook
    if (event.isWebhookMessage()) {
      return;
    }

    //ensures guild id is the target guild
    if (event.getGuild().getIdLong() != BotInfo.GUILD_ID) {
      return;
    }

    //raw string content from message
    String content = event.getMessage().getContentRaw();

    //ensures the message is a valid command line
    if (!content.startsWith(BotInfo.PREFIX)) {
      return;
    }

    //splits spaces
    String[] line = StringUtils.split(content, " ");

    //get root node
    String node = line[0].substring(1);

    //get command executor from the root node
    CommandExecutor executor = this.commandMap.get(node.toLowerCase());

    //ensures the command executor is present corresponding to the root node provided
    if (executor == null) {
      return;
    }

    //fallback result in case command execution would fail
    CommandExecutor.Result result = CommandExecutor.DEFAULT_RESULT;

    //tries to execute command and retrieve a result
    try {
      result = executor.execute(this.bot, event, Arrays.copyOfRange(line, 1, line.length));
    } catch (Exception e) {
      e = new CommandException(e);
      LOGGER.error(e.getMessage(), e);
    }

    LOGGER.debug("Command: " + node + " - " + result.getType());

    MessageChannel channel = event.getChannel();

    //post behaviour depending on command execution result
    switch (result.getType()) {
      case UNEXPECTED_ERROR -> channel
          .sendMessage("Ett oväntat fel har inträffats.")
          .delay(10, TimeUnit.SECONDS)
          .flatMap(Message::delete)
          .queue();
      case NO_PERMISSION -> channel
          .sendMessage("Du är inte tillåten att använda detta kommandot.")
          .delay(10, TimeUnit.SECONDS)
          .flatMap(Message::delete)
          .queue();
      case INVALID_ARGUMENT -> channel
          .sendMessage("Ett felaktigt argument har på träffats.")
          .delay(10, TimeUnit.SECONDS)
          .flatMap(Message::delete)
          .queue();
      case UNKNOWN_ARGUMENT -> channel
          .sendMessage("Ett okänt argument har på träffats.")
          .delay(10, TimeUnit.SECONDS)
          .flatMap(Message::delete)
          .queue();
      case MISSING_ARGUMENT -> channel
          .sendMessage("Ett saknat argument har på träffat.")
          .delay(10, TimeUnit.SECONDS)
          .flatMap(Message::delete)
          .queue();
      case BAD_TIMING -> channel
          .sendMessage("Kommandot bör inte utföras just nu.")
          .delay(10, TimeUnit.SECONDS)
          .flatMap(Message::delete)
          .queue();
    }

    Runnable callback = result.getCallback();

    if (callback == null) {
      return;
    }

    //runs callback if the command execution result provided one
    callback.run();
  }
}
