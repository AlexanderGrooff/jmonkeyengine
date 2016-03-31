package com.jme3.terrain.geomipmap;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.geomipmap.lodcalc.LodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class TerrainQuadTest {

    private AssetManager assetManager;
    private FakeTerrainQuad parentTerrainQuad;
    private TerrainQuad terrainQuad = new TerrainQuad();
    private FakeTerrainQuad[] children = new FakeTerrainQuad[4];
    private TerrainPatch[] tpChildren = new TerrainPatch[4];
    private LodCalculator lodCalculator = new DistanceLodCalculator();
    private LodCalculator fakeLodCalculator = new FakeDistanceLodCalculator();
    private List<Vector3f> location = new ArrayList<Vector3f>();
    private HashMap<String, UpdatedTerrainPatch> updates = new HashMap<String, UpdatedTerrainPatch>();
    private Vector3f v3f = new Vector3f();
    private Vector2f v2f = new Vector2f();
    private BoundingBox boundingBox = new BoundingBox();
    private float[] testHeightmap;


    @Before
    public void init() {
        for (int i = 0; i < 4; i++) {
            children[i] = new FakeTerrainQuad();
            // children[i] = new TerrainQuad();
            tpChildren[i] = new TerrainPatch();
        }

        // HEIGHTMAP image (for the terrain heightmap)
        //Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new HillHeightMap(5, 1000, 50, 100, (byte) 3);

            //heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }


        terrainQuad = new TerrainQuad("terrain_1", 3, 5, heightmap.getHeightMap());
        testHeightmap = heightmap.getHeightMap();

    }

    private void fakeCreateQuad(FakeTerrainQuad parent, FakeTerrainQuad[] children) {
        for (int i = 0; i < children.length; i++) {
            children[i].quadrant = i + 1; // Quadrant starts counting from 1
            parent.attachChild(children[i]);
            //parent.getQuad(i).attachChild((tpChildren[i]));
        }
    }

    /**
     * Used to recursively create a nested structure of {@link Spatial}s.
     * If nesting level is > 1, root element will be a {@link TerrainQuad}.
     * Leafs (nesting level 0) are {@link TerrainPatch}es.
     *
     * @param nestLevel Nest level to be created.
     * @return Nested structure of {@link Spatial}s
     */
    private Spatial createNestedQuad(int nestLevel) {
        if (nestLevel == 0) {
            return new TerrainPatch();
        }

        FakeTerrainQuad parent = new FakeTerrainQuad();
        for (int i = 0; i < 4; i++) {
            Spatial child = createNestedQuad(nestLevel - 1);

            if (child instanceof TerrainPatch) {
                TerrainPatch patchChild = (TerrainPatch) child;
                patchChild.quadrant = (short) (i + 1);
                parent.attachChild(patchChild);
            } else if (child instanceof TerrainQuad) {
                FakeTerrainQuad quadChild = (FakeTerrainQuad) child;
                quadChild.quadrant = i + 1;
                parent.attachChild(quadChild);
            }
        }

        return parent;
    }

    @Test
    public void testFakeTerrainQuad() {
        FakeTerrainQuad fake = new FakeTerrainQuad();
        assertEquals(fake, fake.getQuad(0));
    }

    @Test
    public void testNestStructure() {
        Spatial leaf = createNestedQuad(0);
        assertTrue(leaf instanceof TerrainPatch);

        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(1);
        assertEquals(root.getChildren().size(), 4);
        for (int i = 0; i < 4; i++) {
            assertTrue(root.getChild(i) instanceof TerrainPatch); // Ensure children of root are leafs
        }

        root = (FakeTerrainQuad) createNestedQuad(2);
        assertEquals(root.getChildren().size(), 4);
        for (int i = 0; i < 4; i++) {
            assertTrue(root.getChild(i) instanceof TerrainQuad); // Ensure children of root are not leafs
        }
    }

//    @Test
//    public void testGetQuad() {
//        assertEquals(parentTerrainQuad.getQuad(0), parentTerrainQuad);
//        assertEquals(parentTerrainQuad.getQuad(1), children[0]);
//        assertEquals(parentTerrainQuad.getQuad(2), children[1]);
//        assertEquals(parentTerrainQuad.getQuad(3), children[2]);
//        assertEquals(parentTerrainQuad.getQuad(4), children[3]);
//        assertEquals(parentTerrainQuad.getQuad(5), null);
//    }


    /**
     * Tests the calculateLod method, which name has been refactored to hasLodChanged.
     * We came to the conclusion that the method does belong to TerrainQuad, but should be renamed
     * as it does not calculate anything. Is only retrieves values from users of the LodCalculator interface.
     * The actual lodCalculator is defined in the calculateLod method of these childs.
     */
    @Test
    public void testCalculateLod() {
        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(1);
        assertFalse(root.hasLodChanged(location, updates, lodCalculator));
        assertTrue(root.hasLodChanged(location, updates, fakeLodCalculator));

        FakeTerrainQuad leaf = (FakeTerrainQuad) createNestedQuad(1);
        leaf.attachChild(children[1]);
        assertTrue(leaf.hasLodChanged(location, updates, fakeLodCalculator));
    }


    /**
     * Tests the refactored createQuadPatch method, which name is refactored to setPatchChildren.
     * setPatchChildren makes use of two new methods createHeightBlock and createQuadPatch (part of TerrainPatch).
     * This is the first of 4 tests, as setPatchChildren couples 4 TerrainPatches to
     * a TerrainQuad. Each tests makes sure that the correct TerrainPatch child has been coupled.
     */
    @Test
    public void testSetPatchChildren1() {
        String patch1 = "terrain_1Patch1";

        boundingBox.setCenter(1.0f, 54.88082f, 1.0f);
        terrainQuad.setPatchChildren(testHeightmap);

        assertTrue(terrainQuad.getChild(patch1) instanceof TerrainPatch);
        TerrainPatch p1 = (TerrainPatch) terrainQuad.getChild(patch1);

        assertEquals(patch1, p1.getName());
        assertEquals(v2f.set(-1.0f, -1.0f), p1.getOffset());
        assertEquals(1.0f, p1.getOffsetAmount(), 0.0f);
        assertEquals(v3f.add(-2.0f, 0.0f, -2.0f), p1.getLocalTranslation());
        assertEquals(9, p1.getHeightMap().length);
        assertEquals(5, p1.getTotalSize());
        assertEquals(1, p1.getQuadrant());
        assertEquals(boundingBox.getCenter(), p1.getModelBound().getCenter());
    }

    @Test
    public void testSetPatchChildren2() {
        String patch2 = "terrain_1Patch2";

        boundingBox.setCenter(1.0f, 92.78813f, 1.0f);

        terrainQuad.setPatchChildren(testHeightmap);

        assertTrue(terrainQuad.getChild(patch2) instanceof TerrainPatch);
        TerrainPatch p1 = (TerrainPatch) terrainQuad.getChild(patch2);

        assertEquals(patch2, p1.getName());
        assertEquals(v2f.set(-1.0f, 1.0f), p1.getOffset());
        assertEquals(1.0f, p1.getOffsetAmount(), 0.0f);
        assertEquals(v3f.add(-2.0f, 0.0f, 0.0f), p1.getLocalTranslation());
        assertEquals(9, p1.getHeightMap().length);
        assertEquals(5, p1.getTotalSize());
        assertEquals(2, p1.getQuadrant());
        assertEquals(boundingBox.getCenter(), p1.getModelBound().getCenter());
    }

    @Test
    public void testSetPatchChildren3() {
        String patch3 = "terrain_1Patch3";

        boundingBox.setCenter(1.0f, 64.86637f, 1.0f);

        terrainQuad.setPatchChildren(testHeightmap);

        assertTrue(terrainQuad.getChild(patch3) instanceof TerrainPatch);
        TerrainPatch p1 = (TerrainPatch) terrainQuad.getChild(patch3);

        assertEquals(patch3, p1.getName());
        assertEquals(v2f.set(1.0f, -1.0f), p1.getOffset());
        assertEquals(1.0f, p1.getOffsetAmount(), 0.0f);
        assertEquals(v3f.add(0.0f, 0.0f, -2.0f), p1.getLocalTranslation());
        assertEquals(9, p1.getHeightMap().length);
        assertEquals(5, p1.getTotalSize());
        assertEquals(3, p1.getQuadrant());
        assertEquals(boundingBox.getCenter(), p1.getModelBound().getCenter());
    }

    @Test
    public void testSetPatchChildren4() {
        String patch4 = "terrain_1Patch4";

        boundingBox.setCenter(1.0f, 180.92175f, 1.0f);

        terrainQuad.setPatchChildren(testHeightmap);

        assertTrue(terrainQuad.getChild(patch4) instanceof TerrainPatch);
        TerrainPatch p1 = (TerrainPatch) terrainQuad.getChild(patch4);

        assertEquals(patch4, p1.getName());
        assertEquals(v2f.set(1.0f, 1.0f), p1.getOffset());
        assertEquals(1.0f, p1.getOffsetAmount(), 0.0f);
        assertEquals(v3f.add(0.0f, 0.0f, 0.0f), p1.getLocalTranslation());
        assertEquals(9, p1.getHeightMap().length);
        assertEquals(5, p1.getTotalSize());
        assertEquals(4, p1.getQuadrant());
        assertEquals(boundingBox.getCenter(), p1.getModelBound().getCenter());
    }

    /**
     * Tests the method getHeightmapHeight(int x, int z).
     * An extra internal class QuadrantFinder has been created to find the
     * corresponding quadrant for the given coordinates.
     */
    @Test
    public void testGetHeightmapHeight() {
        assertEquals(0.0f, terrainQuad.getHeightmapHeight(6, 6), 0.0f);
        assertEquals(testHeightmap[0], terrainQuad.getHeightmapHeight(0, 0), 0.0f);
        children = new FakeTerrainQuad[3];
        for (int i = 0; i < 3; i++) {
            children[i] = new FakeTerrainQuad();
        }

        parentTerrainQuad = new FakeTerrainQuad();
        parentTerrainQuad.size = 10;
        fakeCreateQuad(parentTerrainQuad, children);

        assertEquals(Float.NaN, parentTerrainQuad.getHeightmapHeight(5, 3), 0.0f);

        children = new FakeTerrainQuad[2];
        for (int i = 0; i < 2; i++) {
            children[i] = new FakeTerrainQuad();
        }
        parentTerrainQuad = new FakeTerrainQuad();
        parentTerrainQuad.size = 50;
        fakeCreateQuad(parentTerrainQuad, children);

        assertEquals(Float.NaN, parentTerrainQuad.getHeightmapHeight(5, 49), 0.0f);


    }

    /**
     * Tests the method getMeshNormal(int x, int z).
     * An extra internal class QuadrantFinder has been created to find the
     * corresponding quadrant for the given coordinates.
     */
    @Test
    public void testGetMeshNormal() {
        Vector3f v1 = new Vector3f(-0.7327255f, 0.043074645f, -0.67915976f);

        assertEquals(null, terrainQuad.getMeshNormal(10, 10));
        assertEquals(v1, terrainQuad.getMeshNormal(0, 0));
        children = new FakeTerrainQuad[3];
        for (int i = 0; i < 3; i++) {
            children[i] = new FakeTerrainQuad();
        }

        parentTerrainQuad = new FakeTerrainQuad();
        parentTerrainQuad.size = 10;
        fakeCreateQuad(parentTerrainQuad, children);

        assertEquals(null, parentTerrainQuad.getMeshNormal(5, 3));

        children = new FakeTerrainQuad[2];
        for (int i = 0; i < 2; i++) {
            children[i] = new FakeTerrainQuad();
        }
        parentTerrainQuad = new FakeTerrainQuad();
        parentTerrainQuad.size = 50;
        fakeCreateQuad(parentTerrainQuad, children);

        assertEquals(null, parentTerrainQuad.getMeshNormal(5, 49));
    }

    /**
     * Tests the method getHeight(int x, int z, float xm, float zm).
     * This method is being tested to make sure the private method
     * findMatchingChild(int x, int z) after its refactoring in which
     * an extra internal class QuadrantFinder has been created to find the
     * corresponding quadrant for the given coordinates.
     */
    @Test
    public void getHeight() {
        assertEquals(Float.NaN, terrainQuad.getHeight(10, 10, 10.0f, 10.0f), 0.0f);
        assertEquals(2.9181213f, terrainQuad.getHeight(0, 0, 0.0f, 0.0f), 0.0f);

        children = new FakeTerrainQuad[3];
        for (int i = 0; i < 3; i++) {
            children[i] = new FakeTerrainQuad();
        }

        parentTerrainQuad = new FakeTerrainQuad();
        parentTerrainQuad.size = 10;
        fakeCreateQuad(parentTerrainQuad, children);

        assertEquals(Float.NaN, parentTerrainQuad.getHeight(0, 0, 0.0f, 0.0f), 0.0f);
    }


}
