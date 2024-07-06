package openblocks.lib.model.variant.eval.ast;

public class StackUnderflowException extends RuntimeException {
    private static final long serialVersionUID = 360455673552034663L;

    public StackUnderflowException(String message) {
        super(message);
    }

    public StackUnderflowException(String format, Object... args) {
        super(String.format(format, args));
    }

    public StackUnderflowException() {
        super("stack underflow");
    }
}