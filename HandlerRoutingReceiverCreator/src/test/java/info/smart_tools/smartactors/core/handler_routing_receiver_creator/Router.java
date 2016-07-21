package info.smart_tools.smartactors.core.handler_routing_receiver_creator;

import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.irouter.exceptions.RouteNotFoundException;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sevenbits on 7/20/16.
 */
public class Router implements IRouter {

    Map<Object, IMessageReceiver> map = new HashMap<>();

    @Override
    public IMessageReceiver route(Object targetId) throws RouteNotFoundException {
        return this.map.get(targetId);
    }

    @Override
    public void register(Object targetId, IMessageReceiver receiver) {
        this.map.put(targetId, receiver);
    }

}