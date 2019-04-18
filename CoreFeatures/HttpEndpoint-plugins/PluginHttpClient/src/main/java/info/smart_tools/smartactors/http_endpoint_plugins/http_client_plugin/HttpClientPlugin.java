package info.smart_tools.smartactors.http_endpoint_plugins.http_client_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.endpoint.interfaces.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.exception.ResponseHandlerException;
import info.smart_tools.smartactors.endpoint.irequest_maker.IRequestMaker;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.http_endpoint.http_client.HttpClient;
import info.smart_tools.smartactors.http_endpoint.http_client_initializer.HttpClientInitializer;
import info.smart_tools.smartactors.http_endpoint.http_request_maker.HttpRequestMaker;
import info.smart_tools.smartactors.http_endpoint.http_response_deserialization_strategy.HttpResponseDeserializationStrategy;
import info.smart_tools.smartactors.http_endpoint.http_response_handler.HttpResponseHandler;
import info.smart_tools.smartactors.http_endpoint.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Plugin for http client
 */
public class HttpClientPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap bootstrap
     */
    public HttpClientPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    private IFieldName uriFieldName;
    private IFieldName startChainNameFieldName,
            queueFieldName,
            stackDepthFieldName,
            exceptionalMessageMapId;

    @Override
    @SuppressWarnings("unchecked")
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateHttpClient");
            item
                    .after("response")
                    .after("response_content_strategy")
                    .process(() -> {
                        try {
                            registerFieldNames();
                            IOC.register(Keys.resolveByName(URI.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                try {
                                                    return new URI((String) args[0]);
                                                } catch (URISyntaxException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    )
                            );
                            IMessageMapper<byte[]> messageMapper = new MessageToBytesMapper();

                            IDeserializeStrategy deserializeStrategy = new HttpResponseDeserializationStrategy(messageMapper);

                            IOC.register(Keys.resolveByName("httpResponseResolver"), new SingletonStrategy(
                                            deserializeStrategy
                                    )
                            );

                            IOC.register(Keys.resolveByName("EmptyIObject"), new ApplyFunctionToArgumentsStrategy(
                                            (args) -> new DSObject()
                                    )
                            );

                            IOC.register(Keys.resolveByName(
                                    IResponseHandler.class.getCanonicalName()),
                                    new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                try {
                                                    IObject configuration = IOC.resolve(
                                                            Keys.resolveByName("responseHandlerConfiguration")
                                                    );
                                                    IObject request = (IObject) args[0];
                                                    return new HttpResponseHandler(
                                                            (IQueue<ITask>) configuration.getValue(queueFieldName),
                                                            (Integer) configuration.getValue(stackDepthFieldName),
                                                            request.getValue(startChainNameFieldName),
                                                            request,
                                                            ScopeProvider.getCurrentScope(),
                                                            ModuleManager.getCurrentModule()
                                                    );
                                                } catch (ResolutionException exc) {
                                                    throw new RuntimeException(
                                                            "HTTP(S) client isn't configured: " +
                                                                    "configuration section 'client' isn't found",
                                                            exc
                                                    );
                                                } catch (ResponseHandlerException |
                                                        ReadValueException | InvalidArgumentException |
                                                        ScopeProviderException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    )
                            );

                            IRequestMaker<FullHttpRequest> requestMaker = new HttpRequestMaker();
                            IOC.register(Keys.resolveByName(
                                    IRequestMaker.class.getCanonicalName()),
                                    new SingletonStrategy(requestMaker)
                            );
                            IOC.register(Keys.resolveByName(MessageToBytesMapper.class.getCanonicalName()),
                                    new SingletonStrategy(
                                            messageMapper
                                    )
                            );
                            IOC.register(Keys.resolveByName("sendHttpRequest"), new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                try {
                                                    HttpClient client = (HttpClient) args[0];
                                                    IObject request = (IObject) args[1];
                                                    client.sendRequest(request);
                                                    IOC.resolve(
                                                            Keys.resolveByName("createTimerOnRequest"),
                                                            request,
                                                            request.getValue(exceptionalMessageMapId)
                                                    );
                                                    return client;
                                                } catch (ResolutionException | RequestSenderException | ReadValueException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    )
                            );

                            IOC.register(Keys.resolveByName("getHttpClient"), new ApplyFunctionToArgumentsStrategy(
                                            (args) -> {
                                                IObject request = (IObject) args[0];
                                                try {
                                                    IResponseHandler responseHandler = IOC.resolve(
                                                            Keys.resolveByName(IResponseHandler.class.getCanonicalName()),
                                                            request
                                                    );
                                                    return new HttpClient(
                                                            URI.create((String) request.getValue(uriFieldName)),
                                                            responseHandler
                                                    );
                                                } catch (ReadValueException | ResolutionException | RequestSenderException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    )
                            );
                            HttpClientInitializer.init();
                        } catch (RegistrationException | ResolutionException | InvalidArgumentException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "CreateHttpClient";
                        String keyName = "";

                        try {
                            keyName = "cancelTimerOnRequest";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "createTimerOnRequest";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "getHttpClient";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "sendHttpRequest";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = MessageToBytesMapper.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = IRequestMaker.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = IResponseHandler.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "EmptyIObject";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = "httpResponseResolver";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }

                        try {
                            keyName = URI.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException ignored) { }
                    });
            bootstrap.add(item);

        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    private void registerFieldNames() throws ResolutionException {
        final IKey fieldNameKey = Keys.resolveByName(IFieldName.class.getCanonicalName());

        this.uriFieldName =            IOC.resolve(fieldNameKey, "uri");
        this.startChainNameFieldName = IOC.resolve(fieldNameKey, "startChain");
        this.queueFieldName =          IOC.resolve(fieldNameKey, "queue");
        this.stackDepthFieldName =     IOC.resolve(fieldNameKey, "stackDepth");
        this.exceptionalMessageMapId = IOC.resolve(fieldNameKey, "exceptionalMessageMapId");
    }
}
