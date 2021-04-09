package ru.hiddenproject.audioworld;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Book> books;
    private Context c;

    public RecyclerAdapter(Context context, List<Book> books) {
        this.books = books;
        this.inflater = LayoutInflater.from(context);
        this.c = context;
        setHasStableIds(true);
    }
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        final Book book = books.get(position);
        holder.bookName.setText(book.title);
        holder.bookAuthor.setText("Автор: "+book.author);
        holder.bookDesc.setText(book.info);
        Glide.with(this.c).load(book.img).into(holder.bookCover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c.getApplicationContext(),BookActivity.class);
                i.putExtra("bookID", book.id);
                c.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
    @Override
    public long getItemId(int position) {
        return books.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView bookCover;
        final TextView bookName, bookAuthor, bookDesc;
        ViewHolder(View view){
            super(view);
            bookCover = (ImageView)view.findViewById(R.id.bookCover);
            bookName = (TextView) view.findViewById(R.id.bookName);
            bookAuthor = (TextView) view.findViewById(R.id.bookAuthor);
            bookDesc = (TextView) view.findViewById(R.id.bookDesc);
        }
    }

}
