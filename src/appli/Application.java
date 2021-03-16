package appli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Application {
	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat imgMat = Imgcodecs.imread("glass_img/19.png", Imgcodecs.IMREAD_ANYCOLOR);
		Mat greyMat = new Mat();
		Imgproc.cvtColor(imgMat, greyMat, Imgproc.COLOR_RGB2GRAY);
		
		//HighGui.imshow("Image contours", contours(detectEdges(greyMat, 300, 3, 5)));
		HighGui.imshow("Image à bords", detectEdges(blur(greyMat), 300, 3, 5));
		//HighGui.imshow("Image binaire", binarization(greyMat, 13));
		//HighGui.imshow("Image floutée", blur(greyMat));
		HighGui.imshow("Image grise", greyMat);
		HighGui.waitKey();
	}
	
	public static Mat binarization(Mat mSource, int blockSize) {
		Mat binaryMat = new Mat();
		Mat mSourceGray = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		// blockSize must be odd
		Imgproc.adaptiveThreshold(mSourceGray, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, 0);
		return binaryMat;
	}
	
	public static Mat blur(Mat mSource) {
		Mat blurredMat = new Mat();
		Mat mSourceGray = new Mat();
		int blurFilterSize = 4;
		mSourceGray = returnGrayIfNotGray(mSource);
		Imgproc.blur(mSourceGray, blurredMat, new Size(blurFilterSize, blurFilterSize));
		return blurredMat;
	}
	
	public static Mat detectEdges(Mat mSource, int lowThreshold, int ratio, int kernelSize) {
		 Mat mSourceGray = new Mat();
		 Mat mDetectedEdges = new Mat();
		 mSourceGray = returnGrayIfNotGray(mSource);
		 Imgproc.Canny(mSourceGray, mDetectedEdges,
		     lowThreshold, lowThreshold * ratio, kernelSize, false);
		 return mDetectedEdges;
	}
	
	public static Mat contours(Mat mSource) {
		Mat contours = new Mat();
		Mat hierarchy = new Mat();
		Mat mSourceGray = new Mat();
		List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();
		mSourceGray = returnGrayIfNotGray(mSource);
		
		Imgproc.findContours(mSourceGray, contourList, hierarchy,
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		contours.create(mSourceGray.rows(), mSourceGray.cols(), CvType.CV_8UC3);
	    Random r = new Random();
	    for (int i = 0; i < contourList.size(); i++) {
	        Imgproc.drawContours(contours, contourList, i, new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255)), -1);
	    }
		
		return contours;
	}
	
	private static Mat returnGrayIfNotGray(Mat mSource) {
		if (mSource.channels() != 1) {
			  Imgproc.cvtColor(mSource, mSource, Imgproc.COLOR_RGB2GRAY);
		}
		return mSource;
	}
}
