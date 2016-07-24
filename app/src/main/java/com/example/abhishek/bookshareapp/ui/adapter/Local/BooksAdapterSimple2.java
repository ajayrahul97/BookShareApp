package com.example.abhishek.bookshareapp.ui.adapter.Local;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhishek.bookshareapp.R;
import com.example.abhishek.bookshareapp.api.NetworkingFactory;
import com.example.abhishek.bookshareapp.api.UsersAPI;
import com.example.abhishek.bookshareapp.api.models.LocalBooks.Book;
import com.example.abhishek.bookshareapp.api.models.Notification.Notifications;
import com.example.abhishek.bookshareapp.utils.Helper;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksAdapterSimple2 extends RecyclerView.Adapter<BooksAdapterSimple2.ViewHolder> {

    String id;
    private Context context;
    private List<Book> bookList;
    Book tempValues = null;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        public void onItemClick(Book book);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleBook;
        public TextView authorBook;
        public ImageView imageBook;
        public Button request;
        public RatingBar ratingBook;
        public TextView ratingCount;
        Context context;


        public ViewHolder(View v, Context context) {
            super(v);
            titleBook = (TextView) v.findViewById(R.id.row_books_title);
            authorBook = (TextView) v.findViewById(R.id.row_books_author);
            imageBook = (ImageView) v.findViewById(R.id.row_books_imageView);
            ratingBook = (RatingBar) v.findViewById(R.id.row_books_rating);
            request = (Button) v.findViewById(R.id.requestButton);
            ratingCount = (TextView) v.findViewById(R.id.row_books_ratings_count);
            this.context = context;
        }

    }

    public BooksAdapterSimple2(Context context, String id, List<Book> bookList, OnItemClickListener listener) {
        this.bookList = bookList;
        this.context = context;
        this.listener = listener;
        this.id=id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_books_simple2, parent, false);
        ViewHolder vh = new ViewHolder(v, context);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        tempValues = bookList.get(position);

        holder.titleBook.setText(tempValues.getTitle());
        holder.authorBook.setText(tempValues.getAuthor());
        if(!tempValues.getGrImgUrl().isEmpty()) {
            Picasso.with(this.context).load(tempValues.getGrImgUrl()).into(holder.imageBook);
        }
        holder.ratingBook.setRating(tempValues.getRating());
        holder.ratingCount.setText(tempValues.getRatingsCount() + " votes");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(bookList.get(position));
            }
        });

        holder.request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CharSequence[] items = { "Yes", "No"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Do you want to send a request?");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("Yes")){
                            String process = "request";
                            UsersAPI usersAPI = NetworkingFactory.getLocalInstance().getUsersAPI();
                            Call<Notifications> sendNotif = usersAPI.sendNotif(Helper.getUserId(),Helper.getUserName(), tempValues.getId(),tempValues.getTitle(),process,id,"request for");
                            sendNotif.enqueue(new Callback<Notifications>() {
                                @Override
                                public void onResponse(Call<Notifications> call, Response<Notifications> response) {
                                    Log.i("Email iD ", Helper.getUserEmail());
                                    if (response.body() != null) {
                                        Log.i("SendNotif", "Success");
                                        Log.d("SendNotif", Helper.getUserId()+" ID"+id);
                                        Toast.makeText(context, response.body().getDetail(), Toast.LENGTH_SHORT).show();
                                        Log.i("response", response.body().getDetail());
                                        holder.request.setEnabled(false);

                                    } else {
                                        Log.i("SendNotif", "Response Null");
                                        Toast.makeText(context, response.body().getDetail() , Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<Notifications> call, Throwable t) {
                                    Log.i("SendNotif","Failed!!");
                                    Toast.makeText(context, "Check your internet connection and try again!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        if (bookList != null)
            return bookList.size();

        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}
