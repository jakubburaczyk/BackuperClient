package backuper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 * Klasa obslugujaca polaczenie z serwerem ze strony klienta
 * @author Jakub Buraczyk
 *
 */
public class MyClient implements Runnable{

    /**
     * Gniazdo serwera
     */
    private Socket socket;

    /**
     * Lokalna instancja okna glownego
     */
    private MainWindow mw;
    
    /**
	 * Buforowany czytnik strumienia znakow przychodzacego od serwera
	 */
    private BufferedReader input;
    /**
     * Obiekt obslugujacy wysylanie strumien znakow do serwera
     */
    private PrintWriter output;
    
    /**
     * Czytnik strumienia danych
     */
    private FileInputStream fis;
    /**
     * Buforowany czytnik strumienia danych przychodzacych z serwera
     */
    private BufferedInputStream bis;
    
    /**
     * Strumien danych do pliku od serwera
     */
    private FileOutputStream fos;
    /**
     * Strumien danych od klienta
     */
    private DataOutputStream dos;
	/**
	 * Zmienna informujaca czy klient jest polaczony z serwerem
	 */
    private boolean connected;
    /**
     * Sciezka do ktorej zostanie pobrany plik z serwera
     */
    private String downloadFilePath;
    
    
    /**
     * Konstruktor klienta
     * @param mw instancja okna glownego programu
     * @throws Exception rzucany wyjatek
     */
    public MyClient(MainWindow mw) throws Exception {
        setConnected(false);
        this.mw = mw;
    }
    
    /**
	 * Nadpisana metoda obslugi watku
	 */
    @Override
    public void run() {
        while (true)
            try {
                String command = receive();
                mw.toOutput("Wiadomosc od serwera: " + command);
                if (!handleCommand(command)) {
                	close();
                    break;
                }
            } catch (IOException e) {
            	e.printStackTrace();
            }          
    }
    
    /**
     * Inicjacja strumieni odczytu i wysylania danych oraz rozpoczecie polaczenia z serwerems
     * @param host adres serwera
     * @param port numer portu przez ktory klient probuje sie polaczyc z serwerem
     * @throws IOException rzucany wyjatek
     */
    public synchronized void init(String host, int port) throws IOException{
        socket = new Socket(host, port);      
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);      
        new Thread(this).start();
        send(Protocol.LOGIN + " " + Config.getName());  	
    }
    
    /**
     * Obsluga komend odbieranych od serwera
     * @param command komenda od serwera
     * @return true jesli udalo sie zareagowac na otrzymana komende, false jesli sie to nie powiodlo lub polaczenie jest konczone
     * @throws IOException rzucany wyjatek
     */
    private boolean handleCommand(String command) throws IOException{
        StringTokenizer st = new StringTokenizer(command);
        String cd = st.nextToken();
        
        if (cd.equals(Protocol.ASKFORPASSWORD)){
        	String password = JOptionPane.showInputDialog("Podaj hasło:");
        	System.out.println(password);
        	send(password);
        }
        
        else if (cd.equals(Protocol.NEWPASSWORD)){
        	String password = JOptionPane.showInputDialog("Utwórz hasło:");
        	System.out.println("Nowe hasło: " + password);
        	send(password);

        }
        
        else if (cd.equals(Protocol.LOGGEDIN)){
        	setConnected(true);
        	Vector<String> remoteFiles = new Vector<String>();
        	while (st.hasMoreTokens()){
        		String remoteFileName = st.nextToken();
        		remoteFiles.addElement(remoteFileName);
        	}	     	
        	mw.getRemoteFileTree(remoteFiles);
        } 
        
        else if (cd.equals(Protocol.SENDINGFILE)){
        	String fileName = st.nextToken();
        	long fileSize = Long.parseLong(st.nextToken());
        	mw.toOutput("Otrzymale info o pliku: " + fileName + ", " + fileSize);
        	downloadFileFromServer(fileName, fileSize);
        }
        
        else if (cd.equals(Protocol.FILEARCHIVIZED)){
        	System.out.println("aktualizujemy");
        	send(Protocol.UPDATEFILES);
        }
        
        else if (cd.equals(Protocol.FILEREMOVED)){
        	System.out.println("aktualizujemy");
        	send(Protocol.UPDATEFILES);
        }
        
        else if (cd.equals(Protocol.DIFFERENTVERSIONARCHIVIZED)){
        	int version = Integer.parseInt(st.nextToken());
        	File localFileChosen = mw.getLocalChosenFile();
        	int reply = JOptionPane.showConfirmDialog(null, 
					"Inna wersja pliku " + localFileChosen.getName() + 
					" znajduje się już na serwerze. Wysłać aktualną wersję pliku?",
					"Uwaga", JOptionPane.YES_NO_OPTION);
        	if ( reply == JOptionPane.YES_OPTION)  {     	
	        	send(Protocol.SENDINGFILE + " " + localFileChosen.getName() + " " + localFileChosen.length() + " " + version);
	        	sendFileToServer(localFileChosen.getPath(), localFileChosen.getName(), 
						localFileChosen.length(),  socket.getOutputStream());
        	}
        }
        
        else if (cd.equals(Protocol.NOTONSERVER)){
        	int version = 1;
        	File localFileChosen = mw.getLocalChosenFile();
        	send(Protocol.SENDINGFILE + " " + localFileChosen.getName() + " " + localFileChosen.length() + " " + version);
        	sendFileToServer(localFileChosen.getPath(), localFileChosen.getName(), 
					localFileChosen.length(),  socket.getOutputStream());
        	

        }
        
        else if (cd.equals(Protocol.SAMEFILEARCHIVIZED)){
        	JOptionPane.showMessageDialog(null, "Zarchiwizowano już ten plik!",
					"Uwaga!", JOptionPane.INFORMATION_MESSAGE);
        }
        
        else if (cd.equals(Protocol.UPDATEFILES)){
        	Vector<String> remoteFiles = new Vector<String>();
        	while (st.hasMoreTokens()){
        		String remoteFile = st.nextToken();
        		remoteFiles.addElement(remoteFile);
        		System.out.println("Plik na serwerze: " + remoteFile);
        	}	 
        	mw.updateRemoteFileTree(remoteFiles);
        }
        
        else if (cd.equals(Protocol.LOGGEDOUT)) {
        	System.out.println("Trace polaczenie");
        	setConnected(false);
        	mw.showConnectButton();
            return false;
        } 
        else if (cd.equals(Protocol.STOP)) {
        	System.out.println("Trace polaczenie");
        	setConnected(false);
        	mw.showConnectButton();
            return false;
        } 

        return true;
    }
    
    /**
     * Metoda obslugujaca wysylanie rozkazu do serwera
     * @param command wysylany rozkaz
     * @throws IOException rzucany wyjatek
     */
    public void send(String command) throws IOException {
        if (output != null){
        	mw.toOutput("Wiadomosc do serwera: " + command);
        		output.println(command);
        }
        else {
        	JOptionPane.showMessageDialog(null, "Brak połączenia z serwerem", "Uwaga", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Metoda obslugujaca wysylanie pliku na serwer
     * @param filePath sciezka do pliku
     * @param fileName nazwa pliku
     * @param fileSize rozmiar pliku w bajtach
     * @param os wyjsciowy strumien danych klienta
     */
    public void sendFileToServer(String filePath, String fileName, long fileSize, OutputStream os) {
    	new Thread(new Runnable() {
    	    public void run() {
    	    	mw.toOutput("File length: " + fileSize);
				byte[] bytes = new byte[16 * 1024];
				File file = new File(filePath);

				try {
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
				
							
					long bytesRead = 0;
				    long totalRead=0;
				    
				    JFrame f = new JFrame("");
				    f.setLayout(new BorderLayout());
				    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				    JProgressBar progressBar = new JProgressBar(0, 100);
				    progressBar.setPreferredSize(new Dimension(300,100));
				    JLabel label = new JLabel("Trwa wysylanie pliku " + fileName + ":");
				    f.setVisible(true);
				    f.add(label, BorderLayout.NORTH);
				    f.add(progressBar, BorderLayout.CENTER);
				    
				    progressBar.setValue(0);
				    progressBar.setStringPainted(true);
				    f.pack();
				  
				  		    
				    while(totalRead != fileSize){
				    	bytesRead = bis.read(bytes,0,bytes.length);
				        totalRead += bytesRead;
				        double procent = 100.0*totalRead/fileSize;
				        progressBar.setValue((int)procent);    
				        os.write(bytes,0, (int)bytesRead);
				        os.flush();
					}
				    f.dispose();
				    mw.toOutput("Wysylanie zakonczone!");
				    			    
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e){
					e.printStackTrace();
				} finally {
					try {
						fis.close();
					    bis.close();					    
					} catch (IOException e){
						e.printStackTrace();
					}				    
				    
				}
    	    }
    	}).start();  	
    }
    
    /**
     * Metoda obslugujaca pobieranie pliku z serwera
     * @param fileName nazwa pliku
     * @param fileSize rozmiar pliku w bajtach
     */
    public void downloadFileFromServer(String fileName, long fileSize) {
		try {
			downloadFilePath = System.getProperty("user.home") + "/" + fileName;	
		    byte [] bytes  = new byte [16 * 1024];
		    InputStream is = socket.getInputStream();
			
		    fos = new FileOutputStream(downloadFilePath);
		    dos = new DataOutputStream(fos);
		    long bytesRead = 0;
		    long totalRead=0;
		    mw.toOutput("pobieranie pliku o rozmiarze: " + fileSize);
		    	    		  
		    while(totalRead != fileSize){
		    	bytesRead = is.read(bytes, 0, bytes.length);
		    	totalRead += bytesRead;
		        dos.write(bytes, 0 , (int)bytesRead);
		        dos.flush();
		      } 	   
		    mw.toOutput("File " + fileName + " downloaded (" + totalRead + " bytes read)"); 
		    mw.updateLocalFileTree();
			send(Protocol.FILEDOWNLOADED);	

		} catch (IOException e) {
			e.printStackTrace();
			File file = new File(downloadFilePath);
			file.delete();
		} catch (Exception e) {			
			e.printStackTrace();
			File file = new File(downloadFilePath);
			file.delete();		 
		}		  
		finally {
		      try {
				fos.close();
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		      
		}				
	}
    
    /**
     * Metoda sluzaca do sprawdzenia sumy kontrolnej MD5 danego pliku
     * @param file plik do sprawdzenia
     * @return suma kontrolna pliku
     */
    public String getFileChecksum(File file) {
		MessageDigest md;
		InputStream is;
		byte[] bytes;
		StringBuffer sb = new StringBuffer("");
		try {
			md = MessageDigest.getInstance("MD5");
			
			bytes = new byte[128 * 1024];
			is = new FileInputStream(file);
			int numBytes;
			while ((numBytes = is.read(bytes)) != -1) {
				md.update(bytes, 0, numBytes);
			}
			byte[] mdbytes = md.digest();
				
			for (int i = 0; i < mdbytes.length; i++) {
			    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			    
			is.close();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 										
		return sb.toString();
	}
    
    /**
     * Metoda sluzaca do odebrania komendy od serwera
     * @return odebrana komenda
     */
    public String receive() {
    	if ( input != null){
	        try {
	            return input.readLine();
	        } catch (IOException e) {
	        	mw.toOutput("Error reading serwer.");
	        }
    	}
        return Protocol.NULLCOMMAND;
    }
    
    /**
     * Metoda zmieniajaca wartosc zmiennej connected
     * @param connected true jesli klient nawiazal polaczenie z serwerem, false jesli nie ma polaczenia
     */
    public void setConnected(boolean connected){
    	this.connected = connected;
    }
    
    /**
     * Metoda sprawdzajaca czy klient jest polaczony z serwerem
     * @return true jesli klient jest polaczony z serwerem, false jesli nie
     */
    public boolean isConnected(){
    	return connected;
    }
    
    /**
     * Metoda zwracajaca gniazdo klienta
     * @return gniazdo klienta
     */
    public Socket getSocket(){
    	return socket;
    }
    
    /**
     * Metoda sluzaca do zammkniecia komunikacji z serwerem
     */
    public void close(){       
        try {
        	output.close();
        	input.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {    	
	    	output = null;
	        input = null;
	        synchronized (this) {
	            socket = null;
	        }
		}
    } 
}
