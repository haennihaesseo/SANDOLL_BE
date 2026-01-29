package haennihaesseo.sandoll.domain.letter.repository;

import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.LetterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {
    Letter findByLetterIdAndSenderUserId(Long letterId, Long userId);

    @Query("select l.letterId from Letter l where l.sender.userId = :userId and l.letterStatus = :letterStatus order by l.createdAt desc")
    List<Long> findIdLetterIdBySenderUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("letterStatus") LetterStatus letterStatus);

    @Query("select l.letterId from Letter l where l.sender.userId = :userId and l.letterStatus = :letterStatus order by l.createdAt asc")
    List<Long> findIdLetterIdBySenderUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, @Param("letterStatus") LetterStatus letterStatus);

    boolean existsByLetterIdAndSenderUserId(Long letterId, Long userId);
}
