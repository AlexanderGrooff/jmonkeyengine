package com.jme3.terrain.geomipmap;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TerrainQuadTest {

    private TerrainQuad parentTerrainQuad;
    private TerrainQuad[] children = new TerrainQuad[4];

    @Before
    public void init() {
        for(int i = 0; i < 4; i++) {
            children[i] = new TerrainQuad();
        }

        parentTerrainQuad = new TerrainQuad();
        fakeCreateQuad(parentTerrainQuad, children);
    }

    private void fakeCreateQuad(TerrainQuad parent, TerrainQuad[] children) {
        for (int i = 0; i < children.length; i++) {
            children[i].quadrant = i + 1; // Quadrant starts counting from 1
            parent.attachChild(children[i]);
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
        assertEquals(parentTerrainQuad.findRightQuad(), null);
        assertEquals(children[0].findRightQuad(), children[2]);
        assertEquals(children[1].findRightQuad(), children[3]);
        assertEquals(children[2].findRightQuad(), null);
        assertEquals(children[3].findRightQuad(), null);
    }

    @Test
    public void testFindDownQuad() {
        assertEquals(parentTerrainQuad.findDownQuad(), null);
        assertEquals(children[0].findDownQuad(), children[1]);
        assertEquals(children[1].findDownQuad(), null);
        assertEquals(children[2].findDownQuad(), children[3]);
        assertEquals(children[3].findDownQuad(), null);
    }

    @Test
    public void testFindLeftQuad() {
        assertEquals(parentTerrainQuad.findLeftQuad(), null);
        assertEquals(children[0].findLeftQuad(), null);
        assertEquals(children[1].findLeftQuad(), null);
        assertEquals(children[2].findLeftQuad(), children[0]);
        assertEquals(children[3].findLeftQuad(), children[1]);
    }

    @Test
    public void testFindTopQuad() {
        assertEquals(parentTerrainQuad.findTopQuad(), null);
        assertEquals(children[0].findTopQuad(), null);
        assertEquals(children[1].findTopQuad(), children[0]);
        assertEquals(children[2].findTopQuad(), null);
        assertEquals(children[3].findTopQuad(), children[2]);
    }
}
