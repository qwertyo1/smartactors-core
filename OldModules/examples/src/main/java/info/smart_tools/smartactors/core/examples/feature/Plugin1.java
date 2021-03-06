package info.smart_tools.smartactors.core.examples.feature;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;

/**
 *  Example of plugin.
 */
public class Plugin1 implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public Plugin1(final IBootstrap<IBootstrapItem<String>> bootstrap) {   // this constructor signature is required
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("Plugin1");
            item.process(() -> System.out.println("Plugin1 initialized"));
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Could not load Plugin1", e);
        }
        System.out.println("Plugin1 loaded");
    }

}
