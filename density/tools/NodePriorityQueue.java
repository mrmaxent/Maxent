/*
Copyright (c) 2016 Steven Phillips, Miro Dudik and Rob Schapire

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions: 

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
*/

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
