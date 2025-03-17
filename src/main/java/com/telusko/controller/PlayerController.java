package com.telusko.controller;

import com.telusko.model.Achievement;
import com.telusko.model.Player;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PlayerController {


    private final ChatClient chatClient;

    public PlayerController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    @GetMapping("/player")
    public List<Player> getPlayerAchievement(@RequestParam String name) {

        BeanOutputConverter<List<Player>> beanOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Player>>() {
        });


        String message = """
                 Generate a list of career achievements for the sportsperson  {name}.\s
                 Include the Player as the key and achievements as the value for it. {format}
                \s""";

        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create(Map.of("name", name, "format", beanOutputConverter.getFormat()));


//        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
//        return chatResponse.getResult().getOutput().getText();

        Generation result =
                chatClient
                        .prompt(prompt)
                        .call()
                        .chatResponse()
                        .getResult();

        return beanOutputConverter.convert(result.getOutput().getText());

    }


    @GetMapping("/achievement/playerName")
    public List<Achievement> getAchievement(@RequestParam String playerName) {

        String message = """
                Generate a list of career achievements for the sportsperson {playerName}.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(message);

        Prompt prompt = promptTemplate.create(Map.of("playerName", playerName));
        return chatClient.prompt(prompt).call().entity(
                new ParameterizedTypeReference<List<Achievement>>() {
                }
        );

    }

}
