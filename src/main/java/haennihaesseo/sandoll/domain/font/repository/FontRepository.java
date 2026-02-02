package haennihaesseo.sandoll.domain.font.repository;

import haennihaesseo.sandoll.domain.font.entity.Font;
import java.util.List;

import haennihaesseo.sandoll.domain.font.entity.enums.FontType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FontRepository extends JpaRepository<Font, Long>,  FontRepositoryCustom {
  List<Font> findByNameIn(List<String> names);
  List<Font> findAllByFontIdIn(List<Long> fontIds);
  List<Font> findAllByType(FontType type);
}
