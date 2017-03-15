package backuper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Klasa okna umozliwiajcego zmiane ustawien programu klienckiego
 * @author Jakub Buraczyk
 *
 */
public class SettingsWindow extends BaseWindow implements ActionListener{

	/**
	 * Numer seryjny klasy
	 */
	private static final long serialVersionUID = 666L;
	/**
	 * Pierwsze pole umozliwiajace zmiane wartosci ustawienia
	 */
	private JTextField tField1;
	/**
	 * Drugie pole umozliwiajace zmiane wartosci ustawienia
	 */
	private JTextField tField2;
	/**
	 * Trzecie  pole umozliwiajace zmiane wartosci ustawienia
	 */
	private JTextField tField3;
	/**
	 * Nazwa pierwszego parametru ustawien
	 */
	private JLabel label1;
	/**
	 * Nazwa drugiego parametru ustawien
	 */
	private JLabel label2;
	/**
	 * Nazwa trzeciego parametru ustawien
	 */
	private JLabel label3;
	/**
	 * Lewy przycisk zatwierdzajacy wybor
	 */
	private JButton leftButton;
	/**
	 * Prawy przycisk anulujacy wybor
	 */
	private JButton rightButton;
	
	/**
	 * Konstruktor okna wyboru ustawien 
	 * @param title tytul okna
	 * @param width szerokosc okna
	 * @param height wysokosc okna
	 * @param isResizable czy istnieje mozliwosc zmiany rozmiaru okna przez uzytkownika
	 * @param pathToIcon sciezka do ikony okna
	 */
	public SettingsWindow(String title, int width, int height, boolean isResizable, String pathToIcon)
	{
		super(title, width, height, isResizable, pathToIcon);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		setLayout(null);
		
		label1 = new JLabel("Podaj adres IP serwera:");
		label1.setBounds(50, 30, 300, 20);
		
		tField1 = new JTextField(Config.getAdress(), 20);
		tField1.setBounds(300, 30, 150, 20);
		
		label2 = new JLabel("Podaj numer portu serwera:");
		label2.setBounds(50, 70, 300, 20);
		
		tField2 = new JTextField(Integer.toString(Config.getPort()), 20);
		tField2.setBounds(300, 70, 150, 20);
		
		label3 = new JLabel("Podaj swój login:");
		label3.setBounds(50, 110, 300, 20);
		
		tField3 = new JTextField(Config.getName(), 20);
		tField3.setBounds(300, 110, 150, 20);
		
		leftButton = new JButton("Ok");
		leftButton.setBounds(100, height-70, 100, 20);
		
		rightButton = new JButton("Anuluj");
		rightButton.setBounds(300, height-70, 100, 20);
		
		add(label1);
		add(tField1);
		add(label2);
		add(tField2);
		add(label3);
		add(tField3);
		add(leftButton);
		add(rightButton);
		
		leftButton.addActionListener(this);
		rightButton.addActionListener(this);
	}
	
	/**
	 * Metoda oblugujaca reakcje na przycisniecie przez uzytkownika jednego z przyciskow 
	 */
	public void actionPerformed(ActionEvent e) {
		
		Object action = e.getSource();
		
		if (action.equals(leftButton))
		{
			Config.setAdress(tField1.getText());
			Config.setPort(Integer.parseInt(tField2.getText()));
			Config.setName(tField3.getText());
			Config.saveConfig();
			dispose();
			System.out.println(Config.getAdress());
			System.out.println(Config.getPort());
			System.out.println(Config.getName());
		}
		else if (action.equals(rightButton))
		{
			dispose();
		}
	}
}
