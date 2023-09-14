package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearched;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArticleSearchModelTest {

    private final ArticleSearchModel<String, String> articleSearchModel = new ArticleSearchModel<>();

    @Test
    public void testSearch() {
        String article = "This is a test article. This article is for testing.";
        String word = "article";
        MessageOfSearched result = articleSearchModel.search(word, article);
        Assertions.assertEquals(2, result.getCount());
    }
}
