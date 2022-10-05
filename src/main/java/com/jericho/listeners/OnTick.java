package com.jericho.listeners;

import com.jericho.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.jericho.Main.settings;
import static com.jericho.Utilities.FileIO.*;
import static com.jericho.Utilities.Servers.getServerPaths;

public class OnTick {

    public static final ArrayList<String> serverPaths = getServerPaths();
    private static final int time = settings.getInt("update_check_interval");
    private static final String channel_id = settings.getString("channel_id");
    private static final JSONObject embed_id;

    static ShardManager jda = Main.shardManager;

    static {
        try {
            embed_id = loadJSONObject("embed_id.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String embedID() {
        return String.valueOf(embed_id.getString("id"));
    }

    public static void createInitialEmbed() {
        if (embedID().equals("null")) {
            EmbedBuilder e = new EmbedBuilder();
            e.setTitle("**RANK SYSTEM**");
            e.setFooter("Waiting for stats...");
            jda.getTextChannelById(channel_id).sendMessageEmbeds(e.build()).queue();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    jda.getTextChannelById(channel_id).retrieveMessageById(jda.getTextChannelById(channel_id).getLatestMessageId()).queue(message -> {
                        embed_id.put("id", message.getId());
                        try {
                            writeJSONObject(embed_id, "embed_id.json");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    });
                    this.cancel();
                }
            }, 2000);
        }
    }

    public static void Tick() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // Create a list that will store stats:
                ArrayList<String> stats = new ArrayList<>();

                String embedID = embedID();
                String header_line1 = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
                String header = String.format("`%-20s %-10s %-10s %-10s %-10s`", "NAME", "RANK", "CREDITS", "GRADE", "PRESTIGE");
                String header_line2 = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
                stats.add(header_line1);
                stats.add(header);
                stats.add(header_line2);

                for (String path : serverPaths) {
                    try {
                        JSONObject server = loadJSONObjectFromDir(path);
                        for (String username : server.keySet()) {
                            int credits = server.getJSONObject(username).getInt("credits");
                            int grade = server.getJSONObject(username).getInt("grade");
                            int prestige = server.getJSONObject(username).getInt("prestige");
                            String name = server.getJSONObject(username).getString("name");
                            String rank = server.getJSONObject(username).getString("rank");

                            // Format each line so there is equal spacing between each column:
                            String line = String.format("`%-20s %-10s %-10s %-10s %-10s`", name, rank, credits, grade, prestige);
                            stats.add(line);
                           // stats.add(String.format("%s - `Rank:` %s `Grade:` %s - `Prestige:` %s - `Credits:` %s", name, rank, prestige, grade, credits));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                TextChannel channel = jda.getTextChannelById(channel_id);
                Message message = channel.retrieveMessageById(embedID).complete();

                EmbedBuilder e = new EmbedBuilder();
                e.setTitle("**RANK SYSTEM**");
                e.setDescription(String.join("\n\n", stats));
                e.setFooter("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                message.editMessageEmbeds(e.build()).queue();

            }
        }, 1, time * 1000L);
    }
}
