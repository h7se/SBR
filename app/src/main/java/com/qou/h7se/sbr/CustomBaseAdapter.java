package com.qou.h7se.sbr;

import android.view.View;
import android.widget.BaseAdapter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by k0de9x on 9/22/2015.
 */

abstract class CustomBaseAdapter<E> extends BaseAdapter {

    abstract class ViewHolder {
        View itemView;
    }

    static abstract class DataFilterWithState<T> {
        boolean include(T item) {
            return false;
        }

        DataFilterWithState<T> getSelf() {
            return this;
        }

        boolean enabled() {
            if(bindTo != null) {
                return bindTo.enabled();
            }
            return false;
        }

        PredicateIsEnabledEx bindTo = null;
    }

    private List<E> data;
    private Comparator<E> comparator = null;
    public Map<String, DataFilterWithState<E>> dataFilters;
    private final Queue<E> trash;

    public abstract ViewHolder getHolder();

    CustomBaseAdapter(List<E> data) {
        this.dataFilters = new HashMap<>();

        this.data = new ArrayList<>(data);
        this.trash = new ArrayDeque<>();
    }

    public void addItem(E entry) {
        this.data.add(entry);
    }

    public void addAll(List<E> entries) {
        this.data.addAll(entries);
    }

    public void removeItemWithAnimation(E entry) {
        trash.add(entry);
        notifyDataSetChanged();
    }

    public void removeItem(E entry, boolean notify) {
        boolean trashed = trash.contains(entry);
        if(trashed) {
            trash.remove(entry);
        }

        this.data.remove(entry);

        if (notify || trashed) {
            notifyDataSetChanged();
        }
    }

    public int indexOf(E entry) {
       return this.data.indexOf(entry);
    }

    public void setData(List<E> data, boolean notify) {
        this.data = data;

        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public E getItem(int position) {
        return getFilteredData(this.data).get(position);
    }

    @Override
    public int getCount() {
        return getFilteredData(this.data).size();
    }

    public List<E> getData() {
        return getFilteredData(this.data);
    }

    public List<E> getOriginalData() {
        return this.data;
    }

    List<E> getFilteredData(List<E> data__o) {
        ArrayList<E> tmp = new ArrayList<>();

        for(E item : data__o) {
            if(checkFilterAgainstItem(item)) {
                if(!tmp.contains(item)) {
                    tmp.add(item);
                }
            }
        }

        if(getComparator() != null) {
            Collections.sort(tmp, getComparator());
        }
        return tmp;
   }

    public Queue<E> getTrash() {
        return this.trash;
    }

    boolean checkFilterAgainstItem(E item) {
        boolean success = true;
        if (this.dataFilters != null && this.dataFilters.size() > 0) {
            boolean pass = true;
            for (DataFilterWithState<E> entry : this.dataFilters.values()) {
                if (entry.enabled()) {
                    if (!(entry.include(item))) {
                        pass = false; break;
                    }
                }
            }
            success = pass;
        }

        return success;
    }

    public void clearData(boolean notify) {
        this.data.clear();

        if (notify) {
            notifyDataSetChanged();
        }
    }

    public Comparator<E> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private String tag;

    public boolean hasItemsMatchPredicate(DataFilterCallback<E> callback) {
        return (getItemsMatchingPredicate(callback, null, null, null, true).size() > 0);
    }

    public int getItemsCountMatchingPredicate(DataFilterCallback<E> callback) {
        return this.getItemsCountMatchingPredicate(callback, null, null, null);
    }

    public int getItemsCountMatchingPredicate(DataFilterCallback<E> callback, DataFilterActionCallback<E> onSuccess, DataFilterActionCallback<E> onFail, DataFilterCompleteCallback<E> onComplete) {
        return getItemsMatchingPredicate(callback, onSuccess, onFail, onComplete, false).size();
    }

    public List<E> getItemsMatchingPredicate(DataFilterCallback<E> callback) {
        return this.getItemsMatchingPredicate(callback, null, null, null, false);
    }

    public List<E> getItemsMatchingPredicate(DataFilterCallback<E> callback, DataFilterActionCallback<E> onSuccess, DataFilterActionCallback<E> onFail, DataFilterCompleteCallback<E> onComplete) {
        return getItemsMatchingPredicate(callback, onSuccess, onFail, onComplete, false);
    }

    public List<E> getItemsMatchingPredicate(DataFilterCallback<E> callback, DataFilterActionCallback<E> onSuccess, DataFilterActionCallback<E> onFail, DataFilterCompleteCallback<E> onComplete, boolean oneMatchMode) {
        List<E> tmp = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        List<E> entries = new ArrayList<>(getFilteredData(this.data)); // avoid java.util.ConcurrentModificationException if modifying data using onSucess

       for(int i=0, size = entries.size(); i < size; i++) {
           E o = entries.get(i);
            if (callback.include(o)) {
                tmp.add(o);
                positions.add(i);
                if (onSuccess != null) {
                    onSuccess.run(o, i);
                }
                if (oneMatchMode) {
                    break;
                }
            } else {
                if (onFail != null) {
                    onFail.run(o, i);
                }
            }
        }
        if (onComplete != null) {
            onComplete.run(positions, tmp);
        }
        return tmp;
    }
}

