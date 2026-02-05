package haennihaesseo.sandoll.domain.letter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "voices")
public class Voice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voice_id", nullable = false)
    private Long voiceId;

    @Column(name = "voice_url", nullable = false)
    private String voiceUrl;

    @Column(name = "duration", nullable = false)
    private Integer duration;
}
