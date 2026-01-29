package haennihaesseo.sandoll.domain.letter.util;

import haennihaesseo.sandoll.domain.letter.status.LetterErrorStatus;
import haennihaesseo.sandoll.global.exception.GlobalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AESUtil {

    private static final String ALGORITHM = "AES";

    @Value("${app.letter-encrypt-key}")
    private String SECRET_KEY;

    public String encrypt(Long letterId){
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] bytes = cipher.doFinal(String.valueOf(letterId).getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new GlobalException(LetterErrorStatus.LETTER_ENCRYPT_FAILED);
        }
    }

    public Long decrypt(String secretLetterKey){
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] encryptedBytes = Base64.getDecoder().decode(secretLetterKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return Long.parseLong((new String(decryptedBytes)));
        } catch (Exception e) {
            throw new GlobalException(LetterErrorStatus.LETTER_DECRYPT_FAILED);
        }
    }
}
