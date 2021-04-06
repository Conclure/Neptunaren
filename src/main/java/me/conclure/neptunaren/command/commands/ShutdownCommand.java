package me.conclure.neptunaren.command.commands;

import me.conclure.neptunaren.Bot;
import me.conclure.neptunaren.BotInfo;
import me.conclure.neptunaren.command.CommandExecutor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ShutdownCommand extends CommandExecutor {

  @Override
  protected String getName() {
    return "shutdown";
  }

  @Override
  protected Result execute(Bot bot, GuildMessageReceivedEvent event, String[] args) {
    final User author = event.getAuthor();

    if (author.getIdLong() != BotInfo.OWNER_ID) {
      return ResultType.NO_PERMISSION.toResult();
    }

    return ResultType.SUCCESS.toResult().setCallback(bot::shutdown);
  }
}
