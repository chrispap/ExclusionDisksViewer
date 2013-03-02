import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A panel that displays an image which represents a pseudospectrum and the
 * exclusion disks that were used by 2 different algorithms.<br>
 * When the mouse is clicked on this panel, a disk is shown up in the point the
 * click occurred.
 * 
 * @author Chris Papapaulou chrispapapaulou@gmail.com
 * 
 */
public class DiskViewer extends JPanel {
    private static final long serialVersionUID = 1L;

    /* Constants / Parameters */
    static final String user = System.getProperty("user.name");
    static final String dirPath = System.getProperty("user.dir") + "/";
    static final String imgPath = dirPath + "plot.png";
    static final String[] diskPaths = { dirPath + "disk1.txt", dirPath + "disk2.txt", };
    static final Color[] fillCol = { new Color(255, 255, 255, 80), new Color(0, 255, 055, 80), };
    static final String[] algorithms = { "General IE Algorithm", "Our New Approach", };
    static final Color borderCol = new Color(255, 0, 0, 0);

    /* Instance Variables */
    BufferedImage plotImg;
    ArrayList<Disk> diskFullList, diskPlotList;
    int activeGroup;
    boolean hideSameCenteredDisks = true;

    /**
     * 
     * @param plotImgPath
     *            path to the file with the pseudospectrum
     * @param diskDataPaths
     *            paths to the files containing the locations of the exclusion
     *            disks
     * @throws IOException
     */
    public DiskViewer(String plotImgPath, String[] diskDataPaths) throws IOException {
        plotImg = ImageIO.read(new File(plotImgPath));
        setPreferredSize(new Dimension(plotImg.getWidth(), plotImg.getHeight()));
        setMinimumSize(new Dimension(600, 400));
        setBackground(Color.BLACK);

        diskFullList = new ArrayList<Disk>(9000);
        diskPlotList = new ArrayList<Disk>(20);

        for (int i = 0; i < diskDataPaths.length; i++)
            loadDiskData(diskDataPaths[i], i);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                showDisk(evt.getPoint(), activeGroup);
            }
        });
    }

    /**
     * Paint the plot image and the disks that are in the diskPlotList. <br>
     * Use different color for disks of different groups.
     */
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D gg = (Graphics2D) g;
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* Draw the plot image */
        gg.drawImage(plotImg, null, 0, 0);

        /* Draw the disks */
        for (Disk d : diskPlotList) {
            gg.setColor(fillCol[d.group]);
            gg.fill(d.getArc());
            gg.setColor(borderCol);
            gg.draw(d.getArc());
        }

    }

    /**
     * Load the disks of a group.
     * 
     * @param diskDataPath
     *            path to the file with the locations
     * @param grp
     *            The group that the loaded disks will belong to.
     * @throws IOException
     */
    protected void loadDiskData(String diskDataPath, int grp) throws IOException {

        /* Populate the disk list */
        FileInputStream fstream = new FileInputStream(diskDataPath);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line, p[];
        Disk d1, d2;

        Integer.parseInt(br.readLine()); // skip first line
        line = br.readLine();
        p = line.split(",");
        d1 = new Disk(Integer.parseInt(p[0]), 1000 - Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                Integer.parseInt(p[3]));

        while ((line = br.readLine()) != null) {
            p = line.split(",");
            d2 = new Disk(Integer.parseInt(p[0]), 1000 - Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]));

            if (!(d2.x == d1.x && d2.y == d1.y)) d1.max = true;

            d1.group = grp;
            diskFullList.add(d1);
            d1 = d2;

        }

        in.close();

        diskPlotList = new ArrayList<Disk>(20);
    }

    /**
     * Set the group of which disks are displayed in each mouse click.
     * 
     * @param grp
     *            The new active group.
     */
    public void setActiveGroup(int grp) {
        activeGroup = grp;
    }

    /**
     * Look for a disk of this group that contains this point
     * 
     * @param p
     *            The point that the group should contain to be considered valid
     * @param grp
     *            The group that we are interested.
     */
    protected void showDisk(Point p, int grp) {
        ArrayList<Disk> rm = new ArrayList<Disk>(20);

        for (Disk d : diskFullList)
            if (d.group == grp && !(hideSameCenteredDisks && !d.max) && d.getArc().contains(p)) rm.add(d);

        for (Disk d : rm) {
            diskPlotList.add(d);
            diskFullList.remove(d);
        }

        repaint();
    }

    /**
     * Show AAAALL the disks that are loaded from all files
     */
    public void showAllDisks() {
        hideAllDisks();
        diskPlotList = diskFullList;
        diskFullList = new ArrayList<Disk>(1);
        repaint();
    }

    /**
     * Hide all the disks that are currently visible.
     */
    public void hideAllDisks() {
        diskFullList.addAll(diskPlotList);
        diskPlotList.clear();
        repaint();
    }

    /**
     * Construct a window containing a DiskViewer panel. <br>
     * Add some buttons to enable the selection of the algorithm to show.
     */
    public static void main(String[] args) {

        try {
            /* Create GUI components */
            final JFrame win = new JFrame("Exclusion Disks Viewer");
            final DiskViewer plotPanel = new DiskViewer(imgPath, diskPaths);
            final JPanel buttonPanel = new JPanel();
            final JButton hideButton = new JButton("Hide All");
            final JButton showButton0 = new JButton(algorithms[0]);
            final JButton showButton1 = new JButton(algorithms[1]);

            /* Assign operations to the buttons */
            showButton0.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    plotPanel.setActiveGroup(0);
                    showButton0.setEnabled(false);
                    showButton1.setEnabled(true);
                }
            });
            showButton1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    plotPanel.setActiveGroup(1);
                    showButton0.setEnabled(true);
                    showButton1.setEnabled(false);
                }
            });
            hideButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    plotPanel.hideAllDisks();
                }
            });

            /* Initialize */
            plotPanel.activeGroup = 0;
            showButton0.setEnabled(false);
            showButton1.setEnabled(true);

            /* Add the components to the window,panels - Show window */
            buttonPanel.setLayout(new GridLayout(1, 0));
            buttonPanel.add(showButton0);
            buttonPanel.add(showButton1);
            buttonPanel.add(hideButton);

            win.add(plotPanel, BorderLayout.CENTER);
            win.add(buttonPanel, BorderLayout.NORTH);
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            win.setBackground(Color.BLACK);
            win.setExtendedState(win.getExtendedState() | JFrame.MAXIMIZED_BOTH);

            win.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
