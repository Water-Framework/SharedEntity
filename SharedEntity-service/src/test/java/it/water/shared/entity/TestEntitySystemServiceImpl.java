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

package it.water.shared.entity;

import it.water.core.api.model.PaginableResult;
import it.water.core.api.repository.query.Query;
import it.water.core.api.repository.query.QueryBuilder;
import it.water.core.api.repository.query.QueryOrder;
import it.water.core.interceptors.annotations.FrameworkComponent;

import java.util.HashMap;
import java.util.Map;

@FrameworkComponent
public class TestEntitySystemServiceImpl implements TestEntitySystemApi {

    private Map<Long, TestEntityResource> localDb = new HashMap<>();

    @Override
    public TestEntityResource save(TestEntityResource testEntityResource) {
        return localDb.put(testEntityResource.getId(), testEntityResource);
    }

    @Override
    public TestEntityResource update(TestEntityResource testEntityResource) {
        return localDb.put(testEntityResource.getId(), testEntityResource);
    }

    @Override
    public void remove(long id) {
        localDb.remove(id);
    }

    @Override
    public TestEntityResource find(long id) {
        return localDb.get(id);
    }

    @Override
    public TestEntityResource find(Query query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaginableResult<TestEntityResource> findAll(Query query, int i, int i1, QueryOrder queryOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long countAll(Query query) {
        return localDb.size();
    }

    @Override
    public Class<TestEntityResource> getEntityType() {
        return TestEntityResource.class;
    }

    @Override
    public QueryBuilder getQueryBuilderInstance() {
        throw new UnsupportedOperationException();
    }
}
