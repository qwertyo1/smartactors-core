package info.smart_tools.smartactors.ioc.versioned_strategy_container;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.class_management.class_loader_management.VersionManager;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.istrategy_container.exception.StrategyContainerException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IStrategyContainer}
 * <p>
 * Simple key-value storage
 * <ul>
 *     <li>key is a unique object identifier</li>
 *     <li>value is a instance of {@link IResolveDependencyStrategy}</li>
 * </ul>
 * </p>
 * <p>
 * Stores the link to the parent container to make the recursive resolving
 * when the strategy doesn't exist in the current container.
 * </p>
 */
public class StrategyContainer implements IStrategyContainer {

    /**
     * Local storage
     */
    private Map<Object, Map<Object, IResolveDependencyStrategy>> strategyStorage = new ConcurrentHashMap<>();

    /**
     * Resolve {@link IResolveDependencyStrategy} by given unique object identifier.
     * @param key unique object identifier
     * @return instance of {@link IResolveDependencyStrategy}
     * @throws StrategyContainerException if any errors occurred
     */
    public IResolveDependencyStrategy resolve(final Object key)
            throws StrategyContainerException {
        IResolveDependencyStrategy strategy = null;
        Map<Object, IResolveDependencyStrategy> strategyVersions = strategyStorage.get(key);
        if (strategyVersions != null) {
            strategy = VersionManager.getFromMap(strategyVersions);
        }
        return strategy;
    }

    /**
     * Register new dependency of {@link IResolveDependencyStrategy} instance by unique object identifier
     * @param key unique object identifier
     * @param strategy instance of {@link IResolveDependencyStrategy}
     * @throws StrategyContainerException if any error occurred
     */
    public void register(final Object key, final IResolveDependencyStrategy strategy)
            throws StrategyContainerException {
        Map<Object, IResolveDependencyStrategy> strategyVersions = strategyStorage.get(key);
        if (strategyVersions == null) {
            strategyVersions = new ConcurrentHashMap<>();
            strategyStorage.put(key, strategyVersions);
        }
        strategyVersions.put(VersionManager.getCurrentModule(), strategy);
    }

    /**
     * Remove existing dependency of {@link IResolveDependencyStrategy} by unique object identifier.
     * @param key unique object identifier
     * @throws StrategyContainerException  if any error occurred
     */
    public void remove(final Object key)
            throws StrategyContainerException {
        Map<Object, IResolveDependencyStrategy> strategyVersions = strategyStorage.get(key);
        if (strategyVersions != null) {
            IResolveDependencyStrategy strategy = VersionManager.removeFromMap(strategyVersions);
            if (strategy != null) {
                if (strategyVersions.size() == 0) {
                    strategyStorage.remove(key);
                }
            }
        }
    }
}