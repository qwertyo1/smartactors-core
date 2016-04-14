package info.smart_tools.smartactors.core.scope_container_implementation;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.scope_provider.IScopeContainer;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of {@link IScopeContainer}
 */
public class ScopeContainer implements IScopeContainer {

    /**
     * Local storage of all {@link IScope} instances by unique identifier
     */
    private Map<Object, IScope> scopeStorage = new HashMap<Object, IScope>();
    /**
     * Current instance of {@link IScope} for current thread
     */
    private ThreadLocal<IScope> currentScope = new ThreadLocal<IScope>();
    /**
     * Instance of {@link IScopeFactory}
     */
    private IScopeFactory factory;

    /**
     * Default constructor
     */
    private ScopeContainer() {
    }

    /**
     * Constructor with {@link IScopeFactory}
     * @param factory instance of {@link IScopeFactory}
     */
    public ScopeContainer(final IScopeFactory factory) {
        this.factory = factory;
    }

    /**
     * Get {@link IScope} instance from local storage.
     * @param key unique identifier for instance of an object
     * @return instance of {@link IScope}
     * @throws ScopeProviderException if value is not found or any errors occurred
     */
    public IScope getScope(final Object key) throws ScopeProviderException {
        IScope scope = scopeStorage.get(key);
        if (scope == null) {
            throw new ScopeProviderException("Scope not found.");
        }

        return scope;
    }

    /**
     * Get current instance of {@link IScope}
     * @return instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public IScope getCurrentScope()
            throws ScopeProviderException {
        IScope scope = currentScope.get();
        if (scope == null) {
            throw new ScopeProviderException("Current Scope is null.");
        }

        return scope;
    }

    /**
     * Put {@link IScope} instance to the local storage
     * @param key unique identifier for instance of an object
     * @param scope instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public void setScope(final Object key, final IScope scope)
            throws ScopeProviderException {
        try {
            scopeStorage.put(key, scope);
        } catch (Exception e) {
            throw new ScopeProviderException("Error was occurred", e);
        }
    }

    /**
     * Set instance of {@link IScope} as current scope
     * @param scope instance of {@link IScope}
     * @throws ScopeProviderException if any errors occurred
     */
    public void setCurrentScope(final IScope scope)
            throws ScopeProviderException {
        try {
            currentScope.set(scope);
        } catch (Exception e) {
            throw new ScopeProviderException("Error was occurred", e);
        }
    }

    /**
     * Delete {@link IScope} instance from local storage by given key
     * @param key unique identifier for instance of an object
     * @throws ScopeProviderException if any errors occurred
     */
    public void deleteScope(final Object key)
            throws ScopeProviderException {
        try {
            scopeStorage.remove(key);
        } catch (Exception e) {
            throw new ScopeProviderException("Error was occurred", e);
        }
    }

    /**
     * Create new instance of {@link IScope} and put it to the local storage
     * @param params needed parameters for creation
     * @return uniqte instance of {@link IScope} identifier
     * @throws ScopeProviderException if any errors occurred
     */
    public Object createScope(final Object params) throws ScopeProviderException {
        try {
            IScope newScope = factory.createScope(params);
            Object uuid = UUID.randomUUID();
            scopeStorage.put(uuid, newScope);

            return uuid;
        } catch (Exception e) {
            throw new ScopeProviderException("Failed to create instance of IScope.", e);
        }
    }
}
