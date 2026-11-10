package com.nexostock.backend.alerts.infrastructure.notifications;

import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class TwilioWhatsAppSender implements NotificationSender {

    private final RestClient http = RestClient.builder()
            .baseUrl("https://api.twilio.com")
            .build();

    private final String sid;
    private final String token;
    private final String from;

    public TwilioWhatsAppSender(
            @Value("${app.twilio.sid:}") String sid,
            @Value("${app.twilio.token:}") String token,
            @Value("${app.twilio.from:}") String from) {
        this.sid = sid;
        this.token = token;
        this.from = from;
    }

    @Override
    public boolean supports(String channel) {
        return "whatsapp".equals(channel);
    }

    @Override
    public void send(OutboundMessage message) {
        if (sid.isBlank() || token.isBlank() || from.isBlank()) {
            throw new IllegalStateException("Twilio is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", whatsappAddress(from));
        form.add("To", whatsappAddress(message.getDestination()));
        form.add("Body", message.getContent());

        http.post()
                .uri("/2010-04-01/Accounts/{sid}/Messages.json", sid)
                .headers(headers -> headers.setBasicAuth(sid, token))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    private String whatsappAddress(String address) {
        return address.startsWith("whatsapp:")
                ? address
                : "whatsapp:" + address;
    }
}