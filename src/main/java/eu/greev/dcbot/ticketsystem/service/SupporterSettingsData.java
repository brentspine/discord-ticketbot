package eu.greev.dcbot.ticketsystem.service;

import eu.greev.dcbot.ticketsystem.entities.SupporterSettings;
import org.jdbi.v3.core.Jdbi;

import java.time.Instant;

public class SupporterSettingsData {
    private final Jdbi jdbi;

    public SupporterSettingsData(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public SupporterSettings getSettings(String discordId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM supporter_settings WHERE discordId = ?")
                .bind(0, discordId)
                .map((rs, ctx) -> SupporterSettings.builder()
                        .discordId(rs.getString("discordId"))
                        .hideStats(rs.getBoolean("hideStats"))
                        .createdAt(rs.getLong("createdAt"))
                        .updatedAt(rs.getLong("updatedAt"))
                        .build())
                .findFirst()
                .orElse(null));
    }

    public boolean isHideStats(String discordId) {
        SupporterSettings settings = getSettings(discordId);
        return settings != null && settings.isHideStats();
    }

    public void saveSettings(SupporterSettings settings) {
        long now = Instant.now().getEpochSecond();
        jdbi.useHandle(handle -> handle.createUpdate(
                        "INSERT INTO supporter_settings (discordId, hideStats, createdAt, updatedAt) VALUES (?, ?, ?, ?) " +
                                "ON CONFLICT(discordId) DO UPDATE SET hideStats = ?, updatedAt = ?")
                .bind(0, settings.getDiscordId())
                .bind(1, settings.isHideStats())
                .bind(2, settings.getCreatedAt() > 0 ? settings.getCreatedAt() : now)
                .bind(3, now)
                .bind(4, settings.isHideStats())
                .bind(5, now)
                .execute());
    }

    public void setHideStats(String discordId, boolean hideStats) {
        long now = Instant.now().getEpochSecond();
        SupporterSettings settings = SupporterSettings.builder()
                .discordId(discordId)
                .hideStats(hideStats)
                .createdAt(now)
                .updatedAt(now)
                .build();
        saveSettings(settings);
    }
}
