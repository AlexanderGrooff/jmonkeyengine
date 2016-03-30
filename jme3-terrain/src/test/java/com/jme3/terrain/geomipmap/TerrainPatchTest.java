package com.jme3.terrain.geomipmap;

import com.jme3.scene.Spatial;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class TerrainPatchTest {

    final int DIR_RIGHT = 0, DIR_DOWN = 1, DIR_LEFT = 2, DIR_TOP = 3;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private FakeTerrainQuad parentTerrainQuad;
    private FakeTerrainQuad[] children = new FakeTerrainQuad[4];

    @Before
    public void init() {
        for (int i = 0; i < 4; i++) {
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
    public void testFindPatch() {
        // Test for nestlevel 2
        FakeTerrainQuad root = (FakeTerrainQuad)createNestedQuad(2);

        // Test for all quad children
        for (int i = 1; i <= 4; i++) {
            FakeTerrainQuad quadChild = (FakeTerrainQuad) root.getQuad(i);

            // Test for all patch children
            for (int j = 1; j <= 4; j++) {
                TerrainPatch patchChild = quadChild.getPatch(j);

                assertNotNull(patchChild);

                assertEquals(patchChild.findPatch(DIR_RIGHT), quadChild.findRightPatch(patchChild));
                assertEquals(patchChild.findPatch(DIR_DOWN),quadChild.findDownPatch(patchChild));
                assertEquals(patchChild.findPatch(DIR_LEFT),quadChild.findLeftPatch(patchChild));
                assertEquals(patchChild.findPatch(DIR_TOP), quadChild.findTopPatch(patchChild));

                // Test nonsense direction
                assertNull(patchChild.findPatch(-1));
            }
        }

        // Test for nestlevel 1
        root = (FakeTerrainQuad)createNestedQuad(1);

        // Test for all patch children
        for (int j = 1; j <= 4; j++) {
            TerrainPatch patchChild = root.getPatch(j);

            assertNotNull(patchChild);

            assertEquals(patchChild.findPatch(DIR_RIGHT), root.findRightPatch(patchChild));
            assertEquals(patchChild.findPatch(DIR_DOWN),root.findDownPatch(patchChild));
            assertEquals(patchChild.findPatch(DIR_LEFT),root.findLeftPatch(patchChild));
            assertEquals(patchChild.findPatch(DIR_TOP), root.findTopPatch(patchChild));

            // Test nonsense direction
            assertNull(patchChild.findPatch(-1));
        }
    }
}
