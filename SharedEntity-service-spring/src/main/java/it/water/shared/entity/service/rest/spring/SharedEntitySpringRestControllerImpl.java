
package it.water.shared.entity.service.rest.spring;

import it.water.core.api.model.PaginableResult;
import it.water.core.api.repository.query.Query;
import it.water.core.api.repository.query.QueryOrder;
import it.water.shared.entity.model.WaterSharedEntity;
import it.water.shared.entity.service.rest.SharedEntityRestControllerImpl;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;


/**
 * @Generated by Water Generator
 * Rest Api Class for SharedEntity entity. It just overrides method invoking super in order to let spring find web methods.
 */
@RestController
public class SharedEntitySpringRestControllerImpl extends SharedEntityRestControllerImpl implements SharedEntitySpringRestApi {

    @Override
    public WaterSharedEntity save(WaterSharedEntity entity) {
        return super.save(entity);
    }

    @Override
    public WaterSharedEntity update(WaterSharedEntity entity) {
        return super.update(entity);
    }

    @Override
    public void remove(long id) {
        super.remove(id);
    }

    @Override
    public WaterSharedEntity find(long id) {
        return super.find(id);
    }

    @Override
    public PaginableResult<WaterSharedEntity> findAll(Integer delta, Integer page, Query filter, QueryOrder order) {
        return super.findAll(delta, page, filter, order);
    }

    @Override
    public PaginableResult<WaterSharedEntity> findAll() {
        return super.findAll();
    }

    @Override
    public void removeSharedEntityByPK(WaterSharedEntity sharedEntity) {
        super.removeSharedEntityByPK(sharedEntity);
    }

    @Override
    public WaterSharedEntity findByPK(WaterSharedEntity sharedEntity) {
        return super.findByPK(sharedEntity);
    }

    @Override
    public Collection<WaterSharedEntity> findByEntity(String entityResourceName, long entityId) {
        return super.findByEntity(entityResourceName, entityId);
    }

    @Override
    public Collection<WaterSharedEntity> findByUser(long userId) {
        return super.findByUser(userId);
    }

    @Override
    public Set<Long> getUsers(String entityResourceName, long entityId) {
        return super.getUsers(entityResourceName, entityId);
    }
}
