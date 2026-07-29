package com.apppang.apppang2.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.s3.access-key}")      //AWS_ACCESS_KEY_ID
    private String accessKey;

    @Value("${aws.s3.secret-key}")      //AWS_SECRET_ACCESS_KEY
    private String secretKey;

    //S3에 명령을 보내는 클라이언트 빈
    @Bean
    public S3Client s3Client(){
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}