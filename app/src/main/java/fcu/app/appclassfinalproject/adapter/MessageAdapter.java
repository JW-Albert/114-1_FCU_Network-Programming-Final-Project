package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

  private Context context;
  private List<Message> messageList;
  private int currentUserId;
  private SimpleDateFormat dateFormat;

  public MessageAdapter(Context context, List<Message> messageList, int currentUserId) {
    this.context = context;
    this.messageList = messageList;
    this.currentUserId = currentUserId;
    this.dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
  }

  @NonNull
  @Override
  public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
    return new MessageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    Message message = messageList.get(position);
    boolean isSentByMe = message.getSenderId() == currentUserId;

    holder.tvMessage.setText(message.getContent());
    holder.tvTime.setText(dateFormat.format(new Date(message.getTimestamp())));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();

    if (isSentByMe) {
      // 我发送的消息 - 靠右，蓝色背景
      params.gravity = Gravity.END;
      holder.messageContainer.setBackgroundResource(R.drawable.message_sent_background);
      holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.white));
    } else {
      // 对方发送的消息 - 靠左，灰色背景
      params.gravity = Gravity.START;
      holder.messageContainer.setBackgroundResource(R.drawable.message_received_background);
      holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.black));
    }

    holder.messageContainer.setLayoutParams(params);
  }

  @Override
  public int getItemCount() {
    return messageList.size();
  }

  public static class MessageViewHolder extends RecyclerView.ViewHolder {
    LinearLayout messageContainer;
    TextView tvMessage;
    TextView tvTime;

    public MessageViewHolder(@NonNull View itemView) {
      super(itemView);
      messageContainer = itemView.findViewById(R.id.message_container);
      tvMessage = itemView.findViewById(R.id.tv_message);
      tvTime = itemView.findViewById(R.id.tv_time);
    }
  }
}