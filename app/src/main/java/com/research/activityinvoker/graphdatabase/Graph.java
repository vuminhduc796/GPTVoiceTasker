package com.research.activityinvoker.graphdatabase;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class Graph {
    private String appName;
    private String filename;
    private Map<Node, List<Edge>> adjacencyMap;
    private Context context;

    public Graph(String appName, Context context){
        adjacencyMap = new HashMap<>();
        this.appName = appName;
        this.context = context;
        filename = appName + ".txt";
        load();

    }

    public void loadNodeFromFile(Node node) {
        adjacencyMap.putIfAbsent(node, new ArrayList<>());
    }

    public void loadEdgeFromFile(Edge edge) {
        boolean isEdgeExisted = false;
        for (Edge currentEdge : Objects.requireNonNull(adjacencyMap.get(edge.src))) {
            if (currentEdge.dest == edge.dest) {
                isEdgeExisted = true;
                break;
            }
        }
        if (!isEdgeExisted) {
            adjacencyMap.get(edge.src).add(edge);
        }
    }

    public Node getNodeByXML(String currentScreenXML){
        Node bestMatchedNode = null;
        for(Node currentNode: getAllNodes()){
            if(Utils.calculateStringSimilarity(currentScreenXML, currentNode.screenDumperXML)){
                bestMatchedNode = currentNode;
            }
        }

        return bestMatchedNode;
    }

    public Node getNodeByDescription(String desc){
        Node bestMatchedNode = null;
        for(Node currentNode: getAllNodes()){
            if(Utils.calculateDescSimilarity(desc, currentNode.description)){
                bestMatchedNode = currentNode;
            }
        }

        return bestMatchedNode;
    }


    public Node addNode(String currentActivityName, String clickableElements , String text, String currentScreenXML) {
        if (currentActivityName == null || currentActivityName.isEmpty()) {
            return new Node("","","","");
        }

        Node bestMatchedNode = null;
        for(Node currentNode: getAllNodes()){
            if(Utils.calculateStringSimilarity(currentScreenXML, currentNode.screenDumperXML)){
                bestMatchedNode = currentNode;

            }
        }

        if (bestMatchedNode == null) {
            Node newNode = new Node(currentActivityName, clickableElements, text, currentScreenXML);
            adjacencyMap.putIfAbsent(newNode, new ArrayList<>());
            Log.d("NODE", "NEW node: " + newNode.description);
            save();
            return newNode;
        } else {
            Log.d("NODE", "EXISTING node: " + bestMatchedNode.description);

            return bestMatchedNode;
        }




    }

    public Set<Node> getAllNodes(){
        return adjacencyMap.keySet();

    }

    public void addEdge(Edge edge) {
        Node srcNode = edge.src;
        Node destNode = edge.dest;

        if (adjacencyMap.get(srcNode) == null) {

            srcNode = addNode(srcNode.activityName,srcNode.clickableElements,srcNode.description,srcNode.screenDumperXML);
            edge.src = srcNode;
        }
        if (adjacencyMap.get(edge.dest) == null) {
            destNode = addNode(destNode.activityName,destNode.clickableElements,destNode.description,destNode.screenDumperXML);
            edge.dest = destNode;
        }

        boolean isEdgeExisted = false;
        for (Edge currentEdge : Objects.requireNonNull(adjacencyMap.get(srcNode))) {
            if (currentEdge.dest == destNode) {
                isEdgeExisted = true;
                break;
            }
        }
        if (!isEdgeExisted) {
            adjacencyMap.get(edge.src).add(edge);
//        Edge backwardEdge = new Edge(edge.dest, edge.src, "BACK", " ", " ");
//        adjacencyMap.get(edge.dest).add(backwardEdge);
            save();
        }


    }

    public List<Edge> shortestPath(Node start, Node end) {
        Map<Node, Integer> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (Node node : adjacencyMap.keySet()) {
            distances.put(node, node == start ? 0 : Integer.MAX_VALUE);
            priorityQueue.offer(node);
        }

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();

            if (currentNode == end) {
                break;
            }

            for (Edge edge : adjacencyMap.get(currentNode)) {
                Node adjacentNode = edge.dest;
                int distance = distances.get(currentNode) + 1;

                if (distance < distances.get(adjacentNode)) {
                    priorityQueue.remove(adjacentNode);
                    distances.put(adjacentNode, distance);
                    previousNodes.put(adjacentNode, currentNode);
                    priorityQueue.offer(adjacentNode);
                }
            }
        }

        List<Edge> shortestPath = new ArrayList<>();
        Node currentNode = end;

        while (previousNodes.containsKey(currentNode)) {
            Node previousNode = previousNodes.get(currentNode);
            for (Edge edge : adjacencyMap.get(previousNode)) {
                if (edge.dest == currentNode) {
                    shortestPath.add(0, edge);
                    break;
                }
            }
            currentNode = previousNode;
        }

        if (shortestPath.size() == 0 || shortestPath.get(0).src != start) {
            return Collections.emptyList();
        }

        return shortestPath;
    }

     private Node getNodeById(int nodeId) {
        for (Node node : adjacencyMap.keySet()) {
            if (node.nodeID == nodeId) {
                return node;
            }
        }
        return null;
    }

    public void save() {
        try {
            File file = new File(context.getExternalFilesDir(null),filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for (Map.Entry<Node, List<Edge>> entry : adjacencyMap.entrySet()) {
                Node node = entry.getKey();
                osw.write(node.activityName + "**" + node.clickableElements + "**" + node.description + "**" + node.screenDumperXML + "**" + node.nodeID + "///");
                for (Edge edge : entry.getValue()) {
                    osw.write(edge.src.nodeID + "**" + edge.dest.nodeID + "**" + edge.action + "**" + edge.target + "**" + edge.bounds + "^^");
                }
                osw.write("\n");
            }
            osw.close();
            fos.close();

        } catch (IOException e) {
            System.err.println("Error writing graph to file: " + e);
        }
    }

    public void load() {
        try {

            File file = new File(context.getExternalFilesDir(null),filename);
            if (!file.exists()) {
                file.createNewFile();
                return; // Return since file is empty
            }

            BufferedReader nodeReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = nodeReader.readLine()) != null) {
                Log.d("DATA", line);
                String[] nodeAndEdges = line.split("///");
                String[] nodeData = nodeAndEdges[0].split("\\*\\*");

                Node node = new Node(nodeData[0], nodeData[1], nodeData[2], nodeData[3], Integer.parseInt(nodeData[4]));
                loadNodeFromFile(node);
            }


            nodeReader.close();

            BufferedReader edgeReader = new BufferedReader(new FileReader(file));

            while ((line = edgeReader.readLine()) != null) {
                Log.d("DATA", line);
                String[] nodeAndEdges = line.split("///");
                if (nodeAndEdges.length < 2) {
                    continue;
                }
                String[] edgeData = nodeAndEdges[1].split("\\^\\^");
                for (String edgeStr : edgeData) {
                    String[] edge = edgeStr.split("\\*\\*");
                    Node src = getNodeById(Integer.parseInt(edge[0]));
                    Node dest = getNodeById(Integer.parseInt(edge[1]));
                    Edge newEdge = new Edge(src, dest, edge[2], edge[3], edge[4]);
                    loadEdgeFromFile(newEdge);
                }
            }

            edgeReader.close();
            Log.d("GRAPH", this.toString());

        } catch (IOException e) {
            System.err.println("Error reading graph from file: " + e);
        }
    }

    public ArrayList<String> getListOfDescriptions(){
        ArrayList<String> output = new ArrayList<>();

        for (Node node: adjacencyMap.keySet()) {
            output.add(node.description);
        }

        return output;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph for app: " + appName + "\n");

        for (Map.Entry<Node, List<Edge>> entry : adjacencyMap.entrySet()) {
            Node node = entry.getKey();
            List<Edge> edges = entry.getValue();

            sb.append("Node ID: " + node.nodeID + "\n");
            sb.append("Description: " + node.description + "\n");
//            sb.append("Clickable Elements: " + node.clickableElements + "\n");
//            sb.append("Screen Dumper XML: " + node.screenDumperXML + "\n");

            if (edges.isEmpty()) {
                sb.append("No outgoing edges.\n\n");
            } else {
                sb.append("Outgoing edges: ");

                for (Edge edge : edges) {
//                    sb.append("\tAction: " + edge.action + "\n");
//                    sb.append("\tTarget: " + edge.target + "\n");
//                    sb.append("\tBounds: " + edge.bounds + "\n");
//                    sb.append("\tSrc node ID: " + edge.src.nodeID + "\n");
                    sb.append(edge.dest.nodeID + ",");
//                    sb.append("\n");
                }
            }
            sb.append("\n");
        }


        return sb.toString();
    }


}
