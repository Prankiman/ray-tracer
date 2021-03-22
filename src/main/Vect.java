package main;

public class Vect{
	public double x;
	public double y;
	public double z;
	
//	public Vect(int x, int y, int z) {
//		this.x = x;
//		this.y = y;
//		this.z = z;
//	}
	
	public static Vect subV(Vect v1, Vect v2) {
		Vect v3 = new Vect();
		v3.x = v1.x-v2.x;
		v3.y = v1.y-v2.y;
		v3.z = v1.z-v2.z;
		return v3; 
	}
	public static  Vect addV(Vect v1, Vect v2) {
		Vect v3 = new Vect();
		v3.x = v1.x+v2.x;
		v3.y = v1.y+v2.y;
		v3.z = v1.z+v2.z;
		return v3; 
	}
	
	public static  Vect Vector_Mul(Vect v1, double k)
	{
		Vect v2 = new Vect();
		v2.x = v1.x*k;
		v2.y = v1.y*k;
		v2.z = v1.z*k;
		return v2;
	}

	public static  Vect Vector_Div(Vect v1, double k)
	{
		Vect v2 = new Vect();
		v2.x = v1.x/k;
		v2.y = v1.y/k;
		v2.z = v1.z/k;
		return v2;
	}
	
	public static  double Vector_DotProduct(Vect v1, Vect v2)
	{
		return v1.x*v2.x + v1.y*v2.y + v1.z * v2.z;
	}
	
	public static  double Vector_Length(Vect v)
	{
		return (double) Math.sqrt(Vector_DotProduct(v, v));
	}

	public static  Vect Vector_Normalise(Vect v)
	{
		double l = Vector_Length(v);
		Vect v2 = new Vect();
		v2.x = v.x/l;v2.y = v.y/l;v2.z = v.z/l;
		return v2;
	}
	public static  Vect cross(Vect v1, Vect v2) {
		Vect v3 = new Vect();
		v3.x = (v1.y*v2.z)-(v1.z*v2.y);
		v3.y = (v1.z*v2.x)-(v1.x*v2.z);
		v3.z = (v1.x*v2.y)-(v1.y*v2.x);
		return v3;
	}

	
	
	
	// http://paulbourke.net/geometry/reflected/  : how to calculate reflection Vector
	public static  Ray reflect(Vect v, Vect dir, Vect normal) {
		Vect reflectionVector = Vect.subV(dir, Vect.Vector_Mul(normal, 2*Vect.Vector_DotProduct(dir, normal)));
		
		Vect reflectionRayOrigin = Vect.addV(v, Vect.Vector_Mul(reflectionVector, 0.0000001));//need to add a little so that the reflectionVector does not intersect an object at i5ts origin(that would cause problems)

    	Ray r = new Ray();
    	r.origin = reflectionRayOrigin;
    	r.direction = reflectionVector;
    	return r;
	}
	public static  Ray refract(Vect v, Vect dir, Vect normal) {// https://www.scratchapixel.com/lessons/3d-basic-rendering/introduction-to-shading/reflection-refraction-fresnel--refraction
	double n2 = 1.52;//refractionindex
	
		Ray ry = new Ray();
		
	    double cosi = Vect.Vector_DotProduct(dir, normal);
	    double etai = 1.000273, etat = n2;
	    Vect n = normal; 
	    
	    if (cosi < 0) {
	    	cosi = -cosi;
	    } 
	    else {
	    	n = Vect.Vector_Mul(normal, -1);
	    } 
	    double eta = etai / etat; 
	    double k = 1 - eta * eta * (1 - cosi * cosi); 
	    //if k < 0 then its total iternal reflection meaning no refraction;( happens wjem etat < etai
	    ry.direction =  k < 0 ? null : Vect.addV(Vect.Vector_Mul(dir, eta) , Vect.Vector_Mul(n,(eta * cosi - Math.sqrt(k)))); 
		 
		ry.origin = Vect.addV(v, Vect.Vector_Mul(ry.direction, 0.0001));
		
		return ry;
	}
	
	public static  Vect Matrix_MultiplyVector(double[][] m, Vect n)
	{
		Vect v = new Vect();
		
			v.x = n.x * m[0][0] + n.y * m[1][0] + n.z * m[2][0];
			v.y= n.x * m[0][1] + n.y * m[1][1] + n.z * m[2][1];
			v.z = n.x * m[0][2] + n.y * m[1][2] + n.z * m[2][2];
		
		return v;
	}
	public static  Vect rotateYP(Vect v, double yaw, double pitch) {
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