package net.bytten.metazelda.viewer;

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.constraints.ColorMap;

public class FreeformDungeonView implements IDungeonView {

    protected ColorMap colorMap;
    
    public FreeformDungeonView(ColorMap colorMap) {
        this.colorMap = colorMap;
    }
    
    @Override
    public void draw(Graphics2D g, Dimension dim, IDungeon dungeon) {
        // TODO
    }

}
