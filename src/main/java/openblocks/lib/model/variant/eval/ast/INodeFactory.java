package openblocks.lib.model.variant.eval.ast;

import openblocks.lib.model.variant.eval.token.Token;

import java.util.List;

public interface INodeFactory<E, O> {

    E createBracketNode(String openingBracket, String closingBracket, List<E> children);

    E createOpNode(O op, List<E> children);

    E createSymbolGetNode(String id);

    E createValueNode(Token token);
}
