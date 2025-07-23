//package inu.codin.codinticketingapi.domain.ticketing.scheduler;
//
//import inu.codin.codinticketingapi.domain.admin.entity.EventStatus;
//import inu.codin.codinticketingapi.domain.ticketing.dto.stream.EventStockStream;
//import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
//import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
//import inu.codin.codinticketingapi.domain.ticketing.service.EventStockProducerService;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EventStockSchedulerService {
//
//    private final StockRepository stockRepository;
//    private final EventStockProducerService producerService;
//
//    // 스케쥴러 - 별도 스레드 사용
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//    // 이벤트 ID별 마지막 재고 수 저장
//    private final Map<Long, Integer> lastStockMap = new ConcurrentHashMap<>();
//
//    private static final long INITIAL_DELAY = 0L;
//    private static final long PERIOD = 1000L;
//
//    @PostConstruct
//    public void start() {
//        scheduler.scheduleAtFixedRate(this::checkStockChanges, INITIAL_DELAY, PERIOD, TimeUnit.MILLISECONDS);
//        log.info("EventStockSchedulerService 시작, initialDelay : {}, period : {}", INITIAL_DELAY, PERIOD);
//    }
//
//    @Transactional(readOnly = true)
//    public void checkStockChanges() {
//        try {
//            // Event가 열린 경우에만 조회
//            List<Stock> stocks = stockRepository.findAllByEvent_EventStatus(EventStatus.ACTIVE);
//
//            for (Stock stock : stocks) {
//                Long eventId = stock.getEvent().getId();
//                int currentStock = stock.getStock();
//                int previousStock = lastStockMap.getOrDefault(eventId, currentStock);
//
//                if (currentStock != previousStock) {
//                    lastStockMap.put(eventId, currentStock);
//                    producerService.publishEventStock(new EventStockStream(eventId, (long) currentStock));
//                    log.debug("[checkStockChanges] 재고 상황에 대한 변화가 감지되었습니다. {}: {} -> {}", eventId, previousStock, currentStock);
//                }
//            }
//        } catch (Exception e) {
//            log.error("[checkStockChanges] 재고 상황 스케쥴러에서 에러가 발생했습니다, msg:{}", e.getMessage());
//        }
//    }
//
//    @PreDestroy
//    public void stop() {
//        scheduler.shutdownNow();
//        log.info("EventStockSchedulerService가 종료되었습니다.");
//    }
//}
