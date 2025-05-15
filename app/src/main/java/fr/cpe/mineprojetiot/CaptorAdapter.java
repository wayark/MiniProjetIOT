package fr.cpe.mineprojetiot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CaptorAdapter extends RecyclerView.Adapter<CaptorAdapter.CaptorViewHolder> {

    private final List<String> captors;

    public CaptorAdapter(List<String> captors) {
        this.captors = captors;
    }

    public static class CaptorViewHolder extends RecyclerView.ViewHolder {
        TextView captorLabel;

        public CaptorViewHolder(View itemView) {
            super(itemView);
            captorLabel = itemView.findViewById(R.id.captorLabel);
        }
    }

    @NonNull
    @Override
    public CaptorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_captor, parent, false);
        return new CaptorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CaptorViewHolder holder, int position) {
        holder.captorLabel.setText(captors.get(position));
    }

    @Override
    public int getItemCount() {
        return captors.size();
    }

    public void moveItem(int fromPosition, int toPosition) {
        String item = captors.remove(fromPosition);
        captors.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<String> getCaptorOrder() {
        return captors;
    }
}

