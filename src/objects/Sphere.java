package objects;

import java.awt.Color;

import main.Ray;
import main.Vect;
import objects.object3d;

public class Sphere  extends object3d{
    private double radius;
    public Vect position = new Vect();
    
    

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