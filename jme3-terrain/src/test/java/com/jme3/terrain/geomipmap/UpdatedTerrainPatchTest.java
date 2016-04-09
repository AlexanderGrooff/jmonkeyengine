package com.jme3.terrain.geomipmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UpdatedTerrainPatchTest {
    @Test
    public void testSetLeftLod() {
        TerrainPatch tp = new TerrainPatch();
        UpdatedTerrainPatch utp = new UpdatedTerrainPatch(tp);
        utp.setLeftLod(1);
        assertEquals(utp.getLeftLod(), 1);
    }

    @Test
    public void testSetTopLod() {
        TerrainPatch tp = new TerrainPatch();
        UpdatedTerrainPatch utp = new UpdatedTerrainPatch(tp);
        utp.setTopLod(1);
        assertEquals(utp.getTopLod(), 1);
    }

    @Test
    public void testSetRightLod() {
        TerrainPatch tp = new TerrainPatch();
        UpdatedTerrainPatch utp = new UpdatedTerrainPatch(tp);
        utp.setRightLod(1);
        assertEquals(utp.getRightLod(), 1);
    }

    @Test
    public void testSetBottomLod() {
        TerrainPatch tp = new TerrainPatch();
        UpdatedTerrainPatch utp = new UpdatedTerrainPatch(tp);
        utp.setBottomLod(1);
        assertEquals(utp.getBottomLod(), 1);
    }
}
