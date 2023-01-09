package me.hiwhatname.bot.commands;

import me.hiwhatname.bot.Teamy;
import me.hiwhatname.bot.team.Team;
import me.hiwhatname.bot.team.TeamManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/** A slash command showing all teams on the guild.
 *
 *  @author HiWhatName
 */
public class TeamsCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if(!(e.getName().equals("teams"))) return;
        StringBuilder stb = new StringBuilder();
        stb.append("List of all Teams -> ");
        TeamManager tm = Teamy.getTeamManagerForGuild(e.getGuild());
        for(Team team : tm.getTeams()){
            stb.append("> " + team.getTeamName() + "[" + team.getMembers().size() + "/" + tm.getMaxMembersPerTeam() + "]");
        }
        e.reply(stb.toString()).setEphemeral(true).queue();
    }

}
