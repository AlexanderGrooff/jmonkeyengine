package com.jme3.terrain.geomipmap;

import static org.junit.Assert.*;

import com.jme3.scene.Spatial;
import org.junit.Before;
import org.junit.Test;

public class TerrainQuadTest {

    private FakeTerrainQuad parentTerrainQuad;
    private FakeTerrainQuad[] children = new FakeTerrainQuad[4];

    @Before
    public void init() {
        for(int i = 0; i < 4; i++) {
            children[i] = new FakeTerrainQuad();
        }

        parentTerrainQuad = new FakeTerrainQuad();
        fakeCreateQuad(parentTerrainQuad, children);
    }

    private void fakeCreateQuad(FakeTerrainQuad parent, FakeTerrainQuad[] children) {
        for (int i = 0; i < children.length; i++) {
            children[i].quadrant = i + 1; // Quadrant starts counting from 1
            parent.attachChild(children[i]);
        }
    }

    /**
     * Used to recursively create a nested structure of {@link Spatial}s.
     * If nesting level is > 1, root element will be a {@link TerrainQuad}.
     * Leafs (nesting level 0) are {@link TerrainPatch}es.
     * @param nestLevel Nest level to be created.
     * @return Nested structure of {@link Spatial}s
     */
    private Spatial createNestedQuad(int nestLevel) {
        if (nestLevel == 0) {
            return new TerrainPatch();
        }

        FakeTerrainQuad parent = new FakeTerrainQuad();
        for(int i = 0; i < 4; i++) {
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
        for(int i = 0; i < 4; i++) {
            assertTrue(root.getChild(i) instanceof TerrainPatch); // Ensure children of root are leafs
        }

        root = (FakeTerrainQuad) createNestedQuad(2);
        assertEquals(root.getChildren().size(), 4);
        for(int i = 0; i < 4; i++) {
            assertTrue(root.getChild(i) instanceof TerrainQuad); // Ensure children of root are not leafs
        }
    }

    @Test
    public void testGetQuad() {
        assertEquals(parentTerrainQuad.getQuad(0), parentTerrainQuad);
        assertEquals(parentTerrainQuad.getQuad(1), children[0]);
        assertEquals(parentTerrainQuad.getQuad(2), children[1]);
        assertEquals(parentTerrainQuad.getQuad(3), children[2]);
        assertEquals(parentTerrainQuad.getQuad(4), children[3]);
        assertEquals(parentTerrainQuad.getQuad(5), null);
    }

    @Test
    public void testFindRightQuad() {
        assertEquals(children[0].findRightQuad(), children[2]);
        assertEquals(children[1].findRightQuad(), children[3]);
        assertEquals(children[2].findRightQuad(), null);
        assertEquals(children[3].findRightQuad(), null);
    }
}
