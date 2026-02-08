package haennihaesseo.sandoll.domain.letter.repository;

import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.LetterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {
    Letter findByLetterIdAndSenderUserId(Long letterId, Long userId);

    boolean existsByLetterIdAndSenderUserId(Long letterId, Long userId);

    @Query("select l from Letter l join fetch l.font join fetch l.template join fetch l.voice left join fetch l.bgm where l.letterId = :letterId")
    Optional<Letter> findWithAllDetails(@Param("letterId") Long letterId);

    @Modifying
    @Query("update Letter l set l.letterStatus = :letterStatus where l.sender.userId = :userId and l.letterId in :letterIds")
    int updateLetterStatusBySenderUserIdAndLetterIdIn(@Param("letterStatus") LetterStatus letterStatus, @Param("userId") Long userId, @Param("letterIds") List<Long> letterIds);

    @Query("select l from Letter l where l.sender.userId = :userId and l.letterStatus = :letterStatus order by l.createdAt desc")
    List<Letter> findBySenderUserIdAndLetterStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("letterStatus") LetterStatus letterStatus);

    @Query("select l from Letter l where l.sender.userId = :userId and l.letterStatus = :letterStatus order by l.createdAt asc")
    List<Letter> findBySenderUserIdAndLetterStatusOrderByCreatedAtAsc(@Param("userId") Long userId, @Param("letterStatus") LetterStatus letterStatus);

    Integer countBySenderUserIdAndLetterStatus(Long userId, LetterStatus letterStatus);
}
