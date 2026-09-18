package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.validator.Create;
import ru.yandex.practicum.filmorate.validator.Update;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping(value = "/{id}")
    public ReviewDto getReview(@PathVariable @Positive Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<ReviewDto> getReviews(@RequestParam(value = "filmId") Long filmId,
                                      @RequestParam(value = "count", defaultValue = "10") Long count) {
        return reviewService.getReviewsById(filmId, count);
    }

    @DeleteMapping(value = "/{id}")
    public Map<String, String> deleteReview(@PathVariable @Positive Long id) {
        return reviewService.deleteReviewById(id);
    }

    @PutMapping(value = "/{id}/like/{userId}")
    public Map<String, String> addLikeToReview(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return reviewService.addLikeToReview(id, userId);
    }

    @DeleteMapping(value = "/{id}/like/{userId}")
    public Map<String, String> removeLikeFromReview(@PathVariable @Positive Long id,
                                                    @PathVariable @Positive Long userId) {
        return reviewService.removeLikeFromReview(id, userId);
    }

    @DeleteMapping(value = "/{id}/dislike/{userId}")
    public Map<String, String> removeDislikeFromReview(@PathVariable @Positive Long id,
                                                       @PathVariable @Positive Long userId) {
        return reviewService.removeDislikeFromReview(id, userId);
    }

    @PutMapping(value = "/{id}/dislike/{userId}")
    public Map<String, String> addDislikeToReview(@PathVariable @Positive Long id,
                                                  @PathVariable @Positive Long userId) {
        return reviewService.addDislikeToReview(id, userId);
    }

    @PostMapping
    public ReviewDto createReview(@Validated(Create.class) @RequestBody NewReviewRequest reviewRequest) {
        return reviewService.createReview(reviewRequest);
    }

    @PutMapping
    public ReviewDto updateReview(@Validated(Update.class) @RequestBody UpdateReviewRequest reviewRequest) {
        return reviewService.updateReview(reviewRequest);
    }

}
