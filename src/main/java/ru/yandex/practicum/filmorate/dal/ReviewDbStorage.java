package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.RowMapperReview;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewRating;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapperReview rowMapperReview;
    private static final String INSERT_NEW_REVIEW = "INSERT INTO reviews(film_id, user_id, is_positive, content)" +
            "VALUES (?, ?, ?, ?)";
    private static final String INSERT_NEW_LIKE = "INSERT INTO reviewRating(user_id, review_id, rating)" +
            "VALUES (?, ?, 1)";
    private static final String INSERT_NEW_DISLIKE = "INSERT INTO reviewRating(user_id, review_id, rating)" +
            "VALUES (?, ?, -1)";
    private static final String FIND_REVIEW_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_REVIEWS_BY_FILM_ID = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful " +
            "DESC LIMIT ?";
    private static final String FIND_REVIEW_RATING_BY_ID = "SELECT * FROM reviewRating WHERE user_id = ? AND " +
            "review_id = ?";
    private static final String UPDATE_REVIEW = "UPDATE reviews SET is_positive = ?, content = ? WHERE review_id = ?";
    private static final String UPDATE_REVIEW_AFTER_ADD_LIKE = "UPDATE reviews SET useful = useful + 1 WHERE" +
            " review_id = ?";
    private static final String UPDATE_REVIEW_AFTER_ADD_DISLIKE = "UPDATE reviews SET useful = useful - 1 WHERE" +
            " review_id = ?";
    private static final String DELETE_REVIEW_BY_ID = "DELETE FROM reviews WHERE review_id = ?;";
    private static final String DELETE_LIKE_FROM_REVIEW_RATING = "DELETE FROM reviewRating WHERE user_id = ? AND review_id = ?;";

    public Review addReview(NewReviewRequest review) {
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        INSERT_NEW_REVIEW,
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, review.getFilmId());
                ps.setLong(2, review.getUserId());
                ps.setBoolean(3, review.getIsPositive());
                ps.setString(4, review.getContent());
                return ps;
            }, keyHolder);

            Long reviewId = keyHolder.getKeyAs(Long.class);
            return getReviewById(reviewId);
        } catch (DuplicateKeyException e) {
            throw new ValidationException("Попытка добавить отзыв на фильм, на который уже есть отзыв.");
        }
    }

    public Review getReviewById(long reviewId) {
        try {
            Review review = jdbc.queryForObject(
                    FIND_REVIEW_BY_ID_QUERY,
                    rowMapperReview,
                    reviewId
            );

            return review;

        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Указанный id: " + reviewId + " отзыва не найден"
            );
        }
    }

    public Review updateReview(UpdateReviewRequest reviewRequest) {
        Review reviewFromBd = jdbc.queryForObject(FIND_REVIEW_BY_ID_QUERY, rowMapperReview, reviewRequest.getReviewId());
        reviewFromBd = rowMapperReview.updateReviewFields(reviewFromBd, reviewRequest);
        jdbc.update(UPDATE_REVIEW, reviewFromBd.getIsPositive(), reviewFromBd.getContent(), reviewFromBd.getReviewId());
        reviewFromBd = jdbc.queryForObject(FIND_REVIEW_BY_ID_QUERY, rowMapperReview, reviewFromBd.getReviewId());
        return reviewFromBd;
    }

    public List<Review> getReviewsById(Long filmId, Long count) {
        try {
            return jdbc.query(FIND_REVIEWS_BY_FILM_ID, rowMapperReview, filmId, count);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Указанный filmId: " + filmId + " не найден"
            );
        }
    }

    public void addLikeToReview(Long id, Long userId) {
        try {
            ReviewRating reviewRating = jdbc.queryForObject(FIND_REVIEW_RATING_BY_ID, new ReviewRatingRowMapper(),
                    userId, id);
            if (reviewRating.getRating() == 1) {
                throw new DuplicateKeyException("Попытка поставить дополнительный лайк на отзыв");
            } else {
                removeDislikeFromReview(id, userId);
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            INSERT_NEW_LIKE
                    );
                    ps.setLong(1, userId);
                    ps.setLong(2, id);
                    return ps;
                });
            }
        } catch (EmptyResultDataAccessException e) {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        INSERT_NEW_LIKE
                );
                ps.setLong(1, userId);
                ps.setLong(2, id);
                return ps;
            });
        } catch (DuplicateKeyException e) {
            throw new ValidationException(e.getMessage());
        }
        jdbc.update(UPDATE_REVIEW_AFTER_ADD_LIKE, id);
    }

    public void removeLikeFromReview(Long id, Long userId) {
        try {
            ReviewRating reviewRating = jdbc.queryForObject(FIND_REVIEW_RATING_BY_ID, new ReviewRatingRowMapper(),
                    userId, id);
            if (reviewRating.getRating() == 1) {
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            DELETE_LIKE_FROM_REVIEW_RATING
                    );
                    ps.setLong(1, userId);
                    ps.setLong(2, id);
                    return ps;

                });
            } else {
                throw new EmptyResultDataAccessException(1);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("У пользователя нет лайка к отзыву с id: " + id);
        }
        jdbc.update(UPDATE_REVIEW_AFTER_ADD_DISLIKE, id);
    }

    public void addDislikeToReview(Long id, Long userId) {
        try {
            ReviewRating reviewRating = jdbc.queryForObject(FIND_REVIEW_RATING_BY_ID, new ReviewRatingRowMapper(),
                    userId, id);
            if (reviewRating.getRating() == -1) {
                throw new DuplicateKeyException("Попытка поставить дополнительный дизлайк на отзыв");
            } else {
                removeLikeFromReview(id, userId);
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            INSERT_NEW_DISLIKE
                    );
                    ps.setLong(1, userId);
                    ps.setLong(2, id);
                    return ps;
                });
            }
        } catch (EmptyResultDataAccessException e) {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        INSERT_NEW_DISLIKE
                );
                ps.setLong(1, userId);
                ps.setLong(2, id);
                return ps;
            });
        } catch (DuplicateKeyException e) {
            throw new ValidationException(e.getMessage());
        }
        jdbc.update(UPDATE_REVIEW_AFTER_ADD_DISLIKE, id);
    }

    public void removeDislikeFromReview(Long id, Long userId) {
        try {
            ReviewRating reviewRating = jdbc.queryForObject(FIND_REVIEW_RATING_BY_ID, new ReviewRatingRowMapper(),
                    userId, id);
            if (reviewRating.getRating() == -1) {
                jdbc.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            DELETE_LIKE_FROM_REVIEW_RATING
                    );
                    ps.setLong(1, userId);
                    ps.setLong(2, id);
                    return ps;

                });
            } else {
                throw new EmptyResultDataAccessException(1);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("У пользователя нет дизлайка к отзыву с id: " + id);
        }
        jdbc.update(UPDATE_REVIEW_AFTER_ADD_LIKE, id);
    }

    public void deleteReviewById(Long id) {
        jdbc.update(DELETE_REVIEW_BY_ID, id);
    }
}
