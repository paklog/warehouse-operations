package com.paklog.warehouse.adapter.persistence.mongodb;

import com.paklog.warehouse.domain.putwall.PutWall;
import com.paklog.warehouse.domain.putwall.PutWallId;
import com.paklog.warehouse.domain.putwall.PutWallRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PutWallRepositoryAdapter implements PutWallRepository {

    private final MongoTemplate mongoTemplate;

    public PutWallRepositoryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<PutWall> findById(PutWallId putWallId) {
        PutWallDocument document = mongoTemplate.findById(putWallId.getValue(), PutWallDocument.class);
        return Optional.ofNullable(document).map(PutWallDocument::toDomain);
    }

    @Override
    public List<PutWall> findByLocation(String location) {
        Query query = new Query(Criteria.where("location").is(location));
        List<PutWallDocument> documents = mongoTemplate.find(query, PutWallDocument.class);
        return documents.stream()
            .map(PutWallDocument::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<PutWall> findAll() {
        List<PutWallDocument> documents = mongoTemplate.findAll(PutWallDocument.class);
        return documents.stream()
            .map(PutWallDocument::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void save(PutWall putWall) {
        PutWallDocument document = PutWallDocument.fromDomain(putWall);
        mongoTemplate.save(document);
    }

    @Override
    public void delete(PutWallId putWallId) {
        Query query = new Query(Criteria.where("_id").is(putWallId.getValue()));
        mongoTemplate.remove(query, PutWallDocument.class);
    }

    @Override
    public List<PutWall> findByAvailableCapacityGreaterThan(int minCapacity) {
        List<PutWallDocument> documents = mongoTemplate.findAll(PutWallDocument.class);
        return documents.stream()
            .map(PutWallDocument::toDomain)
            .filter(putWall -> putWall.getAvailableCapacity() > minCapacity)
            .collect(Collectors.toList());
    }

    @Override
    public boolean exists(PutWallId putWallId) {
        Query query = new Query(Criteria.where("_id").is(putWallId.getValue()));
        return mongoTemplate.exists(query, PutWallDocument.class);
    }
}