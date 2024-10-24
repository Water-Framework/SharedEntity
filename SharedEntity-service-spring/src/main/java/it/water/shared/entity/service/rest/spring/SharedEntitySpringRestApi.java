/*
 * Copyright 2024 Aristide Cittadino
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.water.shared.entity.service.rest.spring;

import com.fasterxml.jackson.annotation.JsonView;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.service.rest.api.security.LoggedIn;
import it.water.shared.entity.api.rest.SharedEntityRestApi;
import it.water.shared.entity.model.WaterSharedEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Set;

/**
 * @Author Aristide Cittadino
 * Interface exposing same methods of its parent SharedEntityRestApi but adding Spring annotations.
 * Swagger annotation should be found because they have been defined in the parent SharedEntityRestApi.
 */
@RequestMapping("/entities/shared")
@FrameworkRestApi
public interface SharedEntitySpringRestApi extends SharedEntityRestApi {
    @LoggedIn
    @PostMapping
    @JsonView(WaterJsonView.Public.class)
    WaterSharedEntity save(@RequestBody WaterSharedEntity sharedentity);

    @LoggedIn
    @PutMapping
    @JsonView(WaterJsonView.Public.class)
    WaterSharedEntity update(@RequestBody WaterSharedEntity sharedentity);

    @LoggedIn
    @GetMapping("/{id}")
    @JsonView(WaterJsonView.Public.class)
    WaterSharedEntity find(@PathVariable("id") long id);

    @LoggedIn
    @GetMapping
    @JsonView(WaterJsonView.Public.class)
    PaginableResult<WaterSharedEntity> findAll();

    @LoggedIn
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @JsonView(WaterJsonView.Public.class)
    void remove(@PathVariable("id") long id);

    @LoggedIn
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @JsonView(WaterJsonView.Public.class)
    void removeSharedEntityByPK(@RequestBody WaterSharedEntity sharedEntity);

    @LoggedIn
    @GetMapping("/findByPK")
    @JsonView(WaterJsonView.Public.class)
    WaterSharedEntity findByPK(@RequestBody WaterSharedEntity sharedEntity);

    @LoggedIn
    @GetMapping("/findByEntity")
    @JsonView(WaterJsonView.Public.class)
    Collection<WaterSharedEntity> findByEntity(@RequestParam("entityResourceName") String entityResourceName, @RequestParam("entityId") long entityId);

    @LoggedIn
    @GetMapping("/findByUser/{userId}")
    @JsonView(WaterJsonView.Public.class)
    Collection<WaterSharedEntity> findByUser(@RequestParam("userId") long userId);

    @LoggedIn
    @GetMapping("/sharingUsers")
    @JsonView(WaterJsonView.Public.class)
    Set<Long> getUsers(@RequestParam("entityResourceName") String entityResourceName, @RequestParam("entityId") long entityId);
}
