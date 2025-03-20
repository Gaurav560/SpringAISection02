package com.telusko.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
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

    // This variable will hold our AI chat client that communicates with the AI service
    private final ChatClient chatClient;

    // This annotation loads a file from the classpath into the cityInfoPrompt variable
    // The file contains a template for a prompt that asks for information about a city
    @Value("classpath:/prompts/cityInfo.st")
    private Resource cityInfoPrompt;


    public HelloController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    //****************************************************************************





    @GetMapping
    public String prompt(@RequestParam String message) {


        //Le-11
//        return chatClient
//                .prompt(message)    // Creates a prompt object with the user's message
//                .call()             // Makes an HTTP request to the AI service/model
//                .content();         // Extracts the text response from the AI's reply
//
//


       // le-12
        ChatResponse chatResponse = chatClient
                .prompt(message)    // Creates a prompt with the user's message
                .call()             // Sends the request to the AI service
                .chatResponse();    // Gets the complete response object


        //to get metadata call getMetadata() on chatResponse

        // Checks if we got a valid response
        if (chatResponse == null) {
            return "No response from the server";
        }

        // Extracts and returns just the text portion of the AI's response
        return chatResponse.getResult()
                .getOutput()
                .getText();
    }








    //****************************************************************************




    //le-13 & 14
    // It provides information about a city based on the cityName parameter
    @GetMapping("/cityInfo")
    public String getCityInformation(@RequestParam String cityName) {
        // Creates a template string with placeholders for the city name
        // The {cityName} will be replaced with the actual city name provided
        String message = """
                Provide detailed information about: {cityName}
                including population, geographical location, and climate.
                Also include major landmarks, economic activities, and historical significance.
                Present the information in an easily readable format with sections
                """;

        // Creates a prompt template from the message string
//        PromptTemplate template = new PromptTemplate(message);

        //        using string templates
        PromptTemplate template = new PromptTemplate(cityInfoPrompt);

        // Creates the actual prompt by replacing {cityName} with the provided city name
        Prompt prompt = template.create(Map.of("cityName", cityName));

        // Sends the prompt to the AI service and gets the response
        ChatResponse chatResponse = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        // Checks if we got a valid response
        if (chatResponse == null) {
            return "No response from the server";
        }

        // Extracts and returns just the text portion of the AI's response
        return chatResponse.getResult()
                .getOutput()
                .getText();
    }







    //****************************************************************************






    //le-15
    // It demonstrates a more advanced prompt with both user and system messages
    @GetMapping("/movieDetails")
    public String getMovieDetails(@RequestParam String title) {
        // Template for asking about movie details
        // %s is a placeholder that will be replaced with the movie title
        String message = """
                Provide details of the movie: %s
                including director, main cast, release year, and box office performance.
                Also include critical reception and any major awards.
                Present the information in a well-structured readable format
                """;

        // This defines instructions for how the AI should behave (its role)
        String systemMessage = """
                You are a knowledgeable film database assistant.
                If someone asks about anything beyond movies and films, politely inform them
                that your expertise is limited to movie information only.
                """;

        // Creates a user message by formatting the message template with the movie title
        UserMessage userMessage = new UserMessage(String.format(message, title));

        // Creates a system message from the system message string
        SystemMessage systemMsg = new SystemMessage(systemMessage);

        // Creates a prompt that includes both the user message and system message
        // The system message sets the AI's behavior, while the user message asks the question
        Prompt prompt = new Prompt(List.of(userMessage, systemMsg));

        // Sends the prompt to the AI service and gets the response
        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();

        // Checks if we got a valid response
        if (chatResponse == null) {
            return "No response from the server";
        }

        // Extracts and returns just the text portion of the AI's response
        return chatResponse.getResult()
                .getOutput()
                .getText();
    }
}