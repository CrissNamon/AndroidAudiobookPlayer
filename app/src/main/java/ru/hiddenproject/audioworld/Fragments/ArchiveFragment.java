package ru.hiddenproject.audioworld;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static ru.hiddenproject.audioworld.Helper.API_VERSION;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArchiveFragment extends Fragment {

    private LinearLayout loading;
    private RecyclerView recyclerView;
    private Helper helper;
    public ArchiveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        loading = (LinearLayout)view.findViewById(R.id.loading);
        helper = new Helper();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://185.125.217.104")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        API api = retrofit.create(API.class);
        String books = helper.getArchive(getActivity());
        api.getUserBooks(API_VERSION,"book.getuserbooks", books).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                // создаем адаптер

                Log.d("RESPONSE",response.toString());
                RecyclerAdapter adapter = new RecyclerAdapter(getActivity(), response.body());
                // устанавливаем для списка адаптер
                loading.setVisibility(View.GONE);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.d("RESPONSE",call.request().toString());
                helper.makeAlert(getActivity(), "Ошибка", "Произошла ошибка\nСообщите нам информацию ниже\n"+t.getMessage(), "Ок");
            }
        });
        return view;

    }

}
