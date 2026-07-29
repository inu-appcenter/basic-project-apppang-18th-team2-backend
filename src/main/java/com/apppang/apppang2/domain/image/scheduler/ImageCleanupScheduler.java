package com.apppang.apppang2.domain.image.scheduler;

import com.apppang.apppang2.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

//DB에서 사용되지 않는 S3 이미지를 주기적으로 회수
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupScheduler {

    private final S3Client s3Client;
    private final ReviewRepository reviewRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    //매일 새벽 4시 실행 (초 분 시 일 월 요일, 한국시간 기준)
    @Scheduled(cron = "0 0 4 * * *",  zone = "Asia/Seoul")
    public void cleanOrphanImages() {
        //DB에 저장된 모든 이미지 URL 수집
        Set<String> usedUrls = new HashSet<>();
        usedUrls.addAll(reviewRepository.findAllImageUrl1());
        usedUrls.addAll(reviewRepository.findAllImageUrl2());

        //24시간 안 된 파일은 리뷰 작성 진행 중일 수 있으므로 삭제하지 않는다.
        Instant threshold = Instant.now().minus(Duration.ofHours(24));

        //S3의 reviews/ 전체 순회 (1000개 넘어도 자동 페이징)
        int[] deleted = {0};
        s3Client.listObjectsV2Paginator(b -> b.bucket(bucket).prefix("reviews/"))
                .contents()
                .forEach(obj -> {
                    //업로드 때와 같은 규칙으로 URL 조립해 대조 — ImageService의 규칙과 반드시 일치해야 함
                    String url = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + obj.key();
                    if (!usedUrls.contains(url) && obj.lastModified().isBefore(threshold)) {
                        s3Client.deleteObject(d -> d.bucket(bucket).key(obj.key()));
                        deleted[0]++;
                    }
                });

        log.info("고아 이미지 청소 완료: {}건 삭제", deleted[0]);
    }
}