package com.example.abhishek.bookshareapp.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.abhishek.bookshareapp.R;
import com.example.abhishek.bookshareapp.api.NetworkingFactory;
import com.example.abhishek.bookshareapp.api.UsersAPI;
import com.example.abhishek.bookshareapp.api.models.Notification.Notifications;
import com.example.abhishek.bookshareapp.ui.adapter.NotificationsAdapter;
import com.example.abhishek.bookshareapp.utils.CommonUtilities;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity{

    RecyclerView notificationsListView;
    NotificationsAdapter adapter;
    List<Notifications> notificationsList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        notificationsListView = (RecyclerView) findViewById(R.id.notifications_list);
        notificationsListView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationsAdapter(this, notificationsList);
        notificationsListView.setAdapter(adapter);

        getNotifications();
    }

    public void getNotifications() {
        UsersAPI usersAPI = NetworkingFactory.getLocalInstance().getUsersAPI();
        Call<List<Notifications>> call = usersAPI.getNotifs(CommonUtilities.getUserId(this));
        call.enqueue(new Callback<List<Notifications>>() {
            @Override
            public void onResponse(Call<List<Notifications>> call, Response<List<Notifications>> response) {
                if (response.body() != null) {
                    List<Notifications> notifList = response.body();
                    notificationsList.clear();
                    notificationsList.addAll(notifList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Notifications>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Check your internet connection and try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
