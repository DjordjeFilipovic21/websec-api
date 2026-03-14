package com.example.websecurity.facade;


import com.example.websecurity.api.dto.ReviewResponse;
import com.example.websecurity.api.dto.UpdateReviewRequest;
import com.example.websecurity.exception.AccessDeniedException;
import com.example.websecurity.persistence.Review;
import com.example.websecurity.persistence.User;
import com.example.websecurity.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewFacade {

    private final ReviewService reviewService;

    public ReviewResponse getReviewById(Long id, User user) {
        Review review = reviewService.getReviewById(id);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this review");
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .movieTitle(review.getMovieTitle())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .reviewDate(review.getCreated())
                .build();
    }

    public ReviewResponse updateReview(Long id, UpdateReviewRequest updateReviewRequest, User user) {
        Review review = reviewService.getReviewById(id);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this review");
        }

        review.setReviewText(updateReviewRequest.getReviewText());
        review.setRating(updateReviewRequest.getRating());
        Review updatedReview = reviewService.updateReview(review);
        return ReviewResponse.builder()
                .id(updatedReview.getId())
                .movieTitle(updatedReview.getMovieTitle())
                .reviewText(updatedReview.getReviewText())
                .rating(updatedReview.getRating())
                .reviewDate(updatedReview.getCreated())
                .build();
    }

    public List<ReviewResponse> getReviewsForUser(Long userId) {
        List <ReviewResponse> reviewResponses = new ArrayList<>();
        List<Review> reviews = reviewService.getReviewsByUser(userId);
        for (Review review : reviews) {
            reviewResponses.add(
                    ReviewResponse.builder()
                            .id(review.getId())
                            .movieTitle(review.getMovieTitle())
                            .rating(review.getRating())
                            .build()
            );
        }
        return reviewResponses;
    }
}
