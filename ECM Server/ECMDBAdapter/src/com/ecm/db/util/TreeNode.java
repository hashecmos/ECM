package com.ecm.db.util;

import java.util.ArrayList;

public class TreeNode<T>  {
    private T data;
    private TreeNode<T> parent;
    private ArrayList<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new ArrayList<TreeNode<T>>();
    }

    /*public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    } */

    public T getData() { return data; }
    public TreeNode<T> getParent() { return parent; }
    public ArrayList<TreeNode<T>> getChildren() { return children; }
    
    public void setParent(TreeNode<T> pt) { 
    	this.parent = pt; 
    	if(this.parent != null)
    		this.parent.addChild(this);
    }
    
    public void addChild(TreeNode<T> ch) { 
    	for(TreeNode<T> item: this.children) {
    		if(item == ch)
    			return;
    	}
    	this.children.add(ch);
    }

}