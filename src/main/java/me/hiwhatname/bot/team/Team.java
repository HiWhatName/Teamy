package me.hiwhatname.bot.team;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

/**
 * A team object
 *  @author HiWhatName
 */
@Getter
@Setter
@Data
// @AllArgsConstructor <- Will be heavily overwritten, so let's not use it
public class Team {
    Guild guild;
    String teamName;
    List<Member> members;
    Role teamRole;

    public Team(String teamName, List<Member> members, Guild guild, Role role) {
        this.teamName = teamName;
        this.members = members;
        this.teamRole = role;
        this.guild = guild;

        for (Member member : members) {
            addPlayer(member);
        }
    }

    public Team(String teamName, Guild guild, Role role) {
        this.teamName = teamName;
        this.teamRole = role;
        this.guild = guild;
    }

    private void addToRole(Member member) {
        Member Bot = guild.getSelfMember();
        if(member.getRoles().contains(teamRole)) return;
        guild.addRoleToMember(member, teamRole).queue();
    }

    public void addPlayer(Member player) {
        if (members.contains(player)) return;
        this.members.add(player);

        addToRole(player);
    }
    public void addPlayers(List<Member> playerList){
        for(Member player : playerList){
            if (members.contains(player)) return;
            this.members.add(player);

            addToRole(player);
        }
    }

    public void removePlayer(Member player) {
        if (!(members.contains(player))) return;

        this.members.remove(player);
        addToRole(player);
    }
}