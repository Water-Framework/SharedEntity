package it.water.shared.entity.api;

import it.water.core.api.service.BaseEntitySystemApi;
import it.water.shared.entity.model.WaterSharedEntity;

import java.util.List;

/**
 * @Generated by Water Generator
 * This interface defines the internally exposed methods for the entity and allows interaction with it bypassing permission system.
 * The main goals of SharedEntitySystemApi is to validate the entity and pass it to the persistence layer.
 */
public interface SharedEntitySystemApi extends BaseEntitySystemApi<WaterSharedEntity> {
    WaterSharedEntity findByPK(String entityResourceName, long entityId, long userId);

    void removeByPK(String entityResourceName, long entityId, long userId);

    List<WaterSharedEntity> findByEntity(String entityResourceName, long entityId);

    List<WaterSharedEntity> findByUser(long userId);

    List<Long> getSharingUsers(String entityResourceName, long entityId);

    List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId);
}