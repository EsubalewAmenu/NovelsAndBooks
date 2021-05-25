package com.herma.apps.novelsandbooks.ui.bookmarks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.herma.apps.novelsandbooks.usefull.DBHelper;
import com.herma.apps.novelsandbooks.R;
import com.herma.apps.novelsandbooks.usefull.PostRecyclerAdapter;

import java.util.ArrayList;

public class BookmarksFragment extends Fragment {

    RecyclerView mRecyclerView;
    private PostRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);

        if (new DBHelper(getActivity()).getAllBookmarks().size() == 0) {

            root = inflater.inflate(R.layout.no_bookmark, container, false);

        }
        else
            setUpAdapter();

        return root;
    }


    private void setUpAdapter() {
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

//        adapter = new PostRecyclerAdapter(new ArrayList<>());
        adapter = new PostRecyclerAdapter(new ArrayList<Object>());
        mRecyclerView.setAdapter(adapter);

        adapter.addItems(new DBHelper(getActivity()).getAllBookmarks());
    }
}