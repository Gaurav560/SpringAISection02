package com.telusko.model;

//Le-18
public record BookRecommendation(String title,
                                 String author,
                                 String similarityReason,
                                 Double rating) {
}
