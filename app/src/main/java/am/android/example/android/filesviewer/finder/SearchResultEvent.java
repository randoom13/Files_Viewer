package am.android.example.android.filesviewer.finder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;


public class SearchResultEvent {
    private final SearchProcessStatus mProcessStatus;
    private final Map<File, List<SearchedItemInfo>> mValues;
    private String mSearchFilter;

    public SearchResultEvent(SearchProcessStatus operation) {
        this(operation, new HashMap<File, List<SearchedItemInfo>>());
    }

    public SearchResultEvent(SearchProcessStatus processStatus,
                             Map<File, List<SearchedItemInfo>> values) {
        mProcessStatus = processStatus;
        mValues = new HashMap<File, List<SearchedItemInfo>>(values);
    }

    void colorSearchedItemInfos(TextColorProvider textColorProvider) {
        for (Map.Entry<File, List<SearchedItemInfo>> values : mValues.entrySet()) {
            for (SearchedItemInfo searchedItemInfo : values.getValue()) {
                searchedItemInfo.colorText(textColorProvider);
            }
        }
    }

    public Map<File, List<SearchedItemInfo>> getValues() {
        return mValues;
    }

    public SearchProcessStatus getStatus() {
        return mProcessStatus;
    }

    public String getSearchFilter() {
        return mSearchFilter;
    }

    public void setSearchFilter(String mSearchFilter) {
        this.mSearchFilter = mSearchFilter;
    }
}
