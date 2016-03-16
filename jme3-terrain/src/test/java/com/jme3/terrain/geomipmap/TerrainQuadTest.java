package com.jme3.terrain.geomipmap;

import static org.junit.Assert.*;
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

    @Test
    public void testFakeTerrainQuad() {
        FakeTerrainQuad fake = new FakeTerrainQuad();
        assertEquals(fake, fake.getQuad(0));
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
}
