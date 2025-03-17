package com.telusko.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {


    private final ChatClient chatClient;

    @Value("classpath:/prompts/team-report.st")
    private Resource teamReportPrompt;

    public HelloController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String prompt(@RequestParam String message) {

//Le-11
//        return chatClient
//                .prompt(message)    // Creates a prompt object with the user's message
//                .call()             // Makes an HTTP request to the AI service/model
//                .content();         // Extracts the text response from the AI's reply
//
//


//        to get the metadata
//        return chatClient
//        .prompt()
//        .user(message)
//        .call()
//        .chatResponse()
//        .getMetadata();


        //le-12
//    to get the response using chatResponse()
        return chatClient
                .prompt(message)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();


    }

    //le-13 & 14
    @GetMapping("/teamReport")
    public String getWeatherReport(@RequestParam String teamName) {

        String message = """
                Provide details about IPL team: {teamName}
                including the captain, home ground, and list of key players.
                Also include their championship history and current ranking.
                Present the information in an easily readable format with sections
                """;


        //creating a prompt out of this message
//        PromptTemplate template = new PromptTemplate(message);

//        using string templates
        PromptTemplate template = new PromptTemplate(teamReportPrompt);
        Prompt prompt = template.create(Map.of("teamName", teamName));


        return chatClient
                .prompt(prompt)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }


    //    le number-15
    //role based prompts
    @GetMapping("/movies")
    public String getMovieDetails(@RequestParam String title) {

        String message = """
                Provide details of the movie: %s
                including director, main cast, release year, and box office performance.
                Also include critical reception and any major awards.
                Present the information in a well-structured readable format
                """;

        String systemMessage = """
                You are a knowledgeable film database assistant.
                If someone asks about anything beyond movies and films, politely inform them
                that your expertise is limited to movie information only.
                """;

        UserMessage userMessage = new UserMessage(String.format(message, title));
        SystemMessage systemMessage1 = new SystemMessage(systemMessage);

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage1));

        return chatClient.prompt(prompt)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
    }
}
