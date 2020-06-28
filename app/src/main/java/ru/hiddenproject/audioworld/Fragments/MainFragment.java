package ru.hiddenproject.audioworld.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.hiddenproject.audioworld.API;
import ru.hiddenproject.audioworld.Book;
import ru.hiddenproject.audioworld.Helper;
import ru.hiddenproject.audioworld.MainActivity;
import ru.hiddenproject.audioworld.R;
import ru.hiddenproject.audioworld.RecyclerAdapter;

import static ru.hiddenproject.audioworld.Helper.API_VERSION;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private API api;
    private List<Book> books;
    RecyclerAdapter adapter;
    private int current_page = 1;
    private LinearLayout loading;
    private Helper helper;
    private Call<List<Book>> call;
    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        helper = new Helper();
        loading = (LinearLayout)view.findViewById(R.id.loading);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://185.125.217.104")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(API.class);
        call = api.getBooks(API_VERSION, "book.getall",current_page);
        get();
        return view;

    }
    public void get(){
        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                books = response.body();
                if(books.size()>0) {
                    recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(linearLayoutManager);
                    adapter = new RecyclerAdapter(getActivity(), books);
                    loading.setVisibility(View.GONE);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setItemViewCacheSize(0);
                    recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
                        @Override
                        public void onLoadMore() {
                            current_page++;
                            addDataToList(current_page);
                        }
                    });
                }else{
                    helper.makeAlert(getActivity(), "Ошибка", "Произошла ошибка\nСообщите нам информацию ниже\n"+response.message(), "Ок").show();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.d("RESPONSE",t.getMessage());
                if(t.getMessage().equals("timeout")){
                   get();
                }
                //helper.makeAlert(getActivity(), "Ошибка", "Произошла ошибка\nСообщите нам информацию ниже\n"+t.getMessage(), "Ок");
            }
        });
    }
    public void addDataToList(final int page){
        api.getBooks(API_VERSION, "book.getall",page).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                books.addAll(response.body());
                recyclerView.getAdapter().notifyDataSetChanged();
                Log.d("FRAGMENT", "LOAD MORE: "+String.valueOf(page));
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
               //Log.d("RESPONSE",t.getMessage());
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onPause(){
        super.onPause();

    }
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

    }
}
