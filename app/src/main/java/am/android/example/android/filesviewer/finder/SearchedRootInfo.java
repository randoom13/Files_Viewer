package am.android.example.android.filesviewer.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;

public class SearchedRootInfo {
    private final File mRoot;
    private final List<SearchedItemInfo> mItems = new ArrayList<SearchedItemInfo>();
    private boolean mExpanded;

    public SearchedRootInfo(File root) {
        mRoot = root;
    }

    public SearchedRootInfo(File root, SearchedItemInfo itemInfo) {
        mRoot = root;
        mItems.add(itemInfo);
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean mExpanded) {
        this.mExpanded = mExpanded;
    }

    public File getRoot() {
        return mRoot;
    }

    public boolean isSameRoot(SearchedRootInfo rootInfo) {
        return rootInfo.mRoot == mRoot;
    }

    public void addAllFrom(SearchedRootInfo rootInfo) {
        mItems.addAll(rootInfo.mItems);
    }

    public void clear() {
        mItems.clear();
    }

    public int size() {
        return mItems.size();
    }

    public SearchedItemInfo get(int location) {
        return mItems.get(location);
    }

    void colorText(TextColorProvider colorProvider) {
        for (SearchedItemInfo itemInfo : mItems) {
            itemInfo.colorText(colorProvider);
        }
    }

}
