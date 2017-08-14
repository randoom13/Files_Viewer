package am.android.example.android.filesviewer.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;


public class SearchResultEvent {
    private final ProcessStatus mProcessStatus;
    private final List<SearchedRootInfo> mSearchedRootInfoList;
    private String mSearchFilter;

    public SearchResultEvent(ProcessStatus operation) {
        this(operation, new ArrayList<SearchedRootInfo>());
    }

    public SearchResultEvent(ProcessStatus processStatus,
                             Collection<SearchedRootInfo> values) {
        mProcessStatus = processStatus;
        mSearchedRootInfoList = new ArrayList<SearchedRootInfo>(values);
    }

    void colorText(TextColorProvider textColorProvider) {
        for (SearchedRootInfo rootInfo : mSearchedRootInfoList) {
            rootInfo.colorText(textColorProvider);
        }
    }

    public Collection<SearchedRootInfo> getSearchedRootInfoList() {
        return mSearchedRootInfoList;
    }

    public ProcessStatus getStatus() {
        return mProcessStatus;
    }

    public String getSearchFilter() {
        return mSearchFilter;
    }

    void setSearchFilter(String mSearchFilter) {
        this.mSearchFilter = mSearchFilter;
    }

    public enum ProcessStatus {
        PENDING, RUNNING, FINISHED
    }
}
