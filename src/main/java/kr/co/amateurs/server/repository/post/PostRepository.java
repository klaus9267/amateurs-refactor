package kr.co.amateurs.server.repository.post;

import kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO;
import kr.co.amateurs.server.domain.entity.post.Post;
import kr.co.amateurs.server.domain.entity.post.enums.BoardType;
import kr.co.amateurs.server.domain.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardType(BoardType boardType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.boardType = :boardType")
    Page<Post> findByContentAndBoardType(@Param("keyword") String content, @Param("boardType") BoardType boardType, Pageable pageable);

    List<Post> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findByUser(User user);

    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    List<Post> findByUserIdIn(List<Long> followingUserId);

    @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.id = :id")
    boolean existsByIdUsingCount(@Param("id") Long id);

    @Query("SELECT p.boardType FROM Post p WHERE p.id = :postId")
    Optional<BoardType> findBoardTypeById(Long postId);

    @Query("""
            SELECT p.isBlinded
            FROM Post p
            WHERE p.id = :postId
            """)
    Boolean findIsBlindedByPostId(Long postId);

    @Query("""
            SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
                p.id,
                p.title,
                p.content,
                u.nickname,
                u.imageUrl,
                u.devcourseName,
                u.devcourseBatch,
                p.boardType,
                p.isBlinded,
                ps.viewCount,
                p.likeCount,
                (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
                (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
                p.createdAt,
                p.updatedAt,
                p.tags,
                false,
                false
            )
            FROM Post p
            JOIN p.user u
            JOIN PostStatistics ps ON ps.postId = p.id
            WHERE p.boardType = :boardType
              AND (:keyword IS NULL
                   OR :keyword = ''
                   OR p.title LIKE CONCAT('%', :keyword, '%')
                   OR p.content LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<CommunityResponseDTO> findDTOByContentAndBoardType(String keyword, BoardType boardType, Pageable pageable);

    @Query("""
            SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
                p.id, p.title, p.content, u.nickname, u.imageUrl,
                u.devcourseName, u.devcourseBatch, p.boardType, p.isBlinded, ps.viewCount,
                p.likeCount,
                (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
                (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id),
                p.createdAt, p.updatedAt, p.tags,
                (SELECT CASE WHEN COUNT(pl2.id) > 0 THEN true ELSE false END FROM Like pl2 WHERE pl2.post.id = p.id AND pl2.user.id = :userId),
                (SELECT CASE WHEN COUNT(b3.id) > 0 THEN true ELSE false END FROM Bookmark b3 WHERE b3.post.id = p.id AND b3.user.id = :userId)
            )
            FROM Post p
            JOIN p.user u
            JOIN PostStatistics ps ON ps.postId = p.id
            WHERE p.id = :communityId
            """)
    Optional<CommunityResponseDTO> findDTOByIdForUser( Long communityId,Long userId);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM Post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
        ORDER BY ps.viewCount DESC
        """)
    Page<CommunityResponseDTO> findDTOByBoardTypeOrderByViewCount(@Param("boardType") BoardType boardType,
                                                                  Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM Post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
          AND (:keyword IS NULL
               OR :keyword = ''
               OR p.title LIKE CONCAT('%', :keyword, '%')
               OR p.content LIKE CONCAT('%', :keyword, '%'))
        ORDER BY ps.viewCount DESC
        """)
    Page<CommunityResponseDTO> findDTOByContentAndBoardTypeOrderByViewCount(@Param("keyword") String keyword,
                                                                            @Param("boardType") BoardType boardType,
                                                                            Pageable pageable);

    @Query("""
        SELECT new kr.co.amateurs.server.domain.dto.community.CommunityResponseDTO(
            p.id,
            p.title,
            p.content,
            u.nickname,
            u.imageUrl,
            u.devcourseName,
            u.devcourseBatch,
            p.boardType,
            p.isBlinded,
            ps.viewCount,
            p.likeCount,
            (SELECT CAST(COUNT(c2.id) AS int) FROM Comment c2 WHERE c2.postId = p.id AND c2.isDeleted = false),
            (SELECT CAST(COUNT(b2.id) AS int) FROM Bookmark b2 WHERE b2.post.id = p.id AND b2.user.id = u.id),
            p.createdAt,
            p.updatedAt,
            p.tags,
            false,
            false
        )
        FROM Post p
        JOIN p.user u
        JOIN PostStatistics ps ON ps.postId = p.id
        WHERE p.boardType = :boardType
        """)
    Page<CommunityResponseDTO> findDTOByBoardType(@Param("boardType") BoardType boardType, Pageable pageable);
}