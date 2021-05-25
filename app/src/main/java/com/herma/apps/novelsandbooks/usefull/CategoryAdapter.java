package com.herma.apps.novelsandbooks.usefull;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.herma.apps.novelsandbooks.R;

import java.util.List;

// The adapter class which
// extends RecyclerView Adapter
public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.MyView> {

    // List with String type
    private List<CategoryItem> list;

    protected OnCategoryItemListener mListener;

    ViewGroup viewGroup;
    CategoryItem categoryItem;

    // Constructor for adapter class
    // which takes a list of String type
    public CategoryAdapter(List<CategoryItem> horizontalList, OnCategoryItemListener itemListener)
    {
        this.list = horizontalList;
        mListener = itemListener;
    }
    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyView
            extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Text View
        TextView textView;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyView(View view)
        {
            super(view);

            view.setOnClickListener(this);
            // initialise TextView with id
            textView = (TextView)view
                    .findViewById(R.id.textview);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                categoryItem = list.get(getAdapterPosition());
                mListener.onItemClick(categoryItem);
            }
        }
    }


    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @Override
    public MyView onCreateViewHolder(ViewGroup parent,
                                     int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.category_item,
                        parent,
                        false);

        viewGroup = parent;

        // return itemView
        return new MyView(itemView);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyView holder,
                                 final int position)
    {

        // Set the text of each item of
        // Recycler view with the list items
        holder.textView.setText(list.get(position).categoryName);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(viewGroup.getContext(), list.get(position), Toast.LENGTH_SHORT).show();
//                System.out.println(list.get(position));
//            }
//        });
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public interface OnCategoryItemListener {
        void onItemClick(CategoryItem item);
    }
}