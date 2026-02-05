package haennihaesseo.sandoll.domain.letter.cache;

import org.springframework.data.repository.CrudRepository;

public interface CachedLetterRepository extends CrudRepository<CachedLetter, String> {
}