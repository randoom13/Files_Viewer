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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.android.example.android.filesviewer.finder.FilesFinder;
import am.android.example.android.filesviewer.finder.SearchResultEvent;
import am.android.example.android.filesviewer.finder.SearchedItemInfo;
import am.android.example.android.filesviewer.finder.validation.FilesValidator;
import am.android.example.android.filesviewer.finder.validation.TextColorProvider;
import de.greenrobot.event.EventBus;

/**
 * Created by alexander on 14.02.17.
 */
public class FinderListFragment extends ListFragment
        implements TextView.OnEditorActionListener {
    private static final String sActualFilter = "actual filter";
    private static final String sTypedFilter = "typed filter";
    private static final int[] RowTitles = {R.string.root, R.string.internal, R.string.external, R.string.pub};
    private EditText mSearchFilter = null;
    private FilesFinder mFilesFinder = null;
    private ProgressBar mProgressBar = null;
    private FilesViewerAdapter mAdapter;
    private Map<File, List<SearchedItemInfo>> _fileList = new HashMap<File, List<SearchedItemInfo>>();
    private String mActualFilter;
    private String mTypedFilter;

    private static String getSavedString(Bundle savedInstanceState, String key) {
        return savedInstanceState == null ? "" :
                savedInstanceState.getString(key, "");
    }

    private File[] getRoots() {
        int rootsCount =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? 3 : 4;
        File[] result = new File[rootsCount];
        result[0] = Environment.getRootDirectory();
        result[1] = this.getActivity().getFilesDir();
        result[2] = this.getActivity().getExternalFilesDir(null);
        if (rootsCount > 3)
            result[3] = Environment.getExternalStorageDirectory();
        return result;
    }

    private void clearFilesList() {
        for (Map.Entry<File, List<SearchedItemInfo>> list : _fileList.entrySet()) {
            list.getValue().clear();
        }
    }

    private void addFileList(SearchResultEvent event) {
        for (Map.Entry<File, List<SearchedItemInfo>> sequence : event.getValues().entrySet()) {
            File root = sequence.getKey();
            if (!_fileList.containsKey(root))
                _fileList.put(root, new ArrayList<SearchedItemInfo>());

            _fileList.get(root).addAll(sequence.getValue());
        }
    }

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.listfragment_finder, container, false);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mSearchFilter = (EditText) rootView.findViewById(R.id.search_filter);
        mSearchFilter.setOnEditorActionListener(this);
        Button searchButton = (Button)rootView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSearch();
            }
        });
        mAdapter = new FilesViewerAdapter();
        setListAdapter(mAdapter);
        EventBus.getDefault().register(this);
        File[] roots = getRoots();
        final int paintedBackground = getResources().getColor(R.color.paintedBackground);
        mFilesFinder = new FilesFinder(roots, new FilesValidator(),
                new TextColorProvider() {
                    @Override
                    public int getBackgroundColor(String paintingString, String filter) {
                        return paintedBackground;
                    }
                });
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

    private void launchSearch(){
        InputMethodManager inputMethodManager = (InputMethodManager)
                getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchFilter.getWindowToken(), 0);
        mFilesFinder.reset(mSearchFilter.getText().toString());
    }
    @SuppressWarnings("unused")
    public void onEventMainThread(SearchResultEvent event) {
        switch (event.getStatus()) {
            case BEGINNING:
                clearFilesList();
                mAdapter.notifyDataSetChanged();
                break;
            case COMPLETED:
                if (mProgressBar != null)
                    mProgressBar.setVisibility(View.INVISIBLE);
                Toast toast = Toast.makeText(this.getActivity(),
                        "Search Completed for "
                                + event.getSearchFilter() + " :" + mAdapter.getCount() + " !", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case RUNNING:
                if (mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                addFileList(event);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }


    private class FilesViewerAdapter extends BaseAdapter {
        private View getHeaderView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null)
                row = getActivity().getLayoutInflater().inflate(R.layout.header, parent, false);

            Integer batchIndex = (Integer) getItem(position);
            TextView textView = (TextView) row.findViewById(android.R.id.text1);
            textView.setText(FinderListFragment.this.getString(RowTitles[batchIndex]));
            return row;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            int count = _fileList.keySet().size();
            for (Map.Entry<File, List<SearchedItemInfo>> sequence : _fileList.entrySet()) {
                count += sequence.getValue().size();
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            int offset = position;
            int batchIndex = 0;
            for (Map.Entry<File, List<SearchedItemInfo>> files : _fileList.entrySet()) {
                if (offset == 0)
                    return Integer.valueOf(batchIndex);

                offset--;

                int size = files.getValue().size();
                if (size > offset)
                    return files.getValue().get(offset);

                offset -= size;
                batchIndex++;
            }
            throw new IllegalArgumentException("Invalid position: " + position);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position) instanceof Integer)
                return 0;
            return 1;
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

            SearchedItemInfo searchedItemInfo = (SearchedItemInfo) getItem(position);
            int iconResourceId = searchedItemInfo.getIsDir() ? android.R.drawable.arrow_down_float :
                    android.R.drawable.star_on;
            holder.getIcon().setImageResource(iconResourceId);
            holder.getText().setText(searchedItemInfo.getText());
            return row;
        }
    }
}