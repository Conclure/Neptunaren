package me.conclure.neptunaren.command;


import me.conclure.neptunaren.Bot;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class CommandExecutor {
  public static Result DEFAULT_RESULT = new Result(ResultType.UNEXPECTED_ERROR);

  protected abstract String getName();

  protected abstract Result execute(Bot bot, GuildMessageReceivedEvent event, String[] args);

  protected String[] getAliases() {
    return null;
  }

  protected enum ResultType {
    UNKNOWN_ARGUMENT,
    INVALID_ARGUMENT,
    MISSING_ARGUMENT,
    SUCCESS,
    UNEXPECTED_ERROR,
    BAD_TIMING,
    NO_PERMISSION;

    public Result toResult(String... args) {
      return new Result(this, args);
    }
  }

  protected static final class Result {
    private final ResultType resultType;
    private final String[] args;
    private Runnable callback;

    private Result(ResultType resultType, String... args) {
      this.resultType = resultType;
      this.args = args;
    }

    ResultType getType() {
      return this.resultType;
    }

    String[] getArgs() {
      return this.args;
    }

    Runnable getCallback() {
      return this.callback;
    }

    public Result setCallback(Runnable action) {
      this.callback = action;
      return this;
    }
  }
}
