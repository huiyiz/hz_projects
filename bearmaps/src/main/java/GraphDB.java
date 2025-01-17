import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addVertex and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 * @author Huiyi Zhang, Yanqian Wu
 */
public class GraphDB {

    public class Vertex {
        long id;
        double lon;
        double lat;
        HashMap<String, String> tags;
        double x;
        double y;
        HashSet<Long> neighbors;

        public Vertex(long id, double lat, double lon, HashMap<String, String> tags,
                      HashSet<Long> neighbors) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.tags = tags;
            this.x = projectToX(lon, lat);
            this.y = projectToY(lon, lat);
            this.neighbors = neighbors;
        }

        public Vertex(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.tags = new HashMap<>();
            this.x = projectToX(lon, lat);
            this.y = projectToY(lon, lat);
            this.neighbors = new HashSet<>();
        }

        public Vertex(double lat, double lon) {
            this.id = 0;
            this.lon = lon;
            this.lat = lat;
            this.tags = new HashMap<>();
            this.x = projectToX(lon, lat);
            this.y = projectToY(lon, lat);
        }

        public void addTags(String key, String value) {
            this.tags.put(key, value);
        }

        public double getLon() {
            return this.lon;
        }
        public double getLat() {
            return this.lat;
        }
    }

    public class Path {
        long id;
        boolean isValid;
        ArrayList<Long> vertices;
        HashMap<String, String> tags;

        public Path(long id, ArrayList<Long> vertices, HashMap<String, String> tags) {
            this.id = id;
            this.isValid = false;
            this.vertices = vertices;
            this.tags = tags;
        }

        public Path(long id, ArrayList<Long> vertices) {
            this.id = id;
            this.isValid = false;
            this.vertices = vertices;
            this.tags = new HashMap<>();
        }

        public Path(long id) {
            this.id = id;
            this.isValid = false;
            this.vertices = new ArrayList<>();
            this.tags = new HashMap<>();
        }
    }

    HashMap<Long, Vertex> nodes;
    HashMap<Long, Path> paths;
    KDTree amanda;
    Vertex bestNode;

    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        File inputFile = new File(dbPath);
        this.nodes = new HashMap<>();
        this.paths = new HashMap<>();
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        List<Vertex> lst = nodes.values().stream().collect(Collectors.toList());
        this.amanda = new KDTree(lst);
    }

    public void addVertex(long id, double lat, double lon) {
        if (!nodes.containsKey(id)) {
            Vertex v = new Vertex(id, lat, lon);
            nodes.put(id, v);
        }
    }

    public Vertex constructVertex(long id, double lat, double lon, HashMap<String, String> tags) {
        return new Vertex(id, lat, lon, tags, new HashSet<>());
    }

    public void removeNode(long id) {
        if (nodes != null) {
            nodes.remove(id);
        }
    }

    public void addPath(long id, ArrayList<Long> vertices, HashMap<String, String> tags) {
        Path p = new Path(id, vertices, tags);
        paths.put(id, p);
    }

    public void removePath(long id) {
        if (paths != null) {
            paths.remove(id);
        }
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        // TODO
        HashSet<Long> storage = new HashSet<>();
        HashSet<Long> toRemove = new HashSet<>();
        for (Path p: paths.values()) {
            if (p.vertices.size() > 1) {
                storage.addAll(p.vertices);
            }
        }
        for (Vertex v: nodes.values()) {
            if (!storage.contains(v.id)) {
                toRemove.add(v.id);
            }
        }
        for (Long l: toRemove) {
            nodes.remove(l);
        }
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {
        // TODO
        if (nodes.get(v) != null) {
            return nodes.get(v).getLon();
        }
        return 1000000;
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {
        // TODO
        if (nodes.get(v) != null) {
            return nodes.get(v).getLat();
        }
        return 1000000;
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        // TODO
        return nodes.keySet();
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).neighbors;
//        ArrayList<Long> result = new ArrayList<>();
//        for (Path p: paths.values()) {
//            if (p.vertices.contains(v)) {
//                if (p.vertices.get(0).equals(v) && p.vertices.size() > 1) {
//                    result.add(p.vertices.get(1));
//                } else if (p.vertices.get(p.vertices.size() - 1).equals(v)
//                        && p.vertices.size() > 1) {
//                    result.add(p.vertices.get(p.vertices.size() - 2));
//                } else if (p.vertices.size() > 2) {
//                    int index = p.vertices.indexOf(v);
//                    result.add(p.vertices.get(index - 1));
//                    result.add(p.vertices.get(index + 1));
//                }
//            }
//        }
//        return result;
    }

    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Returns the ID of the vertex closest to the given longitude and latitude.
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    public long closest(double lon, double lat) {
        Vertex v = new Vertex(lat, lon);
        amanda.closestHelper(amanda.root, v, 0);
        return amanda.currBest.item.id;
    }


    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {

        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {

        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /** Radius of the Earth in miles. */
    private static final int R = 3963;
    /** Latitude centered on Berkeley. */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /** Longitude centered on Berkeley. */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;
}

