package com.qou.h7se.sbr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by k0de9x on 10/11/2015.
 */


public class ListViewRollBack {

    public static ListViewRollBack instance = new ListViewRollBack();

    private LinkedList<Integer> position;
    private LinkedList<List<RestoreListViewItem>> data;

    public int clickedIndex = -1;

    public ListViewRollBack() {
        data = new LinkedList<>();
        position = new LinkedList<>();
    }

    public void push(List<RestoreListViewItem> data) {
        this.data.push(new ArrayList<>(data));
    }

    public void push(int position) {
        this.position.push(position);
    }

    public void push(List<RestoreListViewItem> data, int position) {
        this.data.push(new ArrayList<>(data));
        this.position.push(position);
    }

    public int popPosition() {
        return this.position.pop();
    }

    public List<RestoreListViewItem> popData() {
        return this.data.pop();
    }

    public List<RestoreListViewItem> getLast() {
        return this.data.getLast();
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public void clear() {
        this.data.clear();
        this.position.clear();
    }
}