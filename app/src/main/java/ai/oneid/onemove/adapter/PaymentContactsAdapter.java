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
import ai.oneid.onemove.activity.SendPaymentActivity;
import ai.oneid.onemove.data.AsynRestClient;
import ai.oneid.onemove.fragment.ContactsFragment;

public class PaymentContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public SendPaymentActivity activity;
    int scrollCount = 0;
    public PaymentContactsAdapter(SendPaymentActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.recycle_view_payment_contacts, viewGroup, false);
        return new ItemViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final ItemViewHolder viewHolder = (ItemViewHolder)holder;
        viewHolder.textName.setText(activity.contactNameList.get(position));
        viewHolder.textContact.setText(activity.contactContactList.get(position));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.contactIdList.clear();
                activity.contactCreatedList.clear();
                activity.contactAddressList.clear();
                activity.contactContactList.clear();
                activity.contactNameList.clear();
                notifyDataSetChanged();

                activity.recyclerView.setVisibility(View.GONE);
                activity.inputAddress.setVisibility(View.VISIBLE);
            }
        });
        Glide.with(activity).load(AsynRestClient.utilityServiceUrl + "uploads/" + activity.contactIdList.get(position) + ".jpg").error(Glide.with(activity)
                .load(R.drawable.default_user)).apply(RequestOptions.circleCropTransform()).into(viewHolder.imageView);
    }

    @Override
    public int getItemCount()
    {
        return activity.contactNameList.size();
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