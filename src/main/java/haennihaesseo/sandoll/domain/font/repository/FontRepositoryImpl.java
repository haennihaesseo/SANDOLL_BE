package haennihaesseo.sandoll.domain.font.repository;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import haennihaesseo.sandoll.domain.font.entity.Font;
import haennihaesseo.sandoll.domain.font.entity.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static haennihaesseo.sandoll.domain.font.entity.QFont.font;

@Repository
@RequiredArgsConstructor
public class FontRepositoryImpl implements FontRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Font> findByMatchScore(Bone bone, Writer writer, Situation situation, Distance distance, int minScore) {
        NumberExpression<Integer> matchScore =
                new CaseBuilder()
                        .when(font.boneKeyword.eq(bone)).then(1).otherwise(0)
                        .add(new CaseBuilder().when(font.writerKeyword.eq(writer)).then(1).otherwise(0))
                        .add(new CaseBuilder().when(font.situationKeyword.eq(situation)).then(1).otherwise(0))
                        .add(new CaseBuilder().when(font.distanceKeyword.eq(distance)).then(1).otherwise(0));

        return queryFactory
                .selectFrom(font)
                .where(font.type.eq(FontType.CONTEXT))
                .where(matchScore.goe(minScore))
                .orderBy(matchScore.desc())
                .limit(3)
                .fetch();
    }
}
