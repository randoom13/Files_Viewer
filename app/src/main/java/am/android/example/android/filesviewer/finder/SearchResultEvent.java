package am.android.example.android.filesviewer.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;


public class SearchResultEvent {
    public static final int PENDING_PROCESS_STATUS = 0;
    public static final int RUNNING_PROCESS_STATUS = 1;
    public static final int FINISHED_PROCESS_STATUS = 2;
    private final int mProcessStatus;
    private final List<SearchedRootInfo> mSearchedRootInfoList;
    private String mSearchFilter;

    public SearchResultEvent(int processStatus) {
        this(processStatus, new ArrayList<SearchedRootInfo>());
    }

    public SearchResultEvent(int processStatus,
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

    public int getStatus() {
        return mProcessStatus;
    }

    public String getSearchFilter() {
        return mSearchFilter;
    }

    void setSearchFilter(String mSearchFilter) {
        this.mSearchFilter = mSearchFilter;
    }

}
