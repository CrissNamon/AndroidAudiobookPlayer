package ru.hiddenproject.audioworld;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static ru.hiddenproject.audioworld.Helper.API_VERSION;

public class AuthActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button login;

    private Retrofit retrofit;
    private API api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.pass);
        login = (Button)findViewById(R.id.login);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://185.125.217.104")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(API.class);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().length()>1 && email.getText().toString().length()>6){
                    api.login(API_VERSION, "user.login", email.getText().toString(), password.getText().toString()).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {

                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {

                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Неверно введены даныне", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
