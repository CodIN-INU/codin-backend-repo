package inu.codin.codin.global.config;

import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class BulkIngestListenerImpl<Context> implements BulkListener<Context> {

    @Override
    public void beforeBulk(long executionId, BulkRequest request, List<Context> contexts) {
        log.info("number of requests: {}", request.operations().size());
    }

    @Override
    public void afterBulk(long executionId, BulkRequest request, List<Context> contexts, BulkResponse response) {

    }

    @Override
    public void afterBulk(long executionId, BulkRequest request, List<Context> contexts, Throwable failure) {

    }
}