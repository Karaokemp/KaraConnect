/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Caprica Software Limited.
 */

package uk.co.caprica.vlcjplayer.view.playlist;

import static uk.co.caprica.vlcjplayer.Application.application;
import static uk.co.caprica.vlcjplayer.Application.resources;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.eventbus.Subscribe;

import lombok.Cleanup;
import lombok.val;
import net.miginfocom.swing.MigLayout;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcjplayer.Data;
import uk.co.caprica.vlcjplayer.VlcjPlayer;
import uk.co.caprica.vlcjplayer.WiFiHandler;
import uk.co.caprica.vlcjplayer.audioplayer.KaraAnnouncer;
import uk.co.caprica.vlcjplayer.event.ShowPlaylistEvent;
import uk.co.caprica.vlcjplayer.event.StoppedEvent;
import uk.co.caprica.vlcjplayer.songlistreader.DB;
import uk.co.caprica.vlcjplayer.songlistreader.KaraokeReader;
import uk.co.caprica.vlcjplayer.songlistreader.Wachutu;
import uk.co.caprica.vlcjplayer.view.BaseFrame;
import uk.co.caprica.vlcjplayer.view.main.MainFrame;
import uk.co.caprica.vlcjplayer.view.main.PositionPane;
import uk.co.caprica.vlcjplayer.view.main.StatusBar;
import uk.co.caprica.vlcjplayer.view.playlist.KaraRecorder.RecorderNotInitializedException;

@SuppressWarnings("serial")
public class PlaylistFrame extends BaseFrame {
	
	static final int PLAYED = 0;
	static final int PERF = 1;
	static final int SONG = 2;
	static final int FILE = 3;
	static final int TIME = 4;
	static final int EMAIL = 5;
	static final int WAIT = 6;
	static final int PERF_NUMBER = 7;
	static final int RETRIES = 8;
	

	private JPanel fatherPane;
	private JPanel contentPane;
	private JTable mainList;
	private JTable gapList;
	
	private DefaultTableModel mainModel;
	private DefaultTableModel gapModel;
	
	Timer timer = new Timer();
	
	private  JPanel bottomPane;
	
	private  JFileChooser fileChooser;

    private PositionPane positionPane;

//    private  ControlsPane controlsPane;

    private  StatusBar statusBar = new StatusBar();
    
    private  EmbeddedMediaPlayerComponent mediaPlayerComponent;
    
    static PlaylistFrame instance;
    
    private boolean isPlayingGap = true;
    private boolean shouldContinueGap = true;
    
    private long gapTime = 0;
    
    private String gapPlayingFile = null;
    
    private String gapPlayingName = null;
    
    JLabel lblNowPlaying = new JLabel("Now Playing");
    private JTextField txtRootFolder;
    JCheckBox chckbxShow = new JCheckBox("Show");
    JPanel settingsPane = new JPanel();
    JButton btnPlayMain = new JButton("Play");
    JButton btnRemoveMain = new JButton("Remove");
    JButton btnRemoveGap = new JButton("Remove");
    JButton btnClearPlayed = new JButton("Clear Played");
    JButton btnPlayGap = new JButton("Play Selected");
    JButton btnAddGap = new JButton("Add");
    JButton btnContinuePlayGap = new JButton("Continue Play");
    JButton btnClearGap = new JButton("Clear All");
    JButton btnAddMain = new JButton("Manual Add");
    JButton btnMarkFaulty = new JButton("Mark Faulty!");
    JButton btnStopGap = new JButton("Stop");
	JButton btnLoadPlaylist = new JButton("Load Playlist");
	JButton btnSavePlaylist = new JButton("Save Playlist");
	JButton btnTestReecording = new JButton("Test Recording");
	JComboBox<ComboListItem> cmbAudioRec = new JComboBox<ComboListItem>();
	JComboBox<ComboListItem> cmbVideoRec = new JComboBox<ComboListItem>();
	JButton btnChange = new JButton("Change");
	JCheckBox chckbxTestOnAir = new JCheckBox("Test On Air");
	JButton btnEnableCamera = new JButton("Disable Camera");
	JButton btnChangeRecFolder = new JButton("Change");
	JLabel lblRecordingsFolder = new JLabel("Rec");
	JLabel lblVideorec = new JLabel("Video");
	JLabel lblAudiorec = new JLabel("Audio");
	JButton btnClearMain = new JButton("Clear All");
	JPanel bottomControlsPane = new JPanel();
	JLabel lblRecEnabled = new JLabel("Recording Enabled");
	JButton btnViewRecFiles = new JButton("View");
	JLabel lblWF = new JLabel(WiFiHandler.SSID + ":" + WiFiHandler.PASS);
	JButton btnViewRoot = new JButton("View");
	JButton btnHelp = new JButton("Video Tutorial");
	JToggleButton tglbtnAutoPilot = new JToggleButton("Auto Pilot");
	JPanel karaokeControlsPanel = new JPanel();
	JLabel lblAutoPilot = new JLabel("Auto Pilot On!");
	JPanel gapControlsPanel = new JPanel();
    
    boolean isRecording = false;
    boolean isAutoPilot = false;
    Integer waitingForNumber = null;
    boolean isAnnouncing = false;
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH-mm");
	JTextField txtRecFolder;
	static DateTimeFormatter dateFormat = DateTimeFormat.forPattern("HH:mm:ss");
	KaraRecorder recorder;
	long duration;
	
	int performerSerialNumber = 1;
	
	public static final Logger LOGGER = Logger.getLogger(PlaylistFrame.class.getName());
	public static final Logger ACTION_LOGGER = Logger.getLogger(KaraAction.class.getName());
	
	
	
	static {
		createLogger(LOGGER, Data.errorLogFileName);
		createLogger(ACTION_LOGGER, Data.actionLogFileName);
	}
	
	static void createLogger(Logger logger, String file){
		Handler consoleHandler = null;
		Handler fileHandler  = null;
		try{
			//Creating consoleHandler and fileHandler
			consoleHandler = new ConsoleHandler();
			fileHandler  = new FileHandler(file, true);
			fileHandler.setEncoding("UTF-8");
			fileHandler.setFormatter(new SimpleFormatter());
			
			//Assigning handlers to LOGGER object
			logger.addHandler(consoleHandler);
			logger.addHandler(fileHandler);
			
			//Setting levels to handlers and LOGGER
			consoleHandler.setLevel(Level.ALL);
			fileHandler.setLevel(Level.ALL);
			logger.setLevel(Level.ALL);
			
			logger.config("Configuration done.");
		}catch(IOException exception){
			logger.log(Level.SEVERE, "Error occur in FileHandler.", exception);
		}
	}
    	public PlaylistFrame() {
		
        super(resources().getString("dialog.playlist"));
        try{
        
        fatherPane = new JPanel(new BorderLayout());
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(null);
		fatherPane.add(contentPane, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        setContentPane(fatherPane);

        applyPreferences();
		
		gapList = new GapTable();
		gapList.setBackground(new Color(135, 206, 235));
		gapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gapList.setDragEnabled(true);
		gapList.setDropMode(DropMode.ON);
		gapList.setTransferHandler(new PlaylistDND());
		gapModel = new DefaultTableModel(
				new Object[][] {},
				new String[] {
					"Song Name", "File Name"
				}
			) {
				Class<?>[] columnTypes = new Class[] {
					String.class, String.class
				};
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
				boolean[] columnEditables = new boolean[] {
					false, false
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};
		gapList.setModel(gapModel);
		gapList.getColumnModel().getColumn(0).setResizable(false);
		gapList.getColumnModel().getColumn(0).setPreferredWidth(231);
		gapList.getColumnModel().getColumn(1).setResizable(false);
		gapList.getColumnModel().getColumn(1).setPreferredWidth(0);
		gapList.getColumnModel().getColumn(1).setMinWidth(0);
		gapList.getColumnModel().getColumn(1).setMaxWidth(0);
		gapList.setFillsViewportHeight(true);
		mainList = new JTable();
		mainList.setBackground(new Color(221, 160, 221));
		mainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mainList.setDragEnabled(true);
		mainList.setDropMode(DropMode.ON);
		mainList.setTransferHandler(new PlaylistDND());
		mainModel = new DefaultTableModel(
				new Object[][] {},
				new String[] {
					"Played?", "Participant Name", "Song Name", "File Name", "Time", "Email", "Waiting", "Perf Number", "Retries" 
				}
			) {
				Class<?>[] columnTypes = new Class[] {
					Boolean.class, String.class, String.class, String.class, Long.class, String.class, WaitingTime.class, String.class, Long.class
				};
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
				boolean[] columnEditables = new boolean[] {
					false, false, false, false, false, false, false, false, false, false
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};
		
		mainList.setModel(mainModel);
		mainList.getColumnModel().getColumn(0).setResizable(false);
		mainList.getColumnModel().getColumn(0).setPreferredWidth(35);
		mainList.getColumnModel().getColumn(1).setResizable(false);
		mainList.getColumnModel().getColumn(1).setPreferredWidth(155);
		mainList.getColumnModel().getColumn(2).setResizable(false);
		mainList.getColumnModel().getColumn(2).setPreferredWidth(326);
		mainList.getColumnModel().getColumn(3).setResizable(false);
		mainList.getColumnModel().getColumn(3).setPreferredWidth(0);
		mainList.getColumnModel().getColumn(3).setMinWidth(0);
		mainList.getColumnModel().getColumn(3).setMaxWidth(0);
		mainList.getColumnModel().getColumn(4).setResizable(false);
		mainList.getColumnModel().getColumn(4).setPreferredWidth(0);
		mainList.getColumnModel().getColumn(4).setMinWidth(0);
		mainList.getColumnModel().getColumn(4).setMaxWidth(0);
		mainList.getColumnModel().getColumn(5).setResizable(false);
		mainList.getColumnModel().getColumn(5).setPreferredWidth(0);
		mainList.getColumnModel().getColumn(5).setMinWidth(0);
		mainList.getColumnModel().getColumn(5).setMaxWidth(0);
		mainList.getColumnModel().getColumn(6).setResizable(false);
		mainList.getColumnModel().getColumn(6).setPreferredWidth(70);
		mainList.getColumnModel().getColumn(7).setResizable(false);
		mainList.getColumnModel().getColumn(7).setPreferredWidth(0);
		mainList.getColumnModel().getColumn(7).setMinWidth(0);
		mainList.getColumnModel().getColumn(7).setMaxWidth(0);
		mainList.getColumnModel().getColumn(8).setResizable(false);
		mainList.getColumnModel().getColumn(8).setPreferredWidth(0);
		mainList.getColumnModel().getColumn(8).setMinWidth(0);
		mainList.getColumnModel().getColumn(8).setMaxWidth(0);
		mainList.setDefaultRenderer(WaitingTime.class, new KaraRenderer());
		
		JLabel lblNewLabel = new JLabel("Karaoke Playlist");
		lblNewLabel.setForeground(new Color(221, 160, 221));
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 24));
		lblNewLabel.setBounds(115, 0, 218, 39);
		contentPane.add(lblNewLabel);
		mainList.setFillsViewportHeight(true);
		JScrollPane scrollPaneMain = new JScrollPane(mainList);
		scrollPaneMain.setBounds(115, 39, 700, 678);
		scrollPaneMain.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneMain.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		contentPane.add(scrollPaneMain);
		
		JScrollPane scrollPaneGap = new JScrollPane(gapList);
		scrollPaneGap.setBounds(825, 39, 319, 678);
		scrollPaneGap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneGap.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		contentPane.add(scrollPaneGap);
		btnPlayGap.setBounds(0, 35, 129, 25);
		gapControlsPanel.add(btnPlayGap);
		btnPlayGap.setBackground(new Color(135, 206, 235));
		btnPlayGap.setOpaque(true);
		
		
		
		btnRemoveGap.setBounds(1154, 146, 129, 25);
		btnRemoveGap.setBackground(new Color(135, 206, 235));
		btnRemoveGap.setOpaque(true);
		contentPane.add(btnRemoveGap);
		
		
		
		btnAddGap.setBounds(1154, 182, 129, 25);
		btnAddGap.setBackground(new Color(135, 206, 235));
		btnAddGap.setOpaque(true);
		contentPane.add(btnAddGap);
		btnContinuePlayGap.setBounds(0, 0, 129, 25);
		gapControlsPanel.add(btnContinuePlayGap);
		btnContinuePlayGap.setBackground(new Color(135, 206, 235));
		btnContinuePlayGap.setOpaque(true);
		
		
		
		btnClearGap.setBounds(1154, 218, 129, 25);
		btnClearGap.setBackground(new Color(135, 206, 235));
		btnClearGap.setOpaque(true);
		contentPane.add(btnClearGap);
		lblNowPlaying.setVerticalAlignment(SwingConstants.TOP);
		lblNowPlaying.setFont(new Font("Dialog", Font.BOLD, 24));
		
		
		lblNowPlaying.setHorizontalAlignment(SwingConstants.CENTER);
		lblNowPlaying.setBounds(208, 718, 900, 52);
		contentPane.add(lblNowPlaying);
		
		
		settingsPane.setBounds(1154, 409, 201, 308);
		contentPane.add(settingsPane);
		settingsPane.setLayout(null);
		
		JLabel lblRootFolder = new JLabel("Root");
		lblRootFolder.setBounds(6, 16, 30, 15);
		settingsPane.add(lblRootFolder);
		
		txtRootFolder = new JTextField();
		txtRootFolder.setBounds(0, 42, 175, 19);
		settingsPane.add(txtRootFolder);
		txtRootFolder.setColumns(10);
		txtRootFolder.setEditable(false);
		
		btnChange.setBounds(101, 11, 80, 25);
		settingsPane.add(btnChange);
		
		cmbAudioRec.setBounds(0, 145, 186, 25);
		settingsPane.add(cmbAudioRec);
		
		cmbVideoRec.setBounds(0, 192, 186, 25);
		settingsPane.add(cmbVideoRec);
		
		lblAudiorec.setBounds(6, 119, 96, 15);
		settingsPane.add(lblAudiorec);
		
		lblVideorec.setBounds(6, 173, 65, 15);
		settingsPane.add(lblVideorec);
		
		btnTestReecording.setBounds(0, 254, 129, 25);
		settingsPane.add(btnTestReecording);
		
		
		lblRecordingsFolder.setBounds(6, 72, 24, 15);
		settingsPane.add(lblRecordingsFolder);
		
		txtRecFolder = new JTextField();
		txtRecFolder.setColumns(10);
		txtRecFolder.setBounds(0, 98, 175, 19);
		settingsPane.add(txtRecFolder);
		
		btnChangeRecFolder.setBounds(102, 67, 80, 25);
		settingsPane.add(btnChangeRecFolder);
		
		chckbxTestOnAir.setBounds(2, 281, 100, 23);
		settingsPane.add(chckbxTestOnAir);
		lblRecEnabled.setHorizontalAlignment(SwingConstants.CENTER);
		
		lblRecEnabled.setBackground(new Color(0, 250, 154));
		lblRecEnabled.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblRecEnabled.setBounds(975, 6, 179, 29);
		lblRecEnabled.setOpaque(true);
		contentPane.add(lblRecEnabled);
		
		btnEnableCamera.setBounds(0, 220, 129, 23);
		settingsPane.add(btnEnableCamera);
		
		
		
		btnViewRecFiles.setBounds(31, 67, 68, 25);
		settingsPane.add(btnViewRecFiles);
		
		btnViewRoot.setBounds(31, 12, 68, 25);
		
		settingsPane.add(btnViewRoot);
		settingsPane.setVisible(false);
		btnEnableCamera.addPropertyChangeListener("text", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (isCamEnabled()){
					lblRecEnabled.setText("Recording Enabled");
					lblRecEnabled.setBackground(new Color(0, 250, 154));
				}
				else{
					lblRecEnabled.setText("Recording Disabled");
					lblRecEnabled.setBackground(new Color(255, 0, 0));
				}
				
				try {
					saveSettings();
				} catch (Exception ex) {
					LOGGER.log(Level.SEVERE, "Exception", ex);
				}
			}
		});
		
		JLabel lblSettings = new JLabel("Settings");
		lblSettings.setBounds(1150, 338, 70, 15);
		contentPane.add(lblSettings);
		
		
		chckbxShow.setBounds(1250, 334, 129, 23);
		contentPane.add(chckbxShow);
		
		btnSavePlaylist.setBounds(1154, 254, 129, 25);
		btnSavePlaylist.setBackground(new Color(135, 206, 235));
		btnSavePlaylist.setOpaque(true);
		contentPane.add(btnSavePlaylist);
		
		btnLoadPlaylist.setBounds(1154, 290, 129, 25);
		btnLoadPlaylist.setBackground(new Color(135, 206, 235));
		btnLoadPlaylist.setOpaque(true);
		contentPane.add(btnLoadPlaylist);
		btnStopGap.setBounds(0, 72, 129, 25);
		gapControlsPanel.add(btnStopGap);
		btnStopGap.setBackground(new Color(135, 206, 235));
		btnStopGap.setOpaque(true);
		
		JLabel lblGapPlaylist = new JLabel("Gap Playlist");
		lblGapPlaylist.setForeground(new Color(135, 206, 235));
		lblGapPlaylist.setFont(new Font("Dialog", Font.BOLD, 24));
		lblGapPlaylist.setBounds(825, 2, 218, 34);
		contentPane.add(lblGapPlaylist);
		
		JLabel lblWifi = new JLabel("WiFi:");
		lblWifi.setBounds(1154, 720, 190, 15);
		contentPane.add(lblWifi);
		lblWF.setBounds(1154, 730, 190, 15);
		
		contentPane.add(lblWF);
		btnHelp.setFont(new Font("Tahoma", Font.BOLD, 15));
		
		btnHelp.setBounds(636, 9, 179, 26);
		
		contentPane.add(btnHelp);
		
		
		lblAutoPilot.setHorizontalAlignment(SwingConstants.CENTER);
		lblAutoPilot.setOpaque(true);
		lblAutoPilot.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblAutoPilot.setBackground(new Color(30, 144, 255));
		lblAutoPilot.setBounds(385, 6, 179, 29);
		lblAutoPilot.setVisible(false);
		contentPane.add(lblAutoPilot);
		
		karaokeControlsPanel.setBounds(2, 26, 111, 258);
		contentPane.add(karaokeControlsPanel);
		karaokeControlsPanel.setLayout(null);
		btnMarkFaulty.setBounds(0, 222, 111, 25);
		karaokeControlsPanel.add(btnMarkFaulty);
		btnMarkFaulty.setBackground(new Color(221, 160, 221));
		btnMarkFaulty.setOpaque(true);
		btnClearMain.setBounds(10, 138, 95, 25);
		karaokeControlsPanel.add(btnClearMain);
		btnClearMain.setBackground(new Color(221, 160, 221));
		btnClearMain.setOpaque(true);
		btnRemoveMain.setBounds(10, 76, 95, 25);
		karaokeControlsPanel.add(btnRemoveMain);
		btnRemoveMain.setBackground(new Color(221, 160, 221));
		btnRemoveMain.setOpaque(true);
		btnPlayMain.setBounds(10, 11, 95, 25);
		karaokeControlsPanel.add(btnPlayMain);
		btnPlayMain.setBackground(new Color(221, 160, 221));
		btnPlayMain.setOpaque(true);

        bottomPane = new JPanel();
        bottomPane.setLayout(new BorderLayout());

        
        bottomControlsPane.setLayout(new MigLayout("fill, insets 0 n n n", "[grow]", "[]0[]"));
        
        this.mediaPlayerComponent = application().mediaPlayerComponent();

        positionPane = new PositionPane(mediaPlayerComponent.getMediaPlayer());
        bottomControlsPane.add(positionPane, "grow, wrap");
        positionPane.setEnabled(false);

//        MediaPlayerActions mediaPlayerActions = application().mediaPlayerActions();
//        controlsPane = new ControlsPane(mediaPlayerActions);
        bottomPane.add(bottomControlsPane, BorderLayout.CENTER);
//        bottomControlsPane.add(controlsPane, "grow");

        
        bottomPane.add(statusBar, BorderLayout.SOUTH);

        fatherPane.add(bottomPane, BorderLayout.SOUTH);
        
        instance = this;
        
        boolean settingsExist = checkNotEmpty(Data.settingsFileName);

        loadCaptureDevices();
        
        fileChooser = new JFileChooser(); 
        if (!Data.relaunch){
	        if (settingsExist){
	        	loadSettings();
	        	val answer = JOptionPane.showConfirmDialog(this, "Do you want to change the karaoke root location?", "Previous settings exists", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION){
			        if (chooseKaraokeRoot() != JFileChooser.APPROVE_OPTION){
			        	JOptionPane.showMessageDialog(this, "No folder selected. asshole!");
			        	System.exit(0);
			        }
			        saveSettings();
				}
	        }
	        else{
		        if (chooseKaraokeRoot() != JFileChooser.APPROVE_OPTION){
		        	JOptionPane.showMessageDialog(this, "No folder selected. asshole!");
		        	System.exit(0);
		        }
		        
		        chooseRecFolder(true);
	        }
	        checkExistingPlaylistsOnLoad(Data.playlistFileName, "Program might have crashed. Do you want to load the existing requests playlist?");
	        checkExistingPlaylistsOnLoad(Data.gaplistFileName, "Program might have crashed. Do you want to load the existing gap music playlist?");
        }
        else{
        	loadSettings();
        	try {
        		loadPlaylistFromFile(Data.playlistFileName);
        	}
        	catch (FileNotFoundException e){}
        	try {
        	loadPlaylistFromFile(Data.gaplistFileName);
        	}
        	catch (FileNotFoundException e){}
        }
        addEventListeners();
        Wachutu.init(txtRootFolder.getText());
        setVisible(true);
        try {
			recorder = new KaraRecorder();
		} catch (RecorderNotInitializedException e) {
			btnEnableCamera.setText("Enable Camera");
			LOGGER.log(Level.SEVERE, "RecorderNotInitializedException", e);
		}
        updateWaitingTimes();
        ParticipantControl.jumpStart();
    	}
    	catch (Exception ex){
    		LOGGER.log(Level.SEVERE, "Exception", ex);
    	}
    }
    	
    private static class AutomationTask extends TimerTask{
    	@Override
		public void run() {
			if (instance.isAutoPilot && instance.isPlayingGap && instance.waitingForNumber == null){
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						instance.checkForSongsToAutomate();
					}
				});
			}
		}
    }
    	
	void checkForSongsToAutomate(){
		for (int i = 0; i < mainList.getRowCount(); i++){
			if (!(boolean)getVal(i, PLAYED, mainList)){
				val retries = (long)getVal(i, RETRIES, mainList);
				if (retries > 2){
					mainModel.removeRow(i);
					continue;
				}
				
				waitingForNumber = i;
				isAnnouncing = true;
				mediaPlayerComponent.getMediaPlayer().setVolume(45);
				KaraAnnouncer.announce(getVal(i, PERF_NUMBER, mainList).toString());
				mediaPlayerComponent.getMediaPlayer().setVolume(60);
				isAnnouncing = false;
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (isPlayingGap){
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									setVal(retries + 1, waitingForNumber, RETRIES);
									mainModel.moveRow(waitingForNumber, waitingForNumber, mainList.getRowCount() - 1);
									waitingForNumber = null;
								}
							});
						}
					}
				}, 20000);
				break;
			}
    	}
	}
	
	public static void signalReadyToSing(){
		if (instance.waitingForNumber != null && !instance.isAnnouncing){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
					try {
						instance.mainList.setRowSelectionInterval(instance.waitingForNumber, instance.waitingForNumber);
//						KaraAnnouncer.getReady();
						instance.playKaraoke(instance.waitingForNumber);
						instance.waitingForNumber = null;
					} catch (Exception e1) {
						LOGGER.log(Level.SEVERE, "Exception", e1);;
					}
				}
			});
		}
	}
    
    int chooseKaraokeRoot(){
    	fileChooser.setDialogTitle("Choose root folder of karaoke songs");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION){
        	try {
        		KaraokeReader.readSongs(fileChooser.getSelectedFile().getAbsolutePath());
    			txtRootFolder.setText(fileChooser.getSelectedFile().getAbsolutePath());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, "Error reading the files occured! \n" + e1.getMessage());
				LOGGER.log(Level.SEVERE, "Exception", e1);
				System.exit(0);
			}
        	
        }
        return res;
    }
    
    void chooseRecFolder(boolean isInit){
    	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
    	fileChooser.setDialogTitle("Choose recordings folder(need a lot of free GB space)");
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION){
        	if (isInit){
	        	JOptionPane.showMessageDialog(this, "No folder selected. asshole!");
	        	System.exit(0);
        	}
        }
        else{
	        txtRecFolder.setText(fileChooser.getSelectedFile().getAbsolutePath());
	        try {
				saveSettings();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error changing dirs occured! \n" + e.getMessage());
				LOGGER.log(Level.SEVERE, "Exception", e);
			}
        }
    }
    
    @SuppressWarnings("deprecation")
	void updateWaitingTimes(){
    	val now = new Date().getTime();
    	for (int i = mainList.getRowCount() - 1; i >= 0; i--){
    		if (!(boolean)getVal(i, 0, mainList)){
    			val interval = new Date(now - Long.valueOf(getVal(i, TIME, mainList).toString()));
    			interval.setHours(interval.getHours() - 2);
    			setVal(getWait(dateFormat.print(interval.getTime())), i, WAIT);
    		}
    	}
    }
    
    
    void saveSettings() throws Exception {
    	new File(Data.settingsFileName).delete();
    	PrintStream ps = new PrintStream(Data.settingsFileName, "UTF-8");
		ps.println(txtRootFolder.getText());
		ps.println(txtRecFolder.getText());
		if (cmbAudioRec.getItemCount() < 1 || cmbVideoRec.getItemCount() < 1){
			ps.println("none");
			ps.println("none");
		}
		else{
			ps.println(((ComboListItem)cmbAudioRec.getSelectedItem()).name);
			ps.println(((ComboListItem)cmbVideoRec.getSelectedItem()).name);
		}
		ps.println(btnEnableCamera.getText());
		ps.close();
    }
    
    
    void loadSettings() throws Exception {
    	@Cleanup val br = new BufferedReader(new FileReader(Data.settingsFileName));
		txtRootFolder.setText(br.readLine());
		txtRecFolder.setText(br.readLine());
		if (cmbAudioRec.getItemCount() >= 1 && cmbVideoRec.getItemCount() >= 1){
	    	cmbAudioRec.setSelectedItem(new ComboListItem(br.readLine(), ""));
	    	cmbVideoRec.setSelectedItem(new ComboListItem(br.readLine(), ""));
		}
    	if (btnEnableCamera.isVisible())
    		btnEnableCamera.setText(br.readLine());
    }
    
    boolean isCamEnabled(){
    	return (btnEnableCamera.getText().contains("Disable"));
    }
    
    void addEventListeners(){
    	btnPlayMain.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					playKaraokeSong();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});
    	
		chckbxShow.addActionListener(new KaraAction() {
			public void performAction() {
				settingsPane.setVisible(chckbxShow.isSelected());
			}
		});
		
		btnRemoveMain.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					removeSong(mainModel, mainList.getSelectedRow(), true);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new KaraWindowAdapter(this));
		btnClearPlayed.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					clearPlayed();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});
		btnAddMain.addActionListener(new KaraAction() {
			public void performAction() {
				//TODO:manual add functionality
			}
		});
		btnRemoveGap.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					removeSong(gapModel, gapList.getSelectedRow(), false);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});
		btnAddGap.addActionListener(new KaraAction() {
			public void performAction() {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setDialogTitle("Add gap music");
				fileChooser.setMultiSelectionEnabled(true);
				if (fileChooser.showOpenDialog(instance) == JFileChooser.APPROVE_OPTION){
					for (val file : fileChooser.getSelectedFiles())
						addGap(file, gapList.getRowCount());
					try {
						timer.schedule(new TimerTask() {
							
							@Override
							public void run() {
								try {
									savePlaylistToFile(Data.gaplistFileName);
								} catch (Exception e) {
									LOGGER.log(Level.SEVERE, "Exception", e);
								}
								
							}
						}, 2000);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOGGER.log(Level.SEVERE, "Exception", e);;
					}
				}
			}
		});
		btnClearGap.addActionListener(new KaraAction() {
			public void performAction() {
				val answer = JOptionPane.showConfirmDialog(instance, "Are you sure you want to clear the list?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
    			if (answer == JOptionPane.YES_OPTION)
    				gapModel.setRowCount(0);
			}
		});
		btnMarkFaulty.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					markFaulty();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});

		btnSavePlaylist.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					saveAsGapPlaylist();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});
		btnLoadPlaylist.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					loadSavedGapList();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e1);;
				}
			}
		});
		btnTestReecording.addActionListener(new KaraAction()   {
			
			public void performAction()  {
				try{
					shouldContinueGap = false;
					mainList.clearSelection();
					startRecording("Tester", "TestSong", "shachnatz@karaokemp.org.il");
					playASong("Recording Test", "RecordingTestBase.mp4", "Test");
				}
				catch (Exception ex){
					LOGGER.log(Level.SEVERE, "Exception", ex);;
				}
				
			}
		});
		
		btnChange.addActionListener(new KaraAction() {
			public void performAction() {
				if (chooseKaraokeRoot() == JFileChooser.APPROVE_OPTION){
					val answer = JOptionPane.showConfirmDialog(instance, "Would you like to clear the requests list?", "Clear Requests", JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION)
						mainModel.setRowCount(0);
					
					savePlaylist(Data.playlistFileName);
					ParticipantControl.reloadParticipantPlaylist();
					
				}
			}
		});
		
		btnChangeRecFolder.addActionListener(new KaraAction() {
			public void performAction() {
				instance.chooseRecFolder(false);
			}
		});
		
		cmbAudioRec.addActionListener(new KaraAction() {
			@Override
			public void performAction() {
				try {
					saveSettings();
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Exception", e);;
				}
			}
		});
		
		cmbVideoRec.addActionListener(new KaraAction() {
			@Override
			public void performAction() {
				try {
					saveSettings();
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
			}
		});
		
		chckbxTestOnAir.addActionListener(new KaraAction() {
			@Override
			public void performAction() {
				if (chckbxTestOnAir.isSelected())
					OnAirControl.onAir();
				else
					OnAirControl.offAir();
			}
		});
		btnEnableCamera.addActionListener(new KaraAction() {
			@Override
			public void performAction() {
				if (isCamEnabled()){
					btnEnableCamera.setText("Enable Camera");
				}
				else{
					btnEnableCamera.setText("Disable Camera");
					if (recorder == null){
						try {
							recorder = new KaraRecorder();
						} catch (RecorderNotInitializedException e) {
							btnEnableCamera.setText("Enable Camera");
							JOptionPane.showMessageDialog(instance, "Error Initializing The Recorder, Consult The Captain!", "Recording error", ERROR);
							LOGGER.log(Level.SEVERE, "RecorderNotInitializedException", e);
						}
					}
				}
			}
		});
		
		btnClearMain.addActionListener(new KaraAction() {
			@Override
			public void performAction() {
				val answer = JOptionPane.showConfirmDialog(instance, "Are you sure you want to clear all requests?", "Clear Requests", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION){
					mainModel.setRowCount(0);
					savePlaylist(Data.playlistFileName);
				}
			}
		});
		btnViewRecFiles.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					Desktop.getDesktop().open(new File(txtRecFolder.getText()));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(instance, "Error opening the folder", "Error", ERROR);
					LOGGER.log(Level.SEVERE, "Exception", e1);
				}
			}
		});
		
		btnViewRoot.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					Desktop.getDesktop().open(new File(txtRootFolder.getText()));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(instance, "Error opening the folder", "Error", ERROR);
					LOGGER.log(Level.SEVERE, "Exception", e1);
				}
			}
		});
		
		btnHelp.addActionListener(new KaraAction() {
			public void performAction() {
				try {
					Desktop.getDesktop().open(new File("Tutorial.mp4"));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(instance, "Error opening the instructions video", "Error", ERROR);
					LOGGER.log(Level.SEVERE, "Exception", e1);
				}
			}
		});
		tglbtnAutoPilot.setBounds(1154, 364, 121, 23);
		contentPane.add(tglbtnAutoPilot);
		gapControlsPanel.setBounds(1152, 39, 148, 103);
		
		contentPane.add(gapControlsPanel);
		gapControlsPanel.setLayout(null);
		btnStopGap.addActionListener(new KaraAction() {
			public void performAction() {
	    		btnPlayMain.setText("Play");
	    		btnPlayGap.setVisible(true);
	    		btnContinuePlayGap.setVisible(true);
				shouldContinueGap = false;
				stopMPC();
			}
		});
		btnPlayGap.addActionListener(new KaraAction() {
			public void performAction() {
				playGapFromStart();
			}
		});
		btnContinuePlayGap.addActionListener(new KaraAction() {
			public void performAction() {
				continuePlayGap();
			}
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		tglbtnAutoPilot.addActionListener(new KaraAction() {
			@Override
			public void performAction() {
				if (isAutoPilot && !tglbtnAutoPilot.isSelected()){
					val answer = JOptionPane.showConfirmDialog(instance, "Are you sure you want to cancel auto pilot?!", "Cancel Auto Pilot", JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION){
						isAutoPilot = false;
						karaokeControlsPanel.setVisible(true);
						gapControlsPanel.setVisible(true);
						lblAutoPilot.setVisible(false);
					}else {
						tglbtnAutoPilot.setSelected(true);
					}
				}else if (!isAutoPilot && tglbtnAutoPilot.isSelected()){
					val answer = JOptionPane.showConfirmDialog(instance, "Are you sure you want to launch auto pilot?!", "Launch Auto Pilot", JOptionPane.YES_NO_OPTION);
					if (answer == JOptionPane.YES_OPTION){
						isAutoPilot = true;
						karaokeControlsPanel.setVisible(false);
						gapControlsPanel.setVisible(false);
						lblAutoPilot.setVisible(true);
						clearNotNumbered();
						timer.schedule(new AutomationTask(), 10000l, 10000l);
						if (!mediaPlayerComponent.getMediaPlayer().isPlaying()){
							playGapFromStart();
						}
					} else{
						tglbtnAutoPilot.setSelected(false);
					}
				}
			}
		});
    }
    
    boolean  prepareGapList(String action){
    	val savedDir = new File("SavedPlaylists");
    	if (!savedDir.exists())
    		savedDir.mkdirs();
    	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    	fileChooser.setDialogTitle("choose a file to " + action + " the playlist");
    	fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "KaraConnect playlist files";
			}
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".kcp") && !f.getName().equals(Data.gaplistFileName) && !f.getName().equals(Data.playlistFileName);
			}
		});
    	fileChooser.setCurrentDirectory(savedDir);
    	fileChooser.setSelectedFile(null);
    	return fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION;
    }
    
    void loadSavedGapList() throws Exception {
    	if (prepareGapList("load")){
    		gapModel.setRowCount(0);
    		loadPlaylistFromFile(fileChooser.getSelectedFile().getAbsolutePath());
    		savePlaylistToFile(Data.gaplistFileName);
    	}
    }
    
    File selectedFile(){
    	return fileChooser.getSelectedFile();
    }
    
    void saveAsGapPlaylist() throws Exception {
    	if (prepareGapList("save")){    		
    		if (checkNotEmpty(selectedFile().getAbsolutePath())){
    			val answer = JOptionPane.showConfirmDialog(instance, "Are you sure you want to override existing list?", "Confirm Override", JOptionPane.YES_NO_OPTION);
    			if (answer == JOptionPane.NO_OPTION)
    				return;
    		}
    		if (!selectedFile().getName().endsWith(".kcp"))
    			fileChooser.setSelectedFile(new File(selectedFile().getAbsolutePath() + ".kcp"));
    		savePlaylistToFile(selectedFile().getAbsolutePath());
    	}
    }
    
    void loadCaptureDevices() throws Exception {
    	if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC){
            updateCombo(cmbAudioRec, mediaPlayerComponent.getMediaPlayerFactory().newAudioMediaDiscoverer().getMediaList().items(), " ");
            updateCombo(cmbVideoRec, mediaPlayerComponent.getMediaPlayerFactory().newVideoMediaDiscoverer().getMediaList().items(), "920");
    	}
    	else if (SystemUtils.IS_OS_WINDOWS){
    		getWindowsDevices(cmbAudioRec, "audio");
    		getWindowsDevices(cmbVideoRec, "video");
    	}
    	
    	if (cmbAudioRec.getItemCount() < 1 || cmbVideoRec.getItemCount() < 1){
    		btnEnableCamera.setText("Enable Camera");
    		btnEnableCamera.setVisible(false);
    		cmbAudioRec.setVisible(false);
    		cmbVideoRec.setVisible(false);
    		lblRecordingsFolder.setVisible(false);
    		txtRecFolder.setVisible(false);
    		btnChangeRecFolder.setVisible(false);
    		lblAudiorec.setVisible(false);
    		lblVideorec.setVisible(false);
    		btnTestReecording.setVisible(false);
    		chckbxTestOnAir.setVisible(false);
    	}
    }
    
    
    void getWindowsDevices(JComboBox<ComboListItem> cmb, String type) throws Exception {
    	val p =Runtime.getRuntime().exec(new String[]{"WindowsHelper.exe", "-" + type});
    	@Cleanup val br =new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")));
    	String line = br.readLine();
    	while (line != null){
    		cmb.addItem(new ComboListItem(line, line));
    		line = br.readLine();
    	}
    }
    
    void updateCombo(JComboBox<ComboListItem> cmb, List<MediaListItem> deviceList, String filter){
    	for (MediaListItem item : deviceList){
    		if (item.subItems().isEmpty() && item.name().contains(filter))
    			cmb.addItem(new ComboListItem(item));
    		else
	    		for (MediaListItem sub : item.subItems())
	    			if (sub.mrl().contains("input") && sub.name().contains(filter))
	    				cmb.addItem(new ComboListItem(sub));
    	}
    }
    
    public static void addGap(File file, int targetRow){
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				instance.addGapSong(file, targetRow);
				
			}
		});
    	
    }
    
    void addGapSong(File file, int targetRow){
    	if (file.isDirectory())
    		for (File sub : file.listFiles(new KaraokeReader.KaraFilter()))
    			addGapSong(sub, targetRow);
		else
			gapModel.insertRow(targetRow, new Object[] {file.getName(), file.getPath()});
    }
    
    
    void markFaulty() throws Exception {
    	if (mainList.getSelectedRow() != -1){
    		val answer = JOptionPane.showConfirmDialog(instance, "Are you sure you want to mark this as a faulty song?", "Faulty Song", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION){
				@Cleanup val ps = new PrintStream(new FileOutputStream("FaultySongs.list", true), true, "UTF-8"); 
				ps.println(getVal(mainList.getSelectedRow(), 3, mainList).toString());
			}
    	}
    }
    
    void continuePlayGap(){
    	if (gapPlayingFile == null){
    		playGapFromStart();
    		return;
    	}
//    	stopCurrPlay();
    	isPlayingGap = true;
    	playASong(gapPlayingName, gapPlayingFile, "Gap Music", new BigDecimal((double) gapTime / 1000).setScale(1, BigDecimal.ROUND_HALF_EVEN).toString());
    	
    }
    
    void playGapFromStart(){
    	if (gapList.getRowCount() < 1)
    		return;
//    	stopCurrPlay();
    	int selected = Math.max(0, gapList.getSelectedRow());
		isPlayingGap = true;
		gapPlayingFile = (String)getVal(selected, 1, gapList);
		gapPlayingName = (String)getVal(selected, 0, gapList);
		playASong(gapPlayingName, gapPlayingFile, "Gap Music");
		gapModel.moveRow(selected, selected, gapModel.getRowCount() - 1);
    }
    
    void clearPlayed() throws Exception {
    	for (int i = mainList.getRowCount() - 1; i >= 0; i--){
    		try {
				if ((boolean)getVal(i, PLAYED, mainList) && (new Date().getTime() - Long.valueOf(getVal(i, TIME, mainList).toString()) >  1800000)){
					mainModel.removeRow(i);
				}
			} catch (NumberFormatException e) {
				LOGGER.log(Level.SEVERE, "error with: " + getVal(i, TIME, mainList).toString(), e);
			}
    	}
    	
    	savePlaylistToFile(Data.playlistFileName);
    }
    
    void clearNotNumbered() {
    	for (int i = mainList.getRowCount() - 1; i >= 0; i--){
    		try {
				if (getVal(i, PERF_NUMBER, mainList) == null || getVal(i, PERF_NUMBER, mainList).toString().equals("")){
					mainModel.removeRow(i);
				}
			} catch (NumberFormatException e) {
				LOGGER.log(Level.SEVERE, "error with: " + getVal(i, TIME, mainList).toString(), e);
			}
    	}
    	
    	try {
			savePlaylistToFile(Data.playlistFileName);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "error saving playlist after clearing non numbered", e);
		}
    }
    
    int countPlayed(){
    	int played = 0;
    	for (int i = mainList.getRowCount() - 1; i >= 0; i--){
    		if ((boolean)getVal(i, 0, mainList))
    			played++;
    	}
    	
    	return played;
    }
    
    void removeSong(DefaultTableModel model, int selectedIndex, boolean main)  throws Exception {
    	if (selectedIndex == -1)
    		JOptionPane.showMessageDialog(this, "No Song Selected!");
    	else {
    		if (main && !(boolean)getVal(selectedIndex, 0, mainList)){
    			val answer = JOptionPane.showConfirmDialog(this, "Song wasn's played yet, still want to remove it?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
    			if (answer == JOptionPane.NO_OPTION)
    				return;
    		}
    		model.removeRow(selectedIndex);
    		savePlaylistToFile(main ? Data.playlistFileName : Data.gaplistFileName);
    	}
    }
    
    private void checkExistingPlaylistsOnLoad(String fileName, String message)  throws Exception {
    	if (checkNotEmpty(fileName)){
    		val answer = JOptionPane.showConfirmDialog(this, message, "Previous playlist exists", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION)
				loadPlaylistFromFile(fileName);
    	}
    }
    
    private WaitingTime getWait(String val){
    	return new WaitingTime(val);
    }
    
    private boolean checkNotEmpty(String fileName)  throws Exception {
    	boolean notEmpty = false;
    	if (new File(fileName).exists()){
    		@Cleanup val br = new BufferedReader(new FileReader(fileName));
    		notEmpty = br.readLine() != null;
    	}
    	return notEmpty;
    }
    
    
    void loadPlaylistFromFile(String fileName) throws Exception {
    	@Cleanup val br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		String line = br.readLine();
		
		while (line != null){
			val fields = line.split(";");
			if (fileName.equals(Data.playlistFileName)){
				Object[] arr = new Object[9];
				System.arraycopy(fields, 0, arr, 0, fields.length);
				arr[PLAYED] = arr[PLAYED].equals("true");
				arr[WAIT] = getWait(arr[WAIT].toString());
				arr[RETRIES] = Long.valueOf(arr[RETRIES].toString());
				mainModel.addRow(arr);
			}
			else
				gapModel.addRow(fields);

			line = br.readLine();
		}
    	
    }
    
    public static void savePlaylist(String fileName){
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					instance.savePlaylistToFile(fileName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
			}
		});
    	
    }
    
    
    void savePlaylistToFile(String fileName) throws Exception  {
    	new File(fileName).delete();
    	@Cleanup PrintStream ps = new PrintStream(fileName, "UTF-8");
		val list = (fileName.equals(Data.playlistFileName) ? mainModel : gapModel);
		for (val row : list.getDataVector()){
			val newRow = new ArrayList<String>(((Vector<?>)row).size());
			for (val cell : ((Vector<?>)row)){
				newRow.add(cell.toString());
			}
			ps.println(String.join(";", newRow));
		}
    	
    }
    
    public static int addSong(Object... request) throws Exception 
    {
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					instance.addSongToMainPlaylist(request);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
			}
		});
    	
    	return instance.mainList.getRowCount() - instance.countPlayed() + 1;
    }
    
    public static String getNextSerial(){
    	if (!instance.isAutoPilot){
    		return "";
    	}
    	
    	instance.performerSerialNumber = (instance.performerSerialNumber + 1) % 1000;
    	return String.format("%03d", instance.performerSerialNumber);
    }
    
    private void addSongToMainPlaylist(Object... request) throws Exception 
    {
    	int i = Math.max(mainList.getRowCount() - 1, 0);
    	for (; i > 0 && (boolean)getVal(i, 0, mainList); i--){}
    	if (mainList.getRowCount() == 1 && (boolean)getVal(0, 0, mainList))
    		i = -1;
    	mainModel.insertRow(Math.min(i + 1, mainList.getRowCount()), request);
    	savePlaylistToFile(Data.playlistFileName);
    }
    
    
    private void playKaraokeSong()  throws Exception 
    {
    	if (btnPlayMain.getText().equals("Stop")){
    		btnPlayMain.setText("Play");
    		btnPlayGap.setVisible(true);
    		btnContinuePlayGap.setVisible(true);
    		stopMPC();
    		return;
    	}
    	if (mainList.getSelectedRow() == -1){
    		JOptionPane.showMessageDialog(this, "No Song Was Selected!");
    	}
    	else {
    		val selectedRowIndex = mainList.getSelectedRow();
    		if ((boolean)getVal(selectedRowIndex, 0, mainList)){
    			val answer = JOptionPane.showConfirmDialog(this, "Song already played, would you like to play it again?", "Duplicate Song Play", JOptionPane.YES_NO_OPTION);
    			if (answer == JOptionPane.NO_OPTION)
    				return;
    		}
//    		stopCurrPlay();
    		btnPlayMain.setText("Stop");
    		btnPlayGap.setVisible(false);
    		btnContinuePlayGap.setVisible(false);
    		playKaraoke(selectedRowIndex);
    	}
    }
    
    private void playKaraoke(int selectedRowIndex) throws Exception{
    	isPlayingGap = false;
		setVal(true, selectedRowIndex, 0);
		
		if (!getVal(selectedRowIndex, EMAIL, mainList).toString().equals("none") && isCamEnabled()){
			startRecording(getVal(selectedRowIndex, PERF, mainList).toString(), 
					getVal(selectedRowIndex, SONG, mainList).toString(),
					getVal(selectedRowIndex, EMAIL, mainList).toString());
		}
		playASong((String)getVal(selectedRowIndex, SONG, mainList), (String)getVal(selectedRowIndex, FILE, mainList), "Karaoke");
		mainModel.moveRow(selectedRowIndex, selectedRowIndex, mainList.getRowCount() - 1);
		clearPlayed();
		updateWaitingTimes();
		savePlaylistToFile(Data.playlistFileName);
    }
    
//    private void stopCurrPlay(){
//    	if (mediaPlayerComponent.getMediaPlayer().isPlaying()){
//    		mediaPlayerComponent.getMediaPlayer().stop();
//    	}
//    }
    
    
    void startRecording(String performer, String song, String email) throws Exception {
    	timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try{
					isRecording = true;
					val time = sdf.format(new Date());
			    	val fileName = performer + " Performing " + song + " " + time;
			    	val savedDir = new File(txtRecFolder.getText());
			    	if (!savedDir.exists())
			    		savedDir.mkdirs();
			    	@Cleanup PrintStream ps = new PrintStream(savedDir.getAbsolutePath() + "/" + fileName + ".email", "UTF-8");
					ps.println(email);
					ps.println(WordUtils.capitalizeFully(performer));
					ps.println(WordUtils.capitalizeFully(song));
					ps.println(time.split(" ")[0]);
					
					OnAirControl.onAir();
					
					recorder.startRecording(getMrl(cmbVideoRec), getMrl(cmbAudioRec), savedDir.getAbsolutePath() + "/" + fileName + ".mp4", duration - 500);
		    	}
				catch (FileNotFoundException e){
//					JOptionPane.showMessageDialog(instance, "Error Recording! \n" + e.getMessage());
					LOGGER.log(Level.SEVERE, "Exception", e);
				}
		    	catch (Exception e){
		    		LOGGER.log(Level.SEVERE, "Error", e);
		    	}
				
			}
		}, 500);
    	
    }
    
//    private String[] formatMrl(){
//    	String[] vlcParams = new String[2];
//    	vlcParams[0] = SystemUtils.IS_OS_LINUX ? getMrl(cmbVideoRec) : "dshow://";
//    	vlcParams[1] = SystemUtils.IS_OS_LINUX ? ":input-slave=" + getMrl(cmbAudioRec) :
//    		"--dshow-vdev=" /*+ getMrl(cmbVideoRec)*/ + " --dshow-adev="/* + getMrl(cmbAudioRec)*/;  
//    	return vlcParams;
//    }
    
    String getMrl(JComboBox<ComboListItem> cmb){
    	return ((ComboListItem)cmb.getSelectedItem()).mrl;
    }
    
    private Object getVal(int row, int col, JTable list){
    	return list.getValueAt(row, col);
    }
    
    private void setVal(Object val, int row, int col){
    	mainList.setValueAt(val, row, col);
    }
    
    private void playASong(String songName, String fileName, String playlistName){
    	playASong(songName, fileName, playlistName, "0");
    }
    
    private void playASong(String songName, String fileName, String playlistName, String startTime){
    	lblNowPlaying.setText(playlistName + " Playing: " + songName);
    	MainFrame.fileChooser.setSelectedFile(new File(fileName));
    	timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try{
					val volume = isPlayingGap ? 60 : 90;
					mediaPlayerComponent.getMediaPlayer().setVolume(volume);
		    		mediaPlayerComponent.getMediaPlayer().playMedia(fileName, ":start-time=" + startTime);
		    	}
		    	catch (Error e){
		    		LOGGER.log(Level.SEVERE, "Error", e);
		    	}
				
			}
		}, 100);
    }
    
    void stopMPC(){
    	try{
			mediaPlayerComponent.getMediaPlayer().stop();
		}
		catch (Error e){
			MainFrame.instance.dispose();
			this.dispose();
			Data.relaunch = true;
			VlcjPlayer.load();
			LOGGER.log(Level.SEVERE, "Error", e);
		}
    }
    
    private void applyPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(PlaylistFrame.class);
        setBounds(
            prefs.getInt("frameX"     , 300),
            prefs.getInt("frameY"     , 300),
            1362,
            859
        );
    }

    @Override
    protected void onShutdown() {
        if (wasShown()) {
            Preferences prefs = Preferences.userNodeForPackage(PlaylistFrame.class);
            prefs.putInt("frameX"      , getX     ());
            prefs.putInt("frameY"      , getY     ());
            prefs.putInt("frameWidth"  , getWidth ());
            prefs.putInt("frameHeight" , getHeight());
        }
    }
    
    public static void setTime(long time){
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				instance.positionPane.setTime(time);
		    	instance.statusBar.setTime(time);
		    	if (instance.isPlayingGap)
		    		instance.gapTime = time;
				
			}
		});
    	
    }
    
    public static void setDuration(long duration){
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				instance.positionPane.setDuration(duration);
		    	instance.statusBar.setDuration(duration);
				instance.duration = duration;
			}
		});
    	
    }

    @Subscribe
    public void onShowPlaylist(ShowPlaylistEvent event) {
        setVisible(true);
    }
    
    @Subscribe
    public void onMediaStopped(StoppedEvent e){
    	if (isRecording){
    		isRecording = false;
    		recorder.stopRecording();
    		OnAirControl.offAir();
    	}
    	btnPlayMain.setText("Play");
    	btnPlayGap.setVisible(true);
		btnContinuePlayGap.setVisible(true);
    	if (gapList.getRowCount() == 0)
    		return;
    	if (!shouldContinueGap){
    		shouldContinueGap = true;
    		return;
    	}
    	SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				if (isPlayingGap)
		    		playGapFromStart();
		    	else
		    		continuePlayGap();
				
			}
		});
    	
    }
    
    public static void release(){
    	DB.clean();
    	Wachutu.cleanup();
    }
    
    
    public static void main(String[] args)  {
    	VlcjPlayer.main(args);
    }
    
    public static class GapTable extends JTable{
    	
    }
    
    class ComboListItem {
    	String name;
    	String mrl;
    	
		public ComboListItem(MediaListItem item) {
			name = item.name();
			mrl = item.mrl();
		}
		
		public ComboListItem(String name, String mrl){
			this.name = name;
			this.mrl = mrl;
		}
    	
		
		public String toString(){
			return name;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return name.equals(obj.toString());
		}
		
    }
    
    private static class KaraRenderer extends DefaultTableCellRenderer {

//        Color backgroundColor = getBackground();

        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        	Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
        	if (!isSelected && !hasFocus){
        		long time = dateFormat.parseMillis(value.toString());
        		if (((Boolean)table.getValueAt(row, PLAYED)))
        			time = 0l;
	            
	            if (time <= 300000){
	            	c.setBackground(null);
	            }
	            else if (time <= 900000){
	            	c.setBackground(Color.yellow);
	            }
	            else if (time <= 1500000){
	            	c.setBackground(Color.orange);
	            }
	            else {
	            	c.setBackground(Color.red);
	            }
        	}
            return c;
        }
    }
    
    public static class WaitingTime{
    	String val;
    	
    	public WaitingTime(String val) {
    		this.val = val;
		}
    	
    	@Override
    	public String toString(){
    		return val;
    	}
    }
}

