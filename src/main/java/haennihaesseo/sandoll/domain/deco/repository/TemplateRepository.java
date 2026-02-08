package haennihaesseo.sandoll.domain.deco.repository;

import haennihaesseo.sandoll.domain.deco.entity.Template;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    Template findByName(String name);

    List<Template> findByNameIn(List<String> names);
}
