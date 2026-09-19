package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedEventsDb;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final FilmService filmService;
    private final UserService userService;
    private final ReviewDbStorage reviewDbStorage;
    private final FeedEventsDb feedEventsDb;

    public ReviewDto createReview(NewReviewRequest review) {
        filmService.getFilmById(review.getFilmId());
        userService.getUserById(review.getUserId());
        ReviewDto reviewDto = reviewDbStorage.addReview(review);
        feedEventsDb.insertEvents(System.currentTimeMillis(), review.getUserId(), "REVIEW", "ADD",
                reviewDto.getReviewId());
        return reviewDto;
    }

    public ReviewDto getReviewById(Long id) {
        return reviewDbStorage.getReviewById(id);
    }

    public List<ReviewDto> getReviewsById(Long filmId, Long count) {
        filmService.getFilmById(filmId);
        return reviewDbStorage.getReviewsById(filmId, count);
    }

    public Map<String, String> addLikeToReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.addLikeToReview(id, userId);
        return Map.of("Сообщение: ", "лайк успешно поставлен");
    }

    public Map<String, String> removeLikeFromReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.removeLikeFromReview(id, userId);
        return Map.of("Сообщение: ", "лайк успешно удален");
    }

    public Map<String, String> removeDislikeFromReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.removeDislikeFromReview(id, userId);
        return Map.of("Сообщение: ", "дизлайк успешно удален");
    }

    public Map<String, String> addDislikeToReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.addDislikeToReview(id, userId);
        return Map.of("Сообщение: ", "дизлайк успешно поставлен");
    }

    public Map<String, String> deleteReviewById(Long id) {
        ReviewDto reviewDto = getReviewById(id);
        reviewDbStorage.deleteReviewById(id);
        feedEventsDb.insertEvents(System.currentTimeMillis(), reviewDto.getUserId(), "REVIEW", "REMOVE",
                reviewDto.getReviewId());
        return Map.of("Сообщение: ", "отзыв успешно удален");
    }

    public ReviewDto updateReview(UpdateReviewRequest review) {
        filmService.getFilmById(review.getFilmId());
        userService.getUserById(review.getUserId());
        reviewDbStorage.getReviewById(review.getReviewId());
        ReviewDto reviewDto = reviewDbStorage.updateReview(review);
        feedEventsDb.insertEvents(System.currentTimeMillis(), review.getUserId(), "REVIEW", "UPDATE",
                reviewDto.getReviewId());
        return reviewDto;
    }
}
