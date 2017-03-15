package backuper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * Klasa okna umozliwiajcego pokazanie okna zawierajacego tekst - instrukcje programu i informacje o autorze
 * @author Jakub Buraczyk
 *
 */
public class TextWindow extends BaseWindow implements ActionListener{
	static final long serialVersionUID = 1L;

	/**
	 * Przycisk zamykajacy okno
	 */
	private JButton button;
	
	/**
	 * Wczytany tekst
	 */
	private static List<String> txt; 
	
	/**
	 * Konstruktor okna
	 * @param title tytul okna
	 * @param width szerokosc okna
	 * @param height wysokosc okna
	 * @param isResizable czy istnieje mozliwosc zmiany rozmiaru okna przez uzytkownika
	 * @param pathToIcon sciezka do ikony okna
	 * @param pathToText sciezka do pliku z tekstem
	 * @param buttonText napis wyswietlany na przycisku
	 */
	public TextWindow(String title, int width, int height, boolean isResizable, 
			String pathToIcon, String pathToText, String buttonText) {
		super(title, width, height, isResizable, pathToIcon);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		txt = new ArrayList<String>();
		setLayout(null);
		readText(pathToText);
		int rowNumber = txt.size();
		
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);		
		area.setEditable(false);
		area.setBounds(10, 10, width-20, height-100);
		button = new JButton(buttonText);
		button.setBounds(width/2-50, height-70, 100, 20);
		for (int i=0; i<rowNumber;i++) area.append(txt.get(i)+'\n');
		JScrollPane areaScrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		areaScrollPane.setBounds(7, 10, getWidth()-14, getHeight()-100);
		add(areaScrollPane);
		add(button);		
		button.addActionListener(this);
		
	}
	
	/**
	 * Metoda odczytujaca tekst z pliku wiersz po wierszu
	 * @param path
	 */
	public void readText(String path)
	{
		FileReader fr = null;
		BufferedReader bfr = null;
		String linia = "";


		try {
			fr = new FileReader(path);
			bfr = new BufferedReader(fr);
			
			while((linia = bfr.readLine()) != null)
			{
				txt.add(linia);
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("BŁĄD PRZY OTWIERANIU PLIKU!");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bfr.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Metoda obslugi wciasniecia przez uzytkownika przycisku
	 */
public void actionPerformed(ActionEvent e) {

		Object action = e.getSource();

		if (action.equals(button))
		{
			dispose();
		}
		
	}

}
