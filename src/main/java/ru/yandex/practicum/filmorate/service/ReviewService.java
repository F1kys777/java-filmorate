package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedEventsDb;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.RowMapperReview;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.SQLException;
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
    private final RowMapperReview rowMapperReview;

    public ReviewDto createReview(NewReviewRequest review) {
        filmService.getFilmById(review.getFilmId());
        userService.getUserById(review.getUserId());
        try {
            ReviewDto reviewDto = rowMapperReview.mapToReviewDto(reviewDbStorage.addReview(review));
            feedEventsDb.insertEvents(System.currentTimeMillis(), reviewDto.getUserId(), "REVIEW", "ADD",
                    reviewDto.getReviewId());
            return reviewDto;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ReviewDto getReviewById(Long id) {
        try {
            return rowMapperReview.mapToReviewDto(reviewDbStorage.getReviewById(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ReviewDto> getReviewsById(Long filmId, Long count) {
        List<Review> reviews;

        if (filmId == null) {
            reviews = reviewDbStorage.getAllReviews(count);
        } else {
            filmService.getFilmById(filmId);
            reviews = reviewDbStorage.getReviewsById(filmId, count);
        }

        return reviews.stream()
                .map(review -> {
                    try {
                        return rowMapperReview.mapToReviewDto(review);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public Map<String, String> addLikeToReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.addLikeToReview(id, userId);
        //feedEventsDb.insertEvents(System.currentTimeMillis(), userId, "LIKE", "ADD", id);
        return Map.of("Сообщение: ", "лайк успешно поставлен");
    }

    public Map<String, String> removeLikeFromReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.removeLikeFromReview(id, userId);
        //feedEventsDb.insertEvents(System.currentTimeMillis(), userId, "LIKE", "REMOVE", id);
        return Map.of("Сообщение: ", "лайк успешно удален");
    }

    public Map<String, String> removeDislikeFromReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.removeDislikeFromReview(id, userId);
        //feedEventsDb.insertEvents(System.currentTimeMillis(), userId, "LIKE", "REMOVE", id);
        return Map.of("Сообщение: ", "дизлайк успешно удален");
    }

    public Map<String, String> addDislikeToReview(Long id, Long userId) {
        reviewDbStorage.getReviewById(id);
        userService.getUserById(userId);
        reviewDbStorage.addDislikeToReview(id, userId);
        //feedEventsDb.insertEvents(System.currentTimeMillis(), userId, "LIKE", "ADD", id);
        return Map.of("Сообщение: ", "дизлайк успешно поставлен");
    }

    public Map<String, String> deleteReviewById(Long id) {
        ReviewDto reviewDto = getReviewById(id);
        reviewDbStorage.deleteReviewById(id);
        feedEventsDb.insertEvents(System.currentTimeMillis(), reviewDto.getReviewId(), "REVIEW", "REMOVE",
                reviewDto.getFilmId());
        return Map.of("Сообщение: ", "отзыв успешно удален");
    }

    public ReviewDto updateReview(UpdateReviewRequest review) {
        filmService.getFilmById(review.getFilmId());
        userService.getUserById(review.getUserId());
        reviewDbStorage.getReviewById(review.getReviewId());
        try {
            ReviewDto reviewDto = rowMapperReview.mapToReviewDto(reviewDbStorage.updateReview(review));
            feedEventsDb.insertEvents(System.currentTimeMillis(), reviewDto.getUserId(), "REVIEW", "UPDATE",
                    reviewDto.getReviewId());
            return reviewDto;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
