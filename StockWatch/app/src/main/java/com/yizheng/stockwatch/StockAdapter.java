package com.yizheng.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private ArrayList<Stock> sList;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> sList, MainActivity mainActivity) {
        this.sList = sList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_item, parent, false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock s = sList.get(position);

        holder.symbolText.setText(s.getStockSymbol());
        holder.priceText.setText(new Double(s.getPrice()).toString());
        holder.nameText.setText(s.getCompanyName());

        Double priceChange = s.getPriceChange();
        if (priceChange > 0) {
            //holder.changeText.setText("▲ " + priceChange.toString() + " (" + new Double(s.getChangePercentage()).toString() + "%)");
            holder.changeText.setText(String.format("▲ %.2f (%.2f%%)",priceChange,new Double(s.getChangePercentage())));
            holder.setColor(Color.GREEN);
        }
        else if (priceChange < 0){
            //holder.changeText.setText("▼ " + priceChange.toString() + " (" + new Double(s.getChangePercentage()).toString() + "%)");
            holder.changeText.setText(String.format("▼ %.2f (%.2f%%)",priceChange,new Double(s.getChangePercentage())));
            holder.setColor(Color.RED);
        }
        else{
            //holder.changeText.setText(priceChange.toString() + " (" + new Double(s.getChangePercentage()).toString() + "%)");
            holder.changeText.setText(String.format("%.2f (%.2f%%)",priceChange,new Double(s.getChangePercentage())));
            holder.setColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return sList.size();
    }
}
