package com.harbinger.llm;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
class ChatClientConfiguration {

    @Bean
    @Profile("!deepseek")
    ChatClient.Builder defaultChatClientBuilder(AnthropicChatModel model) {
        return ChatClient.builder(model);
    }

    @Bean
    @Profile("deepseek")
    ChatClient.Builder deepseekChatClientBuilder(OpenAiChatModel model) {
        return ChatClient.builder(model);
    }
}
