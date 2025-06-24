package nbc.devmountain.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

@Slf4j
@Service
public class MyAiService {

    private final ChatClient chatClient;

    @Autowired
    public MyAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateReply(String userMessage) {
        ChatClient.ChatClientRequestSpec builder = chatClient
                .prompt()
                .tools("videos_searchVideos")
                .user(userMessage);

        return builder.call().content();
    }
}
