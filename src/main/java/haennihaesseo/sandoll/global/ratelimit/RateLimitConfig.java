package haennihaesseo.sandoll.global.ratelimit;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Getter
@Setter
public class RateLimitConfig {

    private boolean enabled = true;
    private int requestsPerHour = 10;
    private String whitelistIps = "";

    public List<String> getWhitelistIpList() {
        if (whitelistIps == null || whitelistIps.isBlank()) {
            return List.of();
        }
        return Arrays.stream(whitelistIps.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}