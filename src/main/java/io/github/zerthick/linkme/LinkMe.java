/*
 * Copyright (C) 2016  Zerthick
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
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(
        id = "linkme",
        name = "LinkMe",
        description = "A simple Minecraft command link plugin",
        version = "1.1.0",
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

        CommandManager commandManager = Sponge.getCommandManager();

        //Register each link command
        messageMap.entrySet().forEach(e -> commandManager.register(this, CommandSpec.builder()
                        .executor((src, args) -> {
                            src.sendMessage(e.getValue());
                            return CommandResult.success();
                        })
                        .permission("linkme.commands." + e.getKey().toLowerCase()).build(),
                ImmutableList.of(e.getKey().toLowerCase())));
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        getLogger().info(
                instance.getName() + " version " + instance.getVersion().orElse("")
                        + " enabled!");
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
}
