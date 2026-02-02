package haennihaesseo.sandoll.domain.letter.cache;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CachedLetterRepository extends CrudRepository<CachedLetter, String> {

  boolean existByIdAndVoiceFontIdsEmpty(List<Long> voiceFontIds);
}