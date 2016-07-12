package info.smart_tools.smartactors.core;


import info.smart_tools.smartactors.core.imessage.IMessage;

/**
 * Temporary interface for {@link IMessage} serialization/deserialization logic.
 * @param <TSerialized> data structure in/from which message can be serialized/deserialized
 */
public interface IMessageMapper<TSerialized> {
    /**
     * Deserialize {@link IMessage} from the given input.
     * TODO: throw an exception when message can't be deserialized.
     * @param serializedInput serialized message
     * @return a deserialized message
     */
    IMessage deserialize(TSerialized serializedInput);

    /**
     * Serialize a {@link IMessage} into some format. From which it can be deserialized later.
     * TODO: throw an exception when message can't be serialized
     * @param message message to be serialized
     * @return a serialized representation of message
     */
    TSerialized serialize(IMessage message);
}
