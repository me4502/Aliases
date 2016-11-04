package com.me4502.aliases;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.aliases.command.ReloadCommand;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

@Plugin(
        id = "aliases",
        name = "Aliases",
        description = "Command aliases",
        authors = {
                "Me4502"
        }
)
public class Aliases {

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private Map<String, AliasCommand> aliases = Maps.newHashMap();

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        loadAliases();

        CommandSpec reloadCommand = CommandSpec.builder().permission("aliases.reload")
                .executor(new ReloadCommand(this)).build();

        CommandSpec aliasCommand = CommandSpec.builder().child(reloadCommand, "reload").build();

        Sponge.getGame().getCommandManager().register(this, aliasCommand, "aliases", "alias");
    }

    public void loadAliases() {
        aliases.clear();

        try {
            if (!Files.exists(defaultConfig, LinkOption.NOFOLLOW_LINKS)) {
                URL jarConfigFile = this.getClass().getResource("default.conf");
                ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setURL(jarConfigFile).build();
                configManager.save(loader.load());
            }

            ConfigurationNode node = configManager.load();

            ConfigurationNode commandsNode = node.getNode("commands");

            for (ConfigurationNode commandNode : commandsNode.getChildrenMap().values()) {
                try {
                    AliasCommand command = new AliasCommand(
                            commandNode.getNode("alias").getString("example {0}"),
                            commandNode.getNode("permission").getString("alias.example"),
                            commandNode.getNode("commands").getList(TypeToken.of(String.class)));

                    aliases.put(commandNode.getNode("alias").getString("example {0}").split(" ")[0], command);
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onCommand(SendCommandEvent event, @First CommandSource player) {
        AliasCommand alias = aliases.get(event.getCommand());
        if (alias == null) {
            return; //Not an alias.
        }
        if (!player.hasPermission(alias.getPermission())) {
            player.sendMessage(Text.of(TextColors.RED, "You do not have permission to use this alias!"));
            event.setResult(CommandResult.empty());
            return;
        }

        event.setCancelled(true);

        try {
            Map<String, String> aliasMapping = alias.mapToCommand(event.getCommand() + " " + event.getArguments());

            CommandResult.Builder builder = CommandResult.builder();

            for (String command : alias.getCommands()) {
                for (Map.Entry<String, String> entry : aliasMapping.entrySet()) {
                    command = command.replace(entry.getKey(), entry.getValue());
                }
                Sponge.getGame().getCommandManager().process(player, command);
            }

            builder.successCount(alias.getCommands().size());

            event.setResult(builder.build());
        } catch(Throwable e) {
            player.sendMessage(Text.of(TextColors.RED, e.getMessage()));
        }
    }
}
