package openblocks.lib.model.variant.eval.ast;

public interface IParserState<N> {

    public IAstParser<N> getParser();

    public ISymbolCallStateTransition<N> getStateForSymbolCall(String symbol);

    public IModifierStateTransition<N> getStateForModifier(String modifier);
}