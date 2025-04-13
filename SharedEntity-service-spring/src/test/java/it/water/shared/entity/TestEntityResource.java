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

import it.water.core.api.entity.shared.SharedEntity;
import it.water.core.api.model.BaseEntity;
import it.water.core.api.model.User;
import it.water.core.permission.action.CrudActions;
import it.water.core.permission.action.ShareAction;
import it.water.core.permission.annotations.AccessControl;
import it.water.core.permission.annotations.DefaultRoleAccess;
import it.water.shared.entity.model.WaterSharedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Setter;

import java.util.Date;


@Entity
@AccessControl(availableActions = {ShareAction.SHARE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE}, rolesPermissions = {
        //Admin role can do everything
        @DefaultRoleAccess(roleName = WaterSharedEntity.DEFAULT_MANAGER_ROLE, actions = {ShareAction.SHARE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE}),
        //Viwer has read only access
        @DefaultRoleAccess(roleName = WaterSharedEntity.DEFAULT_VIEWER_ROLE, actions = {ShareAction.SHARE, CrudActions.FIND, CrudActions.FIND_ALL}),
        //Editor can do anything but remove
        @DefaultRoleAccess(roleName = WaterSharedEntity.DEFAULT_EDITOR_ROLE, actions = {ShareAction.SHARE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL})
})
public class TestEntityResource implements BaseEntity, SharedEntity {

    private static long localCounter = 1;
    @Setter
    private long id;

    private Long ownerUserId;

    public TestEntityResource() {
        id = localCounter++;
    }

    @Override
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    @Override
    public void setOwnerUserId(Long aLong) {
        this.ownerUserId = aLong;
    }

    @Override
    @GeneratedValue
    @Id
    public long getId() {
        return id;
    }

    @Override
    @Transient
    public Date getEntityCreateDate() {
        return new Date();
    }

    @Override
    @Transient
    public Date getEntityModifyDate() {
        return new Date();
    }

    @Override
    public Integer getEntityVersion() {
        return 1;
    }

    @Override
    public void setEntityVersion(Integer integer) {
        //do nothing
    }
}
