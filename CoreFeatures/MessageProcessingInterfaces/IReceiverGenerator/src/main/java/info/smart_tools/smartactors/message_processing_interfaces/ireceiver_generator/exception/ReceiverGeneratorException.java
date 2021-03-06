package info.smart_tools.smartactors.message_processing_interfaces.ireceiver_generator.exception;

import info.smart_tools.smartactors.message_processing_interfaces.ireceiver_generator.IReceiverGenerator;

/**
 * Exception for error in {@link IReceiverGenerator} method
 */
public class ReceiverGeneratorException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public ReceiverGeneratorException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public ReceiverGeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public ReceiverGeneratorException(final Throwable cause) {
        super(cause);
    }
}
