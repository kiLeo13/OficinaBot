package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.EntityPolicy;
import ofc.bot.domain.entity.enums.PolicyType;
import ofc.bot.domain.tables.EntitiesPoliciesTable;
import org.jooq.DSLContext;

import java.util.*;
import java.util.function.Function;

/**
 * Repository for {@link EntityPolicy} entity.
 */
public class EntityPolicyRepository {
    private static final EntitiesPoliciesTable ENTITIES_POLICIES = EntitiesPoliciesTable.ENTITIES_POLICIES;
    private final DSLContext ctx;

    public EntityPolicyRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void save(EntityPolicy excl) {
        ctx.insertInto(ENTITIES_POLICIES)
                .set(excl.intoMap())
                .onConflictDoNothing()
                .execute();
    }

    public void deleteByPolicyAndResource(PolicyType type, Object resource) {
        ctx.deleteFrom(ENTITIES_POLICIES)
                .where(ENTITIES_POLICIES.POLICY_TYPE.eq(type.name()))
                .and(ENTITIES_POLICIES.RESOURCE.eq(String.valueOf(resource)))
                .execute();
    }

    public boolean existsByTypeAndResource(PolicyType type, Object resource) {
        return ctx.fetchExists(ENTITIES_POLICIES,
                ENTITIES_POLICIES.POLICY_TYPE.eq(type.name())
                        .and(ENTITIES_POLICIES.RESOURCE.eq(String.valueOf(resource)))
        );
    }

    public Map<PolicyType, Set<Long>> mapSetByType(List<PolicyType> types) {
        if (types == null || types.isEmpty())
            return Map.of();

        Map<PolicyType, Set<Long>> map = new HashMap<>();
        List<EntityPolicy> excls = findByTypes(types);

        for (EntityPolicy excl : excls) {
            PolicyType type = excl.getPolicyType();

            Set<Long> ids = map.getOrDefault(type, new HashSet<>());
            // We can safely convert to long here, as the resources of these types
            // will always be a snowflake.
            ids.add(excl.getResource(Long::parseLong));
            map.put(type, ids);
        }
        return Collections.unmodifiableMap(map);
    }

    public List<EntityPolicy> findByTypes(List<PolicyType> types) {
        List<String> strTypes = types.stream().map(PolicyType::toString).toList();
        return ctx.selectFrom(ENTITIES_POLICIES)
                .where(ENTITIES_POLICIES.POLICY_TYPE.in(strTypes))
                .fetch();
    }

    public <T> List<T> findEntitiesIdsByType(PolicyType type, Function<String, T> resolver) {
        return ctx.select(ENTITIES_POLICIES.RESOURCE)
                .from(ENTITIES_POLICIES)
                .where(ENTITIES_POLICIES.POLICY_TYPE.eq(type.toString()))
                .fetchStreamInto(String.class)
                .map(resolver)
                .toList();
    }
}