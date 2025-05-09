package com.firzzle.common.config;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
public class JasyptConfig {

    @Value(value = "${jasypt.secret-key}")
    String secretKey;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setPoolSize(2);
        encryptor.setPassword(secretKey);
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");

        return encryptor;
    }

    public static void main(String[] args) {
        ArrayList<String> encryptList = new ArrayList<>();
        ArrayList<String> decryptList = new ArrayList<>();
        encryptList.add("jdbc:p6spy:mysql://43.202.107.226:3306/firzzle_ai_playground?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8\r\n" + //
                        ""); //암호화 대상 문자 추가
        //복호화 대상 문자열 추가 ENC() 괄호 내부 값으로 넣기
        decryptList.add("ApPE5/heucpTHeTJ6+Axjilq7GH8GkObxfm5QR+BnOQg3LiADkQOVETlEKQYFcUjuMSs8kL31EgPqZsvKLt9AZKw7H+cswqMIofHrk+8ZjN+oZy9rbFAcs1vpFR+LyUeBTR8xcQJN+2fXDqogdXWk/NgcoGolAnjgYXhKJT09YI=");
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setPoolSize(2);
        encryptor.setPassword("firzzle_new");
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        System.out.println("============암호화=============");
        for (String target : encryptList) {
            /* 암호화는 매번 결과 값이 달라짐 */
            System.out.println(target + " => ENC(" + encryptor.encrypt(target) + ")");
        }
        System.out.println("============복호화=============");
        for (String target : decryptList) {
            try {
                System.out.println(target + " => " + encryptor.decrypt(target));
            } catch (Exception e) {
                System.out.println("올바르지 않은 복호화");
            }
        }
    }
}
