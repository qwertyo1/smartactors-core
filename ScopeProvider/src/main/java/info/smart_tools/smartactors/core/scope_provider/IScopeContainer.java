package info.smart_tools.smartactors.core.scope_provider;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;

/**
 * ScopeContainer interface
 * Provides IScope instance by unique identifier from internal storage
 */
public interface IScopeContainer {
    /**
     * Get {@link IScope} instance by unique identifier
     * @param key unique identifier for instance of {@link IScope}
     * @throws ScopeProviderException if value is not found or any errors occurred
     * @return instance of IScope
     */
    IScope getScope(final Object key)
            throws ScopeProviderException;

    /**
     * Get current instance of {@link IScope}
     * @return instance of {@link IScope}
     * @throws ScopeProviderException if value is null or any errors occurred
     */
    IScope getCurrentScope()
            throws ScopeProviderException;

    /**
     * Add new dependency of {@link IScope} instance by unique identifier
     * @param key unique identifier
     * @param value instance of {@link IScope}
     * @throws ScopeProviderException if any error occurred
     */
    void addScope(final Object key, final IScope value)
            throws ScopeProviderException;

    /**
     * Set instance of {@link IScope} to the current scope
     * @param currentScope instance of {@link IScope}
     * @throws ScopeProviderException if any error occurred
     */
    void setCurrentScope(IScope currentScope)
            throws ScopeProviderException;

    /**
     * Removes the value associated with key
     * @param key given key
     * @throws ScopeProviderException if value is absent or any errors occurred
     */
    void deleteScope(final Object key)
            throws ScopeProviderException;

    /**
     * Create new instance of {@link IScope}
     * @param params needed parameters for creation
     * @return unique instance of {@link IScope} identifier
     * @throws ScopeProviderException if any errors occurred
     */
    Object createScope(final Object params)
            throws ScopeProviderException;
}
