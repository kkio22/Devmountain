package nbc.devmountain.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
        this.keyQueue = new LinkedList<>(key);
    }

    public String getNextKey() {
        String next = keyQueue.poll();
        keyQueue.offer(next); // 큐의 형태로 저장하여 돌아가면서 키를 사용
        return next;
    }
}