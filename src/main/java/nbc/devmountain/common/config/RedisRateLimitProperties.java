package nbc.devmountain.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ratelimit.redis")
public class RedisRateLimitProperties {
    private String host;
    private int port;
}
