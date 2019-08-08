package ai.oneid.onemove.adapter;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.SearchContactActivity;
import ai.oneid.onemove.data.AsynRestClient;
import ai.oneid.onemove.fragment.ContactsFragment;

public class SearchContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public SearchContactActivity activity;
    int scrollCount = 0;
    public SearchContactAdapter(SearchContactActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.recycle_view_search_contact, viewGroup, false);
        return new ItemViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final ItemViewHolder viewHolder = (ItemViewHolder)holder;
        viewHolder.textName.setText(activity.nameList.get(position));
        viewHolder.textContact.setText(activity.contactList.get(position));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity)
                        .setMessage(activity.getString(R.string.confirm_add_user))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                activity.addUser(activity.idList.get(position));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        Glide.with(activity).load(AsynRestClient.utilityServiceUrl + "uploads/" + activity.idList.get(position) + ".jpg").error(Glide.with(activity)
                .load(R.drawable.default_user)).apply(RequestOptions.circleCropTransform()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount()
    {
        return activity.nameList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DATA;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder
    {
        public LinearLayout layout;
        public AppCompatImageView imageView;
        public TextView textName;
        public TextView textContact;

        public ItemViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.layout_main);
            imageView = view.findViewById(R.id.image_profile);
            textName = view.findViewById(R.id.text_name);
            textContact = view.findViewById(R.id.text_contact);
        }
    }
}