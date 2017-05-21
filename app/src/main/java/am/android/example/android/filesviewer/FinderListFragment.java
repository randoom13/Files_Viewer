package am.android.example.android.filesviewer;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class FinderListFragment extends ListFragment
        implements TextView.OnEditorActionListener {
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

    private int getRootInfoListSize() {
        int size = 0;
        for (SearchedRootInfo rootInfo : mRootInfoList) {
            size += rootInfo.size();
        }
        return size;
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

    private String getHeaderTitle(SearchedRootInfo rootInfo) {
        int titleIdResource = mRootsMap.get(rootInfo.getRoot());
        return String.format(getString(R.string.header_format),
                getString(titleIdResource), rootInfo.size());
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
        mAdapter = new FilesViewerAdapter();
        initializeRoots();
        initializeRootInfoList();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.listfragment_finder, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mSearchFilter = (EditText) rootView.findViewById(R.id.search_filter);
        mSearchFilter.setOnEditorActionListener(this);
        Button searchButton = (Button) rootView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSearch();
            }
        });
        setListAdapter(mAdapter);
        mTypedFilter = getSavedString(savedInstanceState, sTypedFilter);
        mActualFilter = getSavedString(savedInstanceState, sActualFilter);
        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
            launchSearch();
        }
        return true;
    }


    private void launchSearch() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchFilter.getWindowToken(), 0);
        mFilesFinder.reset(mSearchFilter.getText().toString());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SearchResultEvent event) {
        switch (event.getStatus()) {
            case PENDING:
                clearRootInfoList();
                mAdapter.notifyDataSetChanged();
                break;

            case FINISHED:
                if (mProgressBar != null)
                    mProgressBar.setVisibility(View.INVISIBLE);
                Toast toast = Toast.makeText(this.getActivity(),
                        R.string.search_completed, Toast.LENGTH_SHORT);
                toast.show();
                break;

            case RUNNING:
                if (mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                addToRootInfoList(event.getSearchedRootInfoList());
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    //endregion public methods

    private class FilesViewerAdapter extends BaseAdapter {
        private View getHeaderView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null)
                row = getActivity().getLayoutInflater().inflate(R.layout.header, parent, false);
            SearchedRootInfo rootInfo = (SearchedRootInfo) getItem(position);
            TextView textView = (TextView) row.findViewById(android.R.id.text1);
            textView.setText(getHeaderTitle(rootInfo));
            return row;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == 0)
                return getHeaderView(position, convertView, parent);

            View row = convertView;
            if (row == null)
                row = getActivity().getLayoutInflater()
                        .inflate(R.layout.row, parent, false);

            ViewHolder holder = (ViewHolder) row.getTag();
            if (holder == null) {
                holder = new ViewHolder(row);
                row.setTag(holder);
            }

            SearchedItemInfo itemInfo = (SearchedItemInfo) getItem(position);
            int iconResourceId = itemInfo.getIsDir() ? android.R.drawable.arrow_down_float :
                    android.R.drawable.star_on;
            holder.getIcon().setImageResource(iconResourceId);
            holder.getText().setText(itemInfo.getText());
            return row;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mRootInfoList.size() + getRootInfoListSize();
        }

        @Override
        public Object getItem(int position) {
            int offset = position;
            for (SearchedRootInfo rootInfo : mRootInfoList) {
                if (offset == 0)
                    return rootInfo;

                offset--;

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