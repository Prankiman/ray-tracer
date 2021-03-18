package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


public class Main {


	static BufferedImage skybox;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			Display d = new Display();
			JFrame f = new JFrame();
			try {
				skybox = ImageIO.read(Main.class.getClassLoader().getResource("nightSky.jpg"));//ImageIO.read(new File("res/time_square.jpg"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			f.setSize(500,500);
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			try {
				f.setIconImage(ImageIO.read(new File("res/raytracer.png")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			f.setVisible(true);
			Display.spherecenter2.x = 0; Display.spherecenter2.y = 0; Display.spherecenter2.z = -1f;
			Display.spherecenter3.x = -1; Display.spherecenter3.y = 0.5; Display.spherecenter3.z = 0.5f;
			Display.spherecenter.x = 1; Display.spherecenter.y = 0; Display.spherecenter.z = 0f;
			Display.addObjects(Display.s, Display.p, Display.s2, Display.s3);
			Display.ready = true;
			f.add(d);
			f.addKeyListener(d);
			
			//Display.spherecenter.x = 0; Display.spherecenter.y = -0.1; Display.spherecenter.z = 3f;
			
			Display.orig.x = 0f; Display.orig.y = 0f; Display.orig.z = 0;
			
	}
	
	

}



class Display extends JPanel implements ActionListener, KeyListener{


 	/*TODO
 	 * 
 	 * add skybox
 	 * 
 	 * be able to add more objects easier
 	 * 
 	 * 
 	 * add specular lighting
 	 * 
 	 * add refractions for see-through objects
 	 * 
 	 * */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int Height = 1080, Width = 1920;
	
	Timer t = new Timer(0,this);
	
	
	static double Yaw, Pitch = 0;
	
	Color[][] pixels = new Color[Width][Height];
	
	static Vect orig = new Vect(), cam = new Vect();
	
	static Vect spherecenter = new Vect();
	
	static Vect spherecenter2 = new Vect();
	
	static Vect spherecenter3 = new Vect();
	
	int res = 16;
	
	Graphics g;
	
	static Sphere s = new Sphere(spherecenter ,0.4f, new Color(0.2f, 0.2f, 0.9f), 0.5);//creating a sphere
	
	static Sphere s2 = new Sphere(spherecenter2 ,0.2f, new Color(0.2f, 0.2f, 0.2f), 1);//creating a sphere
	static Sphere s3 = new Sphere(spherecenter3 ,0.4f, new Color(0.9f, 0.2f, 0.2f), 0.1);//creating a sphere
	
	static plane p= new plane(new Color(0.9f,0.3f,0.4f), 0.7f, 0.8);
  
	static List <object3d> objects = new ArrayList<object3d>();
	
	static boolean ready = false;
	
	int rlim = 4;

	int maxThreads = Runtime.getRuntime().availableProcessors()*2;
	
	int numOfThreads = maxThreads;//needs to be adjusted depending on threads running and the number of cores on your cpu
	
	class thread1 extends Thread{
		
		int threadnum;
		
		public thread1(int threadnum) {
			this.threadnum = threadnum;
		}
		
		@Override
		public void run() {
			raycast(threadnum);
		}
	}
	
	static void addObjects(object3d...obj) {
		for(object3d o: obj)
			objects.add(o);
	}
	
	  Color raytrace(Ray ray, int recursionlim) {
		  
  		   Vect hit = new Vect();
	       object3d ob = null;
	       object3d ob2 = null;
	       Ray refl;
	       Vect norm;
	       Vect rPI;
	      
	  	   double f = 0;
	  	   Vect lightblocked = new Vect(); //ray used to check if the ray is being blocked
	       
	       Vect lightSrc = new Vect();
			//creating a vector representing ilumination point used to determine shadows and lighting/shading
	       lightSrc.x = 0f;  lightSrc.z = -10; lightSrc.y = -20f;
	        
	       if(closestHit(ray) != null){
		       	ob = closestHit(ray).o;
		       	hit = closestHit(ray).v;
	       }
	       else {
	    	   ob = null;
	    	   hit = null;
	       }
	       
	       	Color cl = Color.blue;
	        Color reflC = Color.blue;
	        try {
	        	cl = skyBox(Vect.Vector_Mul(ray.direction, -1));
	        }catch(Exception e) {}
	        if (hit != null && ob != null) {
	        
	        
	        	
	        	
	        	refl = Vect.reflect(hit, ray.direction, ob.getNormalAt(hit));
	        	
	        	if(closestHit(refl) != null){
	        		ob2 = closestHit(refl).o;
	        	}
	        	else ob2 = null;
	        		
	        	if(ob2 != null)
	        		rPI = ob2.intersects(refl);//were the reflectionray intersects the other object
	        	else 
	        		rPI = null;
	        	
	        	norm =  ob.getNormalAt(hit);
	        
	        	//trace the reflected ray and get reflected color
	    		if(refl != null && recursionlim > 0) {
		        	reflC = raytrace(refl, recursionlim-1);
	    		}
	    		
	    		
	        	double luminance = 0.7f;
		     	f = luminance-(Vect.Vector_DotProduct(norm,Vect.Vector_Normalise(Vect.subV(hit, lightSrc))));
	        	if(f > 1) {
	        		f = 1f;
	        	}
	        	if(f < 0.3)//minimum lighting
	        		f = 0.3;
	        	
	        	Ray shadowray = new Ray();
	        	
	    		shadowray.direction = Vect.Vector_Normalise(Vect.subV(lightSrc, hit));
	    		shadowray.origin = Vect.addV(hit, Vect.Vector_Mul(shadowray.direction, 0.001));//adding a bit to the origin to avoid bugs
	    		
	    		if(closestHit(shadowray) != null && closestHit(shadowray).o != ob)// checking if the shadowray hits an object thats not the object at the origin
	    			lightblocked = closestHit(shadowray).v;
	    		else
	    			lightblocked = null;
	    		
	    		if(lightblocked != null ) {//if the shadowray intersects the object make the color darker
	    			cl = new Color((int)(skyBox(Vect.Vector_Mul(refl.direction, -1)).getRed()*f*ob.reflectivity+ob.c.getRed()*f*(1-ob.reflectivity))/5,//if the ray does not intersect other object then the sky color should be reflected on to the sphere
	    					(int)(skyBox(Vect.Vector_Mul(refl.direction, -1)).getGreen()*f*ob.reflectivity+ob.c.getGreen()*f*(1-ob.reflectivity))/5,
	    					(int)(skyBox(Vect.Vector_Mul(refl.direction, -1)).getBlue()*f*ob.reflectivity+ob.c.getBlue()*f*(1-ob.reflectivity))/5);
            		
            	}
    			else
    				cl = new Color((int)(skyBox(Vect.Vector_Mul(refl.direction, -1)).getRed()*f*ob.reflectivity+ob.c.getRed()*f*(1-ob.reflectivity)),
	    					(int)(skyBox(Vect.Vector_Mul(refl.direction, -1)).getGreen()*f*ob.reflectivity+ob.c.getGreen()*f*(1-ob.reflectivity)) ,
	    					(int)(skyBox(Vect.Vector_Mul(refl.direction, -1)).getBlue()*f*ob.reflectivity+ob.c.getBlue()*f*(1-ob.reflectivity)));
	    		
	    		
	    		//if the reflection ray has intersected other object
	    		if(rPI != null) {
	    			if(lightblocked != null )
	    				cl = new Color((int)(reflC.getRed()*f*ob.reflectivity+ob.c.getRed()*f*(1-ob.reflectivity))/5,
	    					(int)(reflC.getGreen()*f*ob.reflectivity+ob.c.getGreen()*f*(1-ob.reflectivity))/5 ,
	    					(int)(reflC.getBlue()*f*ob.reflectivity+ob.c.getBlue()*f*(1-ob.reflectivity))/5);//reflC;//new Color((int)((reflC.getBlue()*f/6)+ob.c.getBlue()*f/2),(int)((reflC.getGreen()*f/6)+ob.c.getGreen()*f/2),(int)((reflC.getBlue()*f/6)+ob.c.getBlue()*f/2));
	    			else
	    				cl = new Color((int)(reflC.getRed()*f*ob.reflectivity+ob.c.getRed()*f*(1-ob.reflectivity)),
		    					(int)(reflC.getGreen()*f*ob.reflectivity+ob.c.getGreen()*f*(1-ob.reflectivity)) ,
		    					(int)(reflC.getBlue()*f*ob.reflectivity+ob.c.getBlue()*f*(1-ob.reflectivity)));
	    		}	
	    		
	    		
	    		
	        }
	        return cl;
	      
	  }
	
	  
	void raycast(int threadnum)
	{
		
		Vect lightSrc = new Vect();
		//creating a vector representing ilumination point used to determine shadows and lighting/shading
		lightSrc.x = 0f;  lightSrc.z = -1; lightSrc.y = -2f;

     	//https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays -- examples of how to set up rays from camera position

		// For each row...
	    for (int  y = 0; y < Height; y+=res)
	    {  
	       
	        // For each "pixel" across the row...
	        for (int x = threadnum*res; x < Width; x+=res*numOfThreads)
	        {          
	        	
	        	double xu, yu;    
	        	
	        	//normalize screen coordinates for shooting rays through each pixel
	            if (Width > Height) {
	                xu = (float)(x - Width/2+Height/2) / Height * 2 - 1;
	                yu =  ((float) y / Height * 2 - 1);
	            } else {
	                xu = (float)x / Width * 2 - 1;
	                yu =  ((float) (y - Height/2+Width/2) / Width * 2 - 1);
	            }
		        
	            // Find where this pixel sample hits in the scene
	            Ray ray = new Ray();
	            Camera cam = new Camera();
	           
	            ray = cam.makeCameraRay(
	              	orig,
	                xu,
	                yu);
		         
	         	
	            s.position = spherecenter;

	          
	           pixels[x][y] = raytrace(ray, rlim);
	           
	           
	  
          	}
	        
	    }
      
	}	
	
	
	Hit closestHit(Ray r) {
	 
		Hit h = null;
				
		for(object3d o: objects) {
				
		 Vect hitPos = o.intersects(r);
			if(hitPos != null && (h == null ||Vect.Vector_Length(Vect.subV(h.v, r.origin)) >= Vect.Vector_Length(Vect.subV(hitPos, r.origin)))) {
				h = new Hit(o, hitPos);
			}
		}
		
		return h;
	}
	
		 Color skyBox(Vect d) {
	     	//UV mapping/unwrapping https://en.wikipedia.org/wiki/UV_mapping
	        float u = (float) (0.5+Math.atan2(d.x, d.z)/(2*Math.PI));
	        float v = (float) (0.5 - Math.asin(d.y)/Math.PI);
	        try {
	            return new Color((Main.skybox.getRGB((int)(u*(Main.skybox.getWidth()-1)), (int)(v*(Main.skybox.getHeight()-1)))));//-1 so that the coordinates are inside the skybox-image
	        } catch (Exception e) {
	            System.out.println("U: "+u+" V: "+v);
	            e.printStackTrace();

	            return new Color(0.2f, 0.4f, 0.8f);
	        }
	    }

	public void paintComponent(Graphics g) {
		this.g = g;
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.fillRect(0, 0, (int)Width, (int)Height);

		if(numOfThreads > maxThreads)
			numOfThreads = maxThreads;
		
		for(int i = 1; i < numOfThreads; i ++) {//creating and starting the diferent threads;
			thread1 t1 = new thread1(i);
			t1.start();
		}
		
		raycast(0);
		t.start();
	         
	    for(int x = 0; x < Width; x+=res) {
	    	for(int y = 0; y < Height; y+=res) {

	    		if(still < (8*numOfThreads > 50 ? 16*numOfThreads : 100)) {
		    		g2.setColor(pixels[x][y]);
	        		g2.fillRect((int)x, (int)y, res, res);
	    		}
	          
	        }
	    }
		
	}
	int still;
	@Override
	public void keyPressed(KeyEvent arg0) {
		res = 16;
		still = 0;
		// TODO Auto-generated method stub
		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			Vect temp = new Vect();
			temp.x = 0; temp.y = 0; temp.z = 0.01;
			orig = Vect.subV(orig, Vect.rotateYP(temp, Display.Yaw, Display.Pitch));//move camera backwards according to its orientation
		}
		if(arg0.getKeyCode() == KeyEvent.VK_W) {
			Vect temp = new Vect();
			temp.x = 0; temp.y = 0; temp.z = 0.01;
			orig = Vect.addV(orig, Vect.rotateYP(temp, Display.Yaw, Display.Pitch));// move camera forward accordning to its orientation
		}
		
		if(arg0.getKeyCode() == KeyEvent.VK_A) {
			Vect temp = new Vect();
			temp.x = 0.01; temp.y = 0; temp.z = 0;
			orig = Vect.subV(orig, Vect.rotateYP(temp, Display.Yaw, Display.Pitch));//move camera left accoring to its orientation
		}
		if(arg0.getKeyCode() == KeyEvent.VK_D) {
			Vect temp = new Vect();
			temp.x = 0.01; temp.y = 0; temp.z = 0;
			orig = Vect.addV(orig, Vect.rotateYP(temp, Display.Yaw, Display.Pitch));// move camera right according to its orientation
		}
		if(arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
			orig.y+=0.1;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_SPACE) {
			orig.y -=0.1;
		}
		
		 //change rotation arround y acces(looking up/down)
		if(arg0.getKeyCode() == KeyEvent.VK_UP) {
			Pitch += 0.1;                         
		}
		if(arg0.getKeyCode() == KeyEvent.VK_DOWN) {
			Pitch -=0.1;         
		}
		 //change rotation arround x acces(looking left/right)
		if(arg0.getKeyCode() == KeyEvent.VK_LEFT) {
			Yaw -=0.1;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
			Yaw +=0.1;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		still ++;
		
		if(still > (8*numOfThreads > 50 ? 8*numOfThreads : 50))
			res = 1;
		if(still < (8*numOfThreads > 50 ? 16*numOfThreads : 100))
			repaint();
		
	}
	
}


class Camera{
	Ray makeCameraRay(
            Vect origin,
            double xScreenPos0To1,
            double yScreenPos0To1)
		{
	
		
		
			Ray ray = new Ray();
			
			// Set up ray info
			ray.origin = origin;
			
			Vect v = new Vect(), eyePos = new Vect();
			v.x = xScreenPos0To1;
			v.y = yScreenPos0To1;
			v.z = 0;//v.z = -1;
			
			eyePos.x = 0;
			eyePos.y = 0;
			eyePos.z = -1/Math.tan(Math.PI/8);

			ray.direction = Vect.rotateYP(Vect.Vector_Normalise(Vect.subV(v, eyePos)), Display.Yaw, Display.Pitch);
			
			
			Ray r = new Ray();
			r.origin = Vect.addV(eyePos, origin);
			r.direction = ray.direction;
			 //RayHit hit = scene.raycast(new Ray(eyePos.add(cam.getPosition()), rayDir));
			
			return r;
			
		}
	
}

class Hit{
	object3d o;
	Vect v;
	Hit(object3d o, Vect v){
		this.o = o; this.v = v;
	}
}

abstract class object3d{
	Color c;
	
	Vect normal;
	
	double reflectivity;
	
	Vect getNormalAt(Vect point){
		
		
		return normal;
		
	}

	Vect intersects(Ray refl) {
		return null;
		
	}
}

class Sphere  extends object3d{
    private double radius;
    Vect position = new Vect();
    
    

    public Sphere(Vect position, double radius, Color c, double refl) {
        this.radius = radius;
        this.position = position;
        this.c = c;
        this.reflectivity = refl;
    }
    
    @Override
    public  Vect intersects(Ray r){
    	
    	
    	//https://www.youtube.com/watch?v=bQKy3N4TshU&ab_channel=TwoMinutePapers :video on ray-sphere intersection
        //https://www.youtube.com/watch?v=cDi-uti2oLQ&ab_channel=TwoMinutePapers :implementaion of sphere intersection at 4:10
    	
    	Vect center = this.position;
        Vect oc = Vect.subV(r.origin,center);
        //double a = Vect.Vector_DotProduct(r.direction, r.direction);//the a cancels out so not calculating a will speed up the intersection function
        double b = (double)(2.0 * Vect.Vector_DotProduct(oc, r.direction));
        double c = Vect.Vector_DotProduct(oc,oc) - (this.radius*this.radius);
        double discriminant = b*b - (4*c);// double discriminant = b*b - (4*c);
       
        if(discriminant < 0)
        	return null;
        else discriminant = Math.sqrt(discriminant);
        			
        double t1 = 0;
        double t2 = 0;
       
        t1 = (double) (-b+discriminant);
        t2 = (double) (-b-discriminant);
       
        double t = t1;
        if(t2  < t1)
        	t = t2;
        
        if(discriminant > 0 && t > 0) {
        	return Vect.addV(r.origin, Vect.Vector_Mul(r.direction, t/(2)));//Vect.addV(r.origin, Vect.Vector_Mul(r.direction, t/(2*a)));
        }
        else return null;
        
        
        //https://www.youtube.com/watch?v=HFPlKQGChpE&ab_channel=TheArtofCode : other way to calculate sphere ray intersection
        
//        double t = Vect.Vector_DotProduct(Vect.subV(position, r.origin), r.direction);
//        Vect p =  Vect.addV(r.origin, Vect.Vector_Mul(r.direction, t));
//
//        double y = Vect.Vector_Length(Vect.subV(position, p));
//        if (y < radius) {
//            double x = (double) Math.sqrt(radius*radius - y*y);
//            double t1 = t-x;
//            if (t1 > 0) return Vect.addV(r.origin, Vect.Vector_Mul(r.direction, t1));
//            else return null;
//        } else {
//            return null;
//        }
    }

    @Override
    public Vect getNormalAt(Vect point) {
       Vect t = new Vect();
       
       t.x = (point.x-position.x)/radius;
       t.y = (point.y-position.y)/radius;
       t.z = (point.z-position.z)/radius;
       return Vect.Vector_Normalise(t);
    }
}


class plane extends object3d{
	
	double planeheight;
	
	
	public plane(Color c, double ph, double refl) {
		this.planeheight = ph;
		this.c = c;
		this.reflectivity = refl;
	}
	@Override
	public Vect intersects(Ray r) {

		double t = -(r.origin.y-this.planeheight) / r.direction.y;
	    if (t > 0 && Double.isFinite(t))
	    {
	    	return Vect.addV(r.origin, Vect.Vector_Mul(r.direction, t));
	    }
	
	    return null;
	}
	@Override
	public Vect getNormalAt(Vect point) {
		Vect v = new Vect();
		v.x = 0; v.y = 1; v.z =0;
		return v;
	}
	
	
}

class Ray{
	Vect origin;
	Vect direction;
}

class Vect{
	double x;
	double y;
	double z;
	
//	public Vect(int x, int y, int z) {
//		this.x = x;
//		this.y = y;
//		this.z = z;
//	}
	
	static Vect subV(Vect v1, Vect v2) {
		Vect v3 = new Vect();
		v3.x = v1.x-v2.x;
		v3.y = v1.y-v2.y;
		v3.z = v1.z-v2.z;
		return v3; 
	}
	static Vect addV(Vect v1, Vect v2) {
		Vect v3 = new Vect();
		v3.x = v1.x+v2.x;
		v3.y = v1.y+v2.y;
		v3.z = v1.z+v2.z;
		return v3; 
	}
	
	static Vect Vector_Mul(Vect v1, double k)
	{
		Vect v2 = new Vect();
		v2.x = v1.x*k;
		v2.y = v1.y*k;
		v2.z = v1.z*k;
		return v2;
	}

	static Vect Vector_Div(Vect v1, double k)
	{
		Vect v2 = new Vect();
		v2.x = v1.x/k;
		v2.y = v1.y/k;
		v2.z = v1.z/k;
		return v2;
	}
	
	static double Vector_DotProduct(Vect v1, Vect v2)
	{
		return v1.x*v2.x + v1.y*v2.y + v1.z * v2.z;
	}
	
	static double Vector_Length(Vect v)
	{
		return (double) Math.sqrt(Vector_DotProduct(v, v));
	}

	static Vect Vector_Normalise(Vect v)
	{
		double l = Vector_Length(v);
		Vect v2 = new Vect();
		v2.x = v.x/l;v2.y = v.y/l;v2.z = v.z/l;
		return v2;
	}
	static Vect cross(Vect v1, Vect v2) {
		Vect v3 = new Vect();
		v3.x = (v1.y*v2.z)-(v1.z*v2.y);
		v3.y = (v1.z*v2.x)-(v1.x*v2.z);
		v3.z = (v1.x*v2.y)-(v1.y*v2.x);
		return v3;
	}

	
	
	
	// http://paulbourke.net/geometry/reflected/  : how to calculate reflection Vector
	static Ray reflect(Vect v, Vect dir, Vect normal) {
		Vect reflectionVector = Vect.subV(dir, Vect.Vector_Mul(normal, 2*Vect.Vector_DotProduct(dir, normal)));
		
		Vect reflectionRayOrigin = Vect.addV(v, Vect.Vector_Mul(reflectionVector, 0.0000001));//need to add a little so that the reflectionVector does not intersect an object at i5ts origin(that would cause problems)

    	Ray r = new Ray();
    	r.origin = reflectionRayOrigin;
    	r.direction = reflectionVector;
    	return r;
	}
	
	static Vect Matrix_MultiplyVector(double[][] m, Vect n)
	{
		Vect v = new Vect();
		
			v.x = n.x * m[0][0] + n.y * m[1][0] + n.z * m[2][0];
			v.y= n.x * m[0][1] + n.y * m[1][1] + n.z * m[2][1];
			v.z = n.x * m[0][2] + n.y * m[1][2] + n.z * m[2][2];
		
		return v;
	}
	static Vect rotateYP(Vect v, double yaw, double pitch) {
        // Convert to radians
        double yawRads = yaw;
        double pitchRads = pitch;

        Vect rotateY = new Vect(), rotateX = new Vect();
        
        // Rotate around the Y axis (pitch)
        rotateY.x = v.x;
        rotateY.y = (double) (v.y*Math.cos(pitchRads) + v.z*Math.sin(pitchRads));
        rotateY.z = (double) (-v.y*Math.sin(pitchRads) + v.z*Math.cos(pitchRads));
        
        //Rotate around X axis (yaw)
        rotateX.y = rotateY.y;
        rotateX.x = (double) (rotateY.x*Math.cos(yawRads) + rotateY.z*Math.sin(yawRads));
        rotateX.z = (double) (-rotateY.x*Math.sin(yawRads) + rotateY.z*Math.cos(yawRads));

        
        return rotateX;
    }
	
}

