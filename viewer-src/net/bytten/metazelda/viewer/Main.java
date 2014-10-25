package net.bytten.metazelda.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.bytten.metazelda.constraints.ColorMap;
import net.bytten.metazelda.constraints.CountConstraints;
import net.bytten.metazelda.constraints.FreeformConstraints;
import net.bytten.metazelda.constraints.IDungeonConstraints;
import net.bytten.metazelda.constraints.SpaceConstraints;
import net.bytten.metazelda.constraints.SpaceMap;
import net.bytten.metazelda.generators.DungeonGenerator;
import net.bytten.metazelda.generators.IDungeonGenerator;
import net.bytten.metazelda.generators.LinearDungeonGenerator;
import net.bytten.metazelda.util.Coords;
import net.bytten.metazelda.util.StdoutLogger;


public class Main extends JPanel {
    private static final long serialVersionUID = 1L;

    protected BufferedImage buffer;
    protected Graphics2D bufferG;
    protected Dimension bufferDim;
    
    protected IDungeonGenerator dungeonGen;
    protected IDungeonView dungeonView;
    
    protected Thread generatorThread;
    protected Timer repaintTimer;
    
    protected String[] args;
    
    public Main(String[] args) {
        super();
        this.args = args;
        regenerate(getSeed(args));
        dungeonView = new GridDungeonView();
        
        repaintTimer = new Timer();
        repaintTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, 200);
    }
    
    protected IDungeonGenerator makeDungeonGenerator(long seed) {
        IDungeonConstraints constraints = null;
        
        if (getArg("color") != null) {
            
            try {
                ColorMap colorMap = new ColorMap();
                BufferedImage img = ImageIO.read(new File(getArg("color")));
                for (int x = 0; x < img.getWidth(); ++x)
                for (int y = 0; y < img.getHeight(); ++y) {
                    colorMap.set(x,y,img.getRGB(x,y));
                }
                
                constraints = new FreeformConstraints(colorMap);
                dungeonView = new FreeformDungeonView(colorMap);
            
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Falling back on non-freeform dungeon design");
            }
            
        }
        
        if (constraints == null) {
            
            CountConstraints cons = null;
            
            dungeonView = new GridDungeonView();
        
            if (getArg("space") != null) {
                try {
                    SpaceMap spaceMap = new SpaceMap();
                    
                    BufferedImage img = ImageIO.read(new File(getArg("space")));
                    for (int x = 0; x < img.getWidth(); ++x)
                    for (int y = 0; y < img.getHeight(); ++y) {
                        if ((img.getRGB(x,y) & 0xFFFFFF) != 0) {
                            spaceMap.set(new Coords(x,y), true);
                        }
                    }
                    
                    cons = new SpaceConstraints(spaceMap);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Falling back on CountConstraints");
                }
            }
            
            if (cons == null)
                cons = new CountConstraints(25, 4, 1);
            
            if (getArg("switches") == null) 
                cons.setMaxSwitches(0);
            
            constraints = cons;
        }
        
        DungeonGenerator generator;
        if (getArg("switches") != null) {
            generator = new DungeonGenerator(new StdoutLogger(), seed, constraints);
        } else {
            generator = new LinearDungeonGenerator(new StdoutLogger(), seed,
                    constraints);
        }
        if (getArg("no-goal") != null) {
            generator.setGenerateGoal(false);
        }
        return generator;
    }
    
    public void regenerate(final long seed) {
        if (generatorThread == null) {
            generatorThread = new Thread("Metazelda.regenerate Thread") {
                public void run() {
                    dungeonGen = makeDungeonGenerator(seed);
                    dungeonGen.generate();
                    generatorThread = null;
                }
            };
            generatorThread.start();
        }
    }
    
    @Override
    public void paint(Graphics g) {
        fixBuffer(g);
        
        bufferG.setColor(Color.WHITE);
        bufferG.fillRect(0, 0, bufferDim.width, bufferDim.height);
        
        if (dungeonView != null && dungeonGen != null &&
                dungeonGen.getDungeon() != null) {
            dungeonView.draw(bufferG, bufferDim, dungeonGen.getDungeon());
        }
        
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
        final JFrame frame = new JFrame("Metazelda Dungeon Viewer/Generator");
        final Main panel = new Main(args);
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
    
    private String getArg(String arg) {
        return getArg(arg, args);
    }
    
    private static String getArg(String arg, String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-"+arg)) {
                return args[i];
            } else if (args[i].startsWith("-"+arg+"=")) {
                return args[i].substring(2 + arg.length());
            }
        }
        return null;
    }

    private static long getSeed(String[] args) {
        String val = getArg("seed", args);
        
        if (val == null) return new Random().nextLong();
        return parseSeed(val);
    }
}
