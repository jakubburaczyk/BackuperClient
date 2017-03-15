package backuper;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Abstrakcyjna klasa bazowa dla wszystkich okien w programie
 * @author Jakub Buraczyk
 *
 */
public abstract class BaseWindow extends JFrame {
	/**
	 * Numer seryjny klasy
	 */
	static final long serialVersionUID = 1L;
	

	/**
	 * Kontstruktor klasy, nadaje odpowiednie wlascwiosci tworzonemu oknu
	 * @param title tytul okna
	 * @param width szerokosc okna
	 * @param height wysokosc okna
	 * @param isResizable czy istnieje mozliwosc zmiany rozmiaru okna przez uzytkownika
	 * @param pathToIcon sciezka do ikony dla danego okna
	 */
	public BaseWindow(String title, int width, int height, boolean isResizable, String pathToIcon){
		super(title);
		setSize(width, height);
		setLocationRelativeTo(null);
		setResizable(isResizable);		
		setMinimumSize(new Dimension(200,100));
		
		try {
			Image logo=ImageIO.read(new File(pathToIcon));
			setIconImage(logo);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie udało się wczytać pliku " + pathToIcon,
					 "Uwaga!", JOptionPane.ERROR_MESSAGE);
		}
		setVisible(true);
	}
}
