package application;

public class node implements Comparable<node> {
	char c;
	long num; //freq
	node left;
	node right;
	boolean leaf = false;
	
	
	
	public node(char c, long num, boolean leaf) {
		super();
		this.c = c;
		this.num = num;
		this.leaf = leaf;
	}
	
	public node(long num, boolean leaf) {
		super();
		this.num = num;
		this.leaf = leaf;
	}

	public node(boolean b) {
		leaf = b;
	}
	public node(boolean b,char c) {
		leaf = b;
		this.c = c;
	}
	public node(long num) {
		this.num = num;
	}

	public node(char c, long num) {
		this.c = c;
		this.num = num;
	}

	public node(char c) {
		this.c = c;
	}
	
	public boolean equals(node other){
		return this.num == other.num;
	}
	
	public int compareTo(node other) {
		if(this.equals(other))return 0;
		else if(this.num > other.num)return 1;
		else return -1;
		}
		
	
}
