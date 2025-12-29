package eu.greev.dcbot.ticketsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.greev.dcbot.utils.Config;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class XpService {
    private final Config config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public XpService(Config config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Award XP to a helper for resolving a ticket.
     * This call is SYNCHRONOUS - it waits for the API response before returning.
     * This is important because the backend needs to fetch channel messages before we delete the channel!
     *
     * @param channelId   The ticket channel ID
     * @param supporterId The Discord ID of the supporter who claimed the ticket
     * @param rating      The star rating given by the ticket owner (1-5), can be null if skipped
     * @return true if XP was successfully awarded, false otherwise
     */
    public boolean awardTicketXp(String channelId, String supporterId, Integer rating) {
        if (config.getXpApiUrl() == null || config.getXpApiUrl().isBlank()) {
            log.debug("[XP] XP API not configured, skipping");
            return false;
        }

        if (supporterId == null || supporterId.isBlank()) {
            log.debug("[XP] No supporter for ticket, skipping XP award");
            return false;
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("channelId", channelId);
            body.put("supporterId", supporterId);
            if (rating != null) {
                body.put("rating", rating);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getXpApiUrl() + "/tickets/award-xp"))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", config.getXpApiKey())
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            // SYNC call - wait for response before continuing
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("[XP] Successfully awarded XP for ticket channel {}", channelId);
                return true;
            } else {
                log.warn("[XP] Failed to award XP: {} - {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("[XP] Error calling XP API: {}", e.getMessage());
            return false;
        }
    }
}
