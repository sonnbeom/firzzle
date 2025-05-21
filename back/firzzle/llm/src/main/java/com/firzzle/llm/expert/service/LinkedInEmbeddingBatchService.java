package com.firzzle.llm.expert.service;

import com.firzzle.llm.expert.mapper.LinkedInProfileEmbeddingMapper;
import com.firzzle.llm.expert.dto.LinkedInEmbeddingRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * LinkedIn 프로필 임베딩 배치 처리 서비스
 * - 수동 배치 처리 요청을 처리하는 서비스
 * - REST API 또는 직접 호출을 통해 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedInEmbeddingBatchService {

    private final LinkedInProfileEmbeddingMapper profileEmbeddingMapper;
    private final LinkedInVectorSearchService vectorSearchService;

    /**
     * 프로필 목록을 배치로 임베딩 처리합니다.
     * 주로 관리자 API 또는 일회성 처리 요청에 사용됩니다.
     *
     * @param profileSeqs 처리할 프로필 일련번호 목록
     * @return 처리 결과
     */
    @Async
    public CompletableFuture<BatchProcessResult> processBatch(List<Long> profileSeqs) {
        log.info("LinkedIn 프로필 배치 처리 시작: 총 {}개", profileSeqs.size());
        BatchProcessResult result = new BatchProcessResult();

        try {
            // 컬렉션 확인 및 생성
            if (!vectorSearchService.ensureCollectionExists()) {
                result.setErrorMessage("벡터 데이터베이스 컬렉션 준비 실패");
                return CompletableFuture.completedFuture(result);
            }

            // 프로필 정보 조회
            List<LinkedInEmbeddingRequestDTO> profiles = fetchProfilesWithSkills(profileSeqs);
            log.info("배치 처리를 위한 프로필 정보 조회 완료: {}개", profiles.size());

            // 각 프로필 임베딩 처리
            for (LinkedInEmbeddingRequestDTO profile : profiles) {
                try {
                    boolean success = vectorSearchService.saveProfileToVectorDb(profile);
                    if (success) {
                        result.addSuccess(profile.getProfileSeq());
                        log.info("프로필 임베딩 성공: profileSeq={}", profile.getProfileSeq());
                    } else {
                        result.addFailed(profile.getProfileSeq(), "벡터 저장 실패");
                        log.warn("프로필 임베딩 실패: profileSeq={}", profile.getProfileSeq());
                    }
                } catch (Exception e) {
                    log.error("프로필 {} 처리 중 오류: {}", profile.getProfileSeq(), e.getMessage());
                    result.addFailed(profile.getProfileSeq(), e.getMessage());
                }
            }

            log.info("LinkedIn 프로필 배치 처리 완료: 성공={}, 실패={}",
                    result.getSuccessCount(), result.getFailedCount());

        } catch (Exception e) {
            log.error("배치 처리 전체 오류: {}", e.getMessage(), e);
            result.setErrorMessage(e.getMessage());
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 프로필 정보와 스킬을 함께 조회합니다.
     *
     * @param profileSeqs 프로필 일련번호 목록
     * @return 프로필 정보 목록
     */
    private List<LinkedInEmbeddingRequestDTO> fetchProfilesWithSkills(List<Long> profileSeqs) {
        // 프로필 기본 정보 조회
        List<LinkedInEmbeddingRequestDTO> profiles = profileEmbeddingMapper.selectProfilesForEmbedding(profileSeqs);

        // 각 프로필별 스킬 정보 조회 및 설정
        for (LinkedInEmbeddingRequestDTO profile : profiles) {
            List<String> skills = profileEmbeddingMapper.selectSkillsByProfileSeq(profile.getProfileSeq());
            profile.setSkills(skills);
        }

        return profiles;
    }

    /**
     * 배치 처리 결과 클래스
     */
    public static class BatchProcessResult {
        private final List<Long> successProfileSeqs = new ArrayList<>();
        private final List<FailedProfile> failedProfiles = new ArrayList<>();
        private String errorMessage;

        public void addSuccess(Long profileSeq) {
            successProfileSeqs.add(profileSeq);
        }

        public void addFailed(Long profileSeq, String reason) {
            failedProfiles.add(new FailedProfile(profileSeq, reason));
        }

        public int getSuccessCount() {
            return successProfileSeqs.size();
        }

        public int getFailedCount() {
            return failedProfiles.size();
        }

        public List<Long> getSuccessProfileSeqs() {
            return successProfileSeqs;
        }

        public List<FailedProfile> getFailedProfiles() {
            return failedProfiles;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public static class FailedProfile {
            private final Long profileSeq;
            private final String reason;

            public FailedProfile(Long profileSeq, String reason) {
                this.profileSeq = profileSeq;
                this.reason = reason;
            }

            public Long getProfileSeq() {
                return profileSeq;
            }

            public String getReason() {
                return reason;
            }
        }
    }
}