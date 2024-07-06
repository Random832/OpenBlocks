package openblocks.lib.model.variant.eval.ast;

import com.google.common.collect.PeekingIterator;
import openblocks.lib.model.variant.eval.token.Token;

public interface IAstParser<N> {
    N parse(IParserState<N> state, PeekingIterator<Token> input);
}