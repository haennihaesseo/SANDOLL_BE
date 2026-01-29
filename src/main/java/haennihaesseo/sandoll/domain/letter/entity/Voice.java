package haennihaesseo.sandoll.domain.letter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
