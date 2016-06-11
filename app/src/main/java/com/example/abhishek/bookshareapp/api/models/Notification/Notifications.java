package com.example.abhishek.bookshareapp.api.models.Notification;

import com.google.gson.annotations.SerializedName;

public class Notifications {

    String id;

    @SerializedName("sender_id")
    String senderId;

    @SerializedName("sender_name")
    String senderName;

    @SerializedName("book_id")
    String bookId;

    @SerializedName("book_title")
    String bookTitle;

    String message;

    @SerializedName("is_confirmed")
    String isConfirmed;

    @SerializedName("is_cancelled")
    String isCancelled;

    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getId() {
        return id;
    }

    public String getIsCancelled() {
        return isCancelled;
    }

    public String getIsConfirmed() {
        return isConfirmed;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }
}
