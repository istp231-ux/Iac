package kr.or.nia.dpg.vpos.util.crypto.aria;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class ARIACryptor {

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final String ALGORITHM = "ARIA";
    private final String TRANSFORMATION = "ARIA/ECB/PKCS7Padding";
    private final String PROVIDER = "BC";
    private final String CHARSET = "UTF-8";

    private final Map<String, String> encryptionKeys;

    public ARIACryptor(
            @Value("${encryption.aria.keys.kait}") String kaitEncryptKey,
            @Value("${encryption.aria.keys.log}") String logEncryptKey) {
        this.encryptionKeys = Map.of(
                "kait", kaitEncryptKey,
                "log", logEncryptKey
        );
    }

    public String encrypt(String plainText, String keyName) {
        if (plainText == null || plainText.isEmpty()) {
            log.warn("[ARIACryptor] 암호화 대상 문자열이 null 또는 빈 값입니다. keyName={}", keyName);
            return "";
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(getEncryptionKey(keyName));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(CHARSET));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("[ARIACryptor] 암호화 실패 - keyName={}, exType={}, error={}", keyName, e.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("ARIA 암호화 처리 중 오류 발생");
        }
    }

    public String decrypt(String cipherText, String keyName) {
        if (cipherText == null || cipherText.isEmpty()) {
            log.warn("[ARIACryptor] 복호화 대상 문자열이 null 또는 빈 값입니다. keyName={}", keyName);
            return "";
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(getEncryptionKey(keyName));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, CHARSET);
        } catch (Exception e) {
            log.error("[ARIACryptor] 복호화 실패 - keyName={}, exType={}, error={}", keyName, e.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("ARIA 복호화 처리 중 오류 발생");
        }
    }

    private String getEncryptionKey(String keyName) {
        String encryptionKey = encryptionKeys.get(keyName);
        if (encryptionKey == null) {
            log.error("[ARIACryptor] keyName에 해당하는 암호화키가 존재하지 않습니다. keyName={}", keyName);
            throw new IllegalStateException("ARIA 암호화키 미설정: " + keyName);
        }
        return encryptionKey;
    }
}
