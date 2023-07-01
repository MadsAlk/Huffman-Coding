package application;

import java.io.IOException;
import java.util.ArrayList;


public class functions {

	
//	public static void add(ArrayList<node> H,node n) {
//	node temp = null;
//	H.add(n);
//	int position = H.size()-1;
//	int parent = position/2;
//	
//	while(parent > 0 && H.get(position).num < H.get(position/2).num) {
//		temp = H.get(position); H.add(position, H.get(parent)); H.add(parent, temp);
//		position = parent;
//		parent = position/2;
//	}
//	
//}

public static void checkOpt(ArrayList<node> H,int current){
    node temp;
    int smallestIndex=current;
    if(current*2<=H.size()-1 && H.get(current*2).num<H.get(current).num)smallestIndex=current*2;
    if(current*2+1<=H.size()-1 && H.get(current*2+1).num<H.get(smallestIndex).num)smallestIndex=current*2+1;
    if(smallestIndex != current){
    	temp = H.get(smallestIndex);
    	H.remove(smallestIndex);
    	H.add(smallestIndex, H.get(current)); 
    	H.remove(current);
    	H.add(current, temp);
        checkOpt(H,smallestIndex);
    }
}

public static node removeSmallest(ArrayList<node> H) {
	node s = H.get(1);
	H.remove(1);
	H.add(1, H.get(H.size()-1));
	H.remove(H.size()-1);
	checkOpt(H, 1);
	return s;
}


public static void addNew(ArrayList<node> H,node n) {
	H.add(n);
	node temp = null;
	int current = H.size()-1;
	int next = current/2;
	while(next > 0) {
		if(H.get(current).num < H.get(next).num) {
			temp = H.get(current);
	    	H.remove(current);        //it is necessary to remove the higher-index node first
	    	H.add(current, H.get(next)); 
	    	H.remove(next);
	    	H.add(next, temp);
		}
		current = next;
		next = current/2;
	}
	
}

public static void Heapify(ArrayList<node> H) {
    int current = (H.size()-1) / 2;
    while (current >= 1) {
        checkOpt(H, current);
        current--;
    }
}



public static void drawTree(node n,String code) {System.out.println("x ");
	if(n==null)return;
	if(n.left == null && n.right == null) {System.out.println("L");
		System.out.println(n.c + " " + code);  //System.out.println(n.c + " " +n.num + " " + code);
	}
	else {
	drawTree(n.right,code+1);
	drawTree(n.left,code+0);
}
}


public static void printTree(node n,long[][] Table) {
if(n==null)return;
if(n.left == null && n.right == null) {
	int i = (int)n.c;
	System.out.println(String.format("%15c  %15d  %15d  %15d",(char)i, Table[i][0], Table[i][1], Table[i][2]));
}
else {
	printTree(n.left,Table);
	printTree(n.right,Table);
	}
}


public static void printTree(node n) {
if(n==null)return;
if(n.left == null && n.right == null) {
	int i = (int)n.c;
	System.out.println(String.format("%15c ",(char)i));
}
else {
	printTree(n.left);
	printTree(n.right);
	}
}
	

public static void getCodes(node n,long[][] Table,int binCode,int pointer) {
	if(n==null)return;
	if(n.left == null && n.right == null) {
		Table[n.c][0] = Integer.toUnsignedLong(binCode)>>(pointer + 1); //to get zeros for significant bits
		Table[n.c][1] = 31-pointer;
		//System.out.println("+++" + Integer.toBinaryString(binCode) + " " + Long.toBinaryString(Table[n.c][0]));
	}
	else {
		pointer--;
		getCodes(n.left,Table,binCode,pointer);
		binCode = binCode | 1<<(pointer+1);
		getCodes(n.right,Table,binCode,pointer);
	}
}
	



static int headerSize=0;
public static void countHeaderSize(node n) {
	if(n==null)return;
	if(n.left == null && n.right == null) {
		headerSize += 9;
		return;
	}
	else {
		headerSize++;
		countHeaderSize(n.left);
		countHeaderSize(n.right);
	}
}
	
	
	static boolean flag = false;
	
	public static node construct(node n) {
		if(flag == true)return n;
		if(n==null) {
			flag = true;
			return new node(false);
		}
		if(n.leaf==true)return n;
		n.left = construct(n.left);
		n.right = construct(n.right);
		return n;
		}
	
	
	
	
	public static node constructLeaf(node n,char c) {
		if(flag == true)return n;
		if(n==null) {
			flag = true;
			return new node(true, c);
		}
		if(n.leaf==true)return n;
		n.left = constructLeaf(n.left,c);
		n.right = constructLeaf(n.right,c);
		return n;
	}
	
	
	
	
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



