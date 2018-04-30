package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearched;

/**
 * @author Albert
 */
public interface SearchModel<KeyT, PathT> {

    MessageOfSearched search(KeyT keySearch, PathT canBeSearched);
}
