package haennihaesseo.sandoll.domain.deco.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Size {
    // 기준 글자 수 변경시 변경 예정
    LARGE(1000), MEDIUM(720), SMALL(360);

    private final int maxLength;

    public static Size fromLength(int length) {
        if (length <= SMALL.maxLength) return SMALL;
        if (length <= MEDIUM.maxLength) return MEDIUM;
        return LARGE;
    }
}
