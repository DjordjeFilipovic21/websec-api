package com.example.websecurity.service;

import com.example.websecurity.exception.WebSecMissingDataException;
import com.example.websecurity.persistence.Review;
import com.example.websecurity.persistence.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void getReviewByIdForUserShouldReturnOwnedReview() {
        Review review = new Review();
        review.setId(10L);

        when(reviewRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(review));

        Review found = reviewService.getReviewByIdForUser(10L, 1L);
        assertEquals(10L, found.getId());
    }

    @Test
    void getReviewByIdForUserShouldThrowWhenReviewNotOwnedByUser() {
        when(reviewRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(WebSecMissingDataException.class, () -> reviewService.getReviewByIdForUser(99L, 1L));
    }
}
