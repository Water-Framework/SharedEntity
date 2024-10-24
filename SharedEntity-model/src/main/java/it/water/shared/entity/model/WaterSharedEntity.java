package it.water.shared.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import it.water.core.api.entity.shared.SharedEntity;
import it.water.core.api.model.User;
import it.water.core.api.permission.ProtectedEntity;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.core.permission.action.CrudActions;
import it.water.core.permission.annotations.AccessControl;
import it.water.core.permission.annotations.DefaultRoleAccess;
import it.water.core.validation.annotations.NoMalitiusCode;
import it.water.core.validation.annotations.NotNullOnPersist;
import it.water.repository.jpa.model.AbstractJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;


/**
 * @Generated by Water Generator
 * SharedEntity Entity Class.
 */
//JPA
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"entityResourceName", "entityId", "userId"})})
@Access(AccessType.FIELD)
@IdClass(SharedEntityPK.class)
//Lombok
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(of = {"entityResourceName", "entityId", "userId"})
//Actions and default roles access
@AccessControl(availableActions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE},
        rolesPermissions = {
                //Admin role can do everything
                @DefaultRoleAccess(roleName = WaterSharedEntity.DEFAULT_MANAGER_ROLE, actions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL, CrudActions.REMOVE}),
                //Viwer has read only access
                @DefaultRoleAccess(roleName = WaterSharedEntity.DEFAULT_VIEWER_ROLE, actions = {CrudActions.FIND, CrudActions.FIND_ALL}),
                //Editor can do anything but remove
                @DefaultRoleAccess(roleName = WaterSharedEntity.DEFAULT_EDITOR_ROLE, actions = {CrudActions.SAVE, CrudActions.UPDATE, CrudActions.FIND, CrudActions.FIND_ALL})
        })
public class WaterSharedEntity extends AbstractJpaEntity implements ProtectedEntity {

    public static final String DEFAULT_MANAGER_ROLE = "sharedentityManager";
    public static final String DEFAULT_VIEWER_ROLE = "sharedentityViewer";
    public static final String DEFAULT_EDITOR_ROLE = "sharedentityEditor";

    @Id
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @NonNull
    @JsonView(WaterJsonView.Public.class)
    private String entityResourceName;
    @Id
    @NotNullOnPersist
    @NonNull
    @JsonView(WaterJsonView.Public.class)
    private long entityId;
    @Id
    @NotNullOnPersist
    @NonNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonView(WaterJsonView.Public.class)
    @Setter(AccessLevel.PUBLIC)
    private long userId;
    //Entities can be shared using users emails instead of id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private String userEmail;
    //Entities can be shared using users usernames instead of id
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private String username;

    //removing Id column since it is not required as shared entity as different primary key
    @Override
    @Transient
    public long getId() {
        return super.getId();
    }
}