//package inu.codin.codinticketingapi.domain.ticketing.service;
//
//import inu.codin.codinticketingapi.domain.admin.entity.Event;
//import inu.codin.codinticketingapi.domain.ticketing.dto.event.StockDecrementRequest;
//import inu.codin.codinticketingapi.domain.ticketing.entity.Stock;
//import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
//import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
//import inu.codin.codinticketingapi.domain.ticketing.repository.StockRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class StockService {
//
//    private final StockRepository stockRepository;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleStockDecrementRequest(StockDecrementRequest request){
//        Event event = request.getEvent();
//        Stock stock = stockRepository.findByEvent(event)
//                .orElseThrow(() -> new TicketingException(TicketingErrorCode.STOCK_NOT_FOUND));
//
//        if (stock.decrease()) {
//            stockRepository.save(stock);
//        } else {
//            log.error("[handleStockDecrementRequest] Stock has been decremented by eventId : {}", event.getId());
//        }
//    }
//}
