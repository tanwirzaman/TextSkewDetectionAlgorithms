package org.csatl.edu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/*
Text Skew Angle detection on scanned document images by A. Papandreou and B. Gatos
REF: Papandreou, A. and Gatos, B. “A novel skew detection technique based on vertical projections.” In Proc. of International Conference on Document Analysis and Recognition (ICDAR), pp. 384-388, Sept. 18-21, 2011, Beijing, China.
*/
public class PapandreoTechnique {
	
	static final Scalar COLOR_GREEN = new Scalar(0, 255, 0);
	static final Scalar COLOR_RED = new Scalar(0, 0, 255);
	
	static final String SOURCE_PATH="Input_PATH";
	static final String DEST_PATH="Output_PATH";
	static final String OPENCV_PATH="OPENCV_PATH";
	public static void main(String[] args) {
		
		Mat mat=Highgui.imread(INPUT_PATH);
		Imgproc.threshold(mat, mat, 205, 255, Imgproc.THRESH_BINARY_INV);
		int angle=skewDetectImageRotation(mat);	

		System.out.println("Angle="+angle);

		System.out.println("done");
		
	}
	
	private static int skewDetectImageRotation(Mat mat) {
		int[] projections = null;
		HashMap<Integer, Double> angle_measure=new HashMap<Integer, Double>();
		HashMap<Integer, Double> area_measure=new HashMap<Integer, Double>();
		ArrayList<Point> pointList=new ArrayList<Point>();  
		for(int theta=-5;theta<=5;theta=theta+1)
		{	
			if(theta == 0)
				continue;
			Mat rotImage=RotateImage(mat,-theta);
			projections=new int[mat.cols()];
			for(int i=0;i<mat.cols();i++)
			{
				double[] pixVal;
				for(int j=0;j<mat.rows();j++)
				{
					
					pixVal= rotImage.get(j, i);
					if(pixVal[0]==255)
					{
						pointList.add(new Point(j,i));
						projections[i]++;
					}
				}
			}
			Mat tempMat=rotImage;
			for(int c=0;c<mat.cols();c++)
			{				
				DrawProjection(c,projections[c],tempMat);				
			}			
			Highgui.imwrite(OUTPUT_PATH+theta+".jpg",tempMat);
			angle_measure.put(theta, criterion_func(projections));
			
			MatOfPoint2f points=new MatOfPoint2f();	
		    points.fromList(pointList);
			RotatedRect rrect=Imgproc.minAreaRect(points);
			area_measure.put(theta, rrect.size.width*rrect.size.height);
			
			pointList.clear();
			
		}
		int angle=0;
		double val=0;
		Double area = 0.0;
		for(int k: angle_measure.keySet())
		{
			if(val<angle_measure.get(k))
			{
				//if(area < area_measure.get(k))
				{				
					area= area_measure.get(k);
					val=angle_measure.get(k);
					angle=k;
				}
			}
		}
		return angle;
	}
	
	private static Mat RotateImage(Mat rotImg,double theta)
	{
		double angleToRot=theta;	
		
		Mat rotatedImage = new Mat();
		if(angleToRot>=92 && angleToRot<=93)
		{		
			Core.transpose(rotImg, rotatedImage);
		}
		else
		{
			org.opencv.core.Point center = new org.opencv.core.Point(rotImg.cols()/2, rotImg.rows()/2);
			Mat rotImage = Imgproc.getRotationMatrix2D(center, angleToRot, 1.0);
			
			Imgproc.warpAffine(rotImg, rotatedImage, rotImage, rotImg.size());		
		}
		
		return rotatedImage;
			
	}
	
	private static double criterion_func(int[] projections) {
		double max=0;		
		
		for(int i=0;i<projections.length;i++)
		{
			if(max<Math.pow(projections[i],2))
			{
				max=Math.pow(projections[i],2);
			}
		}
		
		return max;
	}

	
	private static void DrawProjection(int colnum,int projCount,Mat image) {
		final Point pt1 = new Point(-1, 0);
        final Point pt2 = new Point();        
        pt1.x = colnum;
        pt2.x = colnum;
        pt2.y = projCount;
        Core.line(image, pt1, pt2, COLOR_GREEN);
	}

}
