package edu.usu.csatl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/*
Text Skew Angle detection on scanned document images by J.J. Hull
REF: Hull, J.J. "Document image skew detection: survey and annotated bibliography," In J.J. Hull, S.L. Taylor (eds.), Document Analysis Systems II, World Scientific Publishing Co., 1997, pp. 40-64.
*/

public class HullTextSkewDetect {

	static final String SOURCE_PATH="Input_PATH";
	static final String DEST_PATH="Output_PATH";
	static final String OPENCV_PATH="OPENCV_PATH";
	
	static final Scalar COLOR_GREEN = new Scalar(0, 255, 0);
	static final Scalar COLOR_RED = new Scalar(0, 0, 255);
	
	public static void main(String[] args) {
		System.load(OPENCV_PATH);
		
		final File ImageFolder = new File(SOURCE_PATH);
		File dir = new File(DEST_PATH);
		dir.mkdir();
		for (final File fileEntry : ImageFolder.listFiles()) {
			String filepath=fileEntry.getAbsolutePath();
			System.out.println("Processing: "+filepath);
			java.util.Date dateS= new java.util.Date();
		    Mat mat=Highgui.imread(filepath);
		
			int angle=skewDetectPixelRotation(mat);
			
			System.out.println("Angle="+angle);
			//Core.putText(mat, "Theta:"+angle, new org.opencv.core.Point(20,mat.rows()-30) , Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(0, 0, 0));
			java.util.Date dateF= new java.util.Date();
			long time_difference=dateF.getTime()-dateS.getTime();
			//appendLog(DEST_PATH, ""+time_difference);
			appendLog(DEST_PATH, filepath+"_Angle: "+angle);
			
		}
		System.out.println("done");
	}

	
	private static Mat RotateImage(Mat rotImg,double theta)
	{
		Mat rotatedImage = new Mat();
		try{
		double angleToRot=theta;	
		
		
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
		}
		catch(Exception e){}
		return rotatedImage;
			
	}

	

	private static int skewDetectPixelRotation(Mat mat) {
		int[] projections = null;
		int[] angle_measure=new int[181];

		for(int theta=0;theta<=180;theta=theta+5)
		{	
			projections=new int[mat.rows()];
			for(int i=0;i<mat.rows();i++)
			{				
				double[] pixVal;				
				for(int j=0;j<mat.cols();j++)
				{
					pixVal= mat.get(i, j);
					if(pixVal[0]==0)//black pixel
					{
						int new_row=rotate(i,j,theta,mat);
						if(new_row>=0 && new_row<mat.rows())
							projections[new_row]++;
					}
				}
			}
			Mat tempMat=mat.clone();
			for(int r=0;r<mat.rows();r++)
			{				
				DrawProjection(r,projections[r],tempMat);				
			}			
			//Highgui.imwrite(DEST_PATH+"/out_"+theta+".jpg",tempMat);
			angle_measure[theta]=criterion_func(projections);
			
		}
		int angle=0;
		int val=0;
		for(int i=0;i<angle_measure.length;i++)
		{
			if(val<angle_measure[i])
			{
				val=angle_measure[i];
				angle=i;
			}
		}
		return angle;
	}

	private static int criterion_func(int[] projections) {
		int max=0;		
		//use below code for image rotation
		//for(int i=0;i<projections.length-1;i++)
			//result+=Math.pow((projections[i+1]-projections[i]), 2);
		for(int i=0;i<projections.length;i++)
		{
			if(max<projections[i])
			{
				max=projections[i];
			}
		}
		
		return max;
	}

	
	//Rotation about the center of the image
	private static int rotate(double y1, double x1, int theta,Mat mat) {
		int x0=mat.cols()/2;
		int y0=mat.rows()/2;
		
		int new_col=(int) ((x1-x0)*Math.cos(Math.toRadians(theta))-(y1-y0)*Math.sin(Math.toRadians(theta))+x0);
		int new_row=(int) ((x1-x0)*Math.sin(Math.toRadians(theta))+(y1-y0)*Math.cos(Math.toRadians(theta))+y0);
		
		return new_row;
		
	}

	private static void DrawProjection(int rownum,int projCount,Mat image) {
		final Point pt1 = new Point(0, -1);
        final Point pt2 = new Point();        
        pt1.y = rownum;
        pt2.x = projCount;
        pt2.y = rownum;
        Core.line(image, pt1, pt2, COLOR_GREEN);
	}
	

	public static void appendLog(String destPath,String text)
	{       
	   File logFile = new File(destPath+"/Anglelog.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {        
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	  }
	
}
