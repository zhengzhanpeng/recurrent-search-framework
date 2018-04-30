package com.Albert.pojo;

import java.util.List;
import java.util.Optional;

/**
 * @author Albert
 */
public class MessageOfSearched<TrueT, CanBeSearchedT> {
    private final List<TrueT> trueResult;
    private final List<CanBeSearchedT> canBeSearched;

    public MessageOfSearched(List<TrueT> trueResult, List<CanBeSearchedT> canBeSearched) {
        this.trueResult = trueResult;
        this.canBeSearched = canBeSearched;
    }

    public Optional<List<TrueT>> getTrueResult() {
        return Optional.of(trueResult);
    }

    public Optional<List<CanBeSearchedT>> getCanBeSearched() {
        return Optional.of(canBeSearched);
    }
}
