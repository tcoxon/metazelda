package net.bytten.metazelda.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.bytten.metazelda.Dungeon;
import net.bytten.metazelda.DungeonGenerator;


public class Main extends JPanel {
    private static final long serialVersionUID = 1L;

    protected BufferedImage buffer;
    protected Graphics2D bufferG;
    protected Dimension bufferDim;
    
    protected Dungeon dungeon;
    protected DungeonView dungeonView;
    
    public Main(long seed) {
        super();
        regenerate(seed);
        dungeonView = new DungeonView();
        
    }
    
    public void regenerate(long seed) {
        System.out.println("Seed: "+seed);
        DungeonGenerator gen = new DungeonGenerator(seed);
        gen.generate();
        dungeon = gen.getDungeon();
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
        final JFrame frame = new JFrame();
        final Main panel = new Main(getSeed(args));
        panel.setPreferredSize(new Dimension(640, 480));
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        
        frame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                else if (e.getKeyCode() == KeyEvent.VK_F5) {
                    panel.regenerate(new Random().nextLong());
                    panel.repaint();
                }
            }

        });

        frame.setVisible(true);
    }
    
    private static long parseSeed(String seed) {
        try {
            return Long.parseLong(seed);
        } catch (NumberFormatException ex) {
            return seed.hashCode();
        }
    }

    private static long getSeed(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if ("-seed".equals(args[i])) {
                ++i;
                if (i < args.length) {
                    return parseSeed(args[i]);
                }
            } else if ("-seed=".equals(args[i].substring(0,6))) {
                return parseSeed(args[i].substring(6));
            }
        }
        return new Random().nextLong();
    }
}
