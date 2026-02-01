package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.converter.LetterBoxConverter;
import haennihaesseo.sandoll.domain.letter.dto.response.LetterDetailResponse;
import haennihaesseo.sandoll.domain.letter.entity.Letter;
import haennihaesseo.sandoll.domain.letter.entity.Word;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.repository.LetterRepository;
import haennihaesseo.sandoll.domain.letter.repository.WordRepository;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterDetailService {

    private final LetterRepository letterRepository;
    private final WordRepository wordRepository;
    private final LetterBoxConverter letterBoxConverter;

    public LetterDetailResponse getLetterDetails(Long letterId) {
        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

        List<Word> words = wordRepository.findByLetterLetterIdOrderByCreatedAtAsc(letterId);

        return letterBoxConverter.toLetterDetailResponse(letter, letter.getBgm(),
                letter.getTemplate(), letter.getFont(),
                letter.getVoice(), words);
    }
}
