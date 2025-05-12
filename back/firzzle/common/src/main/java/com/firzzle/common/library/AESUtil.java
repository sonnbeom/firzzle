package com.firzzle.common.library;

import com.firzzle.common.constant.CubeOneItem;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES256Cipher와는 별도로 가벼운 암복호화 처리 컴포넌트
 * 일반적인 데이터의 암복호화 처리 담당.
 */
@Slf4j
@Component
public class AESUtil {

    private static final String AESKEY = "92c42d9dk49dhj3480gh280h08sb80f0";
    private static final String MOBILE_AESKEY = "zpDr3tMdlq1cTakZ";
    public static final int GCM_IV_LENGTH = 16;
    public static final int GCM_TAG_LENGTH = 128;
    private static SecretKeySpec secretKeySpec;
    private static SecretKeySpec mobileSecretKeySpec;

    private static String utilProfile;

    @Value("${spring.profiles.active}")
    private void setActiveProfile(String value) {
        utilProfile = value;
    }

    static {
        try {
            //MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = AESKEY.getBytes(StandardCharsets.UTF_8);
            byte[] mobilekey = MOBILE_AESKEY.getBytes(StandardCharsets.UTF_8);
            secretKeySpec = new SecretKeySpec(key, "AES");
            mobileSecretKeySpec = new SecretKeySpec(mobilekey, "AES");
        } catch (Exception e) {
            //log.error(e.getMessage());
        }
    }

    /**
     * AES암호화
     * @param str
     * @param crudLog
     * @return
     */
    public static String AESEncrypt(String str, int crudLog) {
        try {
            if (str == null) {
                return null;
            }
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[GCM_IV_LENGTH];
            random.nextBytes(iv);
            SecretKeySpec keySpec = secretKeySpec;
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] cipherText = cipher.doFinal(str.getBytes());
            return new String(Base64.getEncoder().encode(concatenate(iv, cipherText)),"utf-8");
        } catch (RuntimeException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] concatenate(byte[] firstArray, byte[] secondArray) {
        byte[] result = Arrays.copyOf(firstArray, firstArray.length + secondArray.length);
        System.arraycopy(secondArray, 0, result, firstArray.length, secondArray.length);
        return result;
    }

    /**
     * AES암호화
     * @param str
     * @return
     */
    public static String AESEncrypt(String str) {
        return AESEncrypt(str, 10);
    }

    /**
     * AES 복호화
     * @param str
     * @param checkMaskingSession
     * @return
     */
    public static String AESDecrypt(String str, String checkMaskingSession) {
        try {
            if ("Y".equals(checkMaskingSession)) {
                return AESDecrypt(str);
            } else {
                return MaskingUtil.masking(AESDecrypt(str), "normal", "**", "*");
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES 복호화
     * @param str
     * @return
     */
    public static String AESDecrypt(String str) {
        try {
            if (str == null) {
                return null;
            }
            byte[] ciphertext = Base64.getDecoder().decode(str.getBytes());
            byte[] iv = Arrays.copyOfRange(ciphertext, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(ciphertext, GCM_IV_LENGTH, ciphertext.length);
            SecretKeySpec keySpec = secretKeySpec;
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            return new String(cipher.doFinal(cipherText),"utf-8");
        } catch (RuntimeException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DB 전용 암호화
     * @param str
     * @param crudLog
     * @param cubeOneItem
     * @return
     */
    public static String encrypt(String str, int crudLog, CubeOneItem cubeOneItem) {
        try {
            if (cubeOneItem.equals(CubeOneItem.PWD)) {
                return encodeSha(str);
            } else {
                return AESEncrypt(str, 10);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DB 전용 암호화
     * @param str
     * @param cubeOneItem
     * @return
     */
    public static String encrypt(String str, CubeOneItem cubeOneItem) {
        return encrypt(str, 10, cubeOneItem);
    }

    /**
     * DB 전용 복호화
     * @param str
     * @param checkMaskingSession
     * @param cubeOneItem
     * @return
     */
    public static String decrypt(String str, String checkMaskingSession, CubeOneItem cubeOneItem) {
        try {
            if ("Y".equals(checkMaskingSession)) {
                return decrypt(str, cubeOneItem);
            } else {
                return MaskingUtil.masking(decrypt(str, cubeOneItem), "normal", "**", "*");
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * DB 전용 복호화
     * @param str
     * @param cubeOneItem
     * @return
     */
    public static String decrypt(String str, CubeOneItem cubeOneItem) {
        try {
            return AESDecrypt(str);
        } catch (RuntimeException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodeBase64(byte[] source) {
        return Base64.getEncoder().encodeToString(source);
    }

    private static byte[] decodeBase64(String encodedString) {
        return Base64.getDecoder().decode(encodedString);
    }

    /**
     * SHA 암호화
     * @param planeText
     * @return
     */
    public static String encodeSha(String planeText) {
        String encodingText = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(planeText.getBytes(StandardCharsets.UTF_8));
            encodingText = DatatypeConverter.printBase64Binary(md.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA암호화 오류 : {}", e.getMessage().replaceAll("[\r\n]", ""));
        }
        return encodingText;
    }

    /**
     * 모바일전용 AES PADDING DECRYPT
     * @param str
     * @return
     */
    public static String AESPaddingDecrypt(String str) {
        try {
            byte[] textBytes = Base64.getDecoder().decode(str.getBytes());
            byte[] iv = new byte[16];
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec newKey = mobileSecretKeySpec;
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, newKey, ivSpec);
            return new String(cipher.doFinal(textBytes), "UTF-8");
        } catch (Exception e) {
            log.error("AESPaddingDecrypt Error");
            return "";
        }
    }
}