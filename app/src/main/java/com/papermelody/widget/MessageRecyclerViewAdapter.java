package com.papermelody.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.papermelody.R;
import com.papermelody.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Nibius on 2017/6/15.
 */

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private List<Message> messages;

    public MessageRecyclerViewAdapter(Context context) {
        this.context = context;
        messages = new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
    }

    public MessageRecyclerViewAdapter(Context context, List<Message> m) {
        this.context = context;
        messages = new ArrayList<>(m);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_message_card, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setView(messages.get(position));
        holder.itemView.setOnClickListener((View v) -> {
            int pos = holder.getLayoutPosition();
            onItemClickListener.OnItemClick(messages.get(pos));
        });
    }

    @Override
    public int getItemCount() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_text_message_author)
        TextView textMessageAuthor;
        @BindView(R.id.item_text_message_content)
        TextView textMessageContent;
        @BindView(R.id.item_text_message_time)
        TextView textMessageTime;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        private void setView(Message message) {
            textMessageAuthor.setText(message.getAuthor());
            textMessageContent.setText(message.getMessage());
            textMessageTime.setText(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(message.getCreateTime()));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClick(Message message);
    }
}