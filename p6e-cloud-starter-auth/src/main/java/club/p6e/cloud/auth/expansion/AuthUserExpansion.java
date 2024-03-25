package club.p6e.cloud.auth.expansion;

import club.p6e.coat.auth.AuthUser;
import club.p6e.coat.auth.model.UserAuthModel;
import club.p6e.coat.auth.model.UserModel;
import club.p6e.coat.common.utils.JsonUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnProperty(name = "p6e.cloud.auth.permission.enabled", havingValue = "true")
public class AuthUserExpansion implements AuthUser<AuthUserExpansion.Model> {

    private final PermissionUrlGroupRepositoryExpansion permissionUrlGroupRepositoryExpansion;
    private final PermissionUrlGroupRelationUrlRepositoryExpansion permissionUrlGroupRelationUrlRepositoryExpansion;

    public AuthUserExpansion(PermissionUrlGroupRepositoryExpansion permissionUrlGroupRepositoryExpansion,
                             PermissionUrlGroupRelationUrlRepositoryExpansion permissionUrlGroupRelationUrlRepositoryExpansion) {
        this.permissionUrlGroupRepositoryExpansion = permissionUrlGroupRepositoryExpansion;
        this.permissionUrlGroupRelationUrlRepositoryExpansion = permissionUrlGroupRelationUrlRepositoryExpansion;
    }

    @Override
    public Model create(String content) {
        final Model model = JsonUtil.fromJson(content, Model.class);
        if (model == null) {
            throw new RuntimeException("[ " + this.getClass() + " ] create ==> deserialization failure !!");
        } else {
            return model;
        }
    }

    @Override
    public Model create(UserModel um, UserAuthModel uam) {
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
        return permissionUrlGroupRelationUrlRepositoryExpansion
                .findByUidList(um.getId())
                .flatMap(r -> permissionUrlGroupRepositoryExpansion
                        .findById(r.getGid())
                        .flatMap(g -> {
                            if (g.getParent() != null && g.getParent() != 0) {
                                return permissionUrlGroupRepositoryExpansion
                                        .findByPid(g.getParent())
                                        .collectList()
                                        .map(l -> {
                                            l.add(g);
                                            return l;
                                        });
                            } else {
                                return Mono.just(new ArrayList<>(List.of(g)));
                            }
                        }))
                .collectList()
                .map(l -> {
                    final List<PermissionUrlGroupModelExpansion> expansions = new ArrayList<>();
                    l.forEach(expansions::addAll);
                    return expansions;
                })
                .map(l -> model
                        .setPermissionGroup(l.stream().map(i -> String.valueOf(i.getId())).toList())
                        .setPermissionMark(l.stream().map(PermissionUrlGroupModelExpansion::getMark).toList())
                ).block();
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
        private List<String> permissionMark = new ArrayList<>();
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
            return JsonUtil.toJson(new Model()
                    .setId(this.getId())
                    .setStatus(this.getStatus())
                    .setEnabled(this.getEnabled())
                    .setInternal(this.getInternal())
                    .setAdministrator(this.getAdministrator())
                    .setAccount(this.getAccount())
                    .setPhone(this.getPhone())
                    .setMailbox(this.getMailbox())
                    .setName(this.getName())
                    .setNickname(this.getNickname())
                    .setAvatar(this.getAvatar())
                    .setDescription(this.getDescription())
                    .setPermissionMark(this.getPermissionMark())
                    .setPermissionGroup(this.getPermissionGroup())
            );
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
