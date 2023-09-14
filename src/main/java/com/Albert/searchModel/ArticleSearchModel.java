package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearched;

public class ArticleSearchModel implements SearchModel<String, String> {

    @Override
    public MessageOfSearched search(String keySearch, String canBeSearched) {
        String[] words = canBeSearched.split("\\s+");
        int count = 0;
        for (String word : words) {
            if (word.equalsIgnoreCase(keySearch)) {
                count++;
            }
        }
        return new MessageOfSearched(count);
    }
}
