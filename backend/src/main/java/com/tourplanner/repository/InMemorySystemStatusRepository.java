package com.tourplanner.repository;

import com.tourplanner.model.entity.SystemStatusEntity;
import org.springframework.stereotype.Repository;

@Repository
public class InMemorySystemStatusRepository implements SystemStatusRepository {

    @Override
    public SystemStatusEntity getStatus() {
        return new SystemStatusEntity("Layer architecture is active.");
    }
}