package fcu.app.appclassfinalproject;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.adapter.MessageAdapter;
import fcu.app.appclassfinalproject.helper.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Message;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

  private static final String TAG = "ChatActivity";

  private RecyclerView recyclerView;
  private MessageAdapter adapter;
  private List<Message> messageList;
  private EditText etMessage;
  private ImageButton btnSend;
  private TextView tvFriendName;

  private String currentUserUid;
  private int currentUserId;
  private int friendId;
  private String friendName;
  private SqlDataBaseHelper dbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    // 获取传递的好友信息
    friendId = getIntent().getIntExtra("friend_id", -1);
    friendName = getIntent().getStringExtra("friend_name");

    if (friendId == -1) {
      Toast.makeText(this, "無法獲取好友訊息", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // 获取当前用户信息
    SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    currentUserUid = prefs.getString("uid", "");

    if (currentUserUid.isEmpty()) {
      Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    dbHelper = new SqlDataBaseHelper(this);
    currentUserId = getCurrentUserId();

    if (currentUserId == -1) {
      Toast.makeText(this, "無法取得user訊息", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    initViews();
    loadMessages();
    setupSendButton();
  }

  private void initViews() {
    tvFriendName = findViewById(R.id.tv_friend_name);
    recyclerView = findViewById(R.id.rcy_messages);
    etMessage = findViewById(R.id.et_message);
    btnSend = findViewById(R.id.btn_send);
    ImageButton btnBack = findViewById(R.id.btn_back);
    LinearLayout toolbar = findViewById(R.id.toolbar);

    tvFriendName.setText(friendName);

    if (btnBack != null) {
      btnBack.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d(TAG, "返回鍵被按下");
          finish();
        }
      });
    }

    if (toolbar != null) {
      toolbar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d(TAG, "工具被點擊");
          finish();
        }
      });
    }

    messageList = new ArrayList<>();
    adapter = new MessageAdapter(this, messageList, currentUserId);

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
  }

  private int getCurrentUserId() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery("SELECT id FROM Users WHERE firebase_uid = ?",
        new String[]{currentUserUid});

    int userId = -1;
    if (cursor.moveToFirst()) {
      userId = cursor.getInt(0);
    }
    cursor.close();
    db.close();
    return userId;
  }

  private void loadMessages() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String query = "SELECT * FROM Messages " +
        "WHERE (sender_id = ? AND receiver_id = ?) " +
        "OR (sender_id = ? AND receiver_id = ?) " +
        "ORDER BY timestamp ASC";

    Cursor cursor = db.rawQuery(query, new String[]{
        String.valueOf(currentUserId), String.valueOf(friendId),
        String.valueOf(friendId), String.valueOf(currentUserId)
    });

    messageList.clear();

    if (cursor.moveToFirst()) {
      do {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        int senderId = cursor.getInt(cursor.getColumnIndexOrThrow("sender_id"));
        int receiverId = cursor.getInt(cursor.getColumnIndexOrThrow("receiver_id"));
        String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));

        Message message = new Message(id, senderId, receiverId, content, timestamp);
        messageList.add(message);
      } while (cursor.moveToNext());
    }

    cursor.close();
    db.close();

    adapter.notifyDataSetChanged();

    // 滚动到最新消息
    if (!messageList.isEmpty()) {
      recyclerView.scrollToPosition(messageList.size() - 1);
    }
  }

  private void setupSendButton() {
    btnSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String content = etMessage.getText().toString().trim();

        if (content.isEmpty()) {
          Toast.makeText(ChatActivity.this, "請輸入訊息", Toast.LENGTH_SHORT).show();
          return;
        }

        sendMessage(content);
      }
    });
  }

  private void sendMessage(String content) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    long timestamp = System.currentTimeMillis();

    try {
      db.execSQL("INSERT INTO Messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?)",
          new Object[]{currentUserId, friendId, content, timestamp});

      Log.d(TAG, "訊息發送成功");

      // 清空輸入框
      etMessage.setText("");

      // 重新載入消息
      loadMessages();

    } catch (Exception e) {
      Log.e(TAG, "訊息發送失敗: " + e.getMessage(), e);
      Toast.makeText(this, "訊息發送失敗", Toast.LENGTH_SHORT).show();
    } finally {
      db.close();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    // 每次返回頁面時刷新消息
    loadMessages();
  }
}