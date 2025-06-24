package nbc.devmountain.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    private final ChatModel chatModel;

    public AiConfig(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Bean
    public ChatClient chatClient() {
        return ChatClient.create(chatModel);
    }
}