package openblocks.lib.model.variant.eval.ast;

public interface IOperator<T> {
    String id();

    default OperatorArity arity() {
        return OperatorArity.BINARY;
    }
    boolean isLowerPriority(T other);
}
