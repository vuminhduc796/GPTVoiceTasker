package com.research.activityinvoker.graphdatabase;

public class Edge {
    public Node src, dest;

    public String action;
    public String target;
    public String bounds;

    public Edge(Node src, Node dest, String action, String target, String bounds) {
        this.src = src;
        this.dest = dest;
        if (action.equals("")) {
            this.action = " ";
        } else {
            this.action = action;
        }

        if (target.equals("")) {
            this.target = " ";
        } else {
            this.target = target;
        }

        if (bounds.equals("")) {
            this.target = " ";
        } else {
            this.bounds = bounds;
        }


    }

    @Override
    public String toString() {
        return "Edge: " + src.nodeID + " -> " + dest.nodeID + ", action='" + action + '\'' + ", target='" + target + '\'' +
                ", bounds='" + bounds + "\n";

    }
}
