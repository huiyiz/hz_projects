import java.util.List;

/**
 * This class provides a k-dimensional tree that improves the efficiency of finding nearest adjacent nodes.
 * @author: Huiyi Zhang, Yanqian Wu
 */

public class KDTree {

    Node root;
    Node currBest;

    public double disHelper(GraphDB.Vertex vertex1, GraphDB.Vertex vertex2) {
        return Math.sqrt(Math.pow(vertex1.x - vertex2.x, 2) + Math.pow(vertex1.y - vertex2.y, 2));
    }

    public Node closestHelper(Node currNode, GraphDB.Vertex target, int depth) {
        if (depth % 2 == 0) {
            if (target.x < currNode.item.x) {
                if (currNode.left != null) {
                    currBest = closestHelper(currNode.left, target, depth + 1);
                } else {
                    currBest = currNode;
                }
                if (disHelper(currBest.item, target) > disHelper(currNode.item, target)) {
                    currBest = currNode;
                }
                if (disHelper(currBest.item, target) > Math.abs(target.x - currNode.item.x)
                        && currNode.right != null) {
                    Node temp = currBest;
                    closestHelper(currNode.right, target, depth + 1);
                    if (disHelper(currBest.item, target) > disHelper(temp.item, target)) {
                        currBest = temp;
                    }
                }
            } else {
                if (currNode.right != null) {
                    currBest = closestHelper(currNode.right, target, depth + 1);
                } else {
                    currBest = currNode;
                }
                if (disHelper(currBest.item, target) > disHelper(currNode.item, target)) {
                    currBest = currNode;
                }
                if (disHelper(currBest.item, target) > Math.abs(target.x - currNode.item.x)
                        && currNode.left != null) {
                    Node temp = currBest;
                    closestHelper(currNode.left, target, depth + 1);
                    if (disHelper(currBest.item, target) > disHelper(temp.item, target)) {
                        currBest = temp;
                    }
                }
            }
        } else if (depth % 2 == 1) {
            if (target.y < currNode.item.y) {
                if (currNode.left == null) {
                    currBest = currNode;
                } else {
                    currBest = closestHelper(currNode.left, target, depth + 1);
                }
                if (disHelper(currBest.item, target) > disHelper(currNode.item, target)) {
                    currBest = currNode;
                }
                if (disHelper(currBest.item, target) > Math.abs(target.y - currNode.item.y)
                        && currNode.right != null) {
                    Node temp = currBest;
                    closestHelper(currNode.right, target, depth + 1);
                    if (disHelper(currBest.item, target) > disHelper(temp.item, target)) {
                        currBest = temp;
                    }
                }
            } else {
                if (currNode.right == null) {
                    currBest = currNode;
                } else {
                    currBest = closestHelper(currNode.right, target, depth + 1);
                }
                if (disHelper(currBest.item, target) > disHelper(currNode.item, target)) {
                    currBest = currNode;
                }
                if (disHelper(currBest.item, target) > Math.abs(target.y - currNode.item.y)
                        && currNode.left != null) {
                    Node temp = currBest;
                    closestHelper(currNode.left, target, depth + 1);
                    if (disHelper(currBest.item, target) > disHelper(temp.item, target)) {
                        currBest = temp;
                    }
                }
            }
        }
        return currBest;
    }

    public KDTree(List<GraphDB.Vertex> vertices) {
        for (int i = vertices.size() - 1; i >= 0; i--) {
            add(vertices.get(i));
        }
        this.currBest = new Node();
    }

    public void add(GraphDB.Vertex v) {
        if (root == null) {
            root = new Node(v, null, null);
            return;
        }
        root.addHelper(v, 0);
    }

    class Node {
        GraphDB.Vertex item;
        private Node left;
        private Node right;

        Node() {
            this.item = null;
            this.left = null;
            this.right = null;
        }

        Node(GraphDB.Vertex item, Node left, Node right) {
            this.item = item;
            this.left = left;
            this.right = right;
        }

        public void addHelper(GraphDB.Vertex v, int depth) {
            if (item == null) {
                item = v;
                return;
            }
            double targetX = GraphDB.projectToX(v.getLon(), v.getLat());
            double targetY = GraphDB.projectToY(v.getLon(), v.getLat());
            double itemX = GraphDB.projectToX(item.getLon(), item.getLat());
            double itemY = GraphDB.projectToY(item.getLon(), item.getLat());
            if (depth % 2 == 0) {
                if (targetX < itemX) {
                    if (left != null) {
                        left.addHelper(v, depth + 1);
                    } else {
                        left = new Node(v, null, null);
                    }
                } else {
                    if (right != null) {
                        right.addHelper(v, depth + 1);
                    } else {
                        right = new Node(v, null, null);
                    }
                }
            } else {
                if (targetY < itemY) {
                    if (left != null) {
                        left.addHelper(v, depth + 1);
                    } else {
                        left = new Node(v, null, null);
                    }
                } else {
                    if (right != null) {
                        right.addHelper(v, depth + 1);
                    } else {
                        right = new Node(v, null, null);
                    }
                }
            }
        }
    }
}

