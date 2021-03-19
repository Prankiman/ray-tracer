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





