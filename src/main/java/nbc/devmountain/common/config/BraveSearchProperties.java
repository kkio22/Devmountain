package nbc.devmountain.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

@RefreshScope
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brave.search.api")
public class BraveSearchProperties {
    private List<String> key;
    private String url;
    private Queue<String> keyQueue;

    @PostConstruct
    public void initQueue() {
        if (key != null && key.size() == 1 && key.get(0).contains(",")) {
            // "key1,key2,key3" 형태를 List로 분리
            this.key = List.of(key.get(0).split(","));
        }
        this.keyQueue = new LinkedList<>(Objects.requireNonNull(key));
    }

    public String getNextKey() {
        String next = keyQueue.poll();
        keyQueue.offer(next); // 큐의 형태로 저장하여 돌아가면서 키를 사용
        log.info("현재 이 key를 사용중: {}",next);
        return next;
    }
}