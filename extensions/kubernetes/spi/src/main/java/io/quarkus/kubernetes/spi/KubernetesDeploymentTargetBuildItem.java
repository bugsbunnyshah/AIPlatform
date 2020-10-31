
package io.quarkus.kubernetes.spi;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * Used to control which Kubernetes targets have their files generated and which get deployed
 * Since these can be generated by various locations, code that needs the "final"
 * set of items, should use the {@code mergeList} method to get a new list
 * with merged items.
 * Furthermore, if priorities need to be taken into account, the merged list should
 * also be sorted
 */
public final class KubernetesDeploymentTargetBuildItem extends MultiBuildItem
        implements Comparable<KubernetesDeploymentTargetBuildItem> {

    // vanilla kubernetes always has the lowest priority
    public static final int VANILLA_KUBERNETES_PRIORITY = Integer.MIN_VALUE;

    // the default priority higher than Kubernetes
    public static final int DEFAULT_PRIORITY = 0;

    private final String name;
    private final String kind;
    private final int priority;
    private final boolean enabled;

    public KubernetesDeploymentTargetBuildItem(String name, String kind) {
        this(name, kind, DEFAULT_PRIORITY, false);
    }

    public KubernetesDeploymentTargetBuildItem(String name, String kind, boolean enabled) {
        this(name, kind, DEFAULT_PRIORITY, enabled);
    }

    public KubernetesDeploymentTargetBuildItem(String name, String kind, int priority, boolean enabled) {
        this.name = Objects.requireNonNull(name, "'name' must not be null");
        this.kind = Objects.requireNonNull(kind, "'kind' must not be null");
        this.priority = priority;
        this.enabled = enabled;
    }

    public String getName() {
        return this.name;
    }

    public String getKind() {
        return this.kind;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean nameAndKindMatch(KubernetesDeploymentTargetBuildItem other) {
        return this.name.equals(other.getName()) && this.kind.equals(other.getKind());
    }

    public boolean nameAndKindMatchButNotEquals(KubernetesDeploymentTargetBuildItem other) {
        return !this.equals(other) && nameAndKindMatch(other);
    }

    public Map.Entry<String, String> nameToKindEntry() {
        return new AbstractMap.SimpleEntry<>(this.name, this.kind);
    }

    /**
     * Create a new {@code KubernetesDeploymentTargetBuildItem} that contains the maximum of the two priorities
     * and the logical 'OR' of the {@code enabled} field.
     * {@code name} and {@code kind} must match, otherwise an exception is thrown
     */
    private KubernetesDeploymentTargetBuildItem merge(KubernetesDeploymentTargetBuildItem other) {
        if (!nameAndKindMatch(other)) {
            throw new IllegalArgumentException(
                    "Merging of KubernetesDeploymentTargetBuildItem having different 'name' or 'kind' fields is not allowed.");
        }

        return new KubernetesDeploymentTargetBuildItem(this.name, this.kind, Math.max(this.priority, other.getPriority()),
                this.enabled || other.isEnabled());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KubernetesDeploymentTargetBuildItem that = (KubernetesDeploymentTargetBuildItem) o;
        return priority == that.priority &&
                enabled == that.enabled &&
                name.equals(that.name) &&
                kind.equals(that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind, priority, enabled);
    }

    /**
     * Return a new list containing from the input where entries that match {@code name} and {@code kind}
     * as merged together based on {@code merge}
     */
    public static List<KubernetesDeploymentTargetBuildItem> mergeList(List<KubernetesDeploymentTargetBuildItem> input) {
        List<KubernetesDeploymentTargetBuildItem> result = new ArrayList<>(input.size());
        Set<Map.Entry<String, String>> alreadyProcessedNameKindPairs = new HashSet<>();
        for (KubernetesDeploymentTargetBuildItem candidate : input) {
            Map.Entry<String, String> nameToKindEntry = candidate.nameToKindEntry();
            if (alreadyProcessedNameKindPairs.contains(nameToKindEntry)) {
                continue;
            }
            List<KubernetesDeploymentTargetBuildItem> nameAndKindMatchButNotEqual = input.stream()
                    .filter(i -> i.nameAndKindMatchButNotEquals(candidate)).collect(Collectors.toList());
            KubernetesDeploymentTargetBuildItem merged = candidate;
            for (KubernetesDeploymentTargetBuildItem toBeMerged : nameAndKindMatchButNotEqual) {
                merged = candidate.merge(toBeMerged);
            }
            result.add(merged);
            alreadyProcessedNameKindPairs.add(nameToKindEntry);
        }
        return result;
    }

    @Override
    public int compareTo(KubernetesDeploymentTargetBuildItem o) {
        return Integer.compare(o.priority, this.priority);
    }
}