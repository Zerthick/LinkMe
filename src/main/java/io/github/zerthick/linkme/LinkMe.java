/*
 * Copyright (C) 2018  Zerthick
 *
 * This file is part of LinkMe.
 *
 * LinkMe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * LinkMe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LinkMe.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerthick.linkme;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(
        id = "linkme",
        name = "LinkMe",
        description = "A simple Minecraft command link plugin",
        authors = {
                "Zerthick"
        }
)
public class LinkMe {

    private final Pattern urlPattern = Pattern.compile("https?://\\S*");
    @Inject
    private Logger logger;
    @Inject
    private PluginContainer instance;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    private Collection<CommandMapping> registeredCommands;

    public Logger getLogger() {
        return logger;
    }

    public PluginContainer getInstance() {
        return instance;
    }

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();

        //Generate default config if it doesn't exist
        if (!defaultConfig.toFile().exists()) {
            Asset defaultConfigAsset = getInstance().getAsset("DefaultConfig.conf").get();
            try {
                defaultConfigAsset.copyToFile(defaultConfig);
                configLoader.save(configLoader.load());
            } catch (IOException e) {
                logger.warn("Error loading default config! Error: " + e.getMessage());
            }
        }

        //Load messages
        Map<String, Text> messageMap = loadMessages(configLoader);

        //Register each link command
        registeredCommands = registerCommands(messageMap);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        getLogger().info(
                instance.getName() + " version " + instance.getVersion().orElse("")
                        + " enabled!");
    }

    @Listener
    public void onGameReload(GameReloadEvent event) {
        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();

        //Load messages
        Map<String, Text> messageMap = loadMessages(configLoader);

        //Unregister old commands
        unregisterCommands(registeredCommands);

        //Register new commands
        registerCommands(messageMap);
    }

    //Search message for a link, add click action if one is discovered
    private Text processLinks(Text msg) {
        Matcher matcher = urlPattern.matcher(msg.toPlain());
        if (matcher.find()) {
            try {
                return Text.builder().append(msg).onClick(TextActions.openUrl(new URL(matcher.group()))).build();
            } catch (MalformedURLException e) {
                logger.warn("Error parsing url. Url: " + matcher.group() + " Error: " + e.getMessage());
            }
        }
        return msg;
    }

    private Map<String, Text> loadMessages(ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        //Load messages
        Map<String, Text> messageMap = new HashMap<>();

        try {
            CommentedConfigurationNode linksNode = configLoader.load().getNode("links");
            for (CommentedConfigurationNode node : linksNode.getChildrenMap().values()) {
                if (node.getString().startsWith("{")) {
                    messageMap.put(node.getKey().toString(), node.getValue(TypeToken.of(Text.class)));
                } else {
                    messageMap.put(node.getKey().toString(), processLinks(TextSerializers.FORMATTING_CODE.deserialize(node.getString())));
                }
            }
        } catch (ObjectMappingException | IOException e) {
            logger.warn("Error loading config! Error: " + e.getMessage());
        }

        return messageMap;
    }

    private Collection<CommandMapping> registerCommands(Map<String, Text> messageMap) {

        List<CommandMapping> registeredCommands = new ArrayList<>();

        messageMap.forEach((key, value) -> {
            CommandSpec spec = CommandSpec.builder()
                    .executor(((src, args) -> {
                        src.sendMessage(value);
                        return CommandResult.success();
                    }))
                    .permission("linkme.commands." + key.toLowerCase()).build();

            Sponge.getCommandManager().register(this, spec, ImmutableList.of(key.toLowerCase()))
                    .ifPresent(registeredCommands::add);

        });

        return registeredCommands;
    }

    private void unregisterCommands(Collection<CommandMapping> commands) {
        commands.forEach(command -> Sponge.getCommandManager().removeMapping(command));
    }
}
