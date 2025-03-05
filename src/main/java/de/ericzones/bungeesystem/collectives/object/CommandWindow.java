// Created by Eric B. 16.04.2021 14:36
package de.ericzones.bungeesystem.collectives.object;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class CommandWindow {

    private final LanguageHandler languageHandler;

    private final String colorCode;
    private final String header;
    private final String infoButton;
    private final String pluginPrefix;
    private final String windowCommand;
    private TextComponent[] commandsGerman;
    private TextComponent[] commandsEnglish;

    private final List<TextComponent> tempGerman = new ArrayList<>();
    private final List<TextComponent> tempEnglish = new ArrayList<>();

    public CommandWindow(String header, String colorCode, String pluginPrefix, String windowCommand) {
        this.languageHandler = BungeeSystem.getInstance().getLanguageHandler();
        this.header = header;
        this.colorCode = colorCode;
        this.infoButton = "§8["+colorCode+"Info§8]";
        this.pluginPrefix = pluginPrefix;
        this.windowCommand = windowCommand;
    }

    public CommandWindow registerCommand(Language language, String text, String textHover, String textClick) {
        TextComponent message = new TextBuilder(infoButton).setHoverText("§8● "+colorCode+textHover+" §8●").setClickEvent(TextBuilder.Action.TYPEDTEXT, textClick)
                .setPreText(" §8"+text).build();
        switch (language) {
            case GERMAN:
                tempGerman.add(message);
                break;
            case ENGLISH:
                tempEnglish.add(message);
                break;
        }
        return this;
    }

    public CommandWindow initializeCommands() {
        commandsGerman = new TextComponent[tempGerman.size()];
        commandsEnglish = new TextComponent[tempEnglish.size()];
        for(int i = 0; i < commandsGerman.length; i++)
            commandsGerman[i] = tempGerman.get(i);
        for(int i = 0; i < commandsEnglish.length; i++)
            commandsEnglish[i] = tempEnglish.get(i);
        return this;
    }

    public void sendWindow(ICorePlayer corePlayer, int page) {
        Language language = corePlayer.getLanguage();
        List<TextComponent> commands;
        if (page == 1)
            commands = loadCommands(language, 0, 5);
        else
            commands = loadCommands(language, ((page - 1) * 5) + 1, page * 5);
        if (commands.size() == 0) {
            corePlayer.sendMessage(pluginPrefix + "§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● "+colorCode+header+" §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for (TextComponent current : commands)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if (loadCommands(language, (((page + 1) - 1) * 5) + 1, (page + 1) * 5).size() != 0) {
            if (page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page + 1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, windowCommand+" " + (page + 1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language) + " §8§l┃ ").append(nextButton).append(Component.text(" §8["+colorCode + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page + 1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, windowCommand+" " + (page + 1)).build();
                TextComponent previousButton = new TextBuilder("§7§n" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page - 1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, windowCommand+" " + (page - 1)).build();
                TextComponent text = Component.text("§7« §8["+colorCode + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ ")).append(nextButton).append(Component.text(" §8["+colorCode + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            }
        } else {
            if (page == 1)
                corePlayer.sendMessage("§7« §8[§c§l✘§r§8] §7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)
                        + " §8§l┃ §7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language) + " §8[§c§l✘§r§8] §7»");
            else {
                TextComponent previousButton = new TextBuilder("§7§n" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page - 1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, windowCommand+" " + (page - 1)).build();
                TextComponent text = Component.text("§7« §8["+colorCode + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        + languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language) + " §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadCommands(Language language, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        switch (language) {
            case GERMAN:
                for (int i = from; i < to + 1 && i < commandsGerman.length; i++)
                    list.add(commandsGerman[i]);
                break;
            case ENGLISH:
                for (int i = from; i < to + 1 && i < commandsEnglish.length; i++)
                    list.add(commandsEnglish[i]);
                break;
        }
        return list;
    }

}
