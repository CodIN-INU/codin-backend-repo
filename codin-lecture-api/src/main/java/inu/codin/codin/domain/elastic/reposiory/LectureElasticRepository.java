package inu.codin.codin.domain.elastic.reposiory;

import inu.codin.codin.domain.elastic.document.LectureDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureElasticRepository extends ElasticsearchRepository<LectureDocument, Long> {

}
