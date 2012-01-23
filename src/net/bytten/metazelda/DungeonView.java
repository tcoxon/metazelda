package net.bytten.metazelda;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import net.bytten.metazelda.algo.Dungeon;

public class DungeonView {

    public void draw(Graphics2D g, Dimension dim, Dungeon dungeon) {
        g.setColor(Color.BLACK);
        g.drawString("Hello world", 50, 50);
    }

}
