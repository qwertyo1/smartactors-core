package info.smart_tools.smartactors.core.iclient;

import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.concurrent.CompletableFuture;

/**
 * General interface for clients.
 * @param <Request>
 */
public interface IClient<Request> extends IAsyncService<IClient<Request>> {

    CompletableFuture<Void> send(Request request);

    interface Creator {
        IClient create(IClientConfig params)
                throws ReadValueException, ChangeValueException;
    }
}
