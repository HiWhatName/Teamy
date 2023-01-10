package me.hiwhatname.bot;

import io.github.cdimascio.dotenv.Dotenv;
import me.hiwhatname.bot.commands.CommandManager;
import me.hiwhatname.bot.commands.TeamCommand;
import me.hiwhatname.bot.commands.TeamsCommand;
import me.hiwhatname.bot.listeners.VoiceChatListener;
import me.hiwhatname.bot.team.TeamManager;
import me.hiwhatname.bot.utils.GuildConfig;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Teamy extends ListenerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger( Teamy.class);

    private ShardManager shardManager;
    private static Dotenv botConfig;
    private static final List<TeamManager> teamManagerList = new ArrayList<>();
    private static Teamy bot;

    /**
     * Silly JDA5 bot made for a competition.
     * Loads variables and builds the shard manager instance.
     *
     * @throws LoginException
     * @author HiWhatName
     */
    public Teamy() throws LoginException {
        LOGGER.info("Loading .env file.");
        DefaultShardManagerBuilder builder;

        try { // Get config values
            botConfig = Dotenv.configure().load();
            builder = DefaultShardManagerBuilder.createDefault(botConfig.get("TOKEN"));
            builder.setStatus(OnlineStatus.valueOf(botConfig.get("STATUS")));
            builder.setActivity(Activity.of(Activity.ActivityType.valueOf(botConfig.get("ACTIVITY_TYPE")), botConfig.get("ACTIVITY")));

            LOGGER.info("Enabling Intents, please make sure to enable them in the developer settings. For more info visit README.MD");
            builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));

            LOGGER.info("Caching user information, this may take a while....");
            builder.setMemberCachePolicy(MemberCachePolicy.ALL); // What to cache? -> All (offline/online/etc users)
            builder.setChunkingFilter(ChunkingFilter.ALL); // Will cache all users on startup (/!\)
            builder.enableCache(CacheFlag.ROLE_TAGS, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.ONLINE_STATUS, CacheFlag.ROLE_TAGS); // What should be cached about users? -> Roles,etc

            //Register Commands
            LOGGER.info("Registering commands");
            CommandManager.addSlashCommand("teams", "List all teams");
            CommandManager.addSlashCommand("team", "Manage your team settings");

            if (Boolean.parseBoolean(botConfig.get("CHECK_ON_STARTUP"))) {
                LOGGER.info("Rechecking user roles enabled.");
                //TODO: temp
            }

            shardManager = builder.build();

        } catch (Exception e) {
            LOGGER.error("Incorrect or non existent .env file parsed, see: .env.example");
            System.exit(0);
        }

        //Load guilds.json
        try {
            GuildConfig.loadGuilds();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Add events (listeners)
        shardManager.addEventListener(new VoiceChatListener());
        shardManager.addEventListener(new CommandManager());
        shardManager.addEventListener(new TeamsCommand());
        shardManager.addEventListener(new TeamCommand());

        shardManager.addEventListener(this);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Starting up the bot, this may take a while....");
        try {
            bot = new Teamy(); // Init
        } catch (LoginException e) {
            LOGGER.error("ERR: Could not log into the bot.\nAre you specifying the right token?");
            System.exit(0);
        }
        LOGGER.info("Finished loading up in " + (System.currentTimeMillis() - startTime) + "ms!");

    }

    @Override
    public void onReady(ReadyEvent event) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("Registering TeamManagers...");

        for (Guild guild : shardManager.getGuilds()) {
            Map<String, ?> guildConfig = GuildConfig.getGuildConfigById(guild.getIdLong());
            if (guildConfig == null) {
                try {
                    GuildConfig.addGuild(guild.getIdLong(), 0L, "[MW3-|]", (short) 3, new String[]
                            {"Change","Me"}, true);
                } catch (IOException e) {
                    getLogger().warn("Guild " + guild.getName() + " cloud not be written to guilds.json\n" + e);
                }
            }

            LOGGER.info(((Number) guildConfig.get("reactMessageId")).longValue() + "\n\n"); //TODO: Fix this returning wrong value
            TeamManager tm = new TeamManager(guild, ((Number) guildConfig.get("reactMessageId")).longValue(), (String) guildConfig.get("rolePattern"),
                    ((Number) guildConfig.get("maxMembersPerTeam")).shortValue());
            shardManager.addEventListener(tm);

            LOGGER.info("TeamManager started for: " + guild.getName());
        }

        if (!(Boolean.parseBoolean(botConfig.get("REGISTER_GLOBAL")))) {
            LOGGER.info("Registering guild commands (not recommended) ...");
            for (Guild g : shardManager.getGuilds()) {
                CommandManager.registerCommandsToGuild(g);
            }
        } else {
            CommandManager.registerCommandsGlobally(event.getJDA());
        }
        LOGGER.info("Done! Finished post load setup in: " + (System.currentTimeMillis() - startTime) + "ms!");
    }

    // ===== DATA =====
    public static Dotenv getBotConfig() {
        return botConfig;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static List<TeamManager> getTeamManagerList() {
        return teamManagerList;
    }

    public static TeamManager getTeamManagerForGuild(Guild g) {
        for (TeamManager tm : teamManagerList) {
            if (tm.getGuild().getId().equals(g.getId())) {
                return tm;
            }
        }

        Map<String, ?> guild = GuildConfig.getGuildConfigById(g.getIdLong());
        TeamManager tm = new TeamManager(
                g,
                (long) guild.get("reactMessageId"),
                (String) guild.get("rolePattern"),
                (short) guild.get("maxMembersPerTeam"));
                teamManagerList.add(tm);

        return tm;
    }

    public static Teamy getBot() {
        return bot;
    }

}