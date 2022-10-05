// Copyright (c) 2022, Jericho Crosby <jericho.crosby227@gmail.com>

package com.jericho.listeners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

import static com.jericho.Main.cprint;

public class EventListeners extends ListenerAdapter {
    @Override
    public void onGuildReady(@Nonnull GuildReadyEvent event) {
        cprint("++++++++++++++++++++++++++++++++++++++++++++++++");
        cprint("Guild ready: " + event.getGuild().getName());
        cprint("Bot name: " + event.getJDA().getSelfUser().getName());
        cprint("++++++++++++++++++++++++++++++++++++++++++++++++");
    }
}