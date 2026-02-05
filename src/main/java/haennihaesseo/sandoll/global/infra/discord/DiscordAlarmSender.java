package haennihaesseo.sandoll.global.infra.discord;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordAlarmSender {

  @Value("${app.discord-webhook-url}")
  private String webHookUrl;

  @Value("${app.discord-alarm-enabled:false}")
  private boolean discordAlarmEnabled;

  private final DiscordUtil discordUtil;
  private final WebClient webClient = WebClient.create();

  public void sendDiscordAlarm(Exception exception, HttpServletRequest httpServletRequest) {
    if (discordAlarmEnabled) {
      webClient.post()
          .uri(webHookUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(discordUtil.createMessage(exception, httpServletRequest))
          .retrieve()
          .bodyToMono(Void.class)
          .doOnError(e -> log.error("Failed to send Discord alarm", e))
          .subscribe();
    }
  }
}
