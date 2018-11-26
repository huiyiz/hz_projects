
/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /** The max image depth level. */
    public static final int MAX_DEPTH = 7;
    double lowerLon = -122.2119140625;
    double upperLon = -122.2998046875;
    double lowerLat = 37.82280243352756;
    double upperLat = 37.892195547244356;

    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     *     possible, while still covering less than or equal to the amount of longitudinal distance
     *     per pixel in the query box for the user viewport size.</li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the above
     *     condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    public RasterResultParams getMapRaster(RasterRequestParams params) {
        /*
         * Hint: Define additional classes to make it easier to pass around multiple values, and
         * define additional methods to make it easier to test and reason about code. */

        if ((params.ullat < lowerLat && params.lrlat > upperLat)
                || (params.ullon > lowerLon && params.lrlon < upperLon)) {
            return RasterResultParams.queryFailed();
        }

        if (params.ullat < params.lrlat || params.ullon > params.lrlon) {
            return RasterResultParams.queryFailed();
        }

        double ref = 98 / (288200 * lonDPP(params.lrlon, params.ullon, params.w));
        int depth = depthHelper(ref);

        double dlon = (-upperLon + lowerLon) / Math.pow(2, depth);
        double dlat = (upperLat - lowerLat) / Math.pow(2, depth);

        int[] bounds = xyHelper(params, depth);
        String[][] rGrid = new String[bounds[3] - bounds[2]][bounds[1] - bounds[0]];
        for (int i = bounds[2]; i < rGrid.length + bounds[2]; i++) {
            for (int j = bounds[0]; j < rGrid[0].length + bounds[0]; j++) {
                rGrid[i - bounds[2]][j - bounds[0]] = "d" + depth + "_x" + j + "_y" + i + ".png";
            }
        }
        RasterResultParams.Builder builder = new RasterResultParams.Builder();
        builder.setDepth(depth);
        builder.setRenderGrid(rGrid);
        double lrLat1 = upperLat - bounds[3] * dlat;
        double ulLat1 = upperLat - bounds[2] * dlat;
        double lrLon1 = upperLon + bounds[1] * dlon;
        double ulLon1 = upperLon + bounds[0] * dlon;

        builder.setRasterLrLat(lrLat1);
        builder.setRasterUlLat(ulLat1);
        builder.setRasterLrLon(lrLon1);
        builder.setRasterUlLon(ulLon1);
        if (bounds[4] == 1) {
            builder.setQuerySuccess(true);
        } else {
            builder.setQuerySuccess(false);
        }
        RasterResultParams result = builder.create();

        return result;
    }

    public int depthHelper(double ref) {
        int d = 0;
        while (d <= MAX_DEPTH) {
            if (Math.pow(2, d) >= ref) {
                return d;
            }
            d += 1;
        }
        return MAX_DEPTH;
    }

    /* Return an int array containing [x1, x2, y1, y2]. */
    public int[] xyHelper(RasterRequestParams params, int depth) {
        int[] result = new int[5];
        result[4] = 1;
        double dlon = (-upperLon + lowerLon) / Math.pow(2, depth);
        double dlat = (upperLat - lowerLat) / Math.pow(2, depth);
        if (params.ullat > upperLat) {
            result[2] = 0;
            result[4] = 0;
        } else {
            double lat1 = (upperLat - params.ullat) / dlat;
            result[2] = (int) lat1;
        }

        if (params.lrlat < lowerLat) {
            result[3] = (int) Math.pow(2, depth) - 1;
            result[4] = 0;
        } else {
            double lat2 = (upperLat - params.lrlat) / dlat + 1;
            result[3] = (int) lat2;
        }

        if (params.ullon < upperLon) {
            result[0] = 0;
            result[4] = 0;
        } else {
            double lon1 = (params.ullon - upperLon) / dlon;
            result[0] = (int) lon1;
        }

        if (params.lrlon > lowerLon) {
            result[1] = (int) Math.pow(2, depth) - 1;
            result[4] = 0;
        } else {
            double lon2 = (params.lrlon - upperLon) / dlon + 1;
            result[1] = (int) lon2;
        }

        return result;
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }
}
