package openblocks.lib.model.variant.eval.ast;

import java.util.List;

public interface ISymbolCallStateTransition<N> {
    public IParserState<N> getState();

    public N createRootNode(List<N> children);
}