// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkBitmap.java,v $
// $RCSfile: LinkBitmap.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.OMBitmap;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

import java.awt.BasicStroke;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Reading and writing the Link protocol version of a bitmap..
 */
public class LinkBitmap implements LinkGraphicConstants, LinkPropertiesConstants {

    /**
     * Lat/Lon placement.  
     *
     * @param lt latitude of the top of the image.
     * @param ln longitude of the left side of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes bytes for the bitmap.
     * @param properties attributes for the bitmap.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float lt, float ln, int w, int h, 
			     byte[] bytes, 
			     LinkProperties properties,
			     DataOutputStream dos)
	throws IOException {
	
	dos.write(Link.BITMAP_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_BITMAP);
	dos.writeInt(RENDERTYPE_LATLON);
	dos.writeFloat(lt);
	dos.writeFloat(ln);
	dos.writeInt(w);
	dos.writeInt(h);
	
	dos.writeInt(bytes.length); 
	dos.write(bytes,0,bytes.length);

	properties.write(dos);
    }
  
    /**
     * XY placement. 
     *
     * @param x1 window location of the left side of the image.
     * @param y1 window location of the top of the image.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes bytes for the bitmap.
     * @param properties attributes for the bitmap.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(int x1, int y1, int w, int h, byte[] bytes, 
			     LinkProperties properties,
			     DataOutputStream dos)
	throws IOException {

	dos.write(Link.BITMAP_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_BITMAP);
	dos.writeInt(RENDERTYPE_XY);
	dos.writeInt(x1);
	dos.writeInt(y1);
	dos.writeInt(w);
	dos.writeInt(h);
	
	dos.writeInt(bytes.length);
	dos.write(bytes,0,bytes.length);
	
	properties.write(dos);
    }

    /**
     * Lat/lon placement with XY offset.
     *
     * @param lt latitude of the top of the image, before the offset.
     * @param ln longitude of the left side of the image, before the offset.
     * @param offset_x1 number of pixels to move image to the right.
     * @param offset_y1 number of pixels to move image down.
     * @param w width of the image, in pixels.
     * @param h height of the image, in pixels.
     * @param bytes bytes for the bitmap.
     * @param properties attributes for the bitmap.
     * @param dos DataOutputStream.
     * @throws IOException
     */
    public static void write(float lt, float ln, int offset_x1, int offset_y1,
			     int w, int h, byte[] bytes,
			     LinkProperties properties,
			     DataOutputStream dos)
	throws IOException {

	dos.write(Link.BITMAP_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_BITMAP);
	dos.writeInt(RENDERTYPE_OFFSET);
	dos.writeFloat(lt);
	dos.writeFloat(ln);
	dos.writeInt(offset_x1);
	dos.writeInt(offset_y1);
	dos.writeInt(w);
	dos.writeInt(h);
	
	dos.writeInt(bytes.length);	
	dos.write(bytes,0,bytes.length);

	properties.write(dos);
    }

    /**
     * Write a bitmap to the link.
     */
    public static void write(OMBitmap bitmap, Link link, LinkProperties props)
	throws IOException {

	switch (bitmap.getRenderType()) {
	case OMBitmap.RENDERTYPE_LATLON:
	    LinkBitmap.write(bitmap.getLat(), bitmap.getLon(),
			     bitmap.getWidth(), bitmap.getHeight(),
			     bitmap.getBits(), props, link.dos);
	    break;
	case OMBitmap.RENDERTYPE_XY:
	    LinkBitmap.write(bitmap.getX(), bitmap.getY(),
			     bitmap.getWidth(), bitmap.getHeight(),
			     bitmap.getBits(), props, link.dos);
	    break;
	case OMBitmap.RENDERTYPE_OFFSET:
	    LinkBitmap.write(bitmap.getLat(), bitmap.getLon(),
			     bitmap.getX(), bitmap.getY(),
			     bitmap.getBits(), props, link.dos);
	    break;
	default:
	    Debug.error("LinkBitmap.write: bitmap rendertype not handled.");
	}
    }

    /** 
     * Read a Bitmap off a DataInputStream.  Assumes the Bitmap
     * header has already been read.
     *
     * @param dis DataInputStream to read from.
     * @return OMBitmap
     * @throws IOException
     * @see com.bbn.openmap.omGraphics.OMBitmap 
     */
    public static OMBitmap read(DataInputStream dis)
	throws IOException {

	OMBitmap bitmap = null;
	float lat = 0;
	float lon = 0;
	int x = 0;
	int y = 0;
	int w = 0;
	int h = 0;
	int length, i;
	String url;

	int renderType = dis.readInt();
	
	switch (renderType){
	case RENDERTYPE_OFFSET:
	    lat = dis.readFloat();
	    lon = dis.readFloat();
	    // Fall through...		
	case RENDERTYPE_XY:
	    x = dis.readInt();
	    y = dis.readInt();
	    break;
	case RENDERTYPE_LATLON:
	default:
	    lat = dis.readFloat();
	    lon = dis.readFloat();
	}
	
	w = dis.readInt();
	h = dis.readInt();
	length = dis.readInt();
	
	byte[] bytes = new byte[length];
	dis.readFully(bytes);
	
	switch (renderType){
	case RENDERTYPE_OFFSET:
	    bitmap = new OMBitmap(lat, lon, x, y, w, h, bytes);
	    break;
	case RENDERTYPE_XY:
	    bitmap = new OMBitmap(x, y, w, h, bytes);
	    break;
	case RENDERTYPE_LATLON:
	default:
	    bitmap = new OMBitmap(lat, lon, w, h, bytes);
	}
	
	LinkProperties properties = new LinkProperties(dis);

	if (bitmap != null){
	    bitmap.setLinePaint(ColorFactory.parseColorFromProperties(
		properties, LPC_LINECOLOR,
		BLACK_COLOR_STRING, true));
	    bitmap.setFillPaint(ColorFactory.parseColorFromProperties(
		properties, LPC_FILLCOLOR,
		CLEAR_COLOR_STRING, true));
	    bitmap.setSelectPaint(ColorFactory.parseColorFromProperties(
		properties, LPC_HIGHLIGHTCOLOR,
		BLACK_COLOR_STRING, true));
	    bitmap.setStroke(new BasicStroke(LayerUtils.intFromProperties(
		properties, LPC_LINEWIDTH, 1)));
	    bitmap.setAppObject(properties);
	}

	return bitmap;
    }
}