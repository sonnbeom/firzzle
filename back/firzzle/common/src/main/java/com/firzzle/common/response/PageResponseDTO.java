package com.firzzle.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter

public class PageResponseDTO<T> {

    // 컨텐츠

    private List<T> content;



    // 페이지 정보 (인터페이스에 맞게 속성명 변경)

    private int pageNumber;      // 현재 페이지 번호 (pageNumber → pageNumber)

    private int pageSize;        // 페이지 크기

    private long totalElements;  // 총 요소 수

    private int totalPages;      // 총 페이지 수



    // 페이지 상태

    private boolean last;        // 마지막 페이지 여부

    private boolean hasNext;     // 다음 페이지 존재 여부 (새로 추가)



    @Builder

    public PageResponseDTO(List<T> content, int pageNumber, int pageSize, long totalElements) {

        this.content = content;

        this.pageNumber = pageNumber;

        this.pageSize = pageSize;

        this.totalElements = totalElements;



        // 총 페이지 수 계산

        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);



        // 페이지 상태 계산

        this.last = pageNumber >= totalPages - 1;

        this.hasNext = pageNumber < totalPages - 1;

    }

}
/*
	// User 타입으로 사용할 경우
	PageResponseDTO<User> userPage = new PageResponseDTO<>(
	    userList,      // List<User> 타입
	    0,             // pageNumber
	    10,            // pageSize
	    50,            // totalElements
	    false          // last
	);
*/
