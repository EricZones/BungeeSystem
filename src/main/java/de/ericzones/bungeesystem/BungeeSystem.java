// Created by Eric B. 30.01.2021 17:39
package de.ericzones.bungeesystem;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.ericzones.bungeesystem.collectives.coreplayer.CorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.database.DatabaseHandler;
import de.ericzones.bungeesystem.collectives.friend.FriendManager;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.friend.MsgManager;
import de.ericzones.bungeesystem.collectives.joinme.JoinmeManager;
import de.ericzones.bungeesystem.collectives.party.IPartyManager;
import de.ericzones.bungeesystem.collectives.party.PartyManager;
import de.ericzones.bungeesystem.collectives.plugindata.PluginDataManager;
import de.ericzones.bungeesystem.collectives.punish.IPunishManager;
import de.ericzones.bungeesystem.collectives.punish.PunishManager;
import de.ericzones.bungeesystem.collectives.report.IReportManager;
import de.ericzones.bungeesystem.collectives.report.ReportManager;
import de.ericzones.bungeesystem.collectives.server.CoreServerManager;
import de.ericzones.bungeesystem.collectives.server.ICoreServerManager;
import de.ericzones.bungeesystem.commands.*;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.permission.PermissionHandler;
import de.ericzones.bungeesystem.manager.ChatManager;
import de.ericzones.bungeesystem.manager.ConnectionManager;

import java.io.IOException;

@Plugin(id = "bungeesystem", name = "BungeeSystem", version = "1.0",
        url = "de.ericzones", authors = {"EricZones"})
public class BungeeSystem {

    private DatabaseHandler databaseHandler;
    private ICorePlayerManager corePlayerManager;
    private ICoreServerManager coreServerManager;
    private IPunishManager punishManager;
    private LanguageHandler languageHandler;
    private PluginPrefixHandler pluginPrefixHandler;
    private ChatMessageHandler chatMessageHandler;
    private DisconnectMessageHandler disconnectMessageHandler;
    private PermissionHandler permissionHandler;

    private IReportManager reportManager;
    private IFriendManager friendManager;
    private IPartyManager partyManager;
    private JoinmeManager joinmeManager;
    private MsgManager msgManager;
    private PluginDataManager pluginDataManager;

    private static BungeeSystem instance;
    private final ProxyServer proxyServer;

    @Inject
    public BungeeSystem(ProxyServer proxyServer) {
        instance = this;
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent e) {
        registerObjects();
        registerListener();
        registerCommands();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        this.databaseHandler.disconnectDatabase();
    }

    private void registerObjects() {
        this.databaseHandler = new DatabaseHandler("database");
        this.coreServerManager = new CoreServerManager(this);
        this.corePlayerManager = new CorePlayerManager(this, this.databaseHandler.getSqlAdapter());
        this.punishManager = new PunishManager(this, this.databaseHandler.getSqlAdapter());
        this.languageHandler = new LanguageHandler();
        this.pluginPrefixHandler = new PluginPrefixHandler(this.databaseHandler.getSqlAdapter());
        this.chatMessageHandler = new ChatMessageHandler(this.pluginPrefixHandler);
        this.disconnectMessageHandler = new DisconnectMessageHandler();
        this.permissionHandler = new PermissionHandler();

        new ChatManager(this);
        new ConnectionManager(this);
        new de.ericzones.bungeesystem.manager.CommandManager(this);

        this.reportManager = new ReportManager(this, this.databaseHandler.getSqlAdapter());
        this.friendManager = new FriendManager(this, this.databaseHandler.getSqlAdapter());
        this.partyManager = new PartyManager(this);
        this.joinmeManager = new JoinmeManager(this);
        this.msgManager = new MsgManager(this);
        this.pluginDataManager = new PluginDataManager(this);
    }

    private void registerListener() {
        EventManager eventManager = proxyServer.getEventManager();
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();
        commandManager.register(commandManager.metaBuilder("proxy").build(), new ProxyCommand(this));
        commandManager.register(commandManager.metaBuilder("ping").build(), new PingCommand(this));
        commandManager.register(commandManager.metaBuilder("info").aliases("spy").build(), new InfoCommand(this));
        commandManager.register(commandManager.metaBuilder("chatclear").aliases("clearchat", "cc").build(), new ChatclearCommand(this));
        commandManager.register(commandManager.metaBuilder("ontime").aliases("onlinetime", "playtime").build(), new OntimeCommand(this));
        commandManager.register(commandManager.metaBuilder("coins").build(), new CoinsCommand(this));
        commandManager.register(commandManager.metaBuilder("joinme").build(), new JoinmeCommand(this));
        commandManager.register(commandManager.metaBuilder("friend").build(), new FriendCommand(this));
        commandManager.register(commandManager.metaBuilder("party").build(), new PartyCommand(this));
        commandManager.register(commandManager.metaBuilder("msg").aliases("reply", "r").build(), new MsgCommand(this));
        commandManager.register(commandManager.metaBuilder("socialspy").aliases("chatspy").build(), new SocialspyCommand(this));
        commandManager.register(commandManager.metaBuilder("p").build(), new PCommand(this));
        commandManager.register(commandManager.metaBuilder("report").build(), new ReportCommand(this));
        commandManager.register(commandManager.metaBuilder("ban").aliases("unban").build(), new BanCommand(this));
        commandManager.register(commandManager.metaBuilder("mute").aliases("unmute").build(), new MuteCommand(this));
    }

    public static BungeeSystem getInstance() {
        return instance;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public String getPluginName() {
        return "[BungeeSystem]";
    }

    public String getVersion() {
        return "1.15";
    }

    public int getTimezoneNumber() {
        return 2;
    }

    public DatabaseHandler getDatabaseHandler() {
        return this.databaseHandler;
    }

    public ICorePlayerManager getCorePlayerManager() {
        return this.corePlayerManager;
    }

    public ICoreServerManager getCoreServerManager() {
        return this.coreServerManager;
    }

    public IPunishManager getPunishManager() {
        return this.punishManager;
    }

    public LanguageHandler getLanguageHandler() {
        return languageHandler;
    }

    public PluginPrefixHandler getPluginPrefixHandler() {
        return pluginPrefixHandler;
    }

    public ChatMessageHandler getChatMessageHandler() {
        return chatMessageHandler;
    }

    public DisconnectMessageHandler getDisconnectMessageHandler() {
        return disconnectMessageHandler;
    }

    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    public IReportManager getReportManager() {
        return reportManager;
    }

    public IFriendManager getFriendManager() {
        return friendManager;
    }

    public IPartyManager getPartyManager() {
        return partyManager;
    }

    public JoinmeManager getJoinmeManager() {
        return joinmeManager;
    }

    public MsgManager getMsgManager() {
        return msgManager;
    }

    public PluginDataManager getPluginDataManager() {
        return pluginDataManager;
    }
}
