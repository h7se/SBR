package com.qou.h7se.sbr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by k0de9x on 10/2/2015.
 */
public class TreeNodesAdapter extends CustomBaseAdapter<TreeNode> {
    Context context;
    private ViewHolder holder = null;
    private LayoutInflater inflater = null;

    OnDataChange onDataRequest;
    OnViewChange onViewUpdate;

    interface OnViewChange {
        void event(TreeNode node, TreeNodesAdapter.ViewHolder holder);
    }

    @Override
    public CustomBaseAdapter<TreeNode>.ViewHolder getHolder() {
        return holder;
    }

     final class ViewHolder extends CustomBaseAdapter.ViewHolder {
        TextView txt1;
        TextView txt2;

        ImageView img1;
        ImageView img2;
        ImageView actionImage;

       TextView textrlyo2;
         TextView textrlyo3;

        RelativeLayout rlyo;
        RelativeLayout rlyo2;
        RelativeLayout rlyo3;
         RelativeLayout rlyo4;
    }

    public TreeNodesAdapter(Context context, OnDataChange onDataRequest) {
        super(new ArrayList<TreeNode>());

        this.context = context;
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.onDataRequest = onDataRequest;
    }

    public OnDataChange getOnDataRequest() {
        return onDataRequest;
    }
    public void setOnDataRequest(OnDataChange onDataRequest) {
        this.onDataRequest = onDataRequest;
    }

    public OnViewChange getOnViewUpdate() {
        return onViewUpdate;
    }
    public void setOnViewUpdate(OnViewChange onViewUpdate) {
        this.onViewUpdate = onViewUpdate;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.treenode_entry, null);
            assert convertView != null;

            holder.itemView = convertView;

            holder.txt1 = (TextView) convertView.findViewById(R.id.textView1);
            holder.txt2 = (TextView) convertView.findViewById(R.id.textViewSepHor);
            holder.img1 = (ImageView) convertView.findViewById(R.id.imageView);
            holder.img2 = (ImageView) convertView.findViewById(R.id.imageView2);

            holder.actionImage = (ImageView) convertView.findViewById(R.id.imageView8);

            holder.textrlyo2 = (TextView) convertView.findViewById(R.id.textView8);
            holder.textrlyo3 = (TextView) convertView.findViewById(R.id.textView9);

            holder.rlyo = (RelativeLayout) convertView.findViewById(R.id.rlyo);
            holder.rlyo2 = (RelativeLayout) convertView.findViewById(R.id.rlyo2);
            holder.rlyo3 = (RelativeLayout) convertView.findViewById(R.id.rlyo3);
            holder.rlyo4 = (RelativeLayout) convertView.findViewById(R.id.rlyo4);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (getItem(position) != null) {
                    // Utils.toast(position + ": " + data.get(position).title);
                    getItem(position).toggleChecked();

                    if(getOnDataRequest() != null) {
                        setData(getOnDataRequest().refresh(), true);
                    }
                }
            }
        });

        holder.img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (getItem(position) != null) {
                    if (getItem(position).toggleExpanded()) {
                        if(getOnDataRequest() != null) {
                            setData(getOnDataRequest().refresh(), true);
                        }

                        view.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AnimatorSets.getFadeInAnimatorSet(view, 800).start();
                            }
                        }, 1);
                    }
                }
            }
        });

        final TreeNode node = getItem(position);
        if(node != null) {
            // RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.img2.getLayoutParams();
            //  int x = params.width;
            // x += (data.get(position).getLevel() * 3);

            //params.setMargins(0, 0, params.rightMargin + (data.get(position).getLevel() * 5), 0);

            holder.txt2.setWidth((node.indent) ? ((node.getLevel() - 1) * 24) : 0);

            holder.txt1.setText(String.format("%s%s", node.title, (
                    (node.hasChildes() && !node.getExpanded()) ? " ..." : "")));

            if (node.hasChildes()) {
                if(node.expandMarginVisible) {
                    holder.img2.setVisibility(View.VISIBLE);
                } else {
                    holder.img2.setVisibility(View.GONE);
                }
                    if (node.getExpanded()) {
                        holder.img2.setImageResource(R.drawable.x1);
                    } else {
                        holder.img2.setImageResource(R.drawable.x2);
                    }


                holder.rlyo.setBackgroundResource(R.drawable.shadow_ey_1);
            } else {
                if(node.expandMarginVisible) {
                    holder.img2.setVisibility(View.INVISIBLE);
                } else {
                    holder.img2.setVisibility(View.GONE);
                }
                holder.rlyo.setBackgroundResource(R.drawable.shadow_ey_tree_1);
            }

            if(node.checkable && node.checkMarginVisible) {
                holder.img1.setVisibility(View.VISIBLE);
                if (node.getChecked()) {
                    holder.img1.setImageResource(R.drawable.check1);
                } else {
                    holder.img1.setImageResource(R.drawable.check0);
                }
            } else {
                holder.img1.setVisibility(View.GONE);
            }

            if(getRightTextLevel1Visibility()) {
                holder.rlyo2.setVisibility(View.VISIBLE);
            } else {
                holder.rlyo2.setVisibility(View.GONE);
            }

            if(getRightTextLevel2Visibility()) {
                holder.rlyo3.setVisibility(View.VISIBLE);
            } else {
                holder.rlyo3.setVisibility(View.GONE);
            }

            if(getRightImageMarginVisibility()) {
                holder.rlyo4.setVisibility(View.VISIBLE);
                if(node.getActionImageClickListener() != null) {
                    holder.actionImage.setVisibility(View.VISIBLE);
                    holder.actionImage.setOnClickListener(node.getActionImageClickListener());
                } else {
                    holder.actionImage.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.rlyo4.setVisibility(View.GONE);
            }

            if (node.getChecked()) {
                convertView.setBackgroundResource(R.drawable.a12_row_selector);
            } else {
                convertView.setBackgroundResource(R.drawable.a10_row_selector);
            }

            if(getOnViewUpdate() != null) {
                getOnViewUpdate().event(node, holder);
            }
        }

        return convertView;
    }


    public void invokeOnItemClick(final View view, int position) {
        TreeNode item = getItem(position);
        if (item != null) {
            if (!(item.getNoCollapseOnClick().evaluate(item))) {
                if (item.toggleExpanded()) {
                    // setData(onDataChange.refresh(), true);
                }
            }

            if(item.checkable) {
                item.toggleChecked();
            }

            setData(getOnDataRequest().refresh(), true);

            if(item.getOnClickListener() != null) {
                item.getOnClickListener().onClick(view);
            }
        }

//        view.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                AnimatorSets.getFlashAnimatorSet(view, 400).start();
//            }
//        }, 1);
    }



    public boolean getRightImageMarginVisibility() {
        return rightImageMarginVisibility;
    }

    public void setRightImageMarginVisibility(boolean value) {
        this.rightImageMarginVisibility = value;
    }
    private boolean rightImageMarginVisibility = false;


    public boolean getRightTextLevel1Visibility() {
        return rightTextLevel1Visibility;
    }

    public void setRightTextLevel1Visibility(boolean value) {
        this.rightTextLevel1Visibility = value;
    }
    private boolean rightTextLevel1Visibility = false;



    public boolean getRightTextLevel2Visibility() {
        return rightTextLevel2Visibility;
    }

    public void setRightTextLevel2Visibility(boolean value) {
        this.rightTextLevel2Visibility = value;
    }
    private boolean rightTextLevel2Visibility = false;
}