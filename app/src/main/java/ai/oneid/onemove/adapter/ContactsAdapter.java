package ai.oneid.onemove.adapter;

import android.content.Intent;
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
import ai.oneid.onemove.activity.ActivityDetailActivity;
import ai.oneid.onemove.activity.SearchContactActivity;
import ai.oneid.onemove.activity.SendPaymentActivity;
import ai.oneid.onemove.data.AsynRestClient;
import ai.oneid.onemove.fragment.ActivityFragment;
import ai.oneid.onemove.fragment.ContactsFragment;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public ContactsFragment fragment;
    int scrollCount = 0;
    public ContactsAdapter(ContactsFragment fragment)
    {
        this.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.recycle_view_contacts, viewGroup, false);
        return new ItemViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final ItemViewHolder viewHolder = (ItemViewHolder)holder;
        viewHolder.textName.setText(fragment.nameList.get(position));
        viewHolder.textContact.setText(fragment.contactList.get(position));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(fragment.activity, SendPaymentActivity.class);
                JSONArray jsonArray = new JSONArray();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", fragment.idList.get(position));
                    jsonObject.put("name", fragment.nameList.get(position));
                    jsonObject.put("address", fragment.addressList.get(position));
                    jsonObject.put("contact", fragment.contactList.get(position));
                    jsonObject.put("created", fragment.createdList.get(position));
                    jsonArray.put(jsonObject);
                }
                catch (JSONException e)
                {

                }
                intent.putExtra("data", jsonArray.toString());
                fragment.startActivity(intent);
            }
        });
        Glide.with(fragment).load(AsynRestClient.utilityServiceUrl + "uploads/" + fragment.idList.get(position) + ".jpg").error(Glide.with(fragment)
                .load(R.drawable.default_user)).apply(RequestOptions.circleCropTransform()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount()
    {
        return fragment.nameList.size();
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