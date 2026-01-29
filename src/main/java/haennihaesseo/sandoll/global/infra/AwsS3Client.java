package haennihaesseo.sandoll.global.infra;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class AwsS3Client {

  private final S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${spring.cloud.aws.s3.region}")
  private String region;

  // 파일 업로드
  public String uploadFile(String dirName, MultipartFile multipartFile) {
    try {
      String fileName = dirName + "/" + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(fileName)
          .contentType(multipartFile.getContentType())
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
      return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    } catch (IOException e){
      throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
    }
  }

  // 파일 삭제
  public void delete(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty())
      return;
    String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
    if (!fileUrl.startsWith(prefix))
      return;
    try {
      String fileKey = fileUrl.substring(prefix.length());

      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(fileKey)
          .build();

      s3Client.deleteObject(deleteObjectRequest);
    } catch (Exception e) {
      throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
    }
  }
}
