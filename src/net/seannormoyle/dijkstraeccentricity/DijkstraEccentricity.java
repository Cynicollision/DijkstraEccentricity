package net.seannormoyle.dijkstraeccentricity;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;

@SuppressWarnings("serial")
public class DijkstraEccentricity extends JFrame 
{
	// Constants
	private static final String APP_TITLE = "Dijkstra Eccentricity";
	private final int CANVAS_SIZE = 480;
	private final int MAX_NODES = 12;
	private final int NODE_SIZE = 24;
	private final String APP_DESCRIPTION = "By Sean Normoyle";
	private final String[] functionSelectOptions = { "Node tool", "Edge tool" };
	
	private int nodeCount = 0;
	private boolean isNodeFunction = true;
	private int[][] adj = new int[MAX_NODES][MAX_NODES];
	
	// For dijkstra's algorithm
	private LinkedList<Adjacency>[] adjList;
	
	private JPanel canvas;
	private DrawableNode[] nodes;
	
	/**
	 * Constructor
	 * Sets up the JFrame and initialises the adjacency list
	 */
	public DijkstraEccentricity()
	{
		super(APP_TITLE);
		
		// Initialize adjacency matrix
		for (int i = 0; i < MAX_NODES; i++)
			for (int j = 0; j < MAX_NODES; j++)
				adj[i][j] = 0;
		
		nodes = new DrawableNode[12];
		
		// Build GUI components
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		setJMenuBar(buildMenuBar());
		add(buildMainPanel());
		add(buildRightPanel());
		pack();
		setVisible(true);	
	}
	
	/**
	 * main
	 * Creates the JFrame
	 */
	public static void main(String[] args) 
	{
		new DijkstraEccentricity();
	}
	
	
	/**
	 * buildMenuBar
	 * @return Menu bar to be used by the JFrame
	 */
	private JMenuBar buildMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Control");
		
		menu.add(new JMenuItem(new MenuAction("New", "Create a new graph", KeyEvent.VK_N)));
		menu.addSeparator();
		menu.add(new JMenuItem(new MenuAction("Save", "Save this graph", KeyEvent.VK_S)));
		menu.add(new JMenuItem(new MenuAction("Open", "Open a graph", KeyEvent.VK_O)));
		menu.addSeparator();
		menu.add(new JMenuItem(new MenuAction("Info", "Application information", KeyEvent.VK_I)));
		menu.add(new JMenuItem(new MenuAction("Exit", "Exit the application", KeyEvent.VK_E)));
		menuBar.add(menu);
		
		return menuBar;
	}
	
	
	/**
	 * buildMainPanel
	 * @return Return the "canvas" JPanel
	 */
	private JPanel buildMainPanel()
	{
		canvas = new GraphEditorCanvas();
		return canvas;
	}
	
	/**
	 * buildRightPanel
	 * Builds the right side "control" JPanel and action listener for the combo box
	 */
	private JPanel buildRightPanel()
	{
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(CANVAS_SIZE/3, CANVAS_SIZE));
		
		JComboBox functionSelect = new JComboBox(functionSelectOptions);
		functionSelect.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				JComboBox cb = (JComboBox)e.getSource();
				isNodeFunction = (cb.getSelectedIndex() == 0);
			}
		});
		
		JButton goButton = new JButton("Go");
		goButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				findEccentricity(0);
			}
		});
		
		panel.add(new JLabel("Function:  "));
		panel.add(functionSelect);
		panel.add(goButton);
		return panel;
	}
	
	/**
	 * MenuAction
	 * Action listener for menu events
	 */
	public class MenuAction extends AbstractAction
	{
		// Keyboard shortcut mnemonic
		private int mnemonic;

		/**
		 *  Constructor
		 *  @param text Display text
		 *  @param desc Short description (hover text)
		 *  @param mnemonic Menu item keyboard mnemonic 
		 */
		public MenuAction(String text, String desc, Integer mnemonic)
		{
			super(text, null);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			
			// For use in actionPerformed()
			this.mnemonic = mnemonic.intValue();
		}

		/**
		 * actionPerformed TODO need to make the menu items work
		 * @param e Event
		 */
		public void actionPerformed(ActionEvent e)
		{
			switch (mnemonic)
			{
			// "New"
			case KeyEvent.VK_N:
				// Ask to save the list, then reset it
			break;
			
			// "Save"
			case KeyEvent.VK_S:
				saveGraph("graph.txt");
			break;
			
			// "Open"
			case KeyEvent.VK_O:
				openGraph("graph.txt");
			break;
			
			// "Info"
			case KeyEvent.VK_I:
				JOptionPane.showMessageDialog(null, APP_DESCRIPTION);
			break;

			// "Exit"
			case KeyEvent.VK_E:
				System.exit(0);
			break;
			}
		}
		
		private void saveGraph(String fname)
		{
			try
			{
				String s = "";
				FileWriter fw = new FileWriter(fname);
				BufferedWriter writer = new BufferedWriter(fw);
				for (int i = 0; i < MAX_NODES; i++)
				{
					for (int j = 0; j < MAX_NODES; j++)
						s += adj[i][j];
				}
				writer.write(s);
				writer.close();
				fw.close();
				JOptionPane.showMessageDialog(null, "Graph saved successfully");
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Woops");
			}
		}
		
		private void openGraph(String fname)
		{
			try
			{
				File f = new File(fname);
				Scanner sc = new Scanner(f);
				String s = sc.nextLine();
				System.out.println(s);
				for (int i = 0; i < MAX_NODES; i++)
					for (int j = 0; j < MAX_NODES; j++)
					{
						adj[i][j] = s.charAt((i*MAX_NODES)+j)-'0';
						adj[j][i] = s.charAt((i*MAX_NODES)+j)-'0';
					}
				repaint();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, "Problem opening/reading the file.");
			}
		}
	}
	
	/**
	 * GraphEditorCanvas
	 * Main panel for creating/editing/drawing graphs
	 */
	private class GraphEditorCanvas extends JPanel implements MouseListener, MouseMotionListener
	{
		private Rectangle2D.Double bg;
		private Rectangle2D.Double border;
		private int holdingNode;				// for dragging and dropping nodes
		private DrawableNode startNode = null; 	// for creating edges
		
		/**
		 * Constructor
		 */
		public GraphEditorCanvas()
		{
			bg = new Rectangle2D.Double(0, 0, CANVAS_SIZE-1, CANVAS_SIZE-1);
			border = new Rectangle2D.Double(0, 0, CANVAS_SIZE-1, CANVAS_SIZE-4);
			nodeCount = 0;
			holdingNode = -1;
			setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		/**
		 * Adds a new node at the given coordinates
		 * @param x
		 * @param y
		 */
		public void addNode(int x, int y)
		{
			if (nodeCount < MAX_NODES)
			{
				nodes[nodeCount] = new DrawableNode(nodeCount, x, y);
				nodeCount++;
				repaint();
			}
		}
		
		/**
		 * Deletes the node with the given value and fixes the adjacency matrix
		 * @param node Vertex label
		 */
		public void deleteNode(int node)
		{
			if (node == nodeCount)
			{
				nodes[node] = null;
				for (int i = 0; i < MAX_NODES; i++)
				{
					adj[i][node] = 0;
					adj[node][i] = 0;
				}	
			}
				
			else
			{
				// "Shift up" nodes
				for (int i = node; i < nodeCount-1; i++)
				{
					nodes[i] = nodes[i+1];
					nodes[i].setLabel(nodes[i].getLabel()-1);
				}
				
				// "Shift up" adjacency rows
				for (int i = node+1; i < MAX_NODES; i++)
					for (int j = 0; j < MAX_NODES; j++)
						adj[i-1][j] = adj[i][j];
					
				// "Shift right" adjacency columns
				for (int i = node+1; i < MAX_NODES; i++)
					for (int j = 0; j < MAX_NODES; j++)
						adj[j][i-1] = adj[j][i];
			}
				
			nodeCount--;
			repaint();
		}

		
		/**
		 * addEdge
		 * Establishes an adjacency with the given weight between the two 
		 * given DrawableNode objects
		 * @param startNode
		 * @param endNode
		 * @param weight
		 */
		public void addEdge(DrawableNode startNode, DrawableNode endNode, int weight)
		{
			adj[startNode.getLabel()][endNode.getLabel()] = weight;
			adj[endNode.getLabel()][startNode.getLabel()] = weight;
		}

		/**
		 * paintComponent
		 * @param g Graphics object
		 */
		public void paintComponent(Graphics g) 
		{
			super.paintComponent(g);
		    Graphics2D p = (Graphics2D)g;
		    
		    // Draw the background
		    p.setColor(Color.WHITE);
		    p.fill(bg);
		    p.setColor(Color.BLACK);
		    p.draw(border);
		    
		    // Draw nodes (nodes draw their own edges)
		    for (int i = 0; i < nodeCount; i++)
		    	nodes[i].draw(p);    
		 }

		@Override
		public void mousePressed(MouseEvent e) 
		{
			// Using the node tool
			if (isNodeFunction)
			{
				// Left click
				if (!e.isMetaDown())
				{
					for (int i = 0; i < nodeCount; i++)
					{		
						// Clicked on existing node
						if (nodes[i].getRegion().contains(e.getX(), e.getY()))
						{
							
							nodes[i].held = true;
							holdingNode = i;
							repaint();
							break;
						}
					}
					
					boolean positionFree = true;
					for (int i = 0; i < nodeCount; i++)
						if (nodes[i].getRegion().contains(e.getX(), e.getY()))
							positionFree = false;
					
					if (positionFree)
						addNode(e.getX()-12, e.getY()-12);
				}
				
				// Right click
				else
				{
					int node = -1;
					for (int i = 0; i < nodeCount; i++)
						if (nodes[i].getRegion().contains(e.getX(), e.getY()))
						{
							node = i;
							break;
						}
					if (node != -1)
						deleteNode(node);
				}
			}
			
			// Using the edge tool
			else if (!e.isMetaDown())
			{
				boolean clear = true;
				int w;
				String inputString;
				for (int i = 0; i < nodeCount; i++)
				{
					if (nodes[i].getRegion().contains(e.getX(), e.getY()))
					{
						clear = false;
						if ((startNode != null) && (nodes[i] != startNode))
						{
							// Clicking on second node of adjacency
							if (!startNode.isAdjacentTo(nodes[i]))
							{
								// Ask for a weight, just use 1 when given invalid input
								inputString = JOptionPane.showInputDialog("Enter a weight for this edge:");
								if (inputString.length() == 0)
									w = 1;
								else
								{
									try
									{
										w = Integer.parseInt(inputString);
										if (w < 1)
											w = 1;
									}
									catch (NumberFormatException error)
									{ 
										w = 1; 
									}
								}	
								
								addEdge(startNode, nodes[i], w);
							}
							
							// Reset selections
							startNode.setSelection(false);
							nodes[i].setSelection(false);
							startNode = null;
						}	
						else if (startNode == null)
						{
							// Clicking on first node of adjacency
							startNode = nodes[i];
							startNode.setSelection(true);	
						}
						repaint();
					}
				}
				
				// Clearing selections
				if (clear && (startNode != null))
				{
					clear = false;
					startNode.setSelection(false);
					startNode = null;
					repaint();
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) 
		{
			if (holdingNode != -1)
			{
				nodes[holdingNode].held = false;
				holdingNode = -1;
				repaint();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) 
		{
			if (holdingNode != -1)
				nodes[holdingNode].setLocation(e);

		}

		// Unused mouse listener methods
		@Override
		public void mouseClicked(MouseEvent e) { }
		@Override
		public void mouseEntered(MouseEvent e) { }
		@Override
		public void mouseExited(MouseEvent e) { }
		@Override
		public void mouseMoved(MouseEvent e) { }
	}
	
	/**
	 * DrawableNode class
	 * Represents a vertex of the graph and contains a draw method to draw 
	 * itself and its adjacencies. Also provides methods useful for various 
	 * operations on the graph.
	 */
	private class DrawableNode
	{
		private int label;
		private Rectangle shape, outline;
		private Point loc;
		private boolean held;		// for dragging/dropping nodes to move them
		private boolean selected;	// for creating edges
		
		/**
		 * Constructor
		 * @param label
		 * @param x
		 * @param y
		 */
		public DrawableNode(int label, int x, int y)
		{
			this.label = label;
			shape = new Rectangle(x, y, NODE_SIZE, NODE_SIZE);
			outline = new Rectangle(x-1, y-1, NODE_SIZE+1, NODE_SIZE+1);
			loc = new Point(x, y);
			held = false;
			selected = false;
		}
		
		/**
		 * draw
		 * Draws the node and its edges. Must be passed the Graphics2D
		 * object from the canvas class.
		 * @param p Graphics2D object being used to draw components
		 */
		public void draw(Graphics2D p)
		{
			int x1, x2, y1, y2;
			Line2D[] edges = new Line2D[MAX_NODES];
			DrawableNode otherNode;
			
			// Temporary copy of adjacency matrix so edges are only drawn once each
			int[][] e = new int[MAX_NODES][MAX_NODES];
			System.arraycopy(adj, 0, e, 0, adj.length);
			
			// Draw edges and weights
			for (int i = 0; i < MAX_NODES; i++)
			{
				otherNode = nodes[i];
				if (e[getLabel()][i] != 0 && (otherNode != null))
				{
					e[i][getLabel()] = 0;	// other node should not also draw this edge
					
					x1 = (int)loc.getX();
					y1 = (int)loc.getY();
					x2 = otherNode.getX();
					y2 = otherNode.getY();
					
					edges[i] = new Line2D.Float(x1+(NODE_SIZE/2), y1+(NODE_SIZE/2), x2+(NODE_SIZE/2), y2+(NODE_SIZE/2));
					
					p.setColor(Color.BLACK);
					p.draw(edges[i]);
					p.setColor(new Color(180, 0, 0));
					p.drawString("" + adj[getLabel()][i], x1+(x2-x1)/2, y1+(y2-y1)/2);
				}
			}
					
			// Draw the node & label
			if (held)
				p.setColor(Color.GRAY);
			else if (selected)
				p.setColor(Color.CYAN);
			else
				p.setColor(Color.LIGHT_GRAY);
			
			p.fill(shape);
			p.setColor(Color.BLACK);
			// TODO use the following line to show letters instead of indices
			//p.drawString("" + (char)('A' + label), (float)loc.getX()+(NODE_SIZE/3), (float)loc.getY()+(NODE_SIZE/2)+4);
			p.drawString("" + label, (float)loc.getX()+(NODE_SIZE/3), (float)loc.getY()+(NODE_SIZE/2)+4);
			p.draw(outline);
		}
		
		/**
		 * getRegion
		 * Used for determining if the mouse is within the bounds of this node
		 * @return Rectangle object representing clickable area
		 */
		public Rectangle getRegion()
		{
			return shape;
		}
		
		/**
		 * setLocation
		 * Moves the node to the location of the mouse event
		 * @param e Mouse-click event
		 */
		public void setLocation(MouseEvent e)
		{
			int mouseX = e.getX();
			int mouseY = e.getY();
			if ((mouseX < CANVAS_SIZE-(NODE_SIZE/2)) && (mouseX > (NODE_SIZE/2)) && (mouseY < CANVAS_SIZE-(NODE_SIZE/2)) && (mouseY > (NODE_SIZE/2)))
			{
				loc.setLocation(mouseX-(NODE_SIZE/2)-1, mouseY-(NODE_SIZE/2)-1);
				outline.setLocation(loc);
				loc.setLocation(mouseX-(NODE_SIZE/2), mouseY-(NODE_SIZE/2));
				shape.setLocation(loc);
				repaint();
			}
		}
		
		/**
		 * setLabel
		 * Changes the label of this node, used when deleting nodes from the graph
		 * @param label New label
		 */
		public void setLabel(int label)
		{
			this.label = label;
		}
		
		/**
		 * getLabel
		 * Returns the integer label of this node
		 * @return
		 */
		public int getLabel()
		{
			return label;
		}
		
		/**
		 * getX
		 * @return x-coordinate of this node
		 */
		public int getX()
		{
			return (int)loc.getX();
		}
		
		/**
		 * getY
		 * @return y-coordinate of this node
		 */
		public int getY()
		{
			return (int)loc.getY();
		}
		
		/**
		 * setSelection
		 * For moving nodes via drag/drop
		 * @param s True for being dragged, false for being dropped
		 */
		public void setSelection(boolean s)
		{
			selected = s;
		}
		
		/**
		 * isAdjacentTo
		 * @param other Questionably adjacent node
		 * @return True if adjacent to other node
		 */
		public boolean isAdjacentTo(DrawableNode other)
		{
			return (adj[getLabel()][other.getLabel()] != 0);
		}
	}
	
	public void buildAdjacencyLists()
	{
		adjList = new LinkedList[MAX_NODES];
		// Build adjacency lists
		for (int i = 0; i < MAX_NODES; i++)
		{
			adjList[i] = new LinkedList<Adjacency>();
			for (int j = 0; j < MAX_NODES; j++)
			{
				if (adj[j][i] != 0)
					adjList[i].add(new Adjacency(j, adj[j][i]));
				else if (adj[i][j] != 0)
					adjList[i].add(new Adjacency(j, adj[i][j]));
			}
		}
	}
	
	public void printAdjacencyLists()
	{
		for (int i = 0; i < nodeCount; i++)
		{
			System.out.print(i+"->");
			for (int j = 0; j < adjList[i].size(); j++)
			{
				System.out.print(adjList[i].get(j).label + "->");
			}
			System.out.println();
		}
	}
	
	public void findEccentricity(int start)
	{
		Vertex[] key = new Vertex[nodeCount];
		int[] predecessor = new int[nodeCount];
		int min_cost;
		int n = nodeCount;
		Vertex v;
		int w;
		
		buildAdjacencyLists();
		
		// dijkstra
		for (int i = 0; i < n; i++)
			key[i] = new Vertex(i, Integer.MAX_VALUE);

		key[start].pathWeight = 0;
		predecessor[start] = start;
		
		H h = new H();
		h.init(key, n);
		
		for (int i = 0; i < n; i++)
		{
			System.out.println("node " + i + "'s turn");
			v = h.minimum();
			min_cost = h.keyval(v.label);
			v = h.delete();
			
			for (int j = 0; j < adjList[v.label].size(); j++)
			{
				w = adjList[v.label].get(j).label;
				System.out.println("  adjacency to node " + w);
				
				if (h.isIn(w))
					System.out.println("  " + (min_cost + adjList[v.label].get(j).weight) + "<" + h.keyval(w));
				if (h.isIn(w) && (min_cost + adjList[v.label].get(j).weight < h.keyval(w)))
				{
					predecessor[w] = v.label;
					System.out.println("  predecessor of " + w + " is now " + v.label);
					h.decrease(key, w, min_cost + adjList[v.label].get(j).weight);
					System.out.println("  decrease weight of " + w + " to " + (min_cost + adjList[v.label].get(j).weight));
				}
			}
		}
		for (int i = 0; i < predecessor.length; i++)
			System.out.print(predecessor[i]+", ");
	}
	
	private class Vertex implements Comparable<Vertex>
	{
		private int pathWeight;
		private int label;
		
		public Vertex(int label, int pathWeight)
		{
			this.pathWeight = pathWeight;
			this.label = label;
		}
		
		@Override
		public int compareTo(Vertex other)
		{
			return this.pathWeight - other.pathWeight;
		}
	}
	
	private class H extends PriorityQueue<Vertex>
	{
		public void init(Vertex[] key, int n)
		{
			for (int i = 0; i < n; i++)
				add(key[i]);
		}
		
		public Vertex minimum()
		{
			return peek();
		}
		
		public Vertex delete()
		{
			return remove();
		}
		
		public boolean isIn(int w)
		{
			Object[] a = toArray();
			Vertex v;
			for (int i = 0; i < size(); i++)
			{
				v = (Vertex)a[i];
				if (v.label == w)
					return true;
			}
			return false;
		}
		
		public int keyval(int n)
		{
			int min = Integer.MAX_VALUE;
			Object[] a = toArray();
			Vertex v = peek();
			
			for (int i = 0; i < size(); i++)
			{
				v = (Vertex)a[i];
				if (v.label == n)
				{
					break;
				}		
			}
			
			for (int i = 0; i < size(); i++)
			{
				v = (Vertex)a[i];
				if (v.pathWeight < min)
					min = v.pathWeight;
			}
			return min;
		}
		
		public void decrease(Vertex[] key, int w, int weight)
		{
			Object[] a = toArray();
			Vertex v;
			
			for (int i = 0; i < size(); i++)
			{
				v = (Vertex)a[i];
				if (v.label == w)
				{
					v.pathWeight = weight;
					break;
				}	
			}
		}
	}
	
	private class Adjacency
	{
		int label, weight;
		public Adjacency(int label, int weight)
		{
			this.label = label;
			this.weight = weight;
		}
	}
}