package me.conclure.neptunaren.command.commands;

import me.conclure.neptunaren.Bot;
import me.conclure.neptunaren.command.CommandExecutor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PingCommand extends CommandExecutor {

  @Override
  protected String getName() {
    return "ping";
  }

  @Override
  protected Result execute(Bot bot, GuildMessageReceivedEvent event, String[] args) {
    final TextChannel channel = event.getChannel();

    channel.sendMessage("Pong!").queue();
    return ResultType.SUCCESS.toResult();
  }
}
