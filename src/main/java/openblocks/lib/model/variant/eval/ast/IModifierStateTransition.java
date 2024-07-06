package openblocks.lib.model.variant.eval.ast;

public interface IModifierStateTransition<N> {
    public IParserState<N> getState();

    public N createRootNode(N child);
}