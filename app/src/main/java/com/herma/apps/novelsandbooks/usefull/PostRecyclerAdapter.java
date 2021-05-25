package com.herma.apps.novelsandbooks.usefull;

import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.herma.apps.novelsandbooks.R;
import com.herma.apps.novelsandbooks.ReaderActivity;

import java.util.ArrayList;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
  private static final int VIEW_TYPE_LOADING = 0;
  private static final int VIEW_TYPE_NORMAL = 1;
  private boolean isLoaderVisible = false;

  private List<Object> mPostItems;

  ViewGroup viewGroup;

  // A menu item view type.
  private static final int MAIN_ITEM_VIEW_TYPE = 0;
  // The banner ad view type.
  private static final int BANNER_AD_VIEW_TYPE = 5;

  public PostRecyclerAdapter(List<Object> postItems) {
    this.mPostItems = postItems;
  }

  @NonNull @Override
  public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    viewGroup = parent;

    switch (viewType) {
      case VIEW_TYPE_NORMAL:
        return new ViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
      case VIEW_TYPE_LOADING:
        return new ProgressHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
      default:
//        View bannerLayoutView = LayoutInflater.from(
//                viewGroup.getContext()).inflate(R.layout.banner_ad_container,
//                viewGroup, false);
//        return new ViewHolder(
//                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, (ViewGroup) bannerLayoutView, false));

        return null;
    }
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    int viewType = getItemViewType(position);
    switch (viewType) {
      case BANNER_AD_VIEW_TYPE:
        // Replace holder.onBind(position); by native add
        holder.onBind(position);
        // fall through
      default:
        holder.onBind(position);

    }
  }

  @Override
  public int getItemCount() {
    return mPostItems == null ? 0 : mPostItems.size();
  }

  /**
   * Determines the view type for the given position.
   */
  @Override
  public int getItemViewType(int position) {

    if (isLoaderVisible) {
      return position == mPostItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
    } else {
//      if(position % HomeFragment.ITEMS_PER_AD == 0) return BANNER_AD_VIEW_TYPE;
//      else
        return VIEW_TYPE_NORMAL;
    }
  }
//  @Override
//  public int getItemViewType(int position) {
//    if (isLoaderVisible) {
//      return position == mPostItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
//    } else {
//      return VIEW_TYPE_NORMAL;
//    }
//  }

  public void addItems(ArrayList<Object> postItems) {
    mPostItems.addAll(postItems);
    notifyDataSetChanged();
  }

  public void addLoading() {
    isLoaderVisible = true;
    mPostItems.add(new PostItem());
    notifyItemInserted(mPostItems.size() - 1);
  }

  public void removeLoading() {
    isLoaderVisible = false;
    int position = mPostItems.size() - 1;
    PostItem item = getItem(position);
    if (item != null) {
      mPostItems.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void clear() {
    mPostItems.clear();
    notifyDataSetChanged();
  }

  PostItem getItem(int position) {
    return (PostItem) mPostItems.get(position);
  }

  public class ViewHolder extends BaseViewHolder {
    TextView textViewTitle;
    TextView textViewDescription;
    TextView txtPublished_at;

    ViewHolder(View itemView) {
      super(itemView);

    textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
    textViewDescription = (TextView) itemView.findViewById(R.id.textViewDescription);

    txtPublished_at = (TextView) itemView.findViewById(R.id.txtPublished_at);

    }

    protected void clear() {

    }

    public void onBind(int position) {
      super.onBind(position);
      try{
      final PostItem item = (PostItem) mPostItems.get(position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        textViewTitle.setText(Html.fromHtml(item.getCategoryName().trim(), Html.FROM_HTML_MODE_COMPACT));
      } else {
        textViewTitle.setText(Html.fromHtml(item.getCategoryName().trim()));
      }
        textViewDescription.setText(item.getBlogposts_count() + " Chapters/Parts");

      txtPublished_at.setText(item.getBlogwriter_name()+"");

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(viewGroup.getContext(), ReaderActivity.class);
          intent.putExtra("id", item.getRealId());
          intent.putExtra("blogposts_count", item.getBlogposts_count());
          intent.putExtra("writername", item.getBlogwriter_name());
          intent.putExtra("blogwriter_id", item.getBlogwriter_id());
          intent.putExtra("name", item.getCategoryName());
          viewGroup.getContext().startActivity(intent);
        }
      });
    }catch(Exception lk){System.out.println("error on adapter : " + lk);}
    }

  }

  public class ProgressHolder extends BaseViewHolder {
    ProgressHolder(View itemView) {
      super(itemView);
      ProgressBar progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

    }

    @Override
    protected void clear() {
    }
  }
}
