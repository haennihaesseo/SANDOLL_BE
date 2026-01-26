package haennihaesseo.sandoll.domain.font.entity;

import haennihaesseo.sandoll.domain.font.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    @Enumerated(EnumType.STRING)
    @Column(name = "bone_keyword", nullable = false)
    private Bone boneKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_keyword", nullable = false)
    private Target targetKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "situation_keyword", nullable = false)
    private Situation situationKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_keyword", nullable = false)
    private Distance distanceKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "dynamic_keyword", nullable = false)
    private Dynamic dynamicKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "speed_keyword", nullable = false)
    private Speed speedKeyword;
}
