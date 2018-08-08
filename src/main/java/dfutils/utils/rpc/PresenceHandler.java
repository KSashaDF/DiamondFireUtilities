package dfutils.utils.rpc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import dfutils.utils.playerdata.PlayerMode;
import dfutils.utils.playerdata.PlayerStateHandler;

public class PresenceHandler {
    private static long lastTimestamp = 0;
    private static PlayerMode lastMode;
    private static DiscordRPC lib = DiscordRPC.INSTANCE;
    private static boolean DiscordRPCSetup = false;
    private static boolean wasInSession = false;



    public static void updatePresence() {
        if(!DiscordRPCSetup) {
            initPresence();
        }

        if(wasInSession && !PlayerStateHandler.isInSupportSession ||
                lastMode != PlayerStateHandler.playerMode &&
                        !PlayerStateHandler.isInSupportSession) {
            lastTimestamp = System.currentTimeMillis() / 1000; // epoch second
            lastMode = PlayerStateHandler.playerMode;
            wasInSession = false;

            updatePresenceData();
        } else if(!wasInSession && PlayerStateHandler.isInSupportSession) {
            wasInSession = true;

            lastTimestamp = System.currentTimeMillis() / 1000; // epoch second
            lastMode = PlayerStateHandler.playerMode;

            updatePresenceData();
        }

        System.out.println("wasInSession: " + wasInSession);
        System.out.println("PlayerStateHandler: " + PlayerStateHandler.isInSupportSession);
    }

    private static void updatePresenceData() {
        DiscordRichPresence presence = new DiscordRichPresence();

        presence.smallImageKey = "dflogo";
        presence.startTimestamp = lastTimestamp;

        if(!PlayerStateHandler.isInSupportSession) {
            switch(PlayerStateHandler.playerMode) {
                case SPAWN:
                    presence.largeImageKey = "spawn";
                    presence.details = "At spawn";
                    break;
                case DEV:
                    presence.largeImageKey = "dev";
                    presence.largeImageText = "Mode Dev";
                    break;
                case BUILD:
                    presence.largeImageKey = "build";
                    presence.largeImageText = "Mode Build";
                    break;
                case PLAY:
                    presence.largeImageKey = "play";
                    presence.largeImageText = "Mode Play";
                    break;
            }

            if(PlayerStateHandler.playerMode == PlayerMode.DEV ||
                    PlayerStateHandler.playerMode == PlayerMode.BUILD ||
                    PlayerStateHandler.playerMode == PlayerMode.PLAY) {
                if(PlayerStateHandler.plotId != 0) presence.smallImageText = "Plot ID: " + PlayerStateHandler.plotId;
                if(PlayerStateHandler.plotName != null) presence.details = PlayerStateHandler.plotName;
                if(PlayerStateHandler.plotOwner != null) presence.state = "By " + PlayerStateHandler.plotOwner;
            }
        } else {
            switch (PlayerStateHandler.supportSessionRole) {
                case SUPPORTER:
                    presence.largeImageKey = "supporter";
                    presence.largeImageText = "Supporting " + PlayerStateHandler.supportPartner;
                    presence.details = "Supporting " + PlayerStateHandler.supportPartner;
                    break;
                case SUPPORTEE:
                    presence.largeImageKey = "supportee";
                    presence.largeImageText = "Supported by " + PlayerStateHandler.supportPartner;
                    presence.smallImageText = "Plot ID: " + PlayerStateHandler.plotId;
                    presence.details = "Being Supported";
                    presence.state = "by " + PlayerStateHandler.supportPartner;
            }
        }

        lib.Discord_UpdatePresence(presence);
    }

    public static void destroyPresence() {
        lib.Discord_Shutdown();
        lastTimestamp = 0;
        lastMode = null;
        DiscordRPCSetup = false;
        wasInSession = false;
    }

    private static void initPresence() {
        String applicationId = "476455349780611072";
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Discord RPC Ready!");
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        DiscordRPCSetup = true;
    }
}
