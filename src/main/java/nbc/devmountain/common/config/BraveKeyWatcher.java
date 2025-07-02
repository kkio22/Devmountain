package nbc.devmountain.common.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.stereotype.Component;

@Component
public class BraveKeyWatcher {
    private final CuratorFramework curatorFramework;
    private final ContextRefresher contextRefresher;

    @Autowired
    public BraveKeyWatcher(CuratorFramework curatorFramework,
                           @Qualifier("configDataContextRefresher") ContextRefresher contextRefresher) {
        this.curatorFramework = curatorFramework;
        this.contextRefresher = contextRefresher;
        watchKey("/config/devmountain/brave.search.api.key");
    }

    private void watchKey(String path) {
        try {
            curatorFramework.getData().usingWatcher((Watcher) event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    contextRefresher.refresh();
                    // 다시 watch 재등록
                    watchKey(path);
                }
            }).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
