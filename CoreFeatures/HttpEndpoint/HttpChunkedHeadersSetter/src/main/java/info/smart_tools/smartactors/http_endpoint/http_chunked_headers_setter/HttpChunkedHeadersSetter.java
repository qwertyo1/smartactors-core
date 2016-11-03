package info.smart_tools.smartactors.http_endpoint.http_chunked_headers_setter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.http_endpoint.interfaces.iheaders_extractor.IHeadersExtractor;
import info.smart_tools.smartactors.http_endpoint.interfaces.iheaders_extractor.exceptions.HeadersSetterException;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

import java.util.List;

/**
 * Created by sevenbits on 28.10.16.
 */
public class HttpChunkedHeadersSetter implements IHeadersExtractor {
    @Override
    public void set(final Object response, final IObject environment) throws HeadersSetterException {
        FullHttpResponse httpResponse = (FullHttpResponse) response;
        IField contextField;
        IField headersField;
        IFieldName headerName;
        IFieldName headerValue;
        try {
            contextField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "context");
            headersField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "headers");
            headerName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
            headerValue = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "value");
        } catch (ResolutionException e) {
            throw new HeadersSetterException("Failed to resolve fieldName", e);
        }

        httpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "chunked");
        httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        IObject context = null;
        List<IObject> headers = null;
        try {
            context = contextField.in(environment, IObject.class);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new HeadersSetterException("Failed to get context from environment", e);
        }
        try {
            headers = headersField.in(context, List.class);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new HeadersSetterException("Failed to get cookies from context", e);
        }
        for (IObject header : headers) {
            try {
                httpResponse.headers().set(String.valueOf(header.getValue(headerName)),
                        String.valueOf(header.getValue(headerValue)));
            } catch (ReadValueException | InvalidArgumentException e) {
                throw new HeadersSetterException("Failed to resolve header", e);
            }
        }
    }
}