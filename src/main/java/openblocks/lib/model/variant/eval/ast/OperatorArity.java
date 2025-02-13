package openblocks.lib.model.variant.eval.ast;

public enum OperatorArity {
    UNARY(1), BINARY(2);

    public final int args;

    private OperatorArity(int args) {
        this.args = args;
    }
}