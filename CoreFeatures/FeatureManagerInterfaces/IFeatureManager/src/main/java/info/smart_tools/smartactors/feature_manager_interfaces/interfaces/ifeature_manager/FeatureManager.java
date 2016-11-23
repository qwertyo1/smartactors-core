package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sevenbits on 11/14/16.
 */
// ToDo: need add thread safety logic
public class FeatureManager implements IFeatureManager {

    private Map<Object, IFeature> loadedFeatures;
    private Map<Object, IFeature> failedFeatures;
    private Map<Object, IFeature> processingFeatures;
    private Map<Object, IFeature> frozenForDependencies;

    public FeatureManager() {
        this.loadedFeatures = new ConcurrentHashMap<>();
        this.failedFeatures = new ConcurrentHashMap<>();
        this.processingFeatures = new ConcurrentHashMap<>();
        this.frozenForDependencies = new ConcurrentHashMap<>();
    }

    @Override
    public void addFeatures(final Set<IFeature> features)
            throws FeatureManagementException {
        features.forEach(this::addFeature);
        this.startLoading();
    }

    @Override
    public Collection<IFeature> getLoadedFeatures() {
        return this.loadedFeatures.values();
    }

    @Override
    public Collection<IFeature> getFailedFeatures() {
        return this.failedFeatures.values();
    }

    @Override
    public void onCompleteFeatureOperation(IFeature feature)
            throws FeatureManagementException {
        if (((IFeatureState<String>)feature.getStatus()).completed()) {
            this.processingFeatures.remove(feature.getName());
            this.loadedFeatures.put(feature.getName(), feature);
            this.removeLoadedDependency(feature);
        }
        if (!((IFeatureState<String>)feature.getStatus()).getLastSuccess()) {
            this.processingFeatures.remove(feature.getName());
            this.failedFeatures.put(feature.getName(), feature);
        }
        this.startLoading();
    }

    private void startLoading() throws FeatureManagementException {
        try {
            Set<IFeature> failedFeatures = new HashSet<>();
            Set<IFeature> completedFeatures = new HashSet<>();
            checkForDependencies();
            for (IFeature feature : this.processingFeatures.values()) {
                IFeatureState<String> state = (IFeatureState<String>) feature.getStatus();
                if (!state.isExecuting() && state.getLastSuccess() && !state.completed()) {
                    ((IFeatureState<String>) feature.getStatus()).setExecuting(true);
                    ITask task = IOC.resolve(Keys.getOrAdd(state.getCurrent()), this, feature);
                    IQueue queue = IOC.resolve(Keys.getOrAdd("task_queue"));
                    queue.put(task);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ResolutionException e) {
            System.out.println("Could not resolve needed task.");
            throw new FeatureManagementException("Could not resolve needed task.", e);
        }
    }

    private void checkForDependencies() {
        Set<IFeature> toFailedList = new HashSet<>();
        Set<IFeature> toFrozenList = new HashSet<>();
        Set<IFeature> toProcessingList = new HashSet<>();
        for (IFeature feature : this.processingFeatures.values()) {
            if (null != feature.getDependencies()) {
                Set<Object> loadedDependencies = new HashSet<>();
                for (Object dependency : feature.getDependencies()) {
                    if (this.loadedFeatures.containsKey(dependency)) {
                        loadedDependencies.add(dependency);
                    }
                    if (this.failedFeatures.containsKey(dependency)) {
                        toFailedList.add(feature);
                    }
                }
                feature.getDependencies().removeAll(loadedDependencies);
                if (!feature.getDependencies().isEmpty()) {
                    toFrozenList.add(feature);
                }
            }
        }

        // replace features with not loaded dependencies to the waiting (frozen) list
        for (IFeature feature : toFrozenList) {
            this.processingFeatures.remove(feature.getName());
            this.frozenForDependencies.put(feature.getName(), feature);
        }
        for (IFeature feature : toFailedList) {
            this.processingFeatures.remove(feature.getName());
            this.failedFeatures.put(feature.getName(), feature);
        }

        // find features with loaded dependencies and replace them to the processing list
        for (IFeature feature : this.frozenForDependencies.values()) {
            if (feature.getDependencies().isEmpty()) {
                toProcessingList.add(feature);
            }
        }
        for (IFeature feature : toProcessingList) {
            this.frozenForDependencies.remove(feature.getName());
            this.processingFeatures.put(feature.getName(), feature);
        }
    }

    private void addFeature(final IFeature feature) {
        Object featureName = feature.getName();
        if (null == this.processingFeatures.get(featureName)
                && null == this.loadedFeatures.get(featureName)) {
            this.processingFeatures.put(feature.getName(), feature);
        }
    }

    private void removeLoadedDependency(IFeature loadedFeature) {
        for (IFeature feature : this.processingFeatures.values()) {
            if (null != feature.getDependencies()) {
                feature.getDependencies().remove(loadedFeature.getName());
            }
        }
        for (IFeature feature : this.frozenForDependencies.values()) {
            if (null != feature.getDependencies()) {
                feature.getDependencies().remove(loadedFeature.getName());
            }
        }
    }
}
