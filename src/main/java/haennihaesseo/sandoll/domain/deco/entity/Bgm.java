package haennihaesseo.sandoll.domain.deco.entity;

import haennihaesseo.sandoll.domain.letter.entity.Letter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bgms")
public class Bgm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bgm_id", nullable = false)
    private Long bgmId;

    @Column(name = "bgm_url", nullable = false)
    private String bgmUrl;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "bgm_size", nullable = false)
    private Double bgmSize;
}
