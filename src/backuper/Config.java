package backuper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Klasa sluzaca do wczytania ustawien programu
 * @author Jakub Buraczyk
 *
 */
public class Config {
	/**
	 * Plik z ustawieniami 
	 */
	private static File propertyFile;
	/**
	 * Obiekt ustawien
	 */
	private static Properties prop;
	/**
	 * Strumien wczytujacy dane z pliku
	 */
	private static InputStream is;
	/**
	 * Strumien zapisujacy dane do pliku
	 */
	private static FileOutputStream out;
	/**
	 * Adres serwera z ktorym uzytkownik chce sie polaczyc
	 */
	private static String adress;
	/**
	 * Numer portu na ktorym klient bedzie szukal polaczenia z serwerem
	 */
	private static int port;
	/**
	 * Nazwa uzytkownika
	 */
	private static String clientName;


	/**
	 * Konstruktor klasy, wczytuje plik konfiguracyjny
	 */
	public Config(){
		propertyFile = new File("settings/Config.txt");
		prop = new Properties();
		readConfig();
	}
	
	/**
	 * Metoda odczytujaca dane zawarte w pliku konfiguracyjnym
	 */
	public static void readConfig()  
	{
        try {
            is = new FileInputStream(propertyFile);
            prop.load(is);   
            is.close();
            
            adress = prop.getProperty("adres_serwera");
			port = Integer.parseInt(prop.getProperty("port_nasluchiwania"));
			clientName = prop.getProperty("nazwa_uzytkownika");	
        } catch (Exception e) {
			System.err.println("Exception: " + e);
		} 					
	}
	
	/**
	 * Metoda zapisujaca dane do pliku konfiguracyjnego po ich zmianie
	 */
	public static void saveConfig(){
		try {
			out = new FileOutputStream(propertyFile);
			prop.setProperty("adres_serwera", adress);
			prop.setProperty("port_nasluchiwania", Integer.toString(port));
			prop.setProperty("nazwa_uzytkownika", clientName);
			prop.store(out, null);
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println("Exception: " + e);
		}catch (IOException e) {
			System.err.println("Exception: " + e);
		}
	}

	/**
	 * Ustawienie numeru portu na ktorym klient bedzie szukal polaczenia z serwerem
	 * @param sPort numer portu
	 */
	public static void setPort(int sPort)
	{
		port = sPort;
	}
	
	/**
	 * Odczytanie numeru portu na ktorym klient bedzie szukal polaczenia z serwerem
	 * @return numer portu
	 */
	public static int getPort()
	{
		return port;
	}
	
	/**
	 * Ustwienie adresu z ktorym uzytkownik bedzie probowal sie polaczyc
	 * @param sAdress adres serwera
	 */
	public static void setAdress(String sAdress)
	{
		adress = sAdress;
	}
	
	/**
	 * Odczytanie adresu z ktorym uzytkownik bedzie probowal sie polaczyc
	 * @return adres serwera
	 */
	public static String getAdress()
	{
		return adress;
	}
	
	/**
	 * Ustawienie nazwy uzytkownika
	 * @param newName nazwa uzytkownika
	 */
	public static void setName(String newName)
	{
		clientName = newName;
	}
	
	/**
	 * Odczytanie nazwy uzytkownika
	 * @return nazwa uzytkownika
	 */
	public static String getName()
	{
		return clientName;
	}
}
