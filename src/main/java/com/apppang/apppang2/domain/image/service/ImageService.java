package com.apppang.apppang2.domain.image.service;

import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final StringRedisTemplate redisTemplate;

    @Value("${aws.s3.bucket}")          //AWS_S3_BUCKET
    private String bucket;

    //이미지을 S3에 올리고 접근 URL 반환
    public String upload(Long userId, MultipartFile file){
        checkRateLimit(userId);

        //이미지 파일만 허용
        String contentType = file.getContentType();
        if (file.isEmpty() || contentType == null || !contentType.startsWith("image/")){
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다.");
        }

        //파일명 충돌 방지: UUID + 원본 이름, UUID는 동일한 이름의 이미지를 구별하기 위해 사용
        String key = "reviews/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            s3Client.putObject(b -> b.bucket(bucket).key(key).contentType(contentType),
                    RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
        }

        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + key;
    }

    //유저당 1분에 10장까지만 허용
    private void checkRateLimit(Long userId){
        String key = "ratelimit:image:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            //첫 요청일 때만 60초 타이머 시작 → 만료되면 카운트 자동 리셋
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (count > 10) {
            throw new CustomException(HttpStatus.TOO_MANY_REQUESTS, "업로드 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}