package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.Data;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.ReviewRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Component
public class ReviewRatingRowMapper implements RowMapper<ReviewRating> {

    @Override
    public ReviewRating mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewRating reviewRating = new ReviewRating();
        reviewRating.setUserId(rs.getLong("user_id"));
        reviewRating.setReviewId(rs.getLong("review_id"));
        reviewRating.setRating(rs.getInt("rating"));
        return reviewRating;
    }
}
