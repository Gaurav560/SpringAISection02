package com.telusko.controller;

import com.telusko.model.Book;
import com.telusko.model.BookRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller for retrieving book-related information using Spring AI.
 * This controller demonstrates two different approaches for converting AI responses to Java objects:
 * 1. Explicit BeanOutputConverter approach
 * 2. Direct entity conversion approach with entity() method
 */
@RestController
public class BookController {

    private final ChatClient chatClient;


    public BookController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * Endpoint to get book reviews using explicit BeanOutputConverter approach.
     * This method demonstrates a more verbose but flexible approach where the conversion
     * process is explicitly controlled.
     * Le-17
     */
    @GetMapping("/book")
    public ResponseEntity<List<Book>> getBookReviews(@RequestParam String title) {
        try {
            // APPROACH 1: Explicit BeanOutputConverter
            // Create a converter that will transform the AI response text into a List<Book>
            // The ParameterizedTypeReference preserves the generic type information at runtime
            BeanOutputConverter<List<Book>> beanOutputConverter = new BeanOutputConverter<>(
                    new ParameterizedTypeReference<List<Book>>() {
                    });

            // Define the prompt template with a {format} parameter that will be filled with
            // conversion metadata to guide the AI response structure
            String message = """
                    Generate a list of reviews for the book {title}.
                    Include the Book as the key and reviews as the value for it. {format}
                    """;

            // Create a prompt template and fill in the parameters
            // Note the {format} parameter is populated with converter metadata
            PromptTemplate promptTemplate = new PromptTemplate(message);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title,
                    "format", beanOutputConverter.getFormat()));

            // Send the prompt to the AI model and get the response
            ChatResponse chatResponse = chatClient
                    .prompt(prompt)
                    .call()
                    .chatResponse();

            // Handle null response case
            if (chatResponse == null) {
                return new ResponseEntity<>(List.of(), HttpStatus.NO_CONTENT);
            }

            // Extract the generation result and convert the AI text output to List<Book>
            Generation result = chatResponse.getResult();
            List<Book> books = beanOutputConverter.convert(result.getOutput().getText());

            // Return successful response with books
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            // Handle any exceptions and return error status
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }









   //****************************************************************************










    /**
     * Endpoint to get book recommendations using direct entity conversion approach.
     * This method demonstrates a more concise approach where conversion happens
     * automatically as part of the API call chain.
     * Le-18
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<BookRecommendation>> getRecommendations(@RequestParam String bookTitle,
                                                                       @RequestParam(defaultValue = "5") int count) {
        try {
            // Define the prompt template without a {format} parameter
            // Since we're using direct entity conversion, we don't need to specify format metadata
            String message = """
                    Recommend {count} books similar to '{bookTitle}'.
                    For each book, include title, author, a brief description of why it's similar,
                    and an estimated rating out of 5.
                    """;

            // Create a prompt template and fill in the parameters
            PromptTemplate promptTemplate = new PromptTemplate(message);
            Prompt prompt = promptTemplate.create(Map.of(
                    "bookTitle", bookTitle,
                    "count", String.valueOf(count)));

            try {
                // APPROACH 2: Direct entity conversion
                // Call the AI model and directly convert the response to List<BookRecommendation>
                // using the entity() method with a ParameterizedTypeReference
                // This approach is more concise but provides less control over the conversion process
                List<BookRecommendation> recommendations = chatClient
                        .prompt(prompt)
                        .call()
                        .entity(new ParameterizedTypeReference<List<BookRecommendation>>() {
                        });

                // Return successful response with recommendations
                return new ResponseEntity<>(recommendations, HttpStatus.OK);
            } catch (Exception e) {
                // Handle conversion-specific exceptions separately
                return new ResponseEntity<>(List.of(), HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            // Handle general exceptions and return error status
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}