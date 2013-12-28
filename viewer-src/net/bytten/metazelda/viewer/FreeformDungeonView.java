package net.bytten.metazelda.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.constraints.ColorMap;

public class FreeformDungeonView extends GridDungeonView {
    
    public static enum ColoringMode {
        ID, INTENSITY
    }

    protected ColoringMode coloringMode;
    protected ColorMap colorMap;
    
    public FreeformDungeonView(ColorMap colorMap) {
        this.colorMap = colorMap;
        this.coloringMode = ColoringMode.ID;
    }
    
    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }
    
    public void setColoringMode(ColoringMode coloringMode) {
        this.coloringMode = coloringMode;
    }
    
    protected Color getBackgroundColor(IDungeon dungeon, int id) {
        switch (coloringMode) {
        default: case ID: return new Color(id);
        case INTENSITY: return getIntensityColor(dungeon.get(id).getIntensity());
        }
    }
    
    @Override
    public void draw(Graphics2D g, Dimension dim, IDungeon dungeon) {
        drawColors(g, getScale(dim,dungeon), getRoomSize(dim,dungeon), dungeon);
        super.draw(g,dim,dungeon);
    }
    
    protected void drawColors(Graphics2D g, double scale, double roomSize,
            IDungeon dungeon) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.scale(scale, scale);
        g2.translate(scale*roomSize/4, scale*roomSize/4);

        for (int x = colorMap.getLeft(); x <= colorMap.getRight(); ++x)
            for (int y = colorMap.getTop(); y <= colorMap.getBottom(); ++y) {
                Integer id = colorMap.get(x, y);
                if (id == null) continue;
                
                g2.setColor(getBackgroundColor(dungeon, id));
                g2.fillRect(x, y, 1, 1);
            }
        
        g2.dispose();
    }

}
