package net.bytten.metazelda;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import net.bytten.metazelda.algo.Dungeon;


public class Main extends JFrame {
    private static final long serialVersionUID = 1L;

    protected BufferedImage buffer;
    protected Graphics2D bufferG;
    protected Dimension bufferDim;
    
    protected Dungeon dungeon;
    protected DungeonView dungeonView;
    
    public Main() {
        super("Metazelda demo");
        dungeon = new Dungeon();
        dungeonView = new DungeonView();
        
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
            }

        });
    }
    
    @Override
    public void paint(Graphics g) {
        fixBuffer(g);
        
        bufferG.setColor(Color.WHITE);
        bufferG.fillRect(0, 0, bufferDim.width, bufferDim.height);
        
        dungeonView.draw(bufferG, bufferDim, dungeon);
        
        // Double-buffered drawing
        g.drawImage(buffer, 0, 0, this);
    }

    private void fixBuffer(Graphics g) {
        // If the size of the frame has changed, recreate the buffer
        if (!getSize().equals(bufferDim)) {
            bufferDim = new Dimension(getSize());
            buffer = new BufferedImage(bufferDim.width, bufferDim.height,
                    BufferedImage.TYPE_INT_ARGB);
            bufferG = buffer.createGraphics();
        }
    }


    @Override
    public void update(Graphics g) {
        // Call repaint directly to avoid "flashing"
        repaint();
    }
    

    // main -------------------------------------------------------------------
    public static void main(String[] args) {
        JFrame frame = new Main();
        frame.setSize(640, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
