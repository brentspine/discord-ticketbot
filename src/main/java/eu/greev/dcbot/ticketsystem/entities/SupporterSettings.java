package eu.greev.dcbot.ticketsystem.entities;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupporterSettings {
    private final String discordId;
    private final boolean hideStats;
    private final long createdAt;
    private final long updatedAt;
}
