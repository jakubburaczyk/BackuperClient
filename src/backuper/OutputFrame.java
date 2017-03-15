package backuper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * Klasa okienka zawierajcego zapis komunikacji serwera z klientami
 * @author Jakub Buraczyk
 *
 */
public class OutputFrame extends JFrame implements WindowListener{
	
	/**
	 * Numer seryjny klasy
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Obszar w ktorym pokazywane beda komunikaty
	 */
	private static JTextArea logArea, errorArea;
	
	private static boolean vis = false;
	
	private static ArrayList<String> logs;	
	
	/**
	 * Konstruktor okna
	 */
	public OutputFrame(){
		super("Log programu Backuper");
			
		vis = true;
		
		setSize(500, 600);
		setLocationRelativeTo(null);
			
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(200,300));
		
		JPanel gui = new JPanel(new BorderLayout());
		JPanel upPanel = new JPanel(new GridLayout());
		JPanel downPanel = new JPanel(new GridLayout());
		
		upPanel.setPreferredSize(new Dimension(500,500));
		downPanel.setPreferredSize(new Dimension(500,100));
		
		JSplitPane spliter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, upPanel, downPanel);
		spliter.setDividerSize(6);
		spliter.setDividerLocation(upPanel.getHeight());		
		
		
		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setBackground(Color.DARK_GRAY);
		logArea.setForeground(Color.WHITE);
		logArea.setLineWrap(false);
		
		JScrollPane areaScrollPane1 = new JScrollPane(logArea, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
		ArrayList<String> txt = logs;
		int rowNumber = txt.size();
		for (int i=0; i<rowNumber;i++) 
			logArea.append(txt.get(i)+'\n');
		
		
		upPanel.add(areaScrollPane1);
		
		errorArea = new JTextArea();
		errorArea.setEditable(false);
		errorArea.setBackground(Color.LIGHT_GRAY);
		errorArea.setForeground(Color.RED);
		errorArea.setLineWrap(false);
		
		JScrollPane areaScrollPane2 = new JScrollPane(errorArea, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		downPanel.add(areaScrollPane2);
		
		gui.add(spliter);
		gui.add(upPanel, BorderLayout.CENTER);		
		gui.add(downPanel, BorderLayout.PAGE_END);
		
		add(gui);
		
		
		
		
		setVisible(true);
		
		try {
			Image logo=ImageIO.read(new File("img/output-icon.png"));
			setIconImage(logo);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie udało się wczytać pliku img/output-icon.png",
					 "Uwaga!", JOptionPane.ERROR_MESSAGE);
		}	
	}
	
	/**
	 * Metoda odczytujaca tekst z pliku wiersz po wierszu
	 * @param path
	 */
	public ArrayList<String> readText(String path){
		FileReader fr = null;
		BufferedReader bfr = null;
		String linia = "";

		ArrayList<String> txt = new ArrayList<String>();

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
		return txt;
	}
	
	public static void save(String line){
		logs.add(line);
	}
	
	public static void init(){
		logs = new ArrayList<String>();
	}
	
	/**
	 * Dodanie danego napisu do okna
	 * @param line dodawany napis
	 */
	public static void toOutput(String line){
		logArea.append(line + "\n");
	}
	
	public static void toError(String line){
		errorArea.append(line + "\n");
	}
	
	public static boolean isVis(){
		return vis;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		//vis = false;
		setVisible(false);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
