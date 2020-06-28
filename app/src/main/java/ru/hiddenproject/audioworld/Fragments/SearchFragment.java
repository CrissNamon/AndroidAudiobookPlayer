package ru.hiddenproject.audioworld.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.hiddenproject.audioworld.API;
import ru.hiddenproject.audioworld.Book;
import ru.hiddenproject.audioworld.R;
import ru.hiddenproject.audioworld.RecyclerAdapter;

import static ru.hiddenproject.audioworld.Helper.API_VERSION;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private API api;
    private Retrofit retrofit;
    private SearchView searchView;
    private List<Book> books = new ArrayList<Book>();
    private RecyclerAdapter adapter;
    private boolean flag = false;
    private Call<List<Book>> call;
    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        retrofit = new Retrofit.Builder()
                .baseUrl("http://185.125.217.104")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(API.class);
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onPrepareOptionsMenu(menu);
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Поиск...");
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        adapter = new RecyclerAdapter(getActivity(), books);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Call<List<Book>> searchCallback = api.searchBook(API_VERSION, "book.search", s);
                if(searchCallback.isExecuted())searchCallback.cancel();
                searchCallback.enqueue(new Callback<List<Book>>() {
                    @Override
                    public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                        doSearch(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<Book>> call, Throwable t) {
                        Log.d("SEARCH", "FAILURE"+t.getMessage());
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Call<List<Book>> searchCallback = api.searchBook(API_VERSION,"book.search", s);
                if(searchCallback.isExecuted())searchCallback.cancel();
                searchCallback.enqueue(new Callback<List<Book>>() {
                    @Override
                    public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                        doSearch(response.body());
                    }

                    @Override
                    public void onFailure(Call<List<Book>> call, Throwable t) {
                        Log.d("SEARCH", "FAILURE "+t.getMessage());
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
        });

    }
    public void doSearch(List<Book> result){
        Log.d("SEARCH", "DO");
        books.clear();
        books.addAll(result);
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
