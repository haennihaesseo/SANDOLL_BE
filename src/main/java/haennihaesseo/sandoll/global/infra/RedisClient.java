package haennihaesseo.sandoll.global.infra;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisClient {

  private final StringRedisTemplate redisTemplate;

  public String getData(String prefix, String key) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    return valueOperations.get(prefix + ":" + key);
  }

  public void setData(String prefix, String key, String value, long duration){
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    valueOperations.set(prefix + ":" + key, value, Duration.ofSeconds(duration));
  }

  public void deleteData(String prefix, String key){
    redisTemplate.delete(prefix + ":" + key);
  }

  public boolean acquireLock(String key, String value, long timeout) {
    ValueOperations<String, String> ops = redisTemplate.opsForValue();
    return ops.setIfAbsent(key, value, Duration.ofMillis(timeout)); // key가 없으면 true, 이미 있으면 false
  }

  public void releaseLock(String key) {redisTemplate.delete(key);
  }

}
