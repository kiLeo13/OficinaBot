package ofc.bot.handlers.cache;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.entity.enums.PolicyType;
import ofc.bot.domain.sqlite.repository.EntityPolicyRepository;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is used as cache for database operations
 * for the {@link ofc.bot.domain.tables.EntitiesPoliciesTable EntitiesPoliciesTable}.
 */
public final class PolicyService {
    private static final PolicyService INSTANCE = new PolicyService();
    private static EntityPolicyRepository policyRepo;
    private final Map<PolicyType, Set<Long>> rules = new HashMap<>();

    private PolicyService() {}

    public static PolicyService getService() {
        return INSTANCE;
    }

    public void invalidate() {
        this.rules.clear();
    }

    @NotNull
    public synchronized Set<Long> get(@NotNull PolicyType type) {
        Checks.notNull(type, "PolicyType");
        // We can distinguish between a missing value and an outdated cache entry
        // because we always update the `rules` map with an empty set when a database lookup returns no results.
        // This ensures that once we attempt to retrieve a value from the database and find nothing,
        // we don't repeatedly query the database for the same missing value on subsequent method calls.
        return this.rules.computeIfAbsent(type, this::fetch);
    }

    private Set<Long> fetch(PolicyType type) {
        if (policyRepo == null)
            throw new IllegalStateException("No policy repository provided so far :(");

        return policyRepo.findSetByType(type);
    }

    public static void setPolicyRepo(EntityPolicyRepository policyRepo) {
        PolicyService.policyRepo = policyRepo;
    }
}