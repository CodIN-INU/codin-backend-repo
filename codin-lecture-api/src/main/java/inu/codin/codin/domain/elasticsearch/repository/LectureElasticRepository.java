package inu.codin.codin.domain.elasticsearch.repository;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureElasticRepository extends ElasticsearchRepository<LectureDocument, Long> {
}
