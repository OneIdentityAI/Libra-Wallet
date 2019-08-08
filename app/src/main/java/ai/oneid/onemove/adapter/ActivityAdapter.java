package ai.oneid.onemove.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.ActivityDetailActivity;
import ai.oneid.onemove.fragment.ActivityFragment;
import ai.oneid.onemove.fragment.HomeFragment;

public class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public ActivityFragment fragment;
    int scrollCount = 0;
    public ActivityAdapter(ActivityFragment fragment)
    {
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.recycle_view_transaction, viewGroup, false);
        return new ItemViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final ItemViewHolder viewHolder = (ItemViewHolder)holder;
        viewHolder.textName.setText(fragment.transactionNameList.get(position));
        viewHolder.textComment.setText(fragment.transactionCommentList.get(position));
        viewHolder.textAmount.setText(fragment.transactionAmountList.get(position));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fragment.activity, ActivityDetailActivity.class);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("user_id", fragment.transactionUserIdList.get(position));
                    jsonObject.put("name", fragment.transactionNameList.get(position));
                    jsonObject.put("amount", fragment.transactionAmountList.get(position));
                    jsonObject.put("comment", fragment.transactionCommentList.get(position));
                    jsonObject.put("created", fragment.transactionCreatedList.get(position));
                    jsonObject.put("transaction_id", fragment.transactionTransactionIdList.get(position));
                }
                catch (JSONException e)
                {

                }
                intent.putExtra("data", jsonObject.toString());
                fragment.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return fragment.transactionNameList.size();
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
        public TextView textComment;
        public TextView textAmount;

        public ItemViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.layout_main);
            imageView = view.findViewById(R.id.image_profile);
            textName = view.findViewById(R.id.text_name);
            textComment = view.findViewById(R.id.text_comment);
            textAmount = view.findViewById(R.id.text_amount);
        }
    }
}