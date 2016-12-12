package info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

import java.util.Collection;
import java.util.Set;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface AddFeatureWrapper {

    Collection<IFeature> getFeatures()
            throws ReadValueException;

    IMessageProcessor getMessageProcessor()
            throws ReadValueException;

    String getScatterChainName()
            throws ReadValueException;
}
