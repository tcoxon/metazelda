package net.bytten.metazelda.viewer;

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.bytten.metazelda.IDungeon;

public interface IDungeonView {

    public void draw(Graphics2D g, Dimension dim, IDungeon dungeon);
    
}
