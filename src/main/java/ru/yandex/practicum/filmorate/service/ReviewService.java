package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;

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
