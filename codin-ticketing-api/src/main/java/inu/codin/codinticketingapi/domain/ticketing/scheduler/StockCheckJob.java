package inu.codin.codinticketingapi.domain.ticketing.scheduler;

import inu.codin.codinticketingapi.domain.ticketing.dto.stream.EventStockStream;
import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
import inu.codin.codinticketingapi.domain.ticketing.service.EventStockProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockCheckJob implements Job {

    private final StockRepository stockRepository;
    private final EventStockProducerService producerService;

    // 이벤트 ID별 마지막 재고 수 저장
    private static final Map<Long, Integer> lastStockMap = new ConcurrentHashMap<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long eventId = context.getJobDetail().getJobDataMap().getLong("eventId");
        try {
            Stock stock = stockRepository.findById(eventId).orElseThrow(() -> new TicketingException(TicketingErrorCode.STOCK_NOT_FOUND));

            final int current = stock.getStock();

            lastStockMap.compute(eventId, (id, prev) -> {
                // 최초 등록 시에는 prev == null
                if (prev == null) {
                    return current;
                }
                // 재고 변화가 감지되면 이벤트 발행 및 값 갱신
                if (prev != current) {
                    producerService.publishEventStock(new EventStockStream(eventId, (long) current));
                    log.debug("[checkStockChanges] 재고 상황 변화 감지, eventId:{} = {} -> {}", eventId, prev, current);
                    return current;
                }
                // 변화 없으면 기존 값 유지
                return prev;
            });
        } catch (Exception e) {
            log.error("[checkStockChanges] 재고 상황 스케쥴러에서 에러가 발생했습니다, eventId:{}, msg:{} ", eventId, e.getMessage());
        }
    }
}
