package objects;

import java.awt.Color;

import main.Ray;
import main.Vect;

public abstract class object3d{
	public Color c;
	
	Vect normal;
	
	public boolean seeThrough;
	
	public double reflectivity;
	
	public Vect getNormalAt(Vect point){
		
		
		return normal;
		
	}

	public Vect intersects(Ray refl) {
		return null;
		
	}
}
