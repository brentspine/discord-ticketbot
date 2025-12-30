package eu.greev.dcbot.ticketsystem.interactions.commands;

import eu.greev.dcbot.ticketsystem.service.SupporterSettingsData;
import eu.greev.dcbot.ticketsystem.service.TicketService;
import eu.greev.dcbot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class SetPrivacy extends AbstractCommand {
    private final SupporterSettingsData supporterSettingsData;

    public SetPrivacy(Config config, TicketService ticketService, EmbedBuilder missingPerm, JDA jda, SupporterSettingsData supporterSettingsData) {
        super(config, ticketService, missingPerm, jda);
        this.supporterSettingsData = supporterSettingsData;
    }

    @Override
    public void execute(Event evt) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) evt;
        if (!hasStaffPermission(event.getMember())) {
            event.replyEmbeds(missingPerm.setFooter(config.getServerName(), config.getServerLogo()).build()).setEphemeral(true).queue();
            return;
        }

        String mode = event.getOption("mode").getAsString().toLowerCase();
        boolean hideStats;

        if (mode.equals("hidden") || mode.equals("hide") || mode.equals("private")) {
            hideStats = true;
        } else if (mode.equals("visible") || mode.equals("show") || mode.equals("public")) {
            hideStats = false;
        } else {
            event.replyEmbeds(new EmbedBuilder()
                    .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
                    .setTitle("❌ **Ungültiger Modus**")
                    .setDescription("Verwende `hidden` oder `visible`")
                    .setColor(Color.RED)
                    .setFooter(config.getServerName(), config.getServerLogo())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        String discordId = event.getUser().getId();
        boolean currentHideStats = supporterSettingsData.isHideStats(discordId);

        if (currentHideStats == hideStats) {
            String status = hideStats ? "versteckt" : "sichtbar";
            event.replyEmbeds(new EmbedBuilder()
                    .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
                    .setTitle("ℹ️ **Keine Änderung**")
                    .setDescription("Deine Stats sind bereits " + status + ".")
                    .setColor(Color.decode(config.getColor()))
                    .setFooter(config.getServerName(), config.getServerLogo())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        supporterSettingsData.setHideStats(discordId, hideStats);

        String emoji = hideStats ? "🔒" : "🔓";
        String status = hideStats ? "versteckt" : "sichtbar";
        String description = hideStats
                ? "Deine XP und Ratings werden in öffentlichen Nachrichten und Leaderboards als **Anonym** angezeigt."
                : "Deine XP und Ratings werden in öffentlichen Nachrichten und Leaderboards wieder **normal** angezeigt.";

        event.replyEmbeds(new EmbedBuilder()
                .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
                .setTitle(emoji + " **Privacy-Einstellung geändert**")
                .setDescription("Deine Stats sind jetzt **" + status + "**.\n\n" + description)
                .setColor(Color.decode(config.getColor()))
                .setFooter(config.getServerName(), config.getServerLogo())
                .build()
        ).setEphemeral(true).queue();
    }
}
