package id.yongki.jonastrackingsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.yongki.jonastrackingsystem.R;
import id.yongki.jonastrackingsystem.model.UserModel;

// source https://www.youtube.com/watch?v=69C1ljfDvl0
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context mContext;
    private OnItemListener mOnItemListener;
    private List<UserModel> albumList;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nama, jabatan, status,no;
        CardView cardView;
        OnItemListener onItemListener;

        public MyViewHolder(View v, OnItemListener onItemListener) {
            super(v);
            nama = (TextView) v.findViewById(R.id.rv_labelnama);
            jabatan = (TextView) v.findViewById(R.id.rv_labeljabatan);
            status = (TextView) v.findViewById(R.id.rv_status);
            cardView = (CardView) v.findViewById(R.id.rc_cardview);
            no = (TextView)v.findViewById(R.id.rc_label_no);
            this.onItemListener = onItemListener;
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition());
        }
    }

    public RecyclerAdapter(Context mContext, List<UserModel> albumList, OnItemListener onItemListener) {
        this.mContext = mContext;
        this.albumList = albumList;
        this.mOnItemListener = onItemListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_layout, parent, false);
        return new MyViewHolder(itemView, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final UserModel album = albumList.get(position);
        holder.nama.setText(album.nama);
        holder.jabatan.setText(album.jabatan);
        holder.status.setText(album.status);
        holder.no.setText(String.valueOf(position+1));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }
}
