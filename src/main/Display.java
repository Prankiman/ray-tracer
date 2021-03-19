package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import main.Display.thread1;
import objects.Sphere;
import objects.object3d;
import objects.plane;

public class Display extends JPanel implements ActionListener, KeyListener{


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
	      
	  	   double f = 0, specular = 0;
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
	    		
	    		
	        	double luminance = 0.2f;
		     	f = luminance-(Vect.Vector_DotProduct(norm,Vect.Vector_Normalise(Vect.subV(hit, lightSrc))));
	        	
	        	
	        	
	        	specular = Math.max(-Vect.Vector_DotProduct(refl.direction, Vect.Vector_Normalise(Vect.subV(hit, lightSrc))), 0);
	        	
	        	f+= specular;
	        	
	        	if(f > 1) {
	        		f = 1f;
	        	}
	        	if(f < 0.3)//minimum difuse lighting
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














	
