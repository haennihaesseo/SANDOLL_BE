package haennihaesseo.sandoll.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import haennihaesseo.sandoll.global.exception.GlobalException;
import haennihaesseo.sandoll.global.status.ErrorStatus;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

public class ResourceLoader {

  private final static ObjectMapper objectMapper = new ObjectMapper();

  public static String getResourceContent(String resourceName) {
    try {
      var resource = new ClassPathResource(resourceName);
      return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new GlobalException(ErrorStatus.NOT_FOUND);
    }
  }

  public static InputStream getResourceAsStream(String resourceName) {
    try {
      return new ClassPathResource(resourceName).getInputStream();
    } catch (Exception e) {
      throw new GlobalException(ErrorStatus.NOT_FOUND);
    }
  }

}
