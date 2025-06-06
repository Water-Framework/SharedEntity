package it.water.shared.entity.service;

import it.water.core.api.registry.filter.ComponentFilterBuilder;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.repository.service.BaseEntitySystemServiceImpl;
import it.water.shared.entity.api.SharedEntityRepository;
import it.water.shared.entity.api.SharedEntitySystemApi;
import it.water.shared.entity.model.WaterSharedEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * @Generated by Water Generator
 * System Service Api Class for SharedEntity entity.
 */
@FrameworkComponent
public class SharedEntitySystemServiceImpl extends BaseEntitySystemServiceImpl<WaterSharedEntity> implements SharedEntitySystemApi {
    @Inject
    @Getter
    @Setter
    private SharedEntityRepository repository;

    @Inject
    @Setter
    private ComponentFilterBuilder componentFilterBuilder;

    public SharedEntitySystemServiceImpl() {
        super(WaterSharedEntity.class);
    }

    @Override
    public WaterSharedEntity findByPK(String entityResourceName, long entityId, long userId) {
        return repository.findByPK(entityResourceName, entityId, userId);
    }

    @Override
    public void removeByPK(String entityResourceName, long entityId, long userId) {
        repository.removeByPK(entityResourceName, entityId, userId);
    }

    @Override
    public List<WaterSharedEntity> findByEntity(String entityResourceName, long entityId) {
        return repository.findByEntity(entityResourceName, entityId);
    }

    @Override
    public List<WaterSharedEntity> findByUser(long userId) {
        return repository.findByUser(userId);
    }

    @Override
    public List<Long> getSharingUsers(String entityResourceName, long entityId) {
        return repository.getSharingUsers(entityResourceName, entityId);
    }

    @Override
    public List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId) {
        return repository.getEntityIdsSharedWithUser(entityResourceName, userId);
    }
}