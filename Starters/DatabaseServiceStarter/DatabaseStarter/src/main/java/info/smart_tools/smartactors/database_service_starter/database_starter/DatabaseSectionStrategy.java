package info.smart_tools.smartactors.database_service_starter.database_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.List;

/**
 * Strategy processing "database" configuration section.
 *
 * <pre>
 *     {
 *         "database": [
 *             {
 *                 "key": "PostgresConnectionOptions",        // Key of the dependency
 *                 "type": "PostgresConnectionOptionsStrategy"  // Type of the database
 *                 "config": {
 *                     "url": "",
 *                     "username": "",
 *                     "password": "",
 *                     "maxConnections": n
 *                 }                                    // Configuration for database
 *             }
 *         ]
 *     }
 * </pre>
 */
public class DatabaseSectionStrategy implements ISectionStrategy {
    private final IFieldName sectionFN;
    private final IFieldName typeFN;
    private final IFieldName configFN;
    private final IFieldName keyFN;

    /**
     * The constructor.
     *
     * @throws ResolutionException if cannot resolve any dependencies
     */
    public DatabaseSectionStrategy()
            throws ResolutionException {
        sectionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "database");
        keyFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "key");
        typeFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "type");
        configFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "config");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            List<IObject> databaseObjects = (List<IObject>) config.getValue(sectionFN);
            for (IObject databaseObj : databaseObjects) {
                Object databaseOpts = IOC.resolve(Keys.getOrAdd((String) databaseObj.getValue(typeFN)), databaseObj.getValue(configFN));
                IOC.register(Keys.getOrAdd((String) databaseObj.getValue(keyFN)), new SingletonStrategy(databaseOpts));
            }
        } catch (ReadValueException | InvalidArgumentException | RegistrationException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"database\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"database\".", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return sectionFN;
    }
}
