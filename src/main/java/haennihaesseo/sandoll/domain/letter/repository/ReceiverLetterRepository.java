package haennihaesseo.sandoll.domain.letter.repository;

import haennihaesseo.sandoll.domain.letter.entity.ReceiverLetter;
import haennihaesseo.sandoll.domain.letter.entity.ReceiverLetterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiverLetterRepository extends JpaRepository<ReceiverLetter, ReceiverLetterId> {

    @Query("select rl.id.letterId from ReceiverLetter rl where rl.id.receiverId= :receiverId order by rl.createdAt desc")
    List<Long> findIdLetterIdByIdReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long userId);

    @Query("select rl.id.letterId from ReceiverLetter rl where rl.id.receiverId= :receiverId order by rl.createdAt asc")
    List<Long> findIdLetterIdByIdReceiverIdOrderByCreatedAtAsc(@Param("receiverId") Long userId);

    boolean existsByIdReceiverIdAndIdLetterId(Long userId, Long letterId);

    Long countByIdReceiverId(Long receiverId);

    @Modifying
    @Query("delete from ReceiverLetter rl where rl.id.receiverId = :userId and rl.id.letterId in :letterIds")
    int deleteAllByIdReceiverIdAndIdLetterIdIn(@Param("userId") Long userId, @Param("letterIds") List<Long> letterIds);
}