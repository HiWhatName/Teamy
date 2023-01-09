package me.hiwhatname.bot.team;

import me.hiwhatname.bot.WarfareBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Manage teams for a specific guild.This includes the reaction role.
 * There should always be only 1. teamManager per guild
 * @author HiWhatName
 */
public class TeamManager extends ListenerAdapter {
    private Guild guild;
    private Long reactMessageId;
    private List<Team> teams = new ArrayList<>();
    private String teamRolePattern; // e.G: [MW3-+]
    private short maxMembersPerTeam;
    private String[] predefinedTeamName = new String[] {"127.0.0.1", "Mecklenburg-Vorpommern", "Luftwaffel", "Buxtehude", "Luftangriff", "Fehlgebu-"}; //TODO: load from config
    private Random random = new Random();

    public TeamManager(Guild g, Long reactMessageID, String teamPattern, short maxMembersPerTeam) {
        this.guild = g;
        this.teamRolePattern = teamPattern;
        this.reactMessageId = reactMessageID;
        this.maxMembersPerTeam = maxMembersPerTeam;

        //Load teams from existing roles
        for (Role role : guild.getRoles().subList(0, guild.getRoles().size() - 1)){ //Don't include @everyone
            String[] spl = teamPattern.split("\\|", 2); // TODO: add to config, and documente '|' symbol

            //Make sure the role is part of a 'team role'
            if(role.getName().startsWith(spl[0]) && role.getName().endsWith(spl[1])){
                String teamName = role.getName().replace(spl[0], "").replace(spl[1], "");
                WarfareBot.getLogger().info("[" + g.getName() + "] Found team: '" + teamName + " -> Role: " + role.getName());
                createTeam(teamName, role);
            }
        }

        // Add discord users to their team
        for(Team team : teams){
            team.addPlayers(guild.getMembersWithRoles(team.getTeamRole()));
        }
    }


    /**
     * Adds the reacting player to a free team.
     * @param e
     */
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if (e.getGuild().getId().equals(guild.getId()) && e.getMessageId().equals(reactMessageId)) return;
        e.retrieveMessage().queue(userMessage -> {
            WarfareBot.getLogger().info("[" + guild.getName() + "]" + " User: " + e.getMember().getNickname() + " has reacted to the message.");
            for(Team t : teams){
                if(t.getMembers().size() < maxMembersPerTeam){
                    t.addPlayer(e.getMember());
                }
            } //TODO: implement this!
        });
    }

    /**
     * Removes the reacting player from his team.
     * @param e
     */
    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent e) {
        if (e.getGuild().getId().equals(guild.getId()) && e.getMessageId().equals(reactMessageId)) return;
        e.retrieveMessage().queue(userMessage -> {
            WarfareBot.getLogger().info("[" + guild.getName() + "]" + " User: " + e.getMember().getNickname() + " has removed his reaction from the message.");
            //Remove from team
        });
    }

    /**
     * Detect if the member got a role manually added.
     * @param e
     */
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) { // TODO: Rgb, this event is firing even though each TeamManager gets registered at the main class.
        for(Team team : teams){
            if(e.getMember().getRoles().contains(team.getTeamRole())){
                team.addPlayer(e.getMember());
            }
        }
    }

    /**
     * Detect if the member got his role manually removed.
     * @param e
     */
    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) { // TODO: same problem as above
        WarfareBot.getLogger().info("If this message worked, great \n\n\n\n\n\n .");
        for(Team team : teams){
            if(team.getMembers().contains(e.getMember()) // If the member has joined any team proceed
                    && !(e.getMember().getRoles().contains(team.getTeamRole()))){ // If he doesn't contain the teamRole anymore, we can assume it got removed.
                team.removePlayer(e.getMember());
            }
        }
    }

    /**
     * Generates a jda role based on parsed data. It also formats the name accordingly e.G: eagles -> [MW3-eagles]
     * @param teamName
     * @param color
     * @return
     */
    public Role generateTeamRole(String teamName, Color color) {
        String roleName = String.format(teamRolePattern, teamName);
        List<Role> matchingRoles = guild.getJDA().getRolesByName(roleName, true);
        Role role;

        if (matchingRoles.size() == 0) {
            role = guild.createRole()
                    .setName(roleName)
                    .setColor(color)
                    .setHoisted(true)
                    .setMentionable(true)
                    .setPermissions(Permission.EMPTY_PERMISSIONS)
                    .complete();
            WarfareBot.getLogger().info("[" + guild.getName() + "] created the role: " + role.getName());
        } else {
            role = matchingRoles.get(0);
        }
        return role;
    }

    // DATA bloat
    public Team createTeam(String teamName, Color teamColor) {
        Team team = new Team(teamName, guild, generateTeamRole(teamName, teamColor));
        WarfareBot.getLogger().info("[" + guild.getName() + "] created/restored  team: '" + team.getTeamName() + "'");
        return team;
    }
    public Team createTeam(String teamName, Role teamRole) {
        Team team = new Team(teamName, guild, teamRole);
        WarfareBot.getLogger().info("[" + guild.getName() + "] created/restored team: '" + team.getTeamName() + "'");
        return team;
    }

    public Guild getGuild() {
        return guild;
    }
    public short getMaxMembersPerTeam(){
        return maxMembersPerTeam;
    }
    public List<Team> getTeams() {
        return teams;
    }

}
