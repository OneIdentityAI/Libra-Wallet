package ai.oneid.onemove.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.SendPaymentActivity;
import ai.oneid.onemove.data.AsynRestClient;
import ai.oneid.onemove.fragment.HomeFragment;

public class HorizontalContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public HomeFragment fragment;
    int scrollCount = 0;
    public HorizontalContactAdapter(HomeFragment fragment)
    {
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.recycle_view_horizontal_contact, viewGroup, false);
        return new ItemViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final ItemViewHolder viewHolder = (ItemViewHolder)holder;
        viewHolder.textName.setText(fragment.contactNameList.get(position));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fragment.activity, SendPaymentActivity.class);
                JSONArray jsonArray = new JSONArray();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", fragment.contactIdList.get(position));
                    jsonObject.put("name", fragment.contactNameList.get(position));
                    jsonObject.put("address", fragment.contactAddressList.get(position));
                    jsonObject.put("contact", fragment.contactContactList.get(position));
                    jsonObject.put("created", fragment.contactCreatedList.get(position));
                    jsonArray.put(jsonObject);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                Log.e("Data", jsonArray.toString());
                intent.putExtra("data", jsonArray.toString());
                fragment.startActivity(intent);
            }
        });
        Glide.with(fragment).load(AsynRestClient.utilityServiceUrl + "uploads/" + fragment.contactIdList.get(position) + ".jpg").error(Glide.with(fragment)
                .load(R.drawable.default_user)).apply(RequestOptions.circleCropTransform()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount()
    {
        return fragment.contactNameList.size();
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

        public ItemViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.layout_main);
            imageView = view.findViewById(R.id.image_profile);
            textName = view.findViewById(R.id.text_name);
        }
    }
}