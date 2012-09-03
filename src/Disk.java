import java.awt.geom.Arc2D;


public class Disk {

	int x, y, width, height;
	
	double r;

	int group;
	
	boolean max=false;
	
	public Disk(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		this.r = (width+height)/2;
	}

	public Disk(int x, int y, double r) {
		this.x = x;
		this.y = y;
		this.r = r;
		
		this.width = (int)(2*r);
		this.height = (int)(2*r);
	}
	
	public Arc2D.Double getArc(){
		return new Arc2D.Double(x-width/2, y-width/2, width, height, 0, 360, Arc2D.Double.CHORD);
	}
	
}
