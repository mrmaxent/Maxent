package density.tools;
import java.util.*;

class NodePriorityQueue {
    Node[] heap, nodes;
    int numElements=0;

    NodePriorityQueue(Node[] nds) {
	heap = new Node[nds.length];
	nodes = nds;
    }

    void initialize() { 
	numElements = 0;
	for (int i=0; i<nodes.length; i++) nodes[i].heapIndex=-1;
    }

    void heapSet(int i, Node nd) {
	heap[i] = nd;
	nd.heapIndex = i;
    }	

    void reducedWeight(Node nd) {
	int i = nd.heapIndex;
	while (i>0 && heap[(i-1)/2].qweight > nd.qweight) {
	    heapSet(i, heap[(i-1)/2]);
	    i=(i-1)/2;
	}
	heapSet(i, nd);
    }

    void insert(Node nd) {
	int i = numElements++;
	heapSet(i, nd);
	reducedWeight(nd);
    }

    boolean alreadyDeleted(Node nd) { return nd.heapIndex==0 && heap[0]!=nd; }
    boolean contains(Node nd) { return nd.heapIndex!=-1; }
    boolean isEmpty() { return (numElements == 0); }

    void dump() {
	System.out.println("Heap:");
	for (int i=0; i<numElements; i++)
	    System.out.println(i + " " + heap[i].r + " " + heap[i].c + " " + heap[i].qweight);
    }

    Node deleteMin() {
	if (numElements==0) 
	    throw(new RuntimeException("Cannot deleteMin from empty priority queue"));
	int i;
	Node result = heap[0], target = heap[--numElements];
	for (i=0; 2*i+2 < numElements; ) {
	    Node c1 = heap[2*i+1], c2 = heap[2*i+2];
	    if (c1.qweight >= target.qweight && c2.qweight >= target.qweight)
		break;
	    if (c1.qweight < c2.qweight) {
		heapSet(i, c1);
		i = 2*i+1;
	    } else {
		heapSet(i, c2);
		i = 2*i+2;
	    }
	}
	if (2*i+2==numElements && heap[2*i+1].qweight < target.qweight) {
	    heapSet(i, heap[2*i+1]);
	    i = 2*i+1;
	}
	heapSet(i, target);
	return result;
    }
}
