package eu.greev.dcbot.ticketsystem.interactions.commands;

import eu.greev.dcbot.ticketsystem.service.TicketService;
import eu.greev.dcbot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class ConfigDump extends AbstractCommand {

    public ConfigDump(Config config, TicketService ticketService, EmbedBuilder missingPerm, JDA jda) {
        super(config, ticketService, missingPerm, jda);
    }

    @Override
    public void execute(Event evt) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) evt;

        if (!hasStaffPermission(event.getMember())) {
            event.replyEmbeds(missingPerm.setFooter(config.getServerName(), config.getServerLogo()).build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String yamlDump = config.toYamlString(true).trim();
        if (yamlDump.isEmpty()) {
            event.reply("Config is empty.").setEphemeral(true).queue();
            return;
        }

        // Respect Discord message length limit
        if (yamlDump.length() < 1980) {
            event.reply("```yaml\n" + yamlDump + "\n```").setEphemeral(true).queue();
        } else {
            ByteArrayInputStream data = new ByteArrayInputStream(yamlDump.getBytes(StandardCharsets.UTF_8));
            event.reply("Current config attached as YAML file.")
                    .addFiles(FileUpload.fromData(data, "config-dump.yml"))
                    .setEphemeral(true)
                    .queue();
        }
    }
}

