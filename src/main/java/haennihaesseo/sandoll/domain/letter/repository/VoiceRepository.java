package haennihaesseo.sandoll.domain.letter.repository;

import haennihaesseo.sandoll.domain.letter.entity.Voice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceRepository extends JpaRepository<Voice, Long> {
}
