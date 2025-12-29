package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.ChatActivity;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.main_fragments.FriendFragment;
import fcu.app.appclassfinalproject.model.User;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

  private Context context;
  private List<User> friendList;
  private FriendFragment friendFragment;

  public FriendAdapter(Context context, List<User> friendList) {
    this.context = context;
    this.friendList = friendList;
  }

  public void setFriendFragment(FriendFragment fragment) {
    this.friendFragment = fragment;
  }

  @NonNull
  @Override
  public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
    return new FriendViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
    User friend = friendList.get(position);

    holder.tvFriendName.setText(friend.getAccount());
    holder.tvFriendEmail.setText(friend.getEmail());

    // 点击好友项进入聊天
    holder.itemView.setOnClickListener(v -> {
      Intent intent = new Intent(context, ChatActivity.class);
      intent.putExtra("friend_id", friend.getID());
      intent.putExtra("friend_name", friend.getAccount());
      context.startActivity(intent);
    });

    // 删除好友按钮
    holder.btnDelete.setOnClickListener(v -> {
      if (friendFragment != null) {
        friendFragment.removeFriend(friend, position);
      }
    });
  }

  @Override
  public int getItemCount() {
    return friendList.size();
  }

  public static class FriendViewHolder extends RecyclerView.ViewHolder {
    TextView tvFriendName;
    TextView tvFriendEmail;
    ImageButton btnDelete;

    public FriendViewHolder(@NonNull View itemView) {
      super(itemView);
      tvFriendName = itemView.findViewById(R.id.tv_friend_name);
      tvFriendEmail = itemView.findViewById(R.id.tv_friend_email);
      btnDelete = itemView.findViewById(R.id.btn_delete_friend);
    }
  }
}