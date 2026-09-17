package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.RowMapperReview;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapperReview rowMapperReview;
    private static final String INSERT_NEW_REVIEW = "INSERT INTO reviews(film_id, user_id, is_positive, content)" +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_REVIEW_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String UPDATE_REVIEW = "UPDATE reviews SET is_positive = ?, content = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_BY_ID = "DELETE FROM reviews WHERE review_id = ?;";


    public ReviewDto addReview(NewReviewRequest review) {
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

    public ReviewDto getReviewById(long reviewId) {
        try {
            Review review = jdbc.queryForObject(
                    FIND_REVIEW_BY_ID_QUERY,
                    rowMapperReview,
                    reviewId
            );

            return rowMapperReview.mapToReviewDto(review);

        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(
                    "Указанный id: " + reviewId + " отзыва не найден"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ReviewDto updateReview(UpdateReviewRequest reviewRequest) {
        Review reviewFromBd = jdbc.queryForObject(FIND_REVIEW_BY_ID_QUERY, rowMapperReview, reviewRequest.getReviewId());
        reviewFromBd = rowMapperReview.updateReviewFields(reviewFromBd, reviewRequest);
        jdbc.update(UPDATE_REVIEW, reviewFromBd.getIsPositive(), reviewFromBd.getContent(), reviewFromBd.getReviewId());
        reviewFromBd = jdbc.queryForObject(FIND_REVIEW_BY_ID_QUERY, rowMapperReview, reviewFromBd.getReviewId());
        try {
            return rowMapperReview.mapToReviewDto(reviewFromBd);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteReviewById(Long id) {
        jdbc.update(DELETE_REVIEW_BY_ID, id);
    }
}
