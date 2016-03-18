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
        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(3);
        FakeTerrainQuad topLeftChild = (FakeTerrainQuad) root.getQuad(1);
        FakeTerrainQuad topRight = (FakeTerrainQuad) root.getQuad(3);

        assertEquals(root.findRightQuad(), null);
        assertEquals(topLeftChild.findRightQuad(), topRight); // Confirm position of two parent quads

        // Check quad children of parent
        assertEquals(topLeftChild.getQuad(1).findRightQuad(), topLeftChild.getQuad(3));
        assertEquals(topLeftChild.getQuad(2).findRightQuad(), topLeftChild.getQuad(4));
        assertEquals(topLeftChild.getQuad(3).findRightQuad(), topRight.getQuad(1));
        assertEquals(topLeftChild.getQuad(4).findRightQuad(), topRight.getQuad(2));

        // Check non-existing neighbour quads
        assertEquals(topRight.getQuad(3).findRightQuad(), null);
        assertEquals(topRight.getQuad(4).findRightQuad(), null);
    }

    @Test
    public void testFindDownQuad() {
        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(3);
        FakeTerrainQuad topLeftChild = (FakeTerrainQuad) root.getQuad(1);
        FakeTerrainQuad downLeftChild = (FakeTerrainQuad) root.getQuad(2);

        assertEquals(root.findDownQuad(), null);
        assertEquals(topLeftChild.findDownQuad(), downLeftChild); // Confirm position of two parent quads

        // Check quad children of parent
        assertEquals(topLeftChild.getQuad(1).findDownQuad(), topLeftChild.getQuad(2));
        assertEquals(topLeftChild.getQuad(2).findDownQuad(), downLeftChild.getQuad(1));
        assertEquals(topLeftChild.getQuad(3).findDownQuad(), topLeftChild.getQuad(4));
        assertEquals(topLeftChild.getQuad(4).findDownQuad(), downLeftChild.getQuad(3));

        // Check non-existing neighbour quads
        assertEquals(downLeftChild.getQuad(2).findDownQuad(), null);
        assertEquals(downLeftChild.getQuad(4).findDownQuad(), null);
    }

    @Test
    public void testFindLeftQuad() {
        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(3);
        FakeTerrainQuad topLeftChild = (FakeTerrainQuad) root.getQuad(1);
        FakeTerrainQuad topRightChild = (FakeTerrainQuad) root.getQuad(3);

        assertEquals(root.findLeftQuad(), null);
        assertEquals(topRightChild.findLeftQuad(), topLeftChild); // Confirm position of two parent quads

        // Check quad children of parent
        assertEquals(topRightChild.getQuad(1).findLeftQuad(), topLeftChild.getQuad(3));
        assertEquals(topRightChild.getQuad(2).findLeftQuad(), topLeftChild.getQuad(4));
        assertEquals(topRightChild.getQuad(3).findLeftQuad(), topRightChild.getQuad(1));
        assertEquals(topRightChild.getQuad(4).findLeftQuad(), topRightChild.getQuad(2));

        // Check non-existing neighbour quads
        assertEquals(topLeftChild.getQuad(1).findLeftQuad(), null);
        assertEquals(topLeftChild.getQuad(2).findLeftQuad(), null);
    }

    @Test
    public void testFindTopQuad() {
        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(3);
        FakeTerrainQuad topLeftChild = (FakeTerrainQuad) root.getQuad(1);
        FakeTerrainQuad downLeftChild = (FakeTerrainQuad) root.getQuad(2);

        assertEquals(root.findTopQuad(), null);
        assertEquals(downLeftChild.findTopQuad(), topLeftChild); // Confirm position of two parent quads

        // Check quad children of parent
        assertEquals(downLeftChild.getQuad(1).findTopQuad(), topLeftChild.getQuad(2));
        assertEquals(downLeftChild.getQuad(2).findTopQuad(), downLeftChild.getQuad(1));
        assertEquals(downLeftChild.getQuad(3).findTopQuad(), topLeftChild.getQuad(4));
        assertEquals(downLeftChild.getQuad(4).findTopQuad(), downLeftChild.getQuad(3));

        // Check non-existing neighbour quads
        assertEquals(topLeftChild.getQuad(1).findTopQuad(), null);
        assertEquals(topLeftChild.getQuad(3).findTopQuad(), null);
    }

    @Test
    public void testGetPatch() {
        FakeTerrainQuad root = (FakeTerrainQuad) createNestedQuad(1);
        assertEquals(root.getPatch(0), null);
        assertEquals(root.getPatch(1), root.getChild(0));
        assertEquals(root.getPatch(2), root.getChild(1));
        assertEquals(root.getPatch(3), root.getChild(2));
        assertEquals(root.getPatch(4), root.getChild(3));
        assertEquals(root.getPatch(5), null);
    }
}
