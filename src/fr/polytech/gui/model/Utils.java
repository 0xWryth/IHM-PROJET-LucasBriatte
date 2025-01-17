package fr.polytech.gui.model;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.input.PickResult;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Utils {
    private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;

    // Maths

    /**
     * @param num - given number to round
     * @param x - number of decimals
     * @return rounding a double with x decimals
     */
    public static double roundXNumber(double num, int x) {
        double xPow = Math.pow(10, x);
        return Math.round(num * xPow) / xPow;
    }

    // Points helper

    /**
     * @param lat - given latitude
     * @param lon - given longitude
     * @param radius - sphere radius
     * @return Point3D located at a given (lat,lon) position on a sphere with a given radius
     */
    public static Point3D getCoordTo3dCoord(float lat, float lon, float radius) {
        float lat_cor = lat + TEXTURE_LAT_OFFSET;
        float lon_cor = lon + TEXTURE_LON_OFFSET;

        return new Point3D(-Math.sin(Math.toRadians(lon_cor))
                * Math.cos(Math.toRadians(lat_cor))*radius,
                -Math.sin(Math.toRadians(lat_cor))*radius,
                Math.cos(Math.toRadians(lon_cor))
                        * Math.cos(Math.toRadians(lat_cor))*radius);
    }

    /**
     * @param res - 3D object intersection
     * @return latitude and longitude string of a clicked point
     */
    public static String cursorToCoords(PickResult res)
    {
        Point3D coords = res.getIntersectedPoint();
        double lat = -Math.toDegrees(Math.asin(coords.getY() / Math.sqrt(Math.pow(coords.getX(), 2) + Math.pow(coords.getY(), 2) + Math.pow(coords.getZ(), 2))) );
        double lon = -Math.toDegrees(Math.atan2(coords.getX(), coords.getZ()));

        return roundXNumber(lat, 4) + ", " + roundXNumber(lon, 4);
    }

    // Model helper

    /**
     * @param svgMap - true if the requested model is the svgMap
     * @return loading the earth model
     */
    public static Group loadingEarthModel(Boolean svgMap) {
        Group group = new Group();

        ObjModelImporter objImporter = new ObjModelImporter();
        try {
            String URL = svgMap ? "/fr/polytech/gui/assets/models/earthSvg/earthSvg.obj" : "/fr/polytech/gui/assets/models/earth/earth.obj";
            java.net.URL modeUrl = Utils.class.getResource(URL);
            objImporter.read(modeUrl);
        } catch (ImportException e) {
            System.out.println(e);
        }


        MeshView[] mv = objImporter.getImport();
        group.getChildren().addAll(mv);

        return group;
    }

    // Shape helper

    /**
     * @param topRight
     * @param bottomRight
     * @param bootmLeft
     * @param topLeft
     * @param material
     * @param id
     * @return a quadrilateral between given points
     */
    public static MeshView AddQuadrilateral(Point3D topRight, Point3D bottomRight, Point3D bootmLeft,
                                            Point3D topLeft, PhongMaterial material, String id) {
        final TriangleMesh triangleMesh = new TriangleMesh();

        final float[] points = {
                (float)topRight.getX(), (float)topRight.getY(), (float)topRight.getZ(),
                (float)topLeft.getX(), (float)topLeft.getY(), (float)topLeft.getZ(),
                (float)bootmLeft.getX(), (float)bootmLeft.getY(), (float)bootmLeft.getZ(),
                (float)bottomRight.getX(), (float)bottomRight.getY(), (float)bottomRight.getZ()
        };

        final float[] texCoords = {
                1, 1,
                1, 0,
                0, 1,
                0, 0
        };

        final int[] faces = {
                0, 1, 1, 0, 2, 2,
                0, 1, 2, 2, 3, 3
        };

        triangleMesh.getPoints().setAll(points);
        triangleMesh.getTexCoords().setAll(texCoords);
        triangleMesh.getFaces().setAll(faces);

        final MeshView meshView = new MeshView(triangleMesh);
        meshView.setMaterial(material);
        meshView.setId(id);
        return meshView;
    }

    /**
     * @param origin - origin of the cylinder
     * @param target - target of the cylinder
     * @return a cylinder from one point to an other
     */
    public static Cylinder createLine(Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(0.005f, height);

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }
}
