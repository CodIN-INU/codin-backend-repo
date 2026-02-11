package inu.codin.codin.global.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Configuration
public class ElasticsearchConfig {
    @Value("${SPRING_ELASTICSEARCH_USERNAME}")
    private String username;

    @Value("${SPRING_ELASTICSEARCH_PASSWORD}")
    private String password;

    @Value("${CA_PATH}")
    private String certificationPath;

    @Value("${SPRING_ELASTICSEARCH_URIS}")
    private String[] exHost;

    private final int FLUSH_INTERVAL = 1;
    private final int MAX_OPERATION = 100;
    private final int CONNECT_TIMEOUT = 5_000;
    private final int SOCKET_TIMEOUT = 60_000;

    @Bean
    public RestClient buildClient() throws Exception {
        HttpHost[] httpHosts = new HttpHost[exHost.length];

        for (int i = 0; i < exHost.length; i++) {
            httpHosts[i] = HttpHost.create(exHost[i]);
        }

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts)
                .setDefaultHeaders(new BasicHeader[]{new BasicHeader("my-header", "my-value")})
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(CONNECT_TIMEOUT)
                                .setSocketTimeout(SOCKET_TIMEOUT)
                );

        String caPath = certificationPath;
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate trustedCa;

        try (FileInputStream fis = new FileInputStream(caPath)) {
            trustedCa = (X509Certificate) certificateFactory.generateCertificate(fis);
        }

        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null)
                .build();

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
                .setSSLContext(sslContext)
                .setDefaultCredentialsProvider(credentialsProvider))
        ;

        return restClientBuilder.build();
    }

    @Bean
    RestClientTransport restClientTransport(RestClient restClient, ObjectProvider<RestClientOptions> restClientOptions) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return new RestClientTransport(restClient, new JacksonJsonpMapper(mapper), restClientOptions.getIfAvailable());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClientTransport restClientTransport) {
        return new ElasticsearchClient(restClientTransport);
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient(RestClientTransport restClientTransport) {
        return new ElasticsearchAsyncClient(restClientTransport);
    }

    @Bean
    public BulkIngester<BulkOperation> bulkIngester(ElasticsearchClient client, BulkIngestListenerImpl<BulkOperation> listener) {
        return BulkIngester.of(b -> b
                .client(client)
                .flushInterval(FLUSH_INTERVAL, TimeUnit.SECONDS)
                .maxOperations(MAX_OPERATION)
                .listener(listener));
    }

    @Bean
    public BulkIngestListenerImpl<BulkOperation> bulkIngestListener() {
        return new BulkIngestListenerImpl<>();
    }
}
