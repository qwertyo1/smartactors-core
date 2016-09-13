package info.smart_tools.smartactors.plugin.nested_field;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.nested_field.NestedField;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;

/**
 * Plugin for {@link info.smart_tools.smartactors.plugin.nested_field.NestedFieldPlugin}
 */
public class NestedFieldPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public NestedFieldPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            BootstrapItem item = new BootstrapItem("NestedFieldPlugin");
            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IKey fieldKey = Keys.getOrAdd(NestedField.class.getCanonicalName());
                            IOC.register(fieldKey, new ResolveByNameIocStrategy(
                                    (args) -> {
                                        try {
                                            String fieldName = String.valueOf(args[0]);
                                            return new NestedField(fieldName);
                                        } catch (InvalidArgumentException e) {
                                            throw new RuntimeException("Can't resolve NestedField: ", e);
                                        }
                                    }));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("NestedField plugin can't load: can't get NestedField key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("NestedField plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("NestedField plugin can't load: can't register new strategy", e);
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load NestedField plugin", e);
        }
    }
}
