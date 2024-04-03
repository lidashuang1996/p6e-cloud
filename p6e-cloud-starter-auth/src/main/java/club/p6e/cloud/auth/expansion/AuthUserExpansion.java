package club.p6e.cloud.auth.expansion;

import club.p6e.coat.auth.AuthUser;
import club.p6e.coat.auth.model.UserAuthModel;
import club.p6e.coat.auth.model.UserModel;
import club.p6e.coat.common.utils.JsonUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.*;

/**
 * @author lidashuang
 * @version 1.0
 */
@Primary
@Component
@ConditionalOnProperty(name = "p6e.cloud.auth.permission.enabled", havingValue = "true")
public class AuthUserExpansion implements AuthUser<AuthUserExpansion.Model> {

    private final PermissionUrlGroupRepositoryExpansion permissionUrlGroupRepositoryExpansion;
    private final PermissionUrlGroupRelationUrlRepositoryExpansion permissionUrlGroupRelationUrlRepositoryExpansion;
    private final PermissionUrlGroupRelationUserRepositoryExpansion permissionUrlGroupRelationUserRepositoryExpansion;

    public AuthUserExpansion(PermissionUrlGroupRepositoryExpansion permissionUrlGroupRepositoryExpansion,
                             PermissionUrlGroupRelationUrlRepositoryExpansion permissionUrlGroupRelationUrlRepositoryExpansion,
                             PermissionUrlGroupRelationUserRepositoryExpansion permissionUrlGroupRelationUserRepositoryExpansion) {
        this.permissionUrlGroupRepositoryExpansion = permissionUrlGroupRepositoryExpansion;
        this.permissionUrlGroupRelationUrlRepositoryExpansion = permissionUrlGroupRelationUrlRepositoryExpansion;
        this.permissionUrlGroupRelationUserRepositoryExpansion = permissionUrlGroupRelationUserRepositoryExpansion;
    }

    @Override
    public Mono<Model> create(String content) {
        final Model model = JsonUtil.fromJson(content, Model.class);
        if (model == null) {
            throw new RuntimeException("[ " + this.getClass() + " ] create ==> deserialization failure !!");
        } else {
            return Mono.just(model);
        }
    }

    @Override
    public Mono<Model> create(UserModel um, UserAuthModel uam) {
        final Model model = new Model()
                .setId(um.getId())
                .setStatus(um.getStatus())
                .setEnabled(um.getEnabled())
                .setInternal(um.getInternal())
                .setAdministrator(um.getAdministrator())
                .setAccount(um.getAccount())
                .setPhone(um.getPhone())
                .setMailbox(um.getMailbox())
                .setName(um.getName())
                .setNickname(um.getNickname())
                .setAvatar(um.getAvatar())
                .setDescription(um.getDescription());
        if (uam != null) {
            model.setPassword(uam.getPassword());
        }

        return permissionUrlGroupRelationUserRepositoryExpansion
                .findByUidList(um.getId())
                .flatMap(m -> permissionUrlGroupRepositoryExpansion
                        .findById(m.getGid())
                        .flatMap(gm -> permissionUrlGroupRepositoryExpansion
                                .findByPid(gm.getId())
                                .collectList()
                                .map(l -> {
                                    l.add(gm);
                                    return l;
                                })))
                .flatMap(list -> {
                    final List<PermissionModelExpansion> result = new ArrayList<>();
                    for (PermissionUrlGroupModelExpansion item : list) {
                        result.add(new PermissionModelExpansion()
                                .setGid(item.getId())
                                .setMark(item.getMark())
                                .setName(item.getName())
                                .setWeight(item.getWeight())
                                .setParent(item.getParent())
                        );
                    }
                    return Flux.fromStream(result.stream());
                })
                .flatMap(m -> permissionUrlGroupRelationUrlRepositoryExpansion
                        .findByGidList(m.getGid())
                        .map(rm -> m.setUid(rm.getUid()).setAttribute(rm.getAttribute()).setConfig(rm.getConfig())))
                .collectList()
                .flatMap(list -> {
                    if (list != null && !list.isEmpty()) {
                        list.forEach(item -> {
                            model.getPermissionMark().add(item.getMark());
                            model.getPermissionGroup().add(item.getGid() + ":" + item.getUid()
                                    + ":" + (item.getAttribute() == null ? "" : item.getAttribute()));
                        });
                    }
                    return Mono.just(model);
                });
    }

    @Data
    @Accessors(chain = true)
    public static class Model implements AuthUser.Model, Serializable {
        private Integer id;
        private Integer status;
        private Integer enabled;
        private Integer internal;
        private Integer administrator;
        private String account;
        private String phone;
        private String mailbox;
        private String name;
        private String nickname;
        private String avatar;
        private String description;
        private String password;
        private Set<String> permissionMark = new HashSet<>();
        private List<String> permissionGroup = new ArrayList<>();

        @Override
        public String id() {
            return String.valueOf(id);
        }

        @Override
        public String password() {
            return password;
        }

        @Override
        public String serialize() {
            return JsonUtil.toJson(toMap());
        }

        @Override
        public Map<String, Object> toMap() {
            return new HashMap<>() {{
                put("id", id);
                put("status", status);
                put("enabled", enabled);
                put("internal", internal);
                put("administrator", administrator);
                put("account", account);
                put("phone", phone);
                put("mailbox", mailbox);
                put("name", name);
                put("nickname", nickname);
                put("avatar", avatar);
                put("description", description);
                put("permissionMark", permissionMark);
                put("permissionGroup", permissionGroup);
            }};
        }
    }
}
