package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public ReviewDto createReview(NewReviewRequest review) {
        filmService.getFilmById(review.getFilmId());
        userService.getUserById(review.getUserId());
        return reviewDbStorage.addReview(review);
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
        getReviewById(id);
        reviewDbStorage.deleteReviewById(id);
        return Map.of("Сообщение: ", "отзыв успешно удален");
    }

    public ReviewDto updateReview(UpdateReviewRequest review) {
        filmService.getFilmById(review.getFilmId());
        userService.getUserById(review.getUserId());
        reviewDbStorage.getReviewById(review.getReviewId());
        return reviewDbStorage.updateReview(review);
    }
}
