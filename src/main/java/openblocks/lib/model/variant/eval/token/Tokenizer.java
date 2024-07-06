package openblocks.lib.model.variant.eval.token;

import java.util.HashSet;
import java.util.Set;

public class Tokenizer {

    final Set<String> operators = new HashSet<>();

    final Set<String> modifiers = new HashSet<>();

    public void addOperator(String operator) {
        operators.add(operator);
    }

    public void addModifier(String special) {
        modifiers.add(special);
    }

    public TokenIterator tokenize(String input) {
        return new TokenIterator(input, operators, modifiers);
    }
}