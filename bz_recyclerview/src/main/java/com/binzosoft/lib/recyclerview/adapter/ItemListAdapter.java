package com.binzosoft.lib.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.binzosoft.lib.recyclerview.R;
import com.binzosoft.lib.recyclerview.model.Item;

import java.util.List;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder> {

    private List<Item> itemList;
    private SelectionTracker selectionTracker;

    public ItemListAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public SelectionTracker getSelectionTracker() {
        return selectionTracker;
    }

    public void setSelectionTracker(SelectionTracker selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @NonNull
    @Override
    public ItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_list_item, parent, false);
        return new ItemListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.bind(item, selectionTracker.isSelected(item));

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ItemListViewHolder extends RecyclerView.ViewHolder implements ViewHolderWithDetails {
        TextView itemId, itemName, itemPrice;

        public ItemListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemId = itemView.findViewById(R.id.rvItemId);
            itemName = itemView.findViewById(R.id.rvItemName);
            itemPrice = itemView.findViewById(R.id.rvItemPrice);
        }

        public final void bind(Item item, boolean isActive) {
            itemView.setActivated(isActive);
            itemPrice.setText(item.getItemPrice() + "$");
            itemName.setText(item.getItemName());
            itemId.setText(item.getItemId() + "");

        }

        @Override
        public ItemDetailsLookup.ItemDetails getItemDetails() {
            return new MyItemDetail(getAdapterPosition(), itemList.get(getAdapterPosition()));
        }
    }
}
