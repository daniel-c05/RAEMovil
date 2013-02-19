package com.lazybits.rae.movil.utils;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider{

	    public final static String AUTHORITY = "com.lazybits.rae.movil.utils.SearchSuggestionsProvider";
	    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

	    public SearchSuggestionsProvider() {
	        setupSuggestions(AUTHORITY, MODE);
	    }
}
