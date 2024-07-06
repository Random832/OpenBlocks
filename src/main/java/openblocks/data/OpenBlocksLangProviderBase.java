package openblocks.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import openblocks.OpenBlocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class OpenBlocksLangProviderBase extends LanguageProvider {
    public OpenBlocksLangProviderBase(PackOutput output, String locale) {
        super(output, OpenBlocks.MODID, locale);
    }

    Set<String> validationKeys = new TreeSet<>();

    @Override
    public void add(String key, String value) {
        if (!validationKeys.add(key))
            LOGGER.warn("Duplicate translation key " + key);
        else
            super.add(key, value);
    }

    Iterable<String> getRequiredKeys() {
        Collection<String> requiredKeys = new ArrayList<>();
        Stream.of(
                OpenBlocks.BLOCKS.getEntries().stream().map(x -> x.get().getDescriptionId()),
                OpenBlocks.ITEMS.getEntries().stream().map(x -> x.get().getDescriptionId()),
                OpenBlocks.FLUID_TYPES.getEntries().stream().map(x -> x.get().getDescriptionId()),
                getCustomRequiredKeys()
        ).flatMap(x -> x).filter(x -> !x.startsWith("block.minecraft")).forEach(requiredKeys::add);
        return requiredKeys;
    }

    private Stream<String> getCustomRequiredKeys() {
        return Stream.empty();
    }

    public void validate() {
        Set<String> missingKeys = new TreeSet<>();
        for (String key : getRequiredKeys()) {
            if (!validationKeys.contains(key)) {
                missingKeys.add(key);
            }
        }
        if (!missingKeys.isEmpty()) {
            for (String key : missingKeys) {
                LOGGER.warn("Missing required translation key " + key);
            }
            //throw new IllegalStateException("Missing " + missingKeys.size() + " required translation keys.");
        }
    }

    @Override
    protected void addTranslations() {
        validate();
    }
}