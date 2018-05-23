package com.streamreceiver.window;


import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class StreamReceiverWindow extends Frame implements ActionListener, Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public static StreamReceiverWindow streamReceiverWindow;
	public final String destinationDirectory = "tmp";
	
	private Image image;
	private JLabel jlabel;
	private int frameCount = 0;
	
	private Function<Void, Void> callback;

	private int width = 640;
	private int height = 360;


	private boolean stopped = false;
	
	private StreamReceiverWindow() {
		super("Stream Receiver");
	}
	
	public void initiate(byte[] data, int width, int height) {
		File dir = new File(destinationDirectory);
		if(!dir.exists() && !dir.mkdirs()){
	    	System.out.println("Failed to create directory");
	    	return;
	    }
		this.width = width;
		this.height = height;
		
		ConvertRawToPng(data);
		
		JFrame frame = new JFrame();
		jlabel = new JLabel(new ImageIcon(image));
		frame.getContentPane().add(jlabel);
		frame.pack();
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        System.out.println("fechando");
		        callback.apply(null);
		        stopped = true;
		    }
		});
		
		frame.setVisible(true);  
	}
	
	public void updateFrame(byte[] data) {
		ConvertRawToPng(data);		
		jlabel.setIcon(new ImageIcon(image));
	}

	public void setOnCloseCallback(Function<Void, Void> callback){
		this.callback = callback;
	}
	
	public void ConvertRawToPng(byte[] data) {
		int samplesPerPixel = 3;
		int[] bandOffsets = {0, 1, 2}; // BGRA order

		DataBuffer buffer = new DataBufferByte(data, data.length);
		WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, samplesPerPixel * width, samplesPerPixel, bandOffsets, null);

		ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

		BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
		//System.out.println("image: " + image); // Should print: image: BufferedImage@<hash>: type = 0 ...


		//Fix the problem of the texture upside down
		AffineTransform tx;
		tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		this.image = image;
		
		try {
			FileOutputStream writer = new FileOutputStream((destinationDirectory + "\\frame" + String.format("%05d", frameCount++)) + ".jpeg", false); 
			ImageIO.write(image, "JPEG", writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static StreamReceiverWindow GetInstance() {
		if(streamReceiverWindow == null)
			streamReceiverWindow = new StreamReceiverWindow();
		return streamReceiverWindow;
	}
	
	@Override
	public void update(Graphics g){
		g.drawImage(image, 0, 0, this);
	}
	
	@Override
	public void run() {
		while (!stopped ) {	
			try { Thread.sleep(45); } 
			catch (InterruptedException e) {
				return;
			}
	
			update(this.getGraphics());
			repaint();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
