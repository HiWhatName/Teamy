package me.hiwhatname.bot.commands;

import me.hiwhatname.bot.Teamy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager to manage all bot commands. WIP
 *  @author HiWhatName
 */
public class CommandManager extends ListenerAdapter {
    private static List<CommandData> cmdData = new ArrayList<>();

    public static void registerCommandsToGuild(Guild g){
        Teamy.getLogger().info("Registered " + cmdData.size() + " guild commands to: " + g.getName());
        g.updateCommands().addCommands(cmdData).queue();
    }
    public static void registerCommandsGlobally(JDA jda){
        Teamy.getLogger().info("Registered " + cmdData.size() + " global commands. \n This may take up to 1h to update");
        jda.updateCommands().addCommands(cmdData);
    }

    public static void addSlashCommand(String name, String description){
        cmdData.add(Commands.slash(name,description));
        Teamy.getLogger().info("Registered: '" + name + "' command.");
    }

}
