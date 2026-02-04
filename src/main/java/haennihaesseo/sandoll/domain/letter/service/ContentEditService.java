package haennihaesseo.sandoll.domain.letter.service;

import haennihaesseo.sandoll.domain.letter.cache.CachedLetter;
import haennihaesseo.sandoll.domain.letter.cache.CachedLetterRepository;
import haennihaesseo.sandoll.domain.letter.cache.CachedWord;
import haennihaesseo.sandoll.domain.letter.dto.request.LetterInfoRequest;
import haennihaesseo.sandoll.domain.letter.exception.LetterException;
import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentEditService {

  private final CachedLetterRepository cachedLetterRepository;

  // 유사도 임계값
  private static final double SIMILARITY_THRESHOLD = 0.65;

  /**
   * 편집 작업 타입
   */
  enum EditType {
    KEEP,
    DELETE,
    INSERT,
    REPLACE
  }

  /**
   * 편집 작업 정보
   */
  @Getter
  @AllArgsConstructor
  class Edit {
    private EditType type;
    private int oldIndex;  // old에서의 인덱스 (-1이면 해당 없음)
    private int newIndex;  // new에서의 인덱스 (-1이면 해당 없음)
    private String word;
  }

  /**
   * 편지 정보 입력 및 내용 수정
   */
  @Transactional
  public void inputLetterInfo(String letterId, LetterInfoRequest request) {
    CachedLetter cachedLetter = cachedLetterRepository.findById(letterId)
        .orElseThrow(() -> new LetterException(LetterErrorStatus.LETTER_NOT_FOUND));

    cachedLetter.setInfo(request.getTitle(), request.getSender());

    String oldContent = cachedLetter.getContent();
    String newContent = request.getContent();

    if(newContent.length() > 1000) {
      throw new LetterException(LetterErrorStatus.TOO_LONG_CONTENT);
    }

    // 정규화된 content로 실제 단어 변경 여부 확인
    String normalizedOld = normalizeContent(oldContent);
    String normalizedNew = normalizeContent(newContent);

    if (!normalizedOld.equals(normalizedNew)) {
      List<CachedWord> updatedWords = updateWords(
          cachedLetter.getWords(),
          newContent
      );
      cachedLetter.setWords(updatedWords);
    }

    // content는 항상 업데이트 (공백 형식 보존)
    cachedLetter.setContent(newContent);

    cachedLetterRepository.save(cachedLetter);
  }

  /**
   * 공백/줄바꿈 정규화
   */
  private String normalizeContent(String content) {
    if (content == null) {
      return "";
    }

    return content
        .replaceAll("\\s+", " ")  // 모든 공백류를 단일 공백으로
        .trim();  // 앞뒤 공백 제거
  }

  /**
   * 컨텐츠를 단어로 분리
   */
  private List<String> tokenizeContent(String content) {
    if (content == null || content.trim().isEmpty()) {
      return new ArrayList<>();
    }

    return Arrays.stream(content.split("\\s+"))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  /**
   * 단어 업데이트 메인 로직
   */
  private List<CachedWord> updateWords(List<CachedWord> existingWords,
      String newContent) {
    // 기존 단어 리스트
    List<String> oldWords = existingWords.stream()
        .map(CachedWord::getWord)
        .collect(Collectors.toList());

    List<String> newWords = tokenizeContent(newContent);

    // 단어 개수가 같으면 위치 기반
    if (oldWords.size() == newWords.size()) {
      return updateWordsByPosition(existingWords, oldWords, newWords);
    }

    // 단어 개수가 다르면 LCS + 유사도 검증
    return updateWordsByLCS(existingWords, oldWords, newWords);
  }

  /**
   * 위치 기반 업데이트 (단어 개수 동일)
   */
  private List<CachedWord> updateWordsByPosition(List<CachedWord> existingWords,
      List<String> oldWords,
      List<String> newWords) {
    List<CachedWord> updatedWords = new ArrayList<>();

    for (int i = 0; i < existingWords.size(); i++) {
      CachedWord existing = existingWords.get(i);
      String oldWord = oldWords.get(i);
      String newWord = newWords.get(i);

      updatedWords.add(CachedWord.builder()
          .word(newWord)
          .startTime(existing.getStartTime())
          .endTime(existing.getEndTime())
          .wordOrder(existing.getWordOrder())
          .build());

    }

    return updatedWords;
  }

  /**
   * LCS 기반 업데이트 (단어 개수 다름)
   */
  private List<CachedWord> updateWordsByLCS(List<CachedWord> existingWords,
      List<String> oldWords,
      List<String> newWords) {

    // 1. LCS 계산
    List<Edit> edits = computeDiff(oldWords, newWords);

    // 2. 유사도 기반 REPLACE 감지
    edits = detectReplacements(edits, oldWords, newWords);

    // 3. Edit 적용
    List<CachedWord> updatedWords = new ArrayList<>();
    int nextOldIdx = 0;

    for (Edit edit : edits) {
      switch (edit.getType()) {
        case KEEP:
          CachedWord existingWord = existingWords.get(edit.getOldIndex());
          updatedWords.add(existingWord);
          nextOldIdx = edit.getOldIndex() + 1;
          break;

        case REPLACE:
          // 유사한 단어 수정 - startTime 상속
          CachedWord originalWord = existingWords.get(edit.getOldIndex());
          updatedWords.add(CachedWord.builder()
              .word(edit.getWord())
              .startTime(originalWord.getStartTime())  // 타임스탬프 상속
              .endTime(originalWord.getEndTime())
              .wordOrder(originalWord.getWordOrder())
              .build());
          nextOldIdx = edit.getOldIndex() + 1;

          break;

        case INSERT:
          CachedWord nextExisting = (nextOldIdx < existingWords.size()) ?
              existingWords.get(nextOldIdx) : null;

          double newOrder = calculateWordOrder(updatedWords, nextExisting);

          updatedWords.add(CachedWord.builder()
              .word(edit.getWord())
              .startTime(null)
              .endTime(null)
              .wordOrder(newOrder)
              .build());

          break;

        case DELETE:
          nextOldIdx = edit.getOldIndex() + 1;
          break;
      }
    }

    return updatedWords;
  }

  /**
   * LCS 계산
   */
  private List<Edit> computeDiff(List<String> oldWords, List<String> newWords) {
    int m = oldWords.size();
    int n = newWords.size();

    // DP 테이블
    int[][] dp = new int[m + 1][n + 1];

    // LCS 길이 계산
    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        if (oldWords.get(i - 1).equals(newWords.get(j - 1))) {
          dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
          dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        }
      }
    }

    // 역추적
    List<Edit> edits = new ArrayList<>();
    int i = m, j = n;

    while (i > 0 || j > 0) {
      if (i > 0 && j > 0 && oldWords.get(i - 1).equals(newWords.get(j - 1))) {
        edits.add(0, new Edit(EditType.KEEP, i - 1, j - 1, oldWords.get(i - 1)));
        i--;
        j--;
      } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
        edits.add(0, new Edit(EditType.INSERT, -1, j - 1, newWords.get(j - 1)));
        j--;
      } else if (i > 0) {
        edits.add(0, new Edit(EditType.DELETE, i - 1, -1, oldWords.get(i - 1)));
        i--;
      }
    }

    return edits;
  }

  /**
   * DELETE + INSERT 패턴 중 유사한 단어는 REPLACE로 변환
   */
  private List<Edit> detectReplacements(List<Edit> edits,
      List<String> oldWords,
      List<String> newWords) {
    List<Edit> optimized = new ArrayList<>();

    for (int idx = 0; idx < edits.size(); idx++) {
      Edit current = edits.get(idx);

      // DELETE 다음에 INSERT가 오는 경우 체크
      if (current.getType() == EditType.DELETE &&
          idx + 1 < edits.size() &&
          edits.get(idx + 1).getType() == EditType.INSERT) {

        Edit deleteEdit = current;
        Edit insertEdit = edits.get(idx + 1);

        String oldWord = oldWords.get(deleteEdit.getOldIndex());
        String newWord = newWords.get(insertEdit.getNewIndex());

        // 같은 위치에 있는지 확인
        boolean samePosition = (deleteEdit.getOldIndex() == insertEdit.getNewIndex());

        double similarity = calculateSimilarity(oldWord, newWord);

        // REPLACE 조건: 유사도 높고 + 같은 위치
        if (similarity >= SIMILARITY_THRESHOLD && samePosition) {
          // REPLACE로 병합
          optimized.add(new Edit(
              EditType.REPLACE,
              deleteEdit.getOldIndex(),
              insertEdit.getNewIndex(),
              newWord
          ));
          idx++;  // INSERT도 처리했으므로 건너뛰기

        } else {
          // 유사도 낮거나 위치 다르면 그대로
          optimized.add(current);

        }
      } else {
        optimized.add(current);
      }
    }

    return optimized;
  }

  /**
   * 단어 유사도 계산 (Levenshtein Distance)
   */
  private double calculateSimilarity(String word1, String word2) {
    if (word1.equals(word2)) {
      return 1.0;
    }

    LevenshteinDistance levenshtein = new LevenshteinDistance();
    int distance = levenshtein.apply(word1, word2);
    int maxLen = Math.max(word1.length(), word2.length());

    return 1.0 - ((double) distance / maxLen);
  }

  /**
   * wordOrder 계산
   */
  private double calculateWordOrder(List<CachedWord> updatedWords,
      CachedWord nextExisting) {
    double prevOrder = updatedWords.isEmpty() ? 0.0 :
        updatedWords.get(updatedWords.size() - 1).getWordOrder();

    if (nextExisting != null) {
      // 중간 삽입
      double nextOrder = nextExisting.getWordOrder();
      return (prevOrder + nextOrder) / 2.0;
    } else {
      // 끝에 추가
      return prevOrder + 1.0;
    }
  }

}
