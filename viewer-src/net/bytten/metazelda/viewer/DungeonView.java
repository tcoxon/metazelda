package net.bytten.metazelda.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import net.bytten.metazelda.Bounds;
import net.bytten.metazelda.Coords;
import net.bytten.metazelda.Direction;
import net.bytten.metazelda.Edge;
import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.Room;

public class DungeonView {

    public void drawRoom(Graphics2D g, double scale, Room room) {
        
        int cx = (int)(room.coords.x * scale + scale/2),
            cy = (int)(room.coords.y * scale + scale/2);
        
        g.setColor(Color.getHSBColor(0.6f - (float)room.getIntensity()*0.6f,
                0.7f, 1.0f));
        
        g.fillOval((int)(cx - scale*0.25),
                (int)(cy - scale*0.25),
                (int)(scale/2), (int)(scale/2));
        
        g.setColor(Color.BLACK);
        
        g.drawOval((int)(cx - scale*0.25),
                (int)(cy - scale*0.25),
                (int)(scale/2), (int)(scale/2));
        
        if (room.isGoal()) {
            g.drawOval((int)(cx - scale*0.2),
                    (int)(cy - scale*0.2),
                    (int)(scale * 0.4), (int)(scale * 0.4));
        }
        
        if (room.getItem() != null) {
            g.drawString(room.getItem().toString(), cx, cy);
        }
        
        g.drawString(String.format("%.2f", room.getIntensity()), cx-12, cy+16);
    }
    
    public void drawArrow(Graphics2D g, double x1, double y1, double x2,
            double y2) {
        AffineTransform origXfm = g.getTransform();
        
        g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
        
        double len = 0.1*Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)),
            rot = Math.atan2(x2-x1,y1-y2);
        g.translate(x2, y2);
        g.rotate(rot);
        g.drawLine(0,0, (int)(-len*2/3), (int)(len));
        g.drawLine(0,0, (int)(len*2/3), (int)(len));
        
        g.setTransform(origXfm);
    }
    
    public void drawEdges(Graphics2D g, double scale, IDungeon dungeon,
            Room room) {
        g.setColor(Color.BLACK);
        
        for (Direction d: Direction.values()) {
            Direction oppD = Direction.oppositeDirection(d);
            
            Edge edge = room.getEdge(d);
            if (edge == null) continue;
            
            Coords coords = room.coords,
                nextCoords = coords.nextInDirection(d);
            
            double x1 = coords.x*scale + scale/2,
                   y1 = coords.y*scale + scale/2,
                   x2 = nextCoords.x*scale + scale/2,
                   y2 = nextCoords.y*scale + scale/2;
            switch (d) {
            case N: y1 -= scale/4; y2 += scale/4; break;
            case E: x1 += scale/4; x2 -= scale/4; break;
            case S: y1 += scale/4; y2 -= scale/4; break;
            case W: x1 -= scale/4; x2 += scale/4; break;
            }

            Room nextRoom = dungeon.get(nextCoords);
            if (nextRoom != null && edge.equals(nextRoom.getEdge(oppD))) {
                // Bidirectional edge
                // avoid drawing twice:
                if (room.coords.compareTo(nextRoom.coords) > 0) continue;
                
                g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
                
                double midx = (x1+x2)/2,
                       midy = (y1+y2)/2;
                if (edge.getSymbol() != null) {
                    g.drawString(edge.getSymbol().toString(),
                            (int)midx, (int)midy);
                }
            } else {
                // Unidirectional edge
                int dx = 0, dy = 0;
                switch (d) {
                case N: dx -= (int)(scale/20); break;
                case E: dy += (int)(scale/20); break;
                case S: dx += (int)(scale/20); break;
                case W: dy -= (int)(scale/20); break;
                }
                x1 += dx; x2 += dx;
                y1 += dy; y2 += dy;
                drawArrow(g, x1, y1, x2, y2);
                
                double midx = (x1+x2)/2,
                       midy = (y1+y2)/2;
                if (dx < 0) midx += dx*1.5;
                if (dy > 0) midy += dy*1.5;
                if (edge.getSymbol() != null) {
                    g.drawString(edge.getSymbol().toString(),
                            (int)midx, (int)midy);
                }
            }
        }
        
    }
    
    public void draw(Graphics2D g, Dimension dim, IDungeon dungeon) {
        AffineTransform origXfm = g.getTransform();
        
        // Figure out scale & translation to draw the dungeon at
        synchronized (dungeon) {
            Bounds bounds = dungeon.getExtentBounds();
            double scale = Math.min(((double)dim.width) / bounds.width(),
                    ((double)dim.height) / bounds.height());
            // move the graph into view
            g.translate(-scale * bounds.left, -scale * bounds.top);
            
            for (Room room: dungeon.getRooms()) {
                // draw the edges between rooms
                drawEdges(g, scale, dungeon, room);
            }
            
            for (Room room: dungeon.getRooms()) {
                // Draw the room
                drawRoom(g, scale, room);
            }
        }
        
        g.setTransform(origXfm);
    }

}
