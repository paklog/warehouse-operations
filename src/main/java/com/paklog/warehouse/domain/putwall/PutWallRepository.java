package com.paklog.warehouse.domain.putwall;

import java.util.List;
import java.util.Optional;

public interface PutWallRepository {

    Optional<PutWall> findById(PutWallId putWallId);

    List<PutWall> findByLocation(String location);

    List<PutWall> findAll();

    void save(PutWall putWall);

    void delete(PutWallId putWallId);

    List<PutWall> findByAvailableCapacityGreaterThan(int minCapacity);

    boolean exists(PutWallId putWallId);
}