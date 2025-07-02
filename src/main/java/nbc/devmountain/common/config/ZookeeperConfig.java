package nbc.devmountain.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;

@Slf4j
@Configuration
public class ZookeeperConfig {
    @Value("${spring.cloud.zookeeper.connect-string}")
    private String zkConnect;

    @Bean(initMethod = "start", destroyMethod = "close")
    @Primary
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                zkConnect,
                new ExponentialBackoffRetry(1000, 3)
        );
    }

    @EventListener(ContextRefreshedEvent.class)
    public void watchYoutubeApiKey(ContextRefreshedEvent event) {
        CuratorFramework client = event.getApplicationContext().getBean(CuratorFramework.class);
        String path = "/config/devmountain/spring.ai.mcp.client.stdio.connections.youtube.env.YOUTUBE_API_KEY";

        CuratorCache cache = CuratorCache.build(client, path);

        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forAll((type, oldData, newData) -> {
                    if (type.name().contains("NODE_CHANGED") && newData != null) {
                        log.info("YouTube API 키 변경 감지됨. MCP 서버 재시작 시도...");
                        restartMcpServer();
                    }
                })
                .build();

        cache.listenable().addListener(listener);
        cache.start();
    }

    private void restartMcpServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "pkill -f 'node ./youtube-mcp-server' && nohup node ./youtube-mcp-server/dist/server.js &");
            pb.directory(new File("/app"));
            pb.start();
            log.info("MCP 서버 재시작 완료.");
        } catch (IOException e) {
            log.error("MCP 서버 재시작 실패", e);
        }
    }
}