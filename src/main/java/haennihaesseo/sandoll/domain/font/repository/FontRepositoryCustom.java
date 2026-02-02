package haennihaesseo.sandoll.domain.font.repository;

import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.entity.enums.Bone;
import haennihaesseo.sandoll.domain.font.entity.enums.Distance;
import haennihaesseo.sandoll.domain.font.entity.enums.Situation;
import haennihaesseo.sandoll.domain.font.entity.enums.Writer;

import java.util.List;


public interface FontRepositoryCustom {
    List<Font> findByMatchScore(Bone bone, Writer writer, Situation situation, Distance distance, int minScore);
}
