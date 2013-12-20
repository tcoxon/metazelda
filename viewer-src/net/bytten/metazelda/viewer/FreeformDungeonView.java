package net.bytten.metazelda.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.constraints.ColorMap;

public class FreeformDungeonView extends GridDungeonView {

    protected ColorMap colorMap;
    
    public FreeformDungeonView(ColorMap colorMap) {
        this.colorMap = colorMap;
    }
    
    @Override
    public void draw(Graphics2D g, Dimension dim, IDungeon dungeon) {
        drawColors(g, getScale(dim,dungeon), getRoomSize(dim,dungeon));
        super.draw(g,dim,dungeon);
    }
    
    protected void drawColors(Graphics2D g, double scale, double roomSize) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.scale(scale, scale);
        g2.translate(scale*roomSize/4, scale*roomSize/4);

        for (int x = colorMap.getLeft(); x <= colorMap.getRight(); ++x)
            for (int y = colorMap.getTop(); y <= colorMap.getBottom(); ++y) {
                Integer val = colorMap.get(x, y);
                if (val == null) continue;
                
                g2.setColor(new Color(val));
                g2.fillRect(x, y, 1, 1);
            }
        
        g2.dispose();
    }

}
