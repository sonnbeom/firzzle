package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : LinkedInCrawlerResponseDTO.java
 * @Description : LinkedIn 크롤링 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 크롤링 응답 DTO")
public class LinkedInCrawlerResponseDTO {

    @Schema(description = "처리 결과 메시지", example = "크롤링이 완료되었습니다.")
    private String message;

    @Schema(description = "요청한 키워드", example = "llm specialist")
    private String keyword;

    @Schema(description = "수집된 프로필 수", example = "3")
    private Integer profileCount;

    @Schema(description = "수집된 LinkedIn 프로필 목록")
    private List<LinkedInProfileDTO> profiles;

    @Schema(description = "크롤링 일시", example = "20250518153045")
    private String crawledAt;

    @Builder(builderMethodName = "responseBuilder")
    public LinkedInCrawlerResponseDTO(String message, String keyword, Integer profileCount,
                                      List<LinkedInProfileDTO> profiles, String crawledAt) {
        this.message = message;
        this.keyword = keyword;
        this.profileCount = profileCount;
        this.profiles = profiles;
        this.crawledAt = crawledAt;
    }

    /**
     * 페이지네이션 적용된 응답 빌더
     */
    public static class PaginationResponseBuilder {
        private String message;
        private String keyword;
        private Integer profileCount;
        private List<LinkedInProfileDTO> profiles;
        private String crawledAt;
        private Integer page;
        private Integer pageSize;
        private Integer totalElements;

        public PaginationResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public PaginationResponseBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public PaginationResponseBuilder profileCount(Integer profileCount) {
            this.profileCount = profileCount;
            return this;
        }

        public PaginationResponseBuilder profiles(List<LinkedInProfileDTO> profiles) {
            this.profiles = profiles;
            return this;
        }

        public PaginationResponseBuilder crawledAt(String crawledAt) {
            this.crawledAt = crawledAt;
            return this;
        }

        public PaginationResponseBuilder page(Integer page) {
            this.page = page;
            return this;
        }

        public PaginationResponseBuilder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginationResponseBuilder totalElements(Integer totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public LinkedInCrawlerResponseDTO build() {
            LinkedInCrawlerResponseDTO dto = new LinkedInCrawlerResponseDTO();
            dto.setMessage(message);
            dto.setKeyword(keyword);
            dto.setProfileCount(profileCount);
            dto.setProfiles(profiles);
            dto.setCrawledAt(crawledAt);

            // 페이지네이션 정보 추가
            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setPage(page != null ? page : 1);
            paginationInfo.setPageSize(pageSize != null ? pageSize : profiles.size());
            paginationInfo.setTotalElements(totalElements != null ? totalElements : profiles.size());
            paginationInfo.setTotalPages((int) Math.ceil((double) paginationInfo.getTotalElements() / paginationInfo.getPageSize()));
            dto.setPagination(paginationInfo);

            return dto;
        }
    }

    public static PaginationResponseBuilder paginationBuilder() {
        return new PaginationResponseBuilder();
    }

    @Schema(description = "페이지네이션 정보")
    private PaginationInfo pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "페이지네이션 정보")
    public static class PaginationInfo {
        @Schema(description = "현재 페이지", example = "1")
        private Integer page;

        @Schema(description = "페이지 크기", example = "10")
        private Integer pageSize;

        @Schema(description = "전체 항목 수", example = "42")
        private Integer totalElements;

        @Schema(description = "전체 페이지 수", example = "5")
        private Integer totalPages;
    }
}