package me.hiwhatname.bot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import me.hiwhatname.bot.Teamy;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HiWhatName, RGB__Toaster
 */

@UtilityClass
public class GuildConfig {

    private final static File JSON = new File("guilds.json");
    private static Map<String, Map<String, ?>> guilds;
    private final static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableJdkUnsafe()
            .serializeNulls()
            .create();

    public void loadGuilds() throws IOException {

        if (!JSON.exists() || Files.readString(JSON.toPath()).isEmpty()) {
            try {
                Teamy.getLogger().info("No " + JSON.getName() + " file found, creating one instead.");
                JSON.createNewFile();
            } catch (IOException e) {
                throw new IOException(JSON.getName() + " could not be created!", e);
            }
        }

        guilds = (Map<String, Map<String, ?>>) GSON.fromJson(new FileReader(JSON), Map.class);

        if (guilds == null)
            guilds = new HashMap<>();
    }

    public Map<String, ?> getGuildConfigById(long guildId) {
        return guilds.get(String.valueOf(guildId));
    }

    public Map<String, ?> getGuildConfigByGuild(Guild guild) {
        return guilds.get(guild.getId());
    }

    public void addGuild(long guildId, long reactMessageId, String rolePattern, short maxMembersPerTeam, String[] predefinedTeamName, boolean allowNameChange) throws IOException {
        Map<String, Object> guild = new HashMap<>();

        guild.put("rolePattern", rolePattern); // How should discord role names be formatted?
        guild.put("reactMessageId", reactMessageId); // What message should be listened to for reactions?
        guild.put("maxMembersPerTeam", maxMembersPerTeam);
        guild.put("predefinedTeamNames", predefinedTeamName); // Default names for teams
        guild.put("allowNameChange", allowNameChange); // Should users be allowed to change their team name to anything?

        guilds.put(String.valueOf(guildId), guild);
        Files.write(JSON.toPath(), GSON.toJson(guilds).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public Map<String, Map<String, ?>> getGuilds() {
        return guilds;
    }

}
