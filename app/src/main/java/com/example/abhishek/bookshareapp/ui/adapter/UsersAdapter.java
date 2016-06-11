package com.example.abhishek.bookshareapp.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhishek.bookshareapp.R;
import com.example.abhishek.bookshareapp.api.NetworkingFactory;
import com.example.abhishek.bookshareapp.api.UsersAPI;
import com.example.abhishek.bookshareapp.api.models.UserInfo;
import com.example.abhishek.bookshareapp.api.models.VerifyToken.Detail;
import com.example.abhishek.bookshareapp.ui.UserProfile;
import com.example.abhishek.bookshareapp.utils.CommonUtilities;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context context;
    private List<UserInfo> userList;
    UserInfo tempValues = null;
    private final OnItemClickListener listener;
    String bookId, bookTitle;

    public interface OnItemClickListener {
        void onItemClick(UserInfo userInfo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameUser;
        public TextView emailUser;
        public TextView hostelUser;
        public Button requestButton;
        Context context;

        public ViewHolder(View v, Context context) {
            super(v);
            nameUser = (TextView) v.findViewById(R.id.row_user_name);
            emailUser = (TextView) v.findViewById(R.id.row_user_email);
            hostelUser = (TextView) v.findViewById(R.id.row_user_hostel);
            requestButton = (Button) v.findViewById(R.id.requestButton);
            this.context = context;
        }
    }

    public UsersAdapter(Context context, List<UserInfo> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.context = context;
        Log.d("UsersAdapter", "Constructor");
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_users, parent, false);
        ViewHolder vh = new ViewHolder(v, context);
        return vh;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String id;

        tempValues = userList.get(position);
        id = tempValues.getId();
        holder.nameUser.setText(tempValues.getName());
        holder.emailUser.setText(tempValues.getEmail());
        holder.hostelUser.setText(tempValues.getHostel());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(userList.get(position));
                Intent i = new Intent(context, UserProfile.class);
                i.putExtra("id", id);
                context.startActivity(i);
            }
        });
        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBook(tempValues);
            }
        });

    }

    public void requestBook(UserInfo info) {
        UsersAPI usersAPI = NetworkingFactory.getLocalInstance().getUsersAPI();
        Call<Detail> call = usersAPI.requestBook(CommonUtilities.getUserId(context),
                CommonUtilities.getUserName(context),
                bookId,
                bookTitle,
                "request",
                info.getId());
        Log.i("harshit", CommonUtilities.getUserId(context));
        Log.i("harshit", CommonUtilities.getUserName(context));
        Log.i("harshit", bookId);
        Log.i("harshit", bookTitle);
        Log.i("harshit", info.getId());

        call.enqueue(new Callback<Detail>() {
            @Override
            public void onResponse(Call<Detail> call, Response<Detail> response) {
                if(response.body() != null) {
                    if(response.body().getDetail().equals("Request sent")) {
                        Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "Unable to send request", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Detail> call, Throwable t) {
                Toast.makeText(context, "Check your internet connection and try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (userList != null)
            return userList.size();

        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
