package com.chuck.android.popularmovies;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chuck.android.popularmovies.adapter.MovieAdapter;
import com.chuck.android.popularmovies.models.Movie;
import com.chuck.android.popularmovies.models.MovieList;
import com.chuck.android.popularmovies.rest.MovieApi;
import com.chuck.android.popularmovies.rest.MovieInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    //used in error logs
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    StaggeredGridLayoutManager movieGridLayoutManger;
    public static final String BUNDLE_RV_POSITION = "Bundle_RV_POS_KEY";
    public static final String BUNDLE_RV_LISTTYPE = "Bundle_RV_LT_KEY";
    private String selectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView = findViewById(R.id.movieDbList);
        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RV_POSITION);
            selectedList = savedInstanceState.getString(BUNDLE_RV_LISTTYPE);
            movieListSelect(selectedList,savedRecyclerLayoutState);
        }
        //Use Grid Layout manager to display Posters in Grids
        else
            movieListSelect("Popular Movies",null);

    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RV_POSITION,recyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString(BUNDLE_RV_LISTTYPE,selectedList);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_popular) {
            selectedList = "Popular Movies";
        }
        else if (id == R.id.action_topRated) {
            selectedList = "Top Rated Movies";
        }
        else if (id == R.id.action_favorites) {
            selectedList = "Popular Movies";
        }
        movieListSelect(selectedList,null);
        return super.onOptionsItemSelected(item);
    }

    public void movieListSelect(String movieList, final Parcelable state){
        //Defines the Parameters for the API call to movieDB
        MovieInterface movieService = MovieApi.getClient().create(MovieInterface.class);
        //Store API Key
        String apiKey = getString(R.string.API_key);
        //Reviewer: Please Enter API Key in secrets.xml for moviedb
        if (apiKey.equals(""))
            Toast.makeText(this, "Please Enter API Key in Secrets.xml", Toast.LENGTH_SHORT).show();
            //Only Run if API key is available
        else {
            //Choose a retrofit call based on sort option
            Call<MovieList> call;
            if ("Popular Movies".equals(movieList))
                call = movieService.getPopularMovies(apiKey);
            else if ("Top Rated Movies".equals(movieList))
                call = movieService.getTopRatedMovies(apiKey);
            else
                call = movieService.getTopRatedMovies(apiKey);

            //Send the network call using Retrofit to background task
            call.enqueue(new Callback<MovieList>() {
                @Override
                public void onResponse(Call<MovieList> call, Response<MovieList> response) {
                    //If Successful Network Call to API bind the Recycleview to MovieAdaptor
                    List<Movie> movies = response.body().getResults();
                    //Make a new RecycleView instance
                    movieGridLayoutManger = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(movieGridLayoutManger);
                    adapter = new MovieAdapter(movies, R.layout.movie_list_item, getApplicationContext());
                    if (state != null)
                        recyclerView.getLayoutManager().onRestoreInstanceState(state);

                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onFailure(Call<MovieList> call, Throwable t) {
                    //If not log the Error
                    Log.e(TAG, t.toString());
                }
            });
        }
    }
}