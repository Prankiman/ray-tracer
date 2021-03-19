package objects;

import java.awt.Color;

import main.Ray;
import main.Vect;
import objects.object3d;

public class plane extends object3d{
	
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