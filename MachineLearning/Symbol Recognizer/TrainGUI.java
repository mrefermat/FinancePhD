import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.io.*;

/**
 * GUI for collecting training set datapoints
 * @author Daniel Sanchez
 * @version 25-oct-2006
 */
public class TrainGUI extends JFrame {
	
	/**
	 * Upper button bar
	 */
	protected ButtonBar buttons;
	
	/**
	 * Drawing board
	 */
	protected SketchBoard board;
	
	/**
	 * Lower panel
	 */
	protected InfoPanel infopanel;
	
	/**
	 * Active sketch
	 */
	protected Sketch sketch;
	
	/**
	 * Set of all the sketches
	 */
	protected Vector<Sketch> dataset;
	
	/**
	 * Index of the current sketch
	 */
	protected int index;
	
	/**
	 * Constructor of the GUI
	 */
	TrainGUI() {
		super("Symbol recognizer - Training GUI");
		
		//Adjust the style
		try {
			   UIManager.setLookAndFeel(
			      UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Exception while trying to set the look&feel");
			return;
		}
		
		//Panel that holds the main components
		JPanel p=new JPanel();
		
		buttons=new ButtonBar(this);
		board=new SketchBoard(this);
		infopanel=new InfoPanel(this);
		
		//Creation of the dataset
		dataset=new Vector<Sketch>();
		sketch=new Sketch();
		int index=infopanel.cbSymbol.getSelectedIndex();
		sketch.setLabel(infopanel.labels.elementAt(index));
		dataset.add(sketch);
		index=0;
		
		// Panel that holds the drawing board 
		JPanel pc=new JPanel();
		pc.setLayout(new FlowLayout(FlowLayout.CENTER));
		pc.add(board);
		
		//Add the components
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.NORTH, buttons);
		p.add(BorderLayout.CENTER, pc);
		p.add(BorderLayout.SOUTH, infopanel);
		
		getContentPane().add(p);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		//Display the frame
		pack();
		setVisible(true);
		
		//Display info bout the symbols used
		System.out.println("Symbol database of " + infopanel.labels.size() + " symbols");
	}
	
	
	
	/**
	 * Run the GUI
	 * @param args arguments (not taken into account)
	 */
	public static void main(String args[]) {
		new TrainGUI();
	}
}


/**
 * Panel with buttons to navigate the sketch set
 * @author Daniel Sanchez
 *
 */
class ButtonBar extends JPanel implements ActionListener {

	/**
	 * Parent TrainGUI component
	 */
	private TrainGUI traingui;
	
	//Buttons
	JButton bLoad;
	JButton bSave;
	JButton bMerge;
	JButton bNew;
	JButton bDelete;
	JButton bPrevious;
	JButton bNext;
	JButton bPrint;
	
	//Label with current position
	JLabel lPos;
	
	/**
	 * State of the program
	 */
	int state;
	
	//Possible states
	final static int FIRST=0;
	final static int LAST=1;
	final static int NORMAL=2;
	final static int EMPTY=3;

	/**
	 * Constructor
	 * @param v Parent component
	 */
	ButtonBar(TrainGUI v) {
		super();
		traingui=v;
		
		//Panels
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER));
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		//Create the buttons
		bLoad = new JButton("Load Set");
		bMerge = new JButton("Merge Set");
		bSave = new JButton("Save Set");
		bNew = new JButton("New");
		bDelete = new JButton("Delete");
		bPrevious = new JButton("<<");
		lPos=new JLabel("1/1");
		bNext = new JButton(">>");
		bPrint = new JButton("Print");
		
		//Add the buttons to the panels
		updateBar();
		p1.add(bLoad);
		p1.add(bMerge);
		p1.add(bSave);
		p1.add(bNew);
		p1.add(bDelete);
		p2.add(bPrevious);
		p2.add(lPos);
		p2.add(bNext);
		p2.add(bPrint);
		
		//Add the panels
		setLayout(new BorderLayout());
		add(p1,BorderLayout.NORTH);
		add(p2,BorderLayout.SOUTH);
		
		//Add the listeners
		bLoad.addActionListener(this);
		bMerge.addActionListener(this);
		bSave.addActionListener(this);
		bNew.addActionListener(this);
		bDelete.addActionListener(this);
		bPrevious.addActionListener(this);
		bNext.addActionListener(this);
		bPrint.addActionListener(this);
		
	}
	
	/**
	 * Performs the actions of the buttons
	 * @param ae ActionEvent event
	 */
	public void actionPerformed(ActionEvent ae) {
		//Load another dataset
		if (ae.getSource().equals(bLoad)) {
		    JFileChooser chooser = new JFileChooser(".");
		    int returnVal = chooser.showOpenDialog(traingui);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	String filename = chooser.getSelectedFile().getPath();
				try {
					//Read the dataset
					FileInputStream fis = new FileInputStream(filename);
					ObjectInputStream in = new ObjectInputStream(fis);
					traingui.dataset = (Vector<Sketch>) in.readObject();
					in.close();
					
					//Setup index and sketch
					traingui.index = 0;
					traingui.sketch = traingui.dataset.elementAt(traingui.index);
					
					//General update
					traingui.infopanel.updateComboBox();
					updateBar();
					traingui.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//Merge this dataset with another one
		if (ae.getSource().equals(bMerge)) {
		    JFileChooser chooser = new JFileChooser(".");
		    int returnVal = chooser.showOpenDialog(traingui);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	String filename = chooser.getSelectedFile().getPath();
				try {
					//Read the new dataset
					FileInputStream fis = new FileInputStream(filename);
					ObjectInputStream in = new ObjectInputStream(fis);
					Vector<Sketch> newData = (Vector<Sketch>) in.readObject();
					in.close();
					
					//Merge with the current one
					traingui.dataset.addAll(newData);
					
					//Setup index and sketch
					traingui.index = 0;
					traingui.sketch = traingui.dataset.elementAt(traingui.index);
					
					//General update
					traingui.infopanel.updateComboBox();
					updateBar();
					traingui.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//Save current dataset
		if (ae.getSource().equals(bSave)) {	
		    JFileChooser chooser = new JFileChooser(".");
		    int returnVal = chooser.showSaveDialog(traingui);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       String filename = chooser.getSelectedFile().getPath();
				try {
					//Write to the file
					FileOutputStream fos = new FileOutputStream(filename);
					ObjectOutputStream out = new ObjectOutputStream(fos);
					out.writeObject(traingui.dataset);
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		//New sketch
		if (ae.getSource().equals(bNew)) {
			traingui.sketch=new Sketch();
			int index=traingui.infopanel.cbSymbol.getSelectedIndex();
			//Select next symbol
			if (index<traingui.infopanel.labels.size()-1) {
				index++;
				traingui.infopanel.cbSymbol.setSelectedIndex(index);
			}
			traingui.sketch.setLabel(traingui.infopanel.labels.elementAt(index));
			traingui.index++;
			traingui.dataset.insertElementAt(traingui.sketch,traingui.index);
			updateBar();
			traingui.infopanel.updateComboBox();
			traingui.repaint();
		}
		
		//Previous sketch
		if (ae.getSource().equals(bPrevious)) {
			traingui.index--;
			traingui.sketch=traingui.dataset.elementAt(traingui.index);
			updateBar();
			traingui.infopanel.updateComboBox();
			traingui.repaint();
		}
		
		//Next sketch
		if (ae.getSource().equals(bNext)) {
			traingui.index++;
			traingui.sketch=traingui.dataset.elementAt(traingui.index);
			updateBar();
			traingui.infopanel.updateComboBox();
			traingui.repaint();
		}
		
		//Delete sketch
		if (ae.getSource().equals(bDelete)) {
			traingui.dataset.removeElementAt(traingui.index);
			if(traingui.dataset.size()==traingui.index) traingui.index--;
			traingui.sketch=traingui.dataset.elementAt(traingui.index);
			traingui.infopanel.updateComboBox();
			updateBar();
			traingui.repaint();
		}
		
		//Print Sketch
		if (ae.getSource().equals(bPrint)) {
			System.out.println(traingui.sketch);
		}

		return;
	}
	
	/**
	 * Eable or disable the buttons of the bar depending on the situation
	 */
	public void updateBar() {
		// Choose the state
		if (traingui.dataset==null) state=EMPTY;
		else if ((traingui.index==0)&&(traingui.dataset.size()==1)) state=EMPTY;
		else if ((traingui.index==0)&&(traingui.dataset.size()!=0)) state=FIRST;
		else if (traingui.index==traingui.dataset.size()-1) state=LAST;
		else state=NORMAL;
		
		switch (state) {
			case FIRST:
				bDelete.setEnabled(true);
				bPrevious.setEnabled(false);
				bNext.setEnabled(true);
				break;
			case LAST:
				bDelete.setEnabled(true);
				bPrevious.setEnabled(true);
				bNext.setEnabled(false);
				break;
			case NORMAL:
				bDelete.setEnabled(true);
				bPrevious.setEnabled(true);
				bNext.setEnabled(true);
				break;
			case EMPTY:
				bDelete.setEnabled(false);
				bPrevious.setEnabled(false);
				bNext.setEnabled(false);
				break;
		}
		if (traingui.dataset!=null) {
			lPos.setText((traingui.index+1)+"/"+traingui.dataset.size());
		}
		return;
	}
}


/**
 * Panel in which the user can draw sketches
 * @author Daniel Sanchez
 *
 */
class SketchBoard extends JPanel {
	
	/**
	 * Parent TrainGUI component
	 */
	private TrainGUI traingui;
	
	public static final int WIDTH=256;
	public static final int HEIGHT=256;
	
	SketchBoard(TrainGUI v){
		super();
		traingui=v;
		
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setSize(new Dimension(WIDTH,HEIGHT));
		
		//Listen to  mouse press/release events
		MouseListener mLis = new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				traingui.sketch.addMouseEvent(Sketch.MOUSE_PRESS);
				Point p = new Point(me.getPoint().x,me.getPoint().y);
				traingui.sketch.addPoint(p);
			}
			
			public void mouseReleased(MouseEvent me) {
				traingui.sketch.addMouseEvent(Sketch.MOUSE_RELEASE);
				repaint();
			}
		};
		
		//Listen to  mouse drag (move while pressed) events and add points to the sketch
		MouseMotionListener mMLis = new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent me) {
				Point p = new Point(me.getPoint().x,me.getPoint().y);
				if ((p.x >= 0)&&(p.y >= 0)&&
						(p.x < SketchBoard.WIDTH)&&(p.y < SketchBoard.HEIGHT)){
					traingui.sketch.addPoint(p);
					repaint();
				}
			}
			
		};
		
		addMouseListener(mLis);
		addMouseMotionListener(mMLis);
	}
	
	/**
	 * Paint the drawing board
	 */
	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(Color.BLACK);
		if((traingui.sketch!=null) && (traingui.sketch.events.size()>1)) {
			for (int i=0; i<traingui.sketch.events.size()-1; i++) {
				Object o1 =  traingui.sketch.events.elementAt(i);
				Object o2 =  traingui.sketch.events.elementAt(i+1);
				//Draw lines
				if((o1 instanceof Point)&&(o2 instanceof Point)) {
					Point p1= (Point) o1;
					Point p2= (Point) o2;
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				} else if (i<traingui.sketch.events.size()-2){
					// Draw single points
					Object o3 = traingui.sketch.events.elementAt(i+2);
					if(!(o1 instanceof Point)&&(o2 instanceof Point)&&!(o3 instanceof Point)){
						Point p= (Point) o2;
						g.drawLine(p.x, p.y, p.x, p.y);
					}
				}
			}	
		}
	}
	
}


/**
 * Panel with the combo box to choose the label associated to the symbol
 * @author Daniel Sanchez
 */
class InfoPanel extends JPanel implements ActionListener, ItemListener {
	
	/**
	 * Parent TrainGUI component
	 */
	private TrainGUI traingui;
	
	public JButton bErase;
	
	public JComboBox cbSymbol;
	
	/**
	 * Contains the possible characters
	 */
	public Vector<Character> labels;
	
	/**
	 * Constructor
	 * @param v parent component
	 */
	InfoPanel(TrainGUI v) {
		super();
		traingui=v;
		
		cbSymbol=new JComboBox();
		cbSymbol.addItemListener(this);
		
		bErase= new JButton("Erase");
		bErase.addActionListener(this);
		
		parseLabels();
		
		setLayout(new FlowLayout());
		add(new JLabel("Corresponding symbol: "));
		add(cbSymbol);
		add(bErase);
		
	}
	
	/**
	 * Perform the actions rtelated to the erase button
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(bErase)) {
			traingui.sketch=new Sketch();
			traingui.dataset.setElementAt(traingui.sketch, traingui.index);
			
			int index=cbSymbol.getSelectedIndex();
			traingui.sketch.setLabel(labels.elementAt(index));
			traingui.board.repaint();
		}
	}
	
	/**
	 * Perform the actions related to a change in the item of the combo box
	 */
	public void itemStateChanged(ItemEvent ie) {
		int index=cbSymbol.getSelectedIndex();
		if ((traingui.sketch!=null)&&(labels.size()>=index)){
			traingui.sketch.setLabel(labels.elementAt(index));
		}
	}
	
	/**
	 * Updates the value of the combo box with the value of the actual sketch
	 */
	public void updateComboBox() {
		int index=labels.indexOf(new Character(traingui.sketch.getLabel()));
		cbSymbol.setSelectedIndex(index);
	}
	
	/**
	 * Reads the labels.txt file and extracts the possible labels and its names
	 */
	private void parseLabels() {
		//Create the labels vector
		labels = new Vector<Character>();
		
		try {
			RandomAccessFile input = new RandomAccessFile("labels.txt", "r");
		    while(true) {
		    	//Read a label
		    	String[] parts = input.readLine().trim().split("@");
		    	if (parts[0].equals("#")) break;
		    	//Skip invalid symbol declarations
		    	if (parts.length < 2) continue; 
		  		
		    	//Add the character value to the list
		    	char ch = (char)Integer.parseInt(parts[0],16);
		    	labels.add(new Character(ch));
		    	
		    	//Add the label to the combo box
		    	cbSymbol.addItem(new String(" " + ch + " - " + parts[1]));
		    }
		    
		    input.close();
		}
		catch(IOException e) {
			System.out.println("I/O Error:" + e.toString());
		}
	}
	
}