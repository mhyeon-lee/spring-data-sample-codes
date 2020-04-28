package spring.data.r2dbc.comment;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CommentRepository extends R2dbcRepository<Comment, Long> {
}
