package info.smart_tools.smartactors.core.in_memory_get_by_id_task;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.IConditionVerifier;
import info.smart_tools.smartactors.core.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.core.in_memory_db_get_by_id_task.InMemoryGetByIdTask;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InMemoryGetByIdTaskTest {

    InMemoryDatabase inMemoryDatabase;

    @Before
    public void setUp() throws ScopeProviderException, ResolutionException, RegistrationException, InvalidArgumentException {
        inMemoryDatabase = mock(InMemoryDatabase.class);
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        Map<String, IConditionVerifier> verifierMap = new HashMap<>();
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );
        IOC.register(Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );

        IOC.register(Keys.getOrAdd(InMemoryDatabase.class.getCanonicalName()), new SingletonStrategy(
                        inMemoryDatabase
                )
        );
    }


    @Test
    public void testPrepare() throws InvalidArgumentException, ChangeValueException, TaskPrepareException {
        InMemoryGetByIdTask getByIdTask = new InMemoryGetByIdTask();
        IObject query = new DSObject("{\"collectionName\": \"collection_name\", \"id\": 3}");
        IAction<IObject> iObjectIAction = mock(IAction.class);
        query.setValue(new FieldName("callback"), iObjectIAction);
        getByIdTask.prepare(query);
    }

    @Test
    public void testExecution() throws InvalidArgumentException, ChangeValueException, TaskPrepareException, IDatabaseException, ActionExecuteException, TaskExecutionException {
        InMemoryGetByIdTask getByIdTask = new InMemoryGetByIdTask();
        IObject query = new DSObject("{\"collectionName\": \"collection_name\", \"id\": 3}");
        IAction<IObject> iObjectIAction = mock(IAction.class);
        query.setValue(new FieldName("callback"), iObjectIAction);
        getByIdTask.prepare(query);
        IObject document = new DSObject("{}");
        when(inMemoryDatabase.getById(3, "collection_name")).thenReturn(document);
        getByIdTask.execute();
        verify(iObjectIAction).execute(document);
    }
}