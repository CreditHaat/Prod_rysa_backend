package com.lsp.web.ONDCService;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TxnLogService {

    private static final String URL = "https://analytics-api.aws.ondc.org/v1/api/push-txn-logs";

    private static final String AUTH_TOKEN =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJsb3MuYXJ5c2VmaW4uY29tQGJ1eWVyIiwiZXhwIjoxODIyOTAwMDAwLCJmcmVzaCI6ZmFsc2UsImlhdCI6MTY1OTE1MTk1NiwianRpIjoiZTQ2NWRkMWYzYTViNDI0MjhhYThlOWM2MjI5ZWNiYTgiLCJuYmYiOjE2NTkxNTE5NTYsInR5cGUiOiJhY2Nlc3MiLCJlbWFpbCI6InRlY2hAb25kYy5vcmciLCJwdXJwb3NlIjoiZGF0YXNoYXJpbmciLCJwaG9uZV9udW1iZXIiOm51bGwsInJvbGVzIjpbImFkbWluaXN0cmF0b3IiXSwiZmlyc3RfbmFtZSI6Im5ldHdvcmsiLCJsYXN0X25hbWUiOiJvYnNlcnZhYmlsaXR5In0.HQxR5FhIvONxvE8FpieBERsM7sqYGBdjPy1QctJAMEw";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Push transaction logs to ONDC Analytics API
     *
     * @param type the API type (e.g. "confirm", "on_confirm", "search")
     * @param data the JSON body (payload) to send
     * @return Response body as String
     */
    public String pushTxnLogs(String type, Object data) {
        // Wrap into required format { "type": "xxx", "data": { ... } }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", AUTH_TOKEN);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(URL, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }
}
