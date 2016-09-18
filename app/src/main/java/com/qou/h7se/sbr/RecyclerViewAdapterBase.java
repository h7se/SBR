package com.qou.h7se.sbr;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by k0de9x on 10/2/2015.
 */

public abstract class RecyclerViewAdapterBase<E,T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    private List<E> data;

    RecyclerViewAdapterBase(ArrayList<E> data) {
        this.data = data; // new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public List<E> getItems() {
        return this.data;
    }

    public void addItem(E item) {
        this.data.add(item);
    }

    public E getItem(int i) {
        return this.data.get(i);
    }

    public void removeItem(E item) {
        this.data.remove(item);
    }

    public int getItemsCountMatchingPredicate(DataFilterCallback<E> callback) {
        return this.getItemsCountMatchingPredicate(callback, null, null, null);
    }

    public int getItemsCountMatchingPredicate(DataFilterCallback<E> callback, DataFilterActionCallback<E> onSuccess, DataFilterActionCallback<E> onFail, DataFilterCompleteCallback<E> onComplete) {
        return getItemsMatchingPredicate(callback, onSuccess, onFail, onComplete).size();
    }

    public ArrayList<E> getItemsMatchingPredicate(DataFilterCallback<E> callback) {
        return this.getItemsMatchingPredicate(callback, null, null, null);
    }

    public ArrayList<E> getItemsMatchingPredicate(DataFilterCallback<E> callback, DataFilterActionCallback<E> onSuccess, DataFilterActionCallback<E> onFail, DataFilterCompleteCallback<E> onComplete) {
        ArrayList<E> tmp = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();

        ArrayList<E> entries = new ArrayList<>(this.data); // avoid java.util.ConcurrentModificationException if modifying data using onSucess
        for(int i =0, size = entries.size(); i < size; i++) {
            E o = entries.get(i);
            if(callback.include(o)) {
                tmp.add(o);
                positions.add(i);
                if(onSuccess != null) {
                    onSuccess.run(o, i);
                }
            } else {
                if(onFail != null) {
                    onFail.run(o, i);
                }
            }
        }
        if(onComplete != null) {
            onComplete.run(positions, tmp);
        }
        return tmp;
    }
}
