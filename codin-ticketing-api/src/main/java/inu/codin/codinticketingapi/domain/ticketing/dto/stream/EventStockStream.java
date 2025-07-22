package inu.codin.codinticketingapi.domain.ticketing.dto.stream;

/**
 * 티켓팅 API 서버에서 전송하는 재고 상태 Update Stream 데이터
 * @param eventId 유저가 구독중인 이벤트 ID
 * @param quantity SSE로 발송할 현재고 수량 데이터
 */
public record EventStockStream(Long eventId, Long quantity) {}