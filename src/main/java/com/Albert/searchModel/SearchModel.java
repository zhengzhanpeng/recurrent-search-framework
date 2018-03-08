package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearched;

/**
 * @author Albert
 * @create 2018-02-03 20:09
 */
public interface SearchModel<KeySearchT, TrueObjectT, CanBeSearchedT> {

    MessageOfSearched<TrueObjectT, CanBeSearchedT> search(KeySearchT keySearch, CanBeSearchedT canBeSearched);

    boolean remove(TrueObjectT object);

    boolean add(TrueObjectT object);

    boolean isTrueObject(KeySearchT keySearch, TrueObjectT object);
}
