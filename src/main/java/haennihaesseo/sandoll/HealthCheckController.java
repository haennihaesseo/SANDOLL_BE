package haennihaesseo.sandoll;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
  @GetMapping
  public String welcome() {
    return "Welcome to OLLLIM!";
  }
}
