package main;

public class Camera{
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
