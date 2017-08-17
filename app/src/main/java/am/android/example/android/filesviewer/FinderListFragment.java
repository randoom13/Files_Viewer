package am.android.example.android.filesviewer;

import android.app.ListFragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.android.example.android.filesviewer.finder.FilesFinder;
import am.android.example.android.filesviewer.finder.SearchResultEvent;
import am.android.example.android.filesviewer.finder.SearchedItemInfo;
import am.android.example.android.filesviewer.finder.SearchedRootInfo;
import am.android.example.android.filesviewer.finder.validation.FilesValidator;
import am.android.example.android.filesviewer.finder.validation.TextColorProvider;
import de.greenrobot.event.EventBus;

public class FinderListFragment extends ListFragment implements TextWatcher {
    private static final String sActualFilter = "actual filter";
    private static final String sTypedFilter = "typed filter";
    private static final int[] sRowTitles = {R.string.root, R.string.internal, R.string.external, R.string.pub};

    private final Map<File, Integer> mRootsMap = new HashMap<File, Integer>();
    private EditText mSearchFilter = null;
    private FilesFinder mFilesFinder = null;
    private ProgressBar mProgressBar = null;
    private FilesViewerAdapter mAdapter;
    private List<SearchedRootInfo> mRootInfoList = new ArrayList<SearchedRootInfo>();
    private String mTypedFilter;
    private String mActualFilter;

    //region private methods

    private static String getSavedString(Bundle savedInstanceState, String key) {
        return savedInstanceState == null ? "" :
                savedInstanceState.getString(key, "");
    }


    private void initializeRoots() {
        int rootsCount =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? 3 : 4;
        List<File> roots = new ArrayList<File>();
        roots.add(Environment.getRootDirectory());
        roots.add(this.getActivity().getFilesDir());
        roots.add(this.getActivity().getExternalFilesDir(null));
        if (rootsCount > 3)
            roots.add(Environment.getExternalStorageDirectory());
        for (int index = 0; index < roots.size(); index++) {
            File root = roots.get(index);
            if (root != null)
                mRootsMap.put(root, sRowTitles[index]);
        }
    }

    private void clearRootInfoList() {
        for (SearchedRootInfo rootInfo : mRootInfoList) {
            rootInfo.clear();
        }
    }

    private void initializeRootInfoList() {
        for (File root : mRootsMap.keySet()) {
            mRootInfoList.add(new SearchedRootInfo(root));
        }
    }

    private void addToRootInfoList(Collection<SearchedRootInfo> newRootInfoList) {
        for (SearchedRootInfo newRootInfo : newRootInfoList) {
            for (SearchedRootInfo rootInfo : mRootInfoList) {
                if (rootInfo.isSameRoot(newRootInfo)) {
                    rootInfo.addAllFrom(newRootInfo);
                    break;
                }
            }
        }
    }


    //endregion private methods

    //region public methods

    @Override
    public void onResume() {
        super.onResume();
        mFilesFinder.launch(mActualFilter);
        mSearchFilter.setText(mTypedFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFilesFinder.stop();
        mActualFilter = mFilesFinder.getFilter();
        mTypedFilter = mSearchFilter.getText().toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        initializeRoots();
        initializeRootInfoList();
        mAdapter = new FilesViewerAdapter(getActivity().getLayoutInflater(), mRootInfoList, mRootsMap);
        final int paintedBackgroundColor = getResources().getColor(R.color.paintedBackground);
        mFilesFinder = new FilesFinder(mRootsMap.keySet(), new FilesValidator(),
                new TextColorProvider() {
                    @Override
                    public int getBackgroundColor(String paintingString, String filter) {
                        return paintedBackgroundColor;
                    }
                });
        EventBus.getDefault().register(this);
    }

    //region TextWatcher implementation

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mFilesFinder.reset(mSearchFilter.getText().toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    //endregion TextWatcher implementation

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.listfragment_finder, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mSearchFilter = (EditText) rootView.findViewById(R.id.search_filter);
        mSearchFilter.addTextChangedListener(this);
        setListAdapter(mAdapter);
        mTypedFilter = getSavedString(savedInstanceState, sTypedFilter);
        mActualFilter = getSavedString(savedInstanceState, sActualFilter);
        return rootView;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SearchResultEvent event) {
        switch (event.getStatus()) {
            case SearchResultEvent.PENDING_PROCESS_STATUS:
                clearRootInfoList();
                mAdapter.notifyDataSetChanged();
                break;

            case SearchResultEvent.FINISHED_PROCESS_STATUS:
                if (mProgressBar != null)
                    mProgressBar.setVisibility(View.INVISIBLE);
                break;

            case SearchResultEvent.RUNNING_PROCESS_STATUS:
                if (mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                addToRootInfoList(event.getSearchedRootInfoList());
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    //endregion public methods

    private static class FilesViewerAdapter extends BaseAdapter implements View.OnClickListener {
        private final List<SearchedRootInfo> mRootInfoList;
        private final LayoutInflater mInflater;
        private final Map<File, Integer> mRootsMap;

        public FilesViewerAdapter(LayoutInflater inflater, List<SearchedRootInfo> rootInfoList, Map<File, Integer> rootsMap) {
            super();
            mInflater = inflater;
            mRootInfoList = rootInfoList;
            mRootsMap = rootsMap;
        }

        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            if (null == holder || !holder.isHeader)
                return;

            SearchedRootInfo rootInfo = mRootInfoList.get(holder.getHeaderPosition());
            rootInfo.setExpanded(!rootInfo.isExpanded());
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            boolean isHeader = getItemViewType(position) == 0;
            View row = convertView;
            if (row == null) {
                int rowId = isHeader ? R.layout.header : R.layout.row;
                row = mInflater.inflate(rowId, parent, false);
            }

            ViewHolder holder = (ViewHolder) row.getTag();
            if (null == holder) {
                holder = new ViewHolder(row, isHeader);
                row.setTag(holder);
            }

            if (isHeader) {
                SearchedRootInfo rootInfo = (SearchedRootInfo) getItem(position);
                int titleIdResource = mRootsMap.get(rootInfo.getRoot());
                holder.bind(rootInfo, titleIdResource);
                holder.setHeaderPosition(mRootInfoList.indexOf(rootInfo));
                if (rootInfo.size() > 0)
                    row.setOnClickListener(this);
            } else {
                SearchedItemInfo itemInfo = (SearchedItemInfo) getItem(position);
                holder.bind(itemInfo);
            }
            return row;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            int size = mRootInfoList.size();
            for (SearchedRootInfo rootInfo : mRootInfoList) {
                if (rootInfo.isExpanded())
                    size += rootInfo.size();
            }
            return size;
        }

        @Override
        public Object getItem(int position) {
            int offset = position;
            for (SearchedRootInfo rootInfo : mRootInfoList) {
                if (offset == 0)
                    return rootInfo;

                offset--;

                if (!rootInfo.isExpanded())
                    continue;

                int size = rootInfo.size();
                if (size > offset)
                    return rootInfo.get(offset);

                offset -= size;
            }
            throw new IllegalArgumentException("Invalid position: " + position);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position) instanceof SearchedRootInfo)
                return 0;
            return 1;
        }
    }
}