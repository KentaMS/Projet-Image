package appli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Application {
	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat imgMat = Imgcodecs.imread("src/img/19.jpeg", Imgcodecs.IMREAD_ANYCOLOR);
		Mat greyMat = new Mat();
		greyMat = imgMat.clone();
		//HighGui.imshow("normal img", greyMat);
		//greyMat = sharpenImage(greyMat);
		
		//HighGui.namedWindow("Image contours", HighGui.WINDOW_AUTOSIZE);
		//HighGui.imshow("Image contours", houghLineTransform(detectEdges(greyMat, 100, 60, 5)));
		HighGui.imshow("Sharpened img", greyMat);
		HighGui.imshow("Image à bords", houghLineTransform(binarization(detectEdgesSobel(greyMat), 13)));
		//HighGui.imshow("Image binaire", binarization(blur(greyMat), 13));
		//HighGui.imshow("Image floutée", blur(greyMat));
		//HighGui.imshow("Image grise", greyMat);
		HighGui.waitKey();
	}
	
	public static Mat sharpenImage(Mat mSource) {
		Mat mSharpened = new Mat();
		
		Size size = new Size(0,0);
		Imgproc.GaussianBlur(mSource, mSharpened, size, 3);
		Core.addWeighted(mSource, 1.5, mSharpened, -0.5, 0, mSharpened);
		
		return mSharpened;
	}
	
	public static Mat houghLineTransform(Mat mSource) {
		Mat mLines = new Mat();
		Mat mLinesOnImage = new Mat();
		Imgproc.cvtColor(mSource, mLinesOnImage, Imgproc.COLOR_GRAY2BGR);
		
		int threshold = 30;
		int minLineLength = 60;
		int lineGap = 12;
		Imgproc.HoughLinesP(mSource, mLines, 1, Math.PI / 180, threshold, minLineLength, lineGap);

		for(int i = 0; i < mLines.rows(); i++) {
			double[] vec = mLines.get(i, 0);
	        double x1 = vec[0], 
	               y1 = vec[1],
	               x2 = vec[2],
	               y2 = vec[3];
	        Point start = new Point(x1, y1);
	        Point end = new Point(x2, y2);

	        Imgproc.line(mLinesOnImage, start, end, new Scalar(255,0,0), 3);
		}
		
		return mLinesOnImage;
	}
	
	public static Mat detectEdgesSobel(Mat mSource) {
		Mat mSourceGray = new Mat();
		Mat mHorizontalGradient = new Mat();
		Mat mAbsoluteHorizontalGradient = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		Imgproc.Sobel(mSourceGray, mHorizontalGradient, CvType.CV_16S, 0, 1, 3, 1, 10);
		Core.convertScaleAbs(mHorizontalGradient, mAbsoluteHorizontalGradient);
		
		return mAbsoluteHorizontalGradient;
	}
	
	public static Mat binarization(Mat mSource, int blockSize) {
		Mat binaryMat = new Mat();
		Mat mSourceGray = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		// blockSize must be odd
		Imgproc.adaptiveThreshold(mSourceGray, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, -60);
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
		 Imgproc.Canny(mSourceGray, mDetectedEdges, lowThreshold, lowThreshold * ratio, kernelSize, false);
		 return mDetectedEdges;
	}
	
	public static Mat filterEdges(Mat mEdges) {
		Mat filteredEdges = new Mat();
		Mat hierarchy = new Mat();
		Mat mSourceGray = new Mat();
		Size maxSize = mEdges.size();
		List<MatOfPoint> edgeList = new ArrayList<MatOfPoint>();
		
		Imgproc.findContours(mSourceGray, edgeList, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		//TODO
		
		return null;
	}
	
	public static Mat contours(Mat mSource) {
		Mat contours = new Mat();
		Mat hierarchy = new Mat();
		Mat mSourceGray = new Mat();
		List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();
		mSourceGray = returnGrayIfNotGray(mSource);
		
		Imgproc.findContours(mSourceGray, contourList, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
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
