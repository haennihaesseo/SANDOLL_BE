package haennihaesseo.sandoll.domain.deco.entity;

import haennihaesseo.sandoll.domain.letter.entity.Letter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "letter_id", nullable = false)
    private Letter letter;
}
