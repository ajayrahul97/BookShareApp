package com.example.abhishek.bookshareapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhishek.bookshareapp.Listeners.EndlessScrollListener;
import com.example.abhishek.bookshareapp.R;
import com.example.abhishek.bookshareapp.api.NetworkingFactory;
import com.example.abhishek.bookshareapp.api.UsersAPI;
import com.example.abhishek.bookshareapp.api.models.LocalBooks.Book;
import com.example.abhishek.bookshareapp.api.models.LocalBooks.BookList;
import com.example.abhishek.bookshareapp.ui.adapter.Local.BooksAdapterSimple;
import com.example.abhishek.bookshareapp.ui.fragments.NotificationFragment;
import com.example.abhishek.bookshareapp.utils.Helper;
import com.example.abhishek.bookshareapp.utils.CommonUtilities;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, NotificationFragment.OnFragmentInteractionListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    List<Book> booksList;
    BooksAdapterSimple adapter;
    SharedPreferences prefs;
    SwipeRefreshLayout refreshLayout;
    SearchView searchView;
    Integer count = 1;
    ProgressDialog progress;
    String Resp;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FloatingActionButton button;
    RecyclerView localBooksList;
    Toolbar toolbar;
    int backCounter=0;
    ImageView _profilePicture;
    TextView no_book;
    String url;

    public String getResp() {
        return Resp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("Token", MODE_PRIVATE);

        Helper.setUserId(prefs.getString("id", prefs.getString("id", "")));
        Helper.setUserName(prefs.getString("first_name", null) + " " + prefs.getString("last_name", null));

        setContentView(R.layout.activity_main);

        button = (FloatingActionButton) findViewById(R.id.button);
        no_book=(TextView)findViewById(R.id.no_book);
        localBooksList = (RecyclerView) findViewById(R.id.localBooksList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        localBooksList.setLayoutManager(layoutManager);
        booksList = new ArrayList<>();
        adapter = new BooksAdapterSimple(this, booksList, new BooksAdapterSimple.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {

                if(isOnline()){
                    Intent intent = new Intent(MainActivity.this,BookDetailsActivity.class);
                    intent.putExtra("id", book.getId());
                    startActivity(intent);
                    Log.i(TAG, "onItemClick");}
                else {
                    Toast.makeText(getApplicationContext(),"Not connected to Internet", Toast.LENGTH_SHORT).show();
                }

            }
        });
        localBooksList.setAdapter(adapter);
        no_book.setVisibility(View.GONE);
        getLocalBooks("1");

        final EndlessScrollListener endlessScrollListener = new EndlessScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                getLocalBooks(String.valueOf(page + 1));
                Toast.makeText(getApplicationContext(), "Loading Page " + (page + 1), Toast.LENGTH_SHORT).show();
            }
        };

        localBooksList.addOnScrollListener(endlessScrollListener);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SearchResultsActivity.class);
                startActivity(i);
            }
        });

        navigationView = (NavigationView) findViewById(R.id.left_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView _name = (TextView) header.findViewById(R.id.nav_name);
        TextView _email = (TextView) header.findViewById(R.id.nav_email);
        ImageView _profilePicture = (ImageView) header.findViewById(R.id.nav_profile_picture);
        this._profilePicture = _profilePicture;
        String url = CommonUtilities.local_books_api_url+"image/"+Helper.getUserId()+"/";
        this.url = url;
        Picasso.with(this).load(url).memoryPolicy(MemoryPolicy.NO_CACHE).into(_profilePicture);

        SharedPreferences preferences = getSharedPreferences("Token", MODE_PRIVATE);

        if (_name != null) {
            _name.setText(preferences.getString("first_name", "") + " " + preferences.getString("last_name", ""));
        }

        if (_email != null) {
            _email.setText(Helper.getUserEmail());
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.White));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);

        toggle.syncState();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout ");
                endlessScrollListener.reset();
                getLocalBooks("1");
            }

        });
    }

    class ProgressLoader extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {

            for (; count <= params[0]; count++) {
                try {
                    Thread.sleep(1000);
                    Log.d("MAAs", getResp() + "+" + count.toString());
                    if (getResp() != null) {
                        break;
                    }
                    publishProgress(count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (getResp() != null) {
                    break;
                }
            }


            return "Task Completed.";
        }

        @Override
        protected void onPostExecute(String result) {
            if (getResp() == null) {
                Toast.makeText(MainActivity.this, "Please Try Again.", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            } else {
                progress.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Turning To Page 394...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setIndeterminateDrawable(getResources().getDrawable(R.drawable.loading));
            progress.setMax(5);
            progress.setProgress(0);
            progress.setCancelable(false);
            progress.show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress.setProgress(values[0]);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        UsersAPI api = NetworkingFactory.getLocalInstance().getUsersAPI();
        Call<List<Book>> call = api.search(query);
        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.body() != null) {
                    Log.d("Search Response:", response.toString());
                    List<Book> localBooksList = response.body();
                    booksList.clear();
                    booksList.addAll(localBooksList);
                    adapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);

                    if(response.body()!=null){
                        Toast.makeText(getApplicationContext(), "404 : Book Not Found, Better Buy It !", Toast.LENGTH_SHORT).show();
                        no_book.setVisibility(View.VISIBLE);
                    }else {
                        no_book.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Check your internet connectivity and try again!", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);

            }
        });
        searchView.clearFocus();
        return true;
    }

    /* When an item in the toolbar is clicked, the following
     * method is called.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem notif_item = menu.findItem(R.id.menu_notifs);

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                notif_item.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                notif_item.setVisible(true);
                no_book.setVisibility(View.GONE);
                getLocalBooks("1");
                return true;
            }
        });
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        notif_item.setIcon(R.drawable.notification);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_notifs) {
            item.setIcon(R.drawable.notification);
            Helper.setOld_total(Helper.getNew_total());
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.closeDrawer(GravityCompat.START);
                drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mybooks) {
            Intent i = new Intent(this, MyBooks.class);
            startActivity(i);

        } else if (id == R.id.nav_myprofile) {
            Intent i = new Intent(this, MyProfile.class);
            i.putExtra("id", prefs.getString("id", prefs.getString("id", "")));
            startActivity(i);

        } else if (id == R.id.nav_logout) {
            SharedPreferences prefs = getSharedPreferences("Token", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_share) {

            PackageManager pm = getPackageManager();
            try {

                Intent waIntent = new Intent(Intent.ACTION_SEND);
                waIntent.setType("text/plain");
                String text = "BookShare App !! .You can download the app from here...!";

                PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                //Check if package exists or not. If not then code
                //in catch block will be called
                waIntent.setPackage("com.whatsapp");

                waIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(waIntent, "Share with"));

            } catch (PackageManager.NameNotFoundException e) {
                //who cares
            }

        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public void getLocalBooks(final String page) {
        new ProgressLoader().execute(15);
        UsersAPI api = NetworkingFactory.getLocalInstance().getUsersAPI();
        Call<BookList> call = api.getBList(page);
        call.enqueue(new Callback<BookList>() {
            @Override
            public void onResponse(Call<BookList> call, Response<BookList> response) {
                if (response.body() != null) {
                    Log.d("Search Response:", response.toString());
                    Resp = response.toString();
                    List<Book> localBooksList = response.body().getResults();
                    if (page.equals("1")) {
                        booksList.clear();
                        adapter.notifyDataSetChanged();
                    }
                    booksList.addAll(localBooksList);
                    adapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onFailure(Call<BookList> call, Throwable t) {
                Log.d("searchresp", "searchOnFail " + t.toString());
                refreshLayout.setRefreshing(false);

            }
        });

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {

        if (backCounter >= 1) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Toast.makeText(this, "Ciao Buddy !", Toast.LENGTH_SHORT).show();
            startActivity(intent);

        } else {
            Toast.makeText(this, "Press  again to exit.", Toast.LENGTH_SHORT).show();
            backCounter++;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Helper.imageChanged){
            Picasso.with(this).load(url).into(_profilePicture);
            Helper.imageChanged = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}