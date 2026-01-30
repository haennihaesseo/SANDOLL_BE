package haennihaesseo.sandoll.domain.font.entity;

import haennihaesseo.sandoll.domain.font.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "fonts")
public class Font {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "font_id", nullable = false)
    private Long fontId;

    @Column(name = "font_url", nullable = false)
    private String fontUrl;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FontType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "bone_keyword", nullable = true)
    private Bone boneKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "writer_keyword", nullable = true)
    private Writer writerKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "situation_keyword", nullable = true)
    private Situation situationKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_keyword", nullable = true)
    private Distance distanceKeyword;
}
