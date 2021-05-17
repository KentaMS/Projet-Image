package appli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Application {
	
	// Enumeration des modes correspondants au type de verre.
	enum Mode {
		VERRE_A_PIED,
		CLASSIQUE
	}
	
	// Initialisation des listes des différents niveaux du verre trouvés.
	public ArrayList<Integer> objets = new ArrayList<>();
	public ArrayList<Integer> objetsFiltres = new ArrayList<>();
	
	// Initialisation du niveau du liquide à 0%.
	public static int niveauLiquide = 0;
	
	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Le mode choisi par défault est est celui du verre classique.
		Mode mode = Mode.CLASSIQUE; 
		
		// Lecture de l'image sélectionnée.
		Mat imgMat = Imgcodecs.imread("src/img/19.jpeg", Imgcodecs.IMREAD_ANYCOLOR);
		
		// Conversion de l'image en niveaux de gris.
		Mat greyMat = new Mat();
		greyMat = imgMat.clone();
		HighGui.imshow("normal img", greyMat);

		HighGui.imshow("Image à bords", openImage(binarization(detectHorizontalEdgesSobel(sharpenImage(greyMat)))));

		HighGui.waitKey();
	}
	
	/**
	 * Fonction rendant l'image plus nette afin de faciliter la détection de contours.
	 * @param mSource L'image que l'on souhaite rendre net.
	 * @return Une image plus nette.
	 */
	public static Mat sharpenImage(Mat mSource) {
		Mat mSharpened = new Mat();
		
		Size size = new Size(0,0);
		Imgproc.GaussianBlur(mSource, mSharpened, size, 3);
		Core.addWeighted(mSource, 1.5, mSharpened, -0.5, 0, mSharpened);
		
		return mSharpened;
	}
	
	/**
	 * Détecte les contours horizontaux de l'image en utilisant le procédé de Sobel.
	 * @param mSource L'image dont les contours sont à détecter.
	 * @return Une image représentant les contours horizontaux de l'image donnée.
	 */
	public static Mat detectHorizontalEdgesSobel(Mat mSource) {
		Mat mSourceGray = new Mat();
		Mat mHorizontalGradient = new Mat();
		Mat mAbsoluteHorizontalGradient = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		Imgproc.Sobel(mSourceGray, mHorizontalGradient, CvType.CV_16S, 0, 1);
		Core.convertScaleAbs(mHorizontalGradient, mAbsoluteHorizontalGradient);
		
		return mAbsoluteHorizontalGradient;
	}
	
	/**
	 * Applique une ouverture à une image.
	 * @param mSource L'image à ouvrir.
	 * @return Une image représentant l'image donnée ouverte.
	 */
	public static Mat openImage(Mat mSource) {
		int horizontalSize = mSource.cols() / 35;
		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalSize, 1));
		Imgproc.erode(mSource, mSource, horizontalStructure, new Point(-1, -1));
		Imgproc.dilate(mSource, mSource, horizontalStructure, new Point(-1, -1));
		
		return mSource;
	}
	
	/**
	 * Applique une binarisation à une image.
	 * @param mSource L'image à binariser.
	 * @return Une image représentant la version binarisée de l'image donnée.
	 */
	public static Mat binarization(Mat mSource) {
		Mat binaryMat = new Mat();
		Mat mSourceGray = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		// blockSize must be odd
		int blockSize = 13;
		Imgproc.adaptiveThreshold(mSourceGray, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, -60);
		
		return binaryMat;
	}
	
	/**
	 * Transforme une image en niveaux de gris si elle ne l'est pas déjà.
	 * @param mSource L'image a transformer.
	 * @return Une image représentant la version en niveaux de gris de l'image donnée.
	 */
	private static Mat returnGrayIfNotGray(Mat mSource) {
		if (mSource.channels() != 1) {
			  Imgproc.cvtColor(mSource, mSource, Imgproc.COLOR_RGB2GRAY);
		}
		
		return mSource;
	}
}
