package ai.oneid.onemove.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.SearchContactActivity;
import ai.oneid.onemove.activity.SelectContactActivity;
import ai.oneid.onemove.data.AsynRestClient;

import static android.app.Activity.RESULT_OK;

public class SelectContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public SelectContactActivity activity;
    int scrollCount = 0;
    public SelectContactAdapter(SelectContactActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.recycle_view_select_contact, viewGroup, false);
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
                Intent data = new Intent();
                JSONArray jsonArray = new JSONArray();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", activity.idList.get(position));
                    jsonObject.put("name", activity.nameList.get(position));
                    jsonObject.put("address", activity.addressList.get(position));
                    jsonObject.put("contact", activity.contactList.get(position));
                    jsonObject.put("created", activity.createdList.get(position));
                    jsonArray.put(jsonObject);
                }
                catch (JSONException e)
                {

                }
                String text = jsonArray.toString();
                data.setData(Uri.parse(text));
                activity.setResult(RESULT_OK, data);
                activity.finish();
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