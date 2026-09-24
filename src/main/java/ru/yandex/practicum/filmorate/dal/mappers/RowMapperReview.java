package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RowMapperReview implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setContent(rs.getString("content"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }

    public ReviewDto mapToReviewDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setReviewId(review.getReviewId());
        reviewDto.setFilmId(review.getFilmId());
        reviewDto.setUserId(review.getUserId());
        reviewDto.setIsPositive(review.getIsPositive());
        reviewDto.setContent(review.getContent());
        reviewDto.setUseful(review.getUseful());
        return reviewDto;
    }

    public Review updateReviewFields(Review review, UpdateReviewRequest request) {
        if (request.getIsPositive() != null) {
            review.setIsPositive(request.getIsPositive());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        return review;
    }
}
