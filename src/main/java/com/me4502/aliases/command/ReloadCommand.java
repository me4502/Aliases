package com.me4502.aliases.command;

import com.me4502.aliases.Aliases;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ReloadCommand implements CommandExecutor {

    private Aliases aliases;

    public ReloadCommand(Aliases aliases) {
        this.aliases = aliases;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        aliases.loadAliases();

        src.sendMessage(Text.of(TextColors.YELLOW, "Reloaded aliases!"));

        return CommandResult.success();
    }
}
