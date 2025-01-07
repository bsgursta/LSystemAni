import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Stack;

public class Project3_Codebase {
    //UI Variables
    static int WIDTH = 500;
    static int HEIGHT = 500;
    static BufferedImage Display;
    static RulePanel r1;
    static RulePanel r2;
    static JTextField width,length, angle, start, iteration;

    //Panning and Zooming Variables
    static float TurtleScale = 1.0f;
    static float XOffset = 0;
    static float YOffset = 0;
    static float oldMouseX, oldMouseY;
    static float changeMouseX, changeMouseY;
    static boolean AnimationOver = false;

    //Animation Variables
    static Timer AnimationTimer;
    static int TurtleStepCount = 0;

    //LSystem
    static String LSystemString = "";
    static AffineTransform beginningTransform;

    //Rules, made by extending the JPanel class to create a new component
    static class RulePanel extends JPanel {
        String name;
        JLabel nameLabel;
        JTextField word;
        JTextField rule;

        RulePanel(String name){
            this.setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

            nameLabel = new JLabel(name);
            word = new JTextField("A");
            rule = new JTextField("AB");
            rule.setPreferredSize(new Dimension(100,25));

            this.add(nameLabel);
            this.add(word);
            this.add(rule);
        }

        public String GetWord(){
            return word.getText();
        }

        public String GetRule(){
            return rule.getText();
        }
    }

    public static void main(String[] args) {

        //make our UI on EDT using invokeLater
        //have an action listener on a button that will make and start a new thread for calucation/image drawing
        //when that thread makes the image, repaint the window on the EDT using invoke later
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Creating the  window
                JFrame window = new JFrame("L-System");
                window.setPreferredSize(new Dimension(WIDTH + 100, HEIGHT + 50));
                window.pack();
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setVisible(true);

                //Display panel containing the BufferedImage
                JPanel DisplayPanel = new JPanel();
                DisplayPanel.setBackground(Color.GRAY);
                Display = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Display.setRGB(250, 250, Color.BLACK.getRGB());
                DisplayPanel.add(new JLabel(new ImageIcon(Display)));
                window.add(DisplayPanel, BorderLayout.CENTER);

                //==============Menu Bar===============
                //sets up some default configurations for different L-systems
                JMenuBar MenuBar = new JMenuBar();
                window.setJMenuBar(MenuBar);

                JMenu Configure = new JMenu("Load Configuration");
                MenuBar.add(Configure);
                JMenuItem bin_tree = new JMenuItem("Binary Tree");
                bin_tree.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        angle.setText("45");
                        start.setText("0");
                        width.setText("1");
                        r1.word.setText("0");
                        r1.rule.setText("1[*<0]>0");
                        r2.word.setText("1");
                        r2.rule.setText("11");
                    }
                });
                Configure.add(bin_tree);

                JMenuItem Sierpinksi = new JMenuItem("Sierpinksi Triangle");
                Sierpinksi.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        angle.setText("120");
                        start.setText("F>F>F");
                        width.setText("1");
                        r1.word.setText("F");
                        r1.rule.setText("F>G<F<G>F");
                        r2.word.setText("G");
                        r2.rule.setText("GG");
                    }
                });
                Configure.add(Sierpinksi);

                JMenuItem Dragon = new JMenuItem("Dragon Curve");
                Dragon.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        angle.setText("90");
                        start.setText("F");
                        width.setText("1");
                        r1.word.setText("F");
                        r1.rule.setText("F<G");
                        r2.word.setText("G");
                        r2.rule.setText("F>G");
                    }
                });
                Configure.add(Dragon);

                JMenuItem Fern = new JMenuItem("Barnsley Fern");
                Fern.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        angle.setText("25");
                        start.setText("X");
                        width.setText("10");
                        r1.word.setText("X");
                        r1.rule.setText("F>--[[X]<X]<F[>FX]>X");
                        r2.word.setText("F");
                        r2.rule.setText("FF");
                    }
                });
                Configure.add(Fern);



                //==========Config panel=============

                Project3_Functions ProjFuncs = new Project3_Functions();

                JPanel Configuration = new JPanel();
                Configuration.setBackground(new Color(230, 230, 230));
                Configuration.setPreferredSize(new Dimension(250, 500));
                Configuration.setLayout(new FlowLayout());

                //iterations
                JPanel Iterations = new JPanel();
                Iterations.add(new JLabel("Number of Iterations"));
                iteration = new JTextField("5");
                iteration.setPreferredSize(new Dimension(50, 20));
                Iterations.add(iteration);
                Configuration.add(Iterations);

                //Angle
                JPanel Angle = new JPanel();
                Angle.add(new JLabel("Turn Angle"));
                angle = new JTextField("45");
                angle.setPreferredSize(new Dimension(50, 20));
                Angle.add(angle);
                Configuration.add(Angle);

                //Length
                JPanel Length = new JPanel();
                Length.add(new JLabel("Segment Length"));
                length = new JTextField("10");
                length.setPreferredSize(new Dimension(50, 20));
                Length.add(length);
                Configuration.add(Length);

                //Width
                JPanel Width = new JPanel();
                Width.add(new JLabel("Segment Width"));
                width = new JTextField("1");
                width.setPreferredSize(new Dimension(50, 20));
                Width.add(width);
                Configuration.add(Width);

                //Start
                JPanel Start = new JPanel();
                Start.add(new JLabel("Start"));
                start = new JTextField("A");
                start.setPreferredSize(new Dimension(50, 20));
                Start.add(start);
                Start.setPreferredSize(new Dimension(150, 25));
                Configuration.add(Start);

                r1 = new RulePanel("Rule 1");
                Configuration.add(r1);

                r2 = new RulePanel("Rule 2");
                Configuration.add(r2);

                JCheckBox DrawTurtleCheck = new JCheckBox("Draw Turtle");
                Configuration.add(DrawTurtleCheck);

                JPanel SpeedPanel = new JPanel();
                SpeedPanel.add(new JLabel("Speed:"));
                JSlider Speed = new JSlider(0,1000);
                Speed.setValue(50);
                Speed.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        //TODO: Adjust the initial delay of the timer when the slider moves
                        AnimationTimer.setInitialDelay(Speed.getValue());
                    }
                });
                SpeedPanel.add(Speed);
                Configuration.add(SpeedPanel);

                //Run Button
                JButton Run = new JButton("Generate");
                Run.setPreferredSize(new Dimension(200,50));
                Run.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        HashMap<Character,String> rulemap = new HashMap<Character, String>();
                        if(r1.GetWord().length()!=0)
                            rulemap.put(r1.GetWord().charAt(0),r1.GetRule());
                        if(r2.GetWord().length()!=0)
                            rulemap.put(r2.GetWord().charAt(0),r2.GetRule());

                        int iterations = Integer.parseInt(iteration.getText());
                        String starting_axiom = start.getText();
                        /*
                        width = Float.parseFloat(width.getText());
                        angle = Float.parseFloat(angle.getText());
                        float turtle_length = Float.parseFloat(length.getText());
                        */

                        //TODO: Generate the L-system string, then draw it. Later, draw it in a Timer to animate it.
                        AnimationOver = false;
                        TurtleStepCount = 0;
                        Graphics2D g = (Graphics2D) Display.getGraphics();
                        beginningTransform = g.getTransform();
                        LSystemString = ProjFuncs.Generate_LSystem(iterations,starting_axiom,rulemap);
/*
                        Draw_LSystem(LsystemString,Float.parseFloat(length.getText()),Float.parseFloat(width.getText()),Float.parseFloat(angle.getText()),DrawTurtleCheck.isSelected());
                        window.repaint();
                         */
                    }
                });
                Configuration.add(Run);
                window.add(Configuration,BorderLayout.EAST);

                //==========EVENTS==============
                DisplayPanel.setFocusable(true);
                DisplayPanel.addMouseWheelListener(new MouseWheelListener() {
                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        //TODO: Use the MouseWheelEvent to modify the current scaling factor, then redraw the L-system at the new scale and repaint the UI
                        if(e.getWheelRotation() < 0){
                            TurtleScale *=1.1f;
                        }
                        else{
                            TurtleScale *= 0.9f;
                        }
                        if(AnimationOver){
                            UpdateDisplay(Display);
                            Draw_LSystem(LSystemString, Float.parseFloat(length.getText()), Float.parseFloat(width.getText()), Float.parseFloat(angle.getText()), DrawTurtleCheck.isSelected());
                        }
                    }
                });

                DisplayPanel.addMouseMotionListener(new MouseMotionListener() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        //TODO: Use the MouseEvent to modify the current X and Y offsets, then redraw the L-system at the new scale on its own thread, then repaint the UI on the EDT. You should account for the difference between the coordinate systems of the turtle and the JPanel
                        changeMouseX = e.getX() - oldMouseX;
                        changeMouseY = e.getY() - oldMouseY;

                        XOffset += changeMouseX;
                        YOffset += changeMouseY;

                        oldMouseX = e.getX();
                        oldMouseY = e.getY();
                        if(AnimationOver){
                            UpdateDisplay(Display);
                            Draw_LSystem(LSystemString, Float.parseFloat(length.getText()), Float.parseFloat(width.getText()), Float.parseFloat(angle.getText()), DrawTurtleCheck.isSelected());
                        }
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        //TODO: Keep track of the mouse position even if the mouse isn't clicked
                            oldMouseX = e.getX();
                            oldMouseY = e.getY();

                    }
                });


                AnimationTimer = new Timer(Speed.getValue(), new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //System.out.print(TurtleScale);
                        //System.out.print("\n");
                        //delay_time = Speed.getValue();
                        //AnimationTimer.setDelay(delay_time);
                        //TODO: Progressively draw more and more of the current L-system each time the Timer starts over. Repaint the UI at the end of the Timer, and restart it.
                        Thread drawThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                TurtleStepCount += 1;
                                //TODO: Run your code
                                if(LSystemString.isEmpty()) {
                                    //UpdateDisplay(Display);
                                    //System.out.print("hi"); // when I press generate it restarts the timer.
                                }
                                else if(TurtleStepCount >= LSystemString.length()){
                                    AnimationTimer.stop();
                                    AnimationOver = true;
                                    Graphics2D g = (Graphics2D) Display.getGraphics();
                                    g.setTransform(beginningTransform);
                                    //Animation Done
                                }
                                else {
                                    //System.out.print("working");
                                    //System.out.print(TurtleStepCount);
                                    UpdateDisplay(Display);
                                    Draw_LSystem(LSystemString.substring(0,TurtleStepCount  + 1), Float.parseFloat(length.getText()), Float.parseFloat(width.getText()), Float.parseFloat(angle.getText()), DrawTurtleCheck.isSelected());
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        //TODO repaint the UI and restart timer
                                        window.repaint();
                                        AnimationTimer.restart();

                                    }
                                });
                            }
                        });
                        drawThread.start();
                    }
                });
                AnimationTimer.start();

            }
        });
    }


    //Recommended function... not required to use this but this might be helpful.
    //You can give it a known system string and implement the drawing part without needed to generate your own strings, for testing.
    //You can also use in you timer to draw the system while animating.
    static BufferedImage Draw_LSystem(String system, float segment_length, float width, float rotation_angle, boolean drawturtle){
        float currWidth = width * TurtleScale;
        if(currWidth <= 0){
            currWidth = 0.00001f;
        }
        Graphics2D g = (Graphics2D) Display.getGraphics();
        //g.setTransform(new AffineTransform());

        g.translate(WIDTH/2 + XOffset, HEIGHT/2 + YOffset);
        g.scale(TurtleScale,TurtleScale);
        g.rotate(Math.toRadians(180));
        g.scale(-1,1);

        Stack<AffineTransform> prevTransforms = new Stack<>();
        Stack<Float> prevWidths = new Stack<>();
        for(int i =0; i < system.length(); i++) {
                //AffineTransform currentTransform = g.getTransform();
                if(system.charAt(i) == '>') {
                    g.rotate(-Math.toRadians(rotation_angle));
                }
                else if(system.charAt(i) == '<') {
                    g.rotate(Math.toRadians(rotation_angle));
                }
                else if(system.charAt(i) == '+') {
                    currWidth += 1.0;
                }
                else if(system.charAt(i) == '-') {
                    currWidth -= 1.0;
                    if(currWidth <= 0){
                        currWidth = 0.0000001f;
                    }
                }
                else if(system.charAt(i) == '[') {
                    prevTransforms.push(g.getTransform());
                    prevWidths.push(currWidth);
                }
                else if(system.charAt(i) == ']') {
                    g.setTransform(prevTransforms.peek());
                    currWidth = prevWidths.peek();
                    prevTransforms.pop();
                    prevWidths.pop();
                }
                else if(system.charAt(i) == '*') {
                    g.setColor(Color.RED);
                    g.drawOval(0, 0, (int)(segment_length/4), (int)(segment_length/4));
                }
                else{
                    g.setColor(Color.green);
                    g.setStroke(new BasicStroke(currWidth));

                    g.drawLine(0,0,0,(int)segment_length);
                    g.translate(0,(int)segment_length);
                }
            }
        if(drawturtle) {
            DrawTurtle(g);
        }
            g.dispose();
        return new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB); //return value of an empty image included to allow the function to compile.
    }

    //==========PROVIDED FUNCTIONS===========
    static void UpdateDisplay(BufferedImage img){
        Graphics2D g = (Graphics2D) Display.getGraphics();
        g.setColor(Color.BLACK); //Used to be WHITE
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.drawImage(img,0,0,null);
        g.dispose();

    }

    //Draws the Turtle at its current spot/orientation.
    static void DrawTurtle(Graphics2D pen){
        //body (centered at current X,Y location)
        pen.setColor(new Color(255,255,100));
        pen.fillOval(-5,-5,10,10);

        //right arm (facing in +X direction)
        pen.setColor(new Color(150,50,50));
        pen.drawLine(0,0,10,0);

        //head (facing in +Y direction)
        pen.setColor(new Color(50,100,50));
        pen.drawLine(0,0,0,10);
    }
}


