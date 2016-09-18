package com.qou.h7se.sbr;

import android.os.AsyncTask;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by k0de9x on 9/23/2015.
 */

public class TreeView {

    interface OnDataCallback {
        void data(final ArrayList<TreeNode> nodes);
    }

    private TreeNode root;

    public TreeView() {
        root = new TreeNode(null, null);
    }

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode add(String title) {
        return getRoot().add(title);
    }

    public TreeNode add(String title, Pair<String, ?>... props) {
        TreeNode node = getRoot().add(title);
        for(Pair<String, ?> pair : props) {
            node.setProperty(pair.first, pair.second);
        }
        return node;
    }

    TreeNode findNodeByPath(final String path) {
            class Func {
                TreeNode run() {
                    TreeNode node = getRoot();
                    String parts[] = (path.startsWith("/") ? path.substring(1) : path).split("/");
                    for (String p : parts) {
                        node = getChildNodeOrNull(node, p);
                        if (node == null) {
                            break;
                        }
                    }
                    return node;
                }

                TreeNode getChildNodeOrNull(TreeNode node, String title) {
                    for (TreeNode n : node.childes) {
                        if (n.title.equalsIgnoreCase(title)) {
                           return n;
                        }
                    }
                    return null;
                }
            }

        return (new Func().run());
    }


    public ArrayList<TreeNode> build() {
       return build(getRoot(), true);
    }

    public ArrayList<TreeNode> build(final TreeNode parent, final boolean expandable) {
        class Func {
            final ArrayList<TreeNode> nodes = new ArrayList<>();

            public ArrayList<TreeNode> run() {
                for (TreeNode n : parent.childes) {
                    this.expandNode(n);
                }
                return this.nodes;
            }

            private void expandNode(TreeNode node) {
                this.nodes.add(node);
                if (expandable && node.getExpanded()) {
                    for (TreeNode child : node.childes) {
                        this.expandNode(child);
                    }
                }
            }
        }

        return (new Func()).run();
    }

    public void buildAsync(final OnDataCallback callback) {
        buildAsync(getRoot(), callback);
    }

    public void buildAsync(final TreeNode parent, final OnDataCallback callback) {
        (new AsyncTask<TreeNode, Void, ArrayList<TreeNode>>() {
            @Override
            protected void onPostExecute(ArrayList<TreeNode> treeNodes) {
                super.onPostExecute(treeNodes);

                callback.data(treeNodes);
            }

            @Override
            protected ArrayList<TreeNode> doInBackground(TreeNode... treeNodes) {
                return build(treeNodes[0], true);
            }
        }).execute(parent);
    }

    public int getCheckedNodesCount() {
        class Func {
            int count = 0;
            public int run() {
                this.expandNode(getRoot());
                return count;
            }

            private void expandNode(TreeNode node) {
                if(node.checkable && node.getChecked()) {
                    count +=1;
                }
                for (TreeNode child : node.childes) {
                    this.expandNode(child);
                }
            }
        }
        return (new Func()).run();
    }
}
