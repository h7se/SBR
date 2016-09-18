package com.qou.h7se.sbr;

import android.os.AsyncTask;
import android.view.View;

import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by k0de9x on 10/2/2015.
 */
public class TreeNode {

    public final static Predicate<TreeNode>  TRUE = new Predicate<TreeNode>() {
        @Override
        public boolean evaluate(TreeNode object) {
            return true;
        }
    };

    public final static Predicate<TreeNode>  FALSE = new Predicate<TreeNode>() {
        @Override
        public boolean evaluate(TreeNode object) {
            return false;
        }
    };

    public final static Predicate<TreeNode> SMART = new Predicate<TreeNode>() {
        @Override
        public boolean evaluate(TreeNode object) {
            return !(object.hasChildes() && !(object.getExpanded()));
        }
    };

    public Predicate<TreeNode> getNoCollapseOnClick() {
        return noCollapseOnClick;
    }

    public TreeNode setNoCollapseOnClick(Predicate<TreeNode> predicate) {
       this.noCollapseOnClick = predicate;
        return this;
    }

    public TreeNode(TreeNode parent, String title) {
        this.title = title;
        this.parent = parent;
        this.childes = new ArrayList<>();

        this.checked = false;
        this.expanded = true;

        this.checkable = false;
        this.collapsible = true;
        this.noCollapseOnClick = FALSE;

        this.checkMarginVisible = false;
        this.expandMarginVisible = true;
        this.indent = true;
        // this.toggleCheckOnTitleClick = false;

        this.onClickListener = null;

        this.properties = new HashMap<>();

        this.task = null;
    }

    public String title;
    public TreeNode parent;
    public ArrayList<TreeNode> childes;

    private boolean checked;
    private boolean expanded;

    boolean checkable;
    boolean collapsible;
    private Predicate<TreeNode> noCollapseOnClick;
    boolean checkMarginVisible;
    boolean expandMarginVisible;
    boolean indent ;
    boolean toggleCheckOnTitleClick ;

    private View.OnClickListener onClickListener;
    private View.OnClickListener onActionImageClickListener;

    private Map<String, Object> properties;

    private AsyncTask<?, ?, ?> task;

    public TreeNode add(String title) {
        TreeNode n = new TreeNode(this, title);
        this.childes.add(n);
        return n;
    }

    int getLevel() {
        int c = 0;
        TreeNode node = this;
        while ((node = node.parent) != null) {
            c += 1;
        }
        return c;
    }

    String getPath() {
        StringBuilder sb = new StringBuilder();
        TreeNode node = this;
        do {
            sb.insert(0, "/" + node.title);
            node = node.parent;
        } while (null != node.parent);

        return sb.toString();
    }

    public boolean hasChildes() {
        return ((this.childes.size()) > 0);
    }

    public boolean getExpanded() {
        return this.expanded;
    }
    public boolean setExpanded(boolean value) {
        if((!(this.collapsible)) && (!value)) {
            return false;
        }
        if(this.hasChildes()) {
            this.expanded = value;
            return true;
        }
        return false;
    }
    boolean toggleExpanded() {
        return this.setExpanded(!(this.getExpanded()));
    }

    public boolean getChecked() {
        return this.checked;
    }
    public void setChecked(boolean value) {
        if(this.checkable) {
            this.checked = value;
        }
    }

    public void toggleChecked() {
        this.setChecked(!(this.getChecked()));
    }

    public boolean containsProperty(String name) {
        return this.properties.containsKey(name.toUpperCase());
    }

    public <T> T getProperty(String name) {
        if (containsProperty(name)) {
            return (T)(this.properties.get(name.toUpperCase()));
        }
        return null;
    }

    public <T> TreeNode setProperty(String name, T value) {
        this.properties.put(name.toUpperCase(), value);
        return this;
    }

    public View.OnClickListener getOnClickListener() {
        return this.onClickListener;
    }

    public TreeNode setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public View.OnClickListener getActionImageClickListener() {
        return this.onActionImageClickListener;
    }

    public TreeNode setActionImageClickListener(View.OnClickListener onActionImageClickListener) {
        this.onActionImageClickListener = onActionImageClickListener;
        return this;
    }
}