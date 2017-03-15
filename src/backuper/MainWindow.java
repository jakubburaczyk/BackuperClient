package backuper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;


/**
 * Klasa glownego okna programu klienckiego Backuper
 * @author Jakub Buraczyk
 *
 */

public class MainWindow extends BaseWindow implements Runnable, ActionListener, WindowListener{
	
	/**
	 * Numer seryjny
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Lokalna instancja klasy klienckiego gniazda
	 */
	private MyClient klient;

	/**
	 * Glowny panel okna programu
	 */
	private JPanel gui;	
	/**
	 * Panel lewej polowy okna
	 */
	private JPanel leftPanel;
	/**
	 * Panel prawej polowy okna
	 */
	private JPanel rightPanel;
	/**
	 * Linia rozdzielajaca okno na dwa panele boczne
	 */
	private JSplitPane spliter;
	/**
	 * Przycisk sluzacy do polaczenia z serwerem
	 */
	private JButton connect;
	/**
	 * Przycisk sluzacy do usuwania pliku z serwera
	 */
	private JButton remove;
	/**
	 * Przycisk sluzacy do pobrania pliku z serwera
	 */
	private JButton download;
	/**
	 * Przycisk sluzacy do wyslania plik na serwer
	 */
	private JButton archivize;
	
	/**
	 * Korzen lokalnego drzewa plikow
	 */
	private DefaultMutableTreeNode localroot;	
	/**
	 * Model lokalnego drzewa plikow
	 */
    private DefaultTreeModel localTreeModel;  
    /**
     * Korzen zdalnego drzewa plikow
     */
	private DefaultMutableTreeNode remoteRootNode;
	/**
	 * Model zdalnego drzewa plikow
	 */
    private DefaultTreeModel remoteTreeModel;
    /**
     * Lokalne drzewo plikow
     */
    private JTree localTree;
    /**
     * Zdalne drzewo plikow
     */
    private JTree remoteTree;
    
    /**
     * Sciezka do korzenia lokalnego drzewa plikow
     */
    private String rootPath;

    /**
     * Czy wybrano plik z drzewa lokalnego
     */
    private boolean isLocalFileChosen;
    /**
     * Czy wybrano plik z drzewa zdalnego
     */
    private boolean isRemoteFileChosen;

    /**
     * Wybrany plik z drzewa lokalnego
     */
    private File localFileChosen;
    /**
     * Wybrany plik z drzewa zdalnego
     */
    private File remoteFileChosen;
    /**
     * Plik korzenia drzewa
     */
    private File fileRoot;
    /**
     * 
     */
    private static BufferedWriter bw;
    

	
	/**
	 * Konstruktor okna glownego
	 * @param title tytul okna
	 * @param width szerokosc okna
	 * @param height wysokoksc
	 * @param isResizable 
	 * @param onClose akcja wykonana po zamknieciu okna
	 * @param pathToIcon sciezka do pliku ikony okna
	 * @throws Exception wyjatek rzucany w razie bledu przy tworzeniu klienta
	 */
	public MainWindow(String title, int width, int height, boolean isResizable, int onClose, String pathToIcon) throws Exception{
		super(title, width, height, isResizable, pathToIcon);
		setDefaultCloseOperation(onClose);
		klient = new MyClient(this);
		isLocalFileChosen = false;
		isRemoteFileChosen = false;
		addWindowListener(this);
		
		
		OutputFrame.init();
	    bw = new BufferedWriter(new FileWriter("log.txt", false));
	    
	    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy | HH:mm:ss");
	    Date date = new Date();
	    
	    bw.write("*********************************\n" + "Log z dnia: " + dateFormat.format(date) + "\n");
	}

	/**
	 * Nadpisana metoda obslugi watku
	 */
	@Override
	public void run()
	{		
		addMenu();
		setBase();
		splitWindow();		
		getLocalFileTree();
		add(gui);
		this.setVisible(true);
		createChildren(fileRoot, localroot); 
	}
	
	/**
	 * inicjacja glownego panelu okna i dwoch paneli w nim
	 */
	public void setBase(){
		gui = new JPanel(new GridLayout());
		leftPanel = new JPanel(new BorderLayout());
		rightPanel = new JPanel(new BorderLayout());
		showConnectButton();
		gui.add(leftPanel);		
		gui.add(rightPanel);
	}
		
	/**
	 * Utworzenie przerwy miedzy lewym a prawym panelem
	 */
	public void splitWindow()
	{		
		spliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, rightPanel);
		spliter.setDividerSize(6);
		spliter.setDividerLocation(this.getWidth()/2);
		gui.add(spliter);
	}
	
	/**
	 * Utworzenie lokalnego drzewa plikow
	 */
	public void getLocalFileTree(){
		fileRoot = new File(System.getProperty("user.home"));		
		rootPath = fileRoot.getAbsolutePath();
		toOutput("Katalog domowy: " + rootPath);
		localroot = new DefaultMutableTreeNode(fileRoot);       
        localTreeModel = new DefaultTreeModel(localroot);             
        localTree = new JTree(localTreeModel);
        localTree.setCellRenderer(new FileTreeCellRenderer());
        localTree.setShowsRootHandles(true);
        
        /*
         * Nasluchiwanie na wybranie pliku w drzewie lokalnym
         */
        localTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
              DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
              
              String path = "";
              
              TreeNode[] paths = node.getPath();
              path = paths[paths.length-1].toString();
           
              localFileChosen = new File(path);
              isLocalFileChosen = true;
              toOutput("Droga do pliku: " + localFileChosen.getPath());
            }
        });
        
        
        JScrollPane localScrollPane = new JScrollPane(localTree);
        
        localScrollPane.setMinimumSize(new Dimension(300,150));
        localScrollPane.setPreferredSize(new Dimension(500, 450));
        localScrollPane.setMaximumSize(new Dimension(800, 800));
       
        archivize = new JButton("Archiwizuj");
        setArchivizeButton(archivize);    
        leftPanel.add(localScrollPane, BorderLayout.CENTER);
        leftPanel.add(archivize, BorderLayout.PAGE_END);
	}
	
	/**
	 * Dodanie galezi do danego korzenia w drzewie
	 * @param fileRoot plik rozpatrywanego korzenia
	 * @param node dodawana galaz 
	 */
	public void createChildren(File fileRoot, 
            DefaultMutableTreeNode node) {
    	FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    	File[] files = fileSystemView.getFiles(fileRoot, true);    	
        if (files == null) return;
        for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
            node.add(childNode);
            if (file.isDirectory()) {
                createChildren(file, childNode);
            }
        }
    }
	
	/**
	 * Odswiezenie lokalnego drzewa plikow po pobraniu pliku z serwera
	 */
	public void updateLocalFileTree(){
		localroot.removeAllChildren();
		createChildren(fileRoot, localroot);
		localTreeModel.reload();
		leftPanel.revalidate();
	}	
	
	/**
	 * Metoda obliczajaca sume kontrolna MD5 danego pliku
	 * @param file rozpatrywany plik
	 * @return zwracana suma kontrolna
	 * @throws NoSuchAlgorithmException wyjatek rzucany gdy nie odnaleziono algorytmu obliczania sumy kontrolnej
	 * @throws IOException wyjatek rzucany gdy nie uda sie wczytac strumienia wejsciowego
	 */
	public String getFileChecksum(File file) throws NoSuchAlgorithmException, IOException{
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] bytes = new byte[128 * 1024];
		InputStream is = new FileInputStream(file);
		int numBytes;		
		while ((numBytes = is.read(bytes)) != -1) {
			md.update(bytes, 0, numBytes);
		}		
		byte[] mdbytes = md.digest();			
		StringBuffer sb = new StringBuffer("");		
		for (int i = 0; i < mdbytes.length; i++) {
		    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}		    
		is.close();	
		
		return sb.toString();
	}
	
	/**
	 * pokazanie przycisku polaczenia z serwerem
	 */
	public void showConnectButton(){
		rightPanel.removeAll();
		connect = new JButton("Połącz z serwerem");
		setConnectButton(connect);
		rightPanel.add(connect, BorderLayout.CENTER);
		rightPanel.revalidate();
	}
	
	/**
	 * Pokazanie drzewa plikow znajdujacych sie na serwerze
	 * @param remoteFiles wektor zawierajacy nazwy plikow znajdujacych sie na serwerze 
	 */
	public  void getRemoteFileTree(Vector<String> remoteFiles){
		rightPanel.removeAll();		
		remoteRootNode = new DefaultMutableTreeNode("Zarchiwizowane pliki");
		remoteTreeModel = new DefaultTreeModel(remoteRootNode);
        for (int i = 0; i < remoteFiles.size(); i++){
        	remoteRootNode.add(new DefaultMutableTreeNode(remoteFiles.get(i)));
        	toOutput("Dodaje pliki: " + remoteFiles.get(i).toString());
        }	
        remoteTree = new JTree(remoteTreeModel);  
        
        /*
         * Nasluchiwanie na wybranie pliku w drzewie zdalnym
         */
        remoteTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
              DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
              String path = "";              
              TreeNode[] paths = node.getPath();
              path = paths[paths.length-1].toString();          
              remoteFileChosen = new File(path);
              isRemoteFileChosen = true;
              toOutput("Droga do pliku: " + remoteFileChosen.getPath());
            }
        });                
        JScrollPane remoteScrollPane = new JScrollPane(remoteTree);         
        remoteScrollPane.setMinimumSize(new Dimension(300,150));
        remoteScrollPane.setPreferredSize(new Dimension(500, 450));
        remoteScrollPane.setMaximumSize(new Dimension(800, 800));       
        JPanel buttonBar = new JPanel(new GridLayout(1,2));
        buttonBar.setPreferredSize(new Dimension(500, 60));       
        download = new JButton("Pobierz");
        setDownloadButton(download);        
        remove = new JButton("Usuń");
        setRemoveButton(remove);        
        buttonBar.add(download);
        buttonBar.add(remove);        
        rightPanel.add(remoteScrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonBar, BorderLayout.PAGE_END);
        rightPanel.revalidate();
	}
	
	/**
	 * odswiezenie drzewa plikow znajdujacych sie na serwerze
	 * @param remoteFiles lista nazw plikow znajdujacych sie na serwerze
	 */
	public void updateRemoteFileTree(Vector<String> remoteFiles){
		remoteRootNode.removeAllChildren();
		getRemoteFileTree(remoteFiles);
	}	
	
	/**
	 * Dodanie menu do glownego okna programu
	 */
	public void addMenu(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu start = new JMenu("Start");
		JMenu help = new JMenu("Pomoc");
	    
	    JMenuItem tryConnectMenu = new JMenuItem("Połącz z serwerem");
	    JMenuItem disconnectMenu = new JMenuItem("Rozłącz z serwerem");
	    JMenuItem settingsMenu = new JMenuItem("Ustawienia połączenia");
	    JMenuItem showOutput = new JMenuItem("Pokaż log połączenia");
	    JMenuItem quit = new JMenuItem("Zakończ");

	    JMenuItem instruction = new JMenuItem("Instrukcja");
	    JMenuItem about = new JMenuItem("O autorze");
	    	    
	    start.add(tryConnectMenu);
	    start.add(disconnectMenu);
	    start.add(settingsMenu);
	    start.add(showOutput);
	    start.add(quit);
	    help.add(instruction);
	    help.add(about);
		
	    tryConnectMenu.addActionListener(this);
	    disconnectMenu.addActionListener(this);
	    settingsMenu.addActionListener(this);
	    showOutput.addActionListener(this);
	    quit.addActionListener(this);

	    instruction.addActionListener(this);
	    about.addActionListener(this);
	    
	    tryConnectMenu.setActionCommand("tryConnect");
	    disconnectMenu.setActionCommand("tryDisconnect");
	    settingsMenu.setActionCommand("settings");
	    showOutput.setActionCommand("commandWindow");
	    quit.setActionCommand("exit");

	    instruction.setActionCommand("howto");
	    about.setActionCommand("aboutme");
	    
	    menuBar.add(start);
	    menuBar.add(help);

	    this.setJMenuBar(menuBar);
	    setVisible(true);
	}
	
	/**
	 * Nadanie wlasciwosci przycisku do archiwizacji plikow na serwer
	 * @param arch przycisk archiwizacji
	 */
	public void setArchivizeButton(JButton arch){
		try {
			Image buttonIcon = ImageIO.read(new File("img/archivize-icon.png"));
			arch.setIcon(new ImageIcon(buttonIcon));
			arch.setIconTextGap(20);
			arch.addActionListener(this);
			arch.setActionCommand("tryArchivize");
			arch.setMinimumSize(new Dimension(300, 50));
			arch.setPreferredSize(new Dimension (500, 60));
			arch.setMaximumSize(new Dimension(800, 200));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie udało się wczytać pliku img/archivize-icon.png!",
					 "Uwaga!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Nadanie wlasciwosci przycisku do polaczenia z serwerem
	 * @param arch przycisk laczenia
	 */
	public void setConnectButton(JButton con)
	{
		try {
			Image buttonIcon = ImageIO.read(new File("img/connect-icon.png"));		
			con.setIcon(new ImageIcon(buttonIcon));
			con.setIconTextGap(40);
			con.setFocusPainted(false);
			con.addActionListener(this);
			con.setActionCommand("tryConnect");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Nie udało się wczytać pliku img/connect-icon.png!",
						 "Uwaga!", JOptionPane.ERROR_MESSAGE);
			}
	}
	
	/**
	 * Nadanie wlasciwosci przycisku do pobrania pliku z serweru
	 * @param arch przycisk pobrania
	 */
	public void setDownloadButton(JButton down){
		try {
			Image buttonIcon = ImageIO.read(new File("img/download-icon.png"));
			down.setIcon(new ImageIcon(buttonIcon));
			down.setIconTextGap(20);
			down.addActionListener(this);
			down.setActionCommand("tryDownload");
			down.setMinimumSize(new Dimension(200, 50));
			down.setPreferredSize(new Dimension (250, 60));
			down.setMaximumSize(new Dimension(400, 200));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie udało się wczytać pliku img/download-icon.png!",
					 "Uwaga!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Nadanie wlasciwosci przycisku do usuwania plikow z serwera
	 * @param arch przycisk usuniecia
	 */
	public void setRemoveButton(JButton rm){
		try {
			Image buttonIcon = ImageIO.read(new File("img/remove-icon.png"));
			rm.setIcon(new ImageIcon(buttonIcon));
			rm.setIconTextGap(20);
			rm.addActionListener(this);
			rm.setActionCommand("tryRemove");
			rm.setMinimumSize(new Dimension(200, 50));
			rm.setPreferredSize(new Dimension (250, 60));
			rm.setMaximumSize(new Dimension(400, 200));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie udało się wczytać pliku img/remove-icon.png!",
					 "Uwaga!", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	/**
	 * getter wybranego przez uzytkownika pliku z drzewa plikow lokalnych
	 * @return wybrany plik
	 */
	public File getLocalChosenFile(){
		return localFileChosen;
	}
	
	/**
	 * getter wybranego przez uzytkownika pliku z drzewa plikow na serwerze
	 * @return wybrany plik
	 */
	public File getRemoteChosenFile(){
		return remoteFileChosen;
	}
	
	public void toOutput(String line){
		try {
			if(OutputFrame.isVis()){
				OutputFrame.toOutput(line);
			}
			OutputFrame.save(line);
			bw.write(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Nadpisana metoda obslugi wykonania przez uzytkownika akcji wybrania opcji z menu lub wcisniecia przycisku
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object pressed = event.getActionCommand();
		
		if (pressed.equals("tryConnect")) {	
			if (!klient.isConnected()){
				try {
					klient.init(Config.getAdress(), Config.getPort());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Nie udało się połączyć z serwerem!",
							"Uwaga!", JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Jesteś już połączony z serwerem!",
						"Uwaga!", JOptionPane.INFORMATION_MESSAGE);
			}
	    }
		
		else if (pressed.equals("tryArchivize")){
			if (isLocalFileChosen && klient.isConnected())
			{
				int reply = JOptionPane.showConfirmDialog(null, 
						"Czy na pewno przesłać plik " + localFileChosen.getAbsolutePath() + "?",
						"Uwaga", JOptionPane.YES_NO_OPTION);
			    if (reply == JOptionPane.YES_OPTION)
			    {
			    	try {			    		
			    		klient.send(Protocol.ISONSERVER + " " + localFileChosen.getName() 
			    				+ " " + localFileChosen.length() + " " + getFileChecksum(localFileChosen));	
			    		
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
			    }		    
			}			
			else {
				JOptionPane.showMessageDialog(null, "Nic do zarchiwizowania!",
						"Uwaga!", JOptionPane.ERROR_MESSAGE);
			}	
		}
		
		else if (pressed.equals("tryDownload")){
			if (klient.isConnected() && isRemoteFileChosen){
				int reply = JOptionPane.showConfirmDialog(null, 
						"Czy na pewno pobrać plik " + remoteFileChosen.getPath() + "?", 
						"Uwaga", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION)
				{
					try {
						klient.send(Protocol.DOWNLOADFILE + " " + remoteFileChosen.getPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Nic do pobrania!",
						"Uwaga!", JOptionPane.ERROR_MESSAGE);
			}				
		}
		
		else if (pressed.equals("tryRemove")){
			if (klient.isConnected() && isRemoteFileChosen){
				int reply = JOptionPane.showConfirmDialog(null, 
						"Czy na pewno usunąć plik " + remoteFileChosen.getPath() + "?", 
						"Uwaga", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION)
				{
					try {
						klient.send(Protocol.REMOVEFILE + " " + remoteFileChosen.getPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}
			else {
				JOptionPane.showMessageDialog(null, "Nic do usunięcia!",
						"Uwaga!", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		else if (pressed.equals("tryDisconnect")) {	
			if (klient.isConnected())	{
				try {
					klient.send(Protocol.LOGOUT);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				JOptionPane.showMessageDialog(null, "Nie jesteś połączony z serwerem!",
						"Uwaga!", JOptionPane.INFORMATION_MESSAGE);
			}
	    }
		
		
		
		else if (pressed.equals("settings")){
			new SettingsWindow("Ustawienia", 500, 250, false, "img/settings-icon.png");			
		}
		
		else if (pressed.equals("commandWindow")){
			new OutputFrame();			
		}	
		
		else if (pressed.equals("exit")) {
			try {
				int reply = JOptionPane.showConfirmDialog(null, 
						"Czy na pewno chcesz wyjść z programu?", 
						"Uwaga", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION){
					if (klient.isConnected()){	
						klient.send(Protocol.STOP);							
					}
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
		else if (pressed.equals("aboutme")){
			new TextWindow("O autorze", 350, 225, false, "img/user-icon.png",
					"txt/about.txt", "OK");
		}		
		else if (pressed.equals("howto")){
			new TextWindow("Instrukcja obsługi programu", 500, 500, false, "img/instruction-icon.png",
					"txt/instruction.txt", "OK");
		}		
	}
	
	/**
	 * Metoda glowna programu
	 * @param args poczatkowe argumenty wywolania programu
	 * @throws Exception rzucany wyjatek
	 */
	public static void main(String[] args) throws Exception {
		new Config();
		SwingUtilities.invokeLater(new MainWindow("Program Backuper", 800, 600, true, 
				JFrame.EXIT_ON_CLOSE, "img/main-icon.png"));

	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}


	@Override
	public void windowClosed(WindowEvent e) {	
	}

	/**
	 * Przy probie zamkniecia okna najpierw zamykane jest polaczenie z serwerem
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		if (klient.isConnected()){	
			try {
				klient.send(Protocol.STOP);
			} catch (IOException e1) {
				e1.printStackTrace();
			}							
		}
		try {
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}


	@Override
	public void windowDeiconified(WindowEvent e) {
	}


	@Override
	public void windowIconified(WindowEvent e) {
		
	}


	@Override
	public void windowOpened(WindowEvent e) {
		
	}
	
       
	/**
	 * Wlasna implementacja klasy TreeCallRender'era 
	 * @author Jakub Buraczyk
	 *
	 */
	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		  
	    /**
		 * numer seryjny klasy
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Nadpisana metoda nadania pozadanego wygladu obiektom w drzewie plikow
		 */
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, 
	    		boolean expanded, boolean leaf, int row, boolean hasFocus)
	    {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	        File file = (File)node.getUserObject();
				
	        setText(file.getName());
	        
            if (file.isDirectory()) {                   
                setIcon(UIManager.getIcon("FileView.directoryIcon"));
            } 
            else {
                setIcon(UIManager.getIcon("FileView.fileIcon"));
            }
			
			return this;
	    }
    }
}
