package it.water.shared.entity.service.integration;

import it.water.core.api.service.integration.SharedEntityIntegrationClient;
import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.shared.entity.api.SharedEntityApi;
import lombok.Setter;

import java.util.Collection;

@FrameworkComponent(priority = 1, services = SharedEntityIntegrationClient.class)
public class SharedEntityLocalIntegrationClient implements SharedEntityIntegrationClient {
    @Inject
    @Setter
    private SharedEntityApi sharedEntityApi;

    @Override
    public Collection<Long> fetchSharingUsersIds(String entityClass, long userId) {
        return sharedEntityApi.getEntityIdsSharedWithUser(entityClass, userId);
    }
}
