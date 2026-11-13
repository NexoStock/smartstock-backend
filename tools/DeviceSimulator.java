import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Random;

/**
 * Simulates a weight sensor that sends a reading every few seconds.
 * Simula un sensor de peso que envía lecturas.
 *
 * Run (Java 27, no build needed):
 *   java tools/DeviceSimulator.java <apiUrl> <deviceCode> <deviceKey> [startKg] [seconds]
 * Example:
 *   java tools/DeviceSimulator.java http://localhost:8080 SN-0001 3f9a... 12.5 10
 *
 * The weight goes down a little on each reading, as if products were sold.
 * Stop it with Ctrl+C.
 */
public class DeviceSimulator {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println(
                    "Usage: java tools/DeviceSimulator.java <apiUrl> <deviceCode> <deviceKey> [startKg] [seconds]");
            return;
        }

        String api = args[0].replaceAll("/+$", "");
        String code = args[1];
        String key = args[2];
        BigDecimal weight = new BigDecimal(args.length > 3 ? args[3] : "12.5");
        long seconds = args.length > 4 ? Long.parseLong(args[4]) : 10;

        HttpClient http = HttpClient.newHttpClient();
        Random random = new Random();
        URI uri = URI.create(api + "/api/device-readings/" + code);

        while (true) {
            String body = "{\"pesoKg\": " + weight + ", \"medidoEn\": \"" + Instant.now() + "\"}";

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Content-Type", "application/json")
                    .header("X-Device-Key", key)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.printf("%s  %s kg  -> %d%n", Instant.now(), weight, response.statusCode());

            if (response.statusCode() == 401) {
                System.out.println("Wrong or missing device key: stopping.");
                return;
            }

            BigDecimal drop = BigDecimal.valueOf(random.nextDouble() * 0.3);
            weight = weight.subtract(drop).max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP);
            Thread.sleep(seconds * 1000);
        }
    }
}
