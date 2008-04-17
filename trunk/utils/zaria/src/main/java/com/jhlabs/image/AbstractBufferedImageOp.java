/**
 * Copyright 2005 Huxtable.com. All rights reserved.
 * From http://www.jhlabs.com/ip/blurring.html, including
 * this file on the basis of the license :
 * 
 * There's source code in Java for pretty well everything I talk 
 * about here. I make no claims that these are optimised in any way 
 * I've opted for simplicity over speed everywhere and you'll 
 * probably be able to make most of these thing go faster with a 
 * bit of effort. You can use the source code for anything you want, 
 * including commercial purposes, but there's no liability. If your 
 * nuclear power station or missile system fails because of an 
 * improper blur, it's not my fault.
 */

package com.jhlabs.image;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * A convenience class which implements those methods of BufferedImageOp which are rarely changed.
 */
public abstract class AbstractBufferedImageOp implements BufferedImageOp {
	
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
		if ( dstCM == null )
			dstCM = src.getColorModel();
		return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
	}
	
	public Rectangle2D getBounds2D( BufferedImage src ) {
		return new Rectangle(0, 0, src.getWidth(), src.getHeight());
	}
	
	public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
		if ( dstPt == null )
			dstPt = new Point2D.Double();
		dstPt.setLocation( srcPt.getX(), srcPt.getY() );
		return dstPt;
	}
	
	public RenderingHints getRenderingHints() {
		return null;
	}
	
	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
	 * penalty of BufferedImage.getRGB unmanaging the image.
	 */
	public int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
		return image.getRGB( x, y, width, height, pixels, 0, width );
	}
	
	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
	 * penalty of BufferedImage.setRGB unmanaging the image.
	 */
	public void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			image.getRaster().setDataElements( x, y, width, height, pixels );
		else
			image.setRGB( x, y, width, height, pixels, 0, width );
	}
}
