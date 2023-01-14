package me.metallicgoat.prizecommands.events;

import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.event.arena.RoundEndEvent;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.event.player.PlayerQuitArenaEvent;
import de.marcely.bedwars.api.event.player.PlayerRejoinArenaEvent;
import me.metallicgoat.prizecommands.Prize;
import me.metallicgoat.prizecommands.config.ConfigValue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import me.metallicgoat.prizecommands.events.DiscordWebhook;

public class LoseWinPrizes implements Listener {

    private final HashMap<Arena, List<Player>> playing = new HashMap<>();
    private final long time = ConfigValue.minimumPlayTime;

    // Add players to map on round start
    @EventHandler
    public void onGameStart(RoundStartEvent e){
        playing.put(e.getArena(), new ArrayList<>(e.getArena().getPlayers()));
    }

    // Remove player from map on leave if playing less than time
    @EventHandler
    public void onLeaveArena(PlayerQuitArenaEvent e){
        final Arena arena = e.getArena();

        if(arena.getStatus() == ArenaStatus.RUNNING && arena.getRunningTime() <= time)
            playing.get(arena).remove(e.getPlayer());
    }

    // Add players back if they rejoin
    @EventHandler
    public void onRejoin(PlayerRejoinArenaEvent e){
        final Arena arena = e.getArena();

        if(arena.getStatus() == ArenaStatus.RUNNING && e.getIssues().isEmpty())
            playing.get(arena).add(e.getPlayer());
    }

    // Run commands on game end
    @EventHandler
    public void onGameEnd(RoundEndEvent e){
        final Arena arena = e.getArena();
        final Collection<Player> activePlayers = playing.get(arena);

        if(activePlayers != null && time <= arena.getRunningTime()) {
            final HashMap<String, String> placeholderReplacements = new HashMap<>();

            if(e.getWinnerTeam() != null) {
                placeholderReplacements.put("winner-team-name", e.getWinnerTeam().getDisplayName());
                placeholderReplacements.put("winner-team-color", e.getWinnerTeam().name());
                placeholderReplacements.put("winner-team-color-code", "&" + e.getWinnerTeam().getChatColor().getChar());
            }

            String winner = e.getWinnerTeam().name();

            DiscordWebhook webhook = new DiscordWebhook("\n" +
                    "https://discord.com/api/webhooks/1063774393412632596/HZxLsvDPk-29W9d8IqR_lo5y75hVjYhbaFioLQbImMqtVI6N8_YMdhJ5tSGTCj_N0Q96");
            webhook.setContent(winner + " wygrał grę!");
            webhook.setAvatarUrl("https://your.awesome/image.png");
            webhook.setUsername("Speedpvp.eu");
            webhook.setTts(true);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("Gra bedwars - wynik")
                    .setDescription(winner + " wygrał grę!")
                    .setColor(Color.RED)
                    .addField("Wynik", winner + " wygrał grę!", false)
                    .setFooter("Speedpvp.eu", "https://i.imgur.com/3vZ7sEw.png")
                    .setThumbnail("https://i.imgur.com/3vZ7sEw.png")
                    .setUrl("https://i.imgur.com/3vZ7sEw.png")
                    .setAuthor("Speedpvp.eu", "https://i.imgur.com/3vZ7sEw.png", "https://i.imgur.com/3vZ7sEw.png"));
//                    .setDescription("Wygrał zespół " + winner)
//                    .setColor(Color.RED));
//                    .addField("1st Field", loser, true)
//                    .addField("2nd Field", winner, true)
//                    .addField("3rd Field", "No-Inline", false)
//                    .setThumbnail("https://kryptongta.com/images/kryptonlogo.png")
//                    .setFooter("Footer text", "https://kryptongta.com/images/kryptonlogodark.png")
//                    .setImage("https://kryptongta.com/images/kryptontitle2.png")
//                    .setAuthor("Author Name", "https://kryptongta.com", "https://kryptongta.com/images/kryptonlogowide.png")
//                    .setUrl("https://kryptongta.com"));
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setDescription("Just another added embed object!"));

            try {
                webhook.execute(); //Handle exception
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }

            for(Player player : activePlayers) {
                final List<Prize> endPrize = e.getWinners().contains(player) ? ConfigValue.playerWinPrize : ConfigValue.playerLosePrize;

                // use the DiscordWebhook class to send a message to the discord channel


                for (Prize prize: endPrize)
                    prize.earn(arena, player, placeholderReplacements);
            }
        }
        playing.remove(arena);
    }
}
