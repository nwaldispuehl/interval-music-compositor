package ch.retorte.intervalmusiccompositor.ui;

import static ch.retorte.intervalmusiccompositor.commons.Utf8Bundle.getBundle;
import static ch.retorte.intervalmusiccompositor.list.BlendMode.SEPARATE;
import static ch.retorte.intervalmusiccompositor.list.CompositionMode.SIMPLE;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.CONTINUOUS;
import static ch.retorte.intervalmusiccompositor.list.EnumerationMode.SINGLE_EXTRACT;
import static ch.retorte.intervalmusiccompositor.list.ListSortMode.SORT;
import static com.google.common.collect.Lists.newArrayList;
import static java.awt.Toolkit.getDefaultToolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.commons.FormatTime;
import ch.retorte.intervalmusiccompositor.commons.MessageFormatBundle;
import ch.retorte.intervalmusiccompositor.commons.platform.Platform;
import ch.retorte.intervalmusiccompositor.commons.platform.PlatformFactory;
import ch.retorte.intervalmusiccompositor.compilation.CompilationParameters;
import ch.retorte.intervalmusiccompositor.list.AudioFileListType;
import ch.retorte.intervalmusiccompositor.list.BlendMode;
import ch.retorte.intervalmusiccompositor.list.CompositionMode;
import ch.retorte.intervalmusiccompositor.list.EnumerationMode;
import ch.retorte.intervalmusiccompositor.list.ListSortMode;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.messagebus.ProgressMessage;
import ch.retorte.intervalmusiccompositor.spi.ApplicationData;
import ch.retorte.intervalmusiccompositor.spi.MusicCompilationControl;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.ProgramControl;
import ch.retorte.intervalmusiccompositor.spi.Ui;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageHandler;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageSubscriber;
import ch.retorte.intervalmusiccompositor.spi.update.UpdateAvailabilityChecker;
import ch.retorte.intervalmusiccompositor.ui.about.AboutDialogControl;
import ch.retorte.intervalmusiccompositor.ui.bpm.DetermineBpmDialogControl;
import ch.retorte.intervalmusiccompositor.ui.gfx.BarChart;
import ch.retorte.intervalmusiccompositor.ui.list.AudioFileCellRenderer;
import ch.retorte.intervalmusiccompositor.ui.list.DraggableAudioFileList;

/**
 * @author nw
 */
public class SwingUserInterface extends JFrame implements Ui {

  private MessageFormatBundle bundle = getBundle("ui_imc");

  private static final long serialVersionUID = 1L;
  private JPanel contentPane;

  private Platform platform = new PlatformFactory().getPlatform();

  private JSpinner periodField;
  private JSpinner breakField;
  private JSpinner iterationsField;

  private JSlider blendSlider;
  private final JProgressBar progressBar;
  private final IMCButton process;
  private final IMCButton quit;
  private final IMCButton about;
  private JButton outfileChooserButton;
  private JButton sortButton;
  private JButton shuffleButton;
  private AboutDialogControl adc;
  private JLabel musicTracksTitle = null;
  private JLabel listSortModeLabel = null;
  private final DraggableAudioFileList musicTracks;
  private final DraggableAudioFileList breakTracks;

  private CompositionMode compositionMode = SIMPLE;

  private ImageIcon titleImage;
  private final JLabel image;
  private JPanel blendPanel;
  private JLabel durationEstimation;
  private JPanel blendModeButtonsPanel;

  private JToggleButton blendModeRadiobutton1;
  private JToggleButton blendModeRadiobutton2;

  private JToggleButton enumerationModeRadiobutton1;
  private JToggleButton enumerationModeRadiobutton2;

  private List<Integer> soundPattern = new ArrayList<Integer>();
  private List<Integer> breakPattern = new ArrayList<Integer>();
  private Integer iterations = 0;
  private double blendTime = 0;
  private BlendMode blendMode = SEPARATE;
  private String outputDirectory = platform.getDesktopPath();
  private ListSortMode listSortMode = SORT;
  private EnumerationMode enumerationMode = SINGLE_EXTRACT;

  private JLabel blendModeLabel;
  private JLabel blendMode1Label;
  private JLabel blendMode2Label;
  private JLabel enumerationModeLabel;
  private JLabel enumerationMode1Label;
  private JLabel enumerationMode2Label;
  private JTabbedPane simpleAdvancedPane;
  private JPanel simplePanel;
  private JPanel advancedPanel;
  private JPanel iterationsPanel;
  private Component verticalStrut;
  private Component verticalStrut_1;
  private Component verticalStrut_2;
  private JPanel settingsPanel;
  private Box horizontalBox;
  private Component verticalStrut_3;
  private Component verticalStrut_4;
  private Box verticalBox_2;
  private Component horizontalStrut_3;
  private Box verticalBox_3;
  private Component verticalStrut_5;
  private Box horizontalBox_4;
  private Component horizontalStrut_4;
  private Component horizontalStrut_5;
  private JPanel blendBorderPanel;
  private JPanel outfileBorderPanel;
  private Box horizontalBox_5;
  private Component horizontalStrut_6;
  private Component horizontalGlue_3;
  private Component horizontalGlue_4;
  private Component verticalStrut_7;
  private JPanel sliderLabelPanel;
  private JPanel blendModeLabelPanel;
  private Component verticalStrut_8;
  private JPanel durationPanel;
  private Component verticalGlue;
  private Box verticalBox_4;
  private JLabel soundPatternLabel;
  private JTextField soundPatternField;
  private JLabel breakPatternLabel;
  private JTextField breakPatternField;
  private Component verticalStrut_9;
  private Component verticalGlue_2;
  private JPanel blendSpacePanel;
  private Box verticalBox_5;
  private Component verticalGlue_3;
  private JPanel numberSpacePanel;
  private Box verticalBox_6;
  private Component verticalGlue_4;
  private Component verticalStrut_10;
  private Component verticalStrut_11;
  private Component verticalGlue_5;
  private Box verticalBox_7;
  private Component verticalStrut_12;
  private Component verticalStrut_13;
  private JPanel sortShuffleButtonPanel;
  private JPanel enumerationModePanel;
  private Component verticalStrut_14;
  private Component verticalStrut_15;
  private Component verticalStrut_16;
  private JPanel breakTracksTitlePanel;
  private JPanel musicTracksTitlePanel;
  private Box horizontalBox_1;
  private Component horizontalStrut;
  private Component horizontalStrut_7;
  private Component horizontalGlue;
  private Component verticalStrut_17;
  private Component verticalStrut_18;
  private Component verticalStrut_19;
  private Component verticalGlue_1;

  private MusicListControl musicListControl;

  private ProgramControl programControl;

  private MessageProducer messageProducer;

  private ApplicationData applicationData;

  /**
   * Create the frame.
   */
  public SwingUserInterface(final MusicListControl musicListControl, final MusicCompilationControl musicCompilationController,
      final ProgramControl programControl, ApplicationData applicationData,
                            UpdateAvailabilityChecker updateAvailabilityChecker, MessageSubscriber messageSubscriber,
                            MessageProducer messageProducer) {

    this.musicListControl = musicListControl;
    this.programControl = programControl;
    this.applicationData = applicationData;
    this.messageProducer = messageProducer;

    setUiProperties();
    addProgressMessageSubscriber(messageSubscriber);
    createAboutDialogControl(updateAvailabilityChecker);

    setTitle();
    setProgramIcons();
    setTitleImage();

    setDefaultClosingAction();


    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
    // contentPane.setMaximumSize(new Dimension(990, 720));
    setContentPane(contentPane);

    JSplitPane splitPane = new JSplitPane();
    splitPane.setEnabled(false);

    JPanel left_panel = new JPanel();
    left_panel.setBorder(null);
    splitPane.setLeftComponent(left_panel);
    left_panel.setLayout(new BoxLayout(left_panel, BoxLayout.X_AXIS));

    verticalBox_7 = Box.createVerticalBox();
    left_panel.add(verticalBox_7);

    musicTracksTitlePanel = new JPanel();
    musicTracksTitlePanel.setMaximumSize(new Dimension(320, 60));
    musicTracksTitlePanel.setAlignmentY(Component.TOP_ALIGNMENT);
    musicTracksTitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    verticalBox_7.add(musicTracksTitlePanel);

    musicTracksTitle = new JLabel(bundle.getString("ui.form.music_list.label"));
    musicTracksTitlePanel.add(musicTracksTitle);
    musicTracksTitle.setHorizontalAlignment(SwingConstants.CENTER);

    verticalStrut_12 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_12);

    musicTracks = new DraggableAudioFileList(this, AudioFileListType.MUSIC, musicListControl, messageProducer);

    musicTracks.getModel().addListDataListener(new ListDataListener() {

      @Override
      public void intervalRemoved(ListDataEvent e) {
        updateUsableTracks();
        updateTrackListExtractLengthInformation();
      }

      @Override
      public void intervalAdded(ListDataEvent e) {
        updateUsableTracks();
        updateTrackListExtractLengthInformation();
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        updateUsableTracks();
        updateTrackListExtractLengthInformation();
      }
    });

    musicTracks.addKeyListener(new KeyListener() {

      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getComponent().hasFocus() && (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
          DraggableAudioFileList src = (DraggableAudioFileList) e.getSource();
          int[] selected = src.getSelectedIndices();

          musicListControl.removeMusicTracks(selected);
          if (0 < selected.length) {
            src.setSelectedIndex(selected[0] - 1);
          }
        }
      }

      @Override
      public void keyPressed(KeyEvent e) {
      }
    });

    JScrollPane musicScrollPane = new JScrollPane(musicTracks);
    musicScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    musicScrollPane.setMinimumSize(new Dimension(320, 320));
    musicScrollPane.setPreferredSize(new Dimension(320, 500));
    musicScrollPane.setMaximumSize(new Dimension(320, Short.MAX_VALUE));

    verticalBox_7.add(musicScrollPane);

    musicScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    musicTracks.setValueIsAdjusting(true);
    musicScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

    verticalStrut_13 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_13);

    sortShuffleButtonPanel = new JPanel();
    sortShuffleButtonPanel.setMaximumSize(new Dimension(320, 40));
    sortShuffleButtonPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    sortShuffleButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    verticalBox_7.add(sortShuffleButtonPanel);
    sortShuffleButtonPanel.setLayout(new BoxLayout(sortShuffleButtonPanel, BoxLayout.X_AXIS));

    horizontalBox_1 = Box.createHorizontalBox();
    sortShuffleButtonPanel.add(horizontalBox_1);

    sortButton = new JButton(bundle.getString("ui.form.sort_button_text"));
    horizontalBox_1.add(sortButton);

    horizontalStrut = Box.createHorizontalStrut(10);
    horizontalBox_1.add(horizontalStrut);

    shuffleButton = new JButton(bundle.getString("ui.form.shuffle_button_text"));
    horizontalBox_1.add(shuffleButton);

    horizontalStrut_7 = Box.createHorizontalStrut(10);
    horizontalBox_1.add(horizontalStrut_7);

    listSortModeLabel = new JLabel("");
    horizontalBox_1.add(listSortModeLabel);

    horizontalGlue = Box.createHorizontalGlue();
    horizontalBox_1.add(horizontalGlue);
    shuffleButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        musicListControl.shuffleMusicList();
        musicTracks.clearSelection();

        addDebugMessage("Shuffle music list");
        refresh();
      }
    });
    sortButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        musicListControl.sortMusicList();
        musicTracks.clearSelection();

        addDebugMessage("Sort music list");
        refresh();
      }
    });

    verticalStrut_14 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_14);

    JSeparator topSeparator = new JSeparator();
    topSeparator.setMaximumSize(new Dimension(320, 10));
    topSeparator.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    topSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
    verticalBox_7.add(topSeparator);

    Component verticalStrut_100 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_100);

    /* Enumeration selectors */
    enumerationModePanel = new JPanel();
    enumerationModePanel.setMaximumSize(new Dimension(320, 40));
    enumerationModePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    enumerationModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    enumerationModePanel.setLayout(new BoxLayout(enumerationModePanel, BoxLayout.X_AXIS));
    verticalBox_7.add(enumerationModePanel);

    ButtonGroup enumerationModeGroup = new ButtonGroup();

    Box enumerationBox = Box.createHorizontalBox();
    enumerationModePanel.add(enumerationBox);

    enumerationBox.add(Box.createHorizontalGlue());

    enumerationModeLabel = new JLabel(bundle.getString("ui.form.enumeration_mode.label"));
    enumerationBox.add(enumerationModeLabel);

    enumerationBox.add(Box.createHorizontalStrut(10));

    enumerationModeRadiobutton1 = new JRadioButton("");
    enumerationModeRadiobutton1.setToolTipText(bundle.getString("ui.form.enumeration_mode.icon1.tooltip"));
    enumerationBox.add(enumerationModeRadiobutton1);
    enumerationModeGroup.add(enumerationModeRadiobutton1);
    enumerationModeGroup.setSelected(enumerationModeRadiobutton1.getModel(), true);

    enumerationModeRadiobutton1.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        addDebugMessage("Enumeration mode: " + SINGLE_EXTRACT);
        enumerationMode = SINGLE_EXTRACT;
        refresh();
      }
    });

    enumerationMode1Label = new JLabel();
    enumerationMode1Label.setIcon(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.form.enumeration_mode.icon1"))));
    enumerationMode1Label.setToolTipText(bundle.getString("ui.form.enumeration_mode.icon1.tooltip"));
    enumerationBox.add(enumerationMode1Label);

    enumerationBox.add(Box.createHorizontalStrut(10));

    enumerationModeRadiobutton2 = new JRadioButton("");
    enumerationModeRadiobutton2.setToolTipText(bundle.getString("ui.form.enumeration_mode.icon2.tooltip"));
    enumerationBox.add(enumerationModeRadiobutton2);
    enumerationModeGroup.add(enumerationModeRadiobutton2);

    enumerationModeRadiobutton2.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        addDebugMessage("Enumeration mode: " + CONTINUOUS);
        enumerationMode = CONTINUOUS;
        refresh();
      }
    });

    enumerationMode2Label = new JLabel();
    enumerationMode2Label.setIcon(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.form.enumeration_mode.icon2"))));
    enumerationMode2Label.setToolTipText(bundle.getString("ui.form.enumeration_mode.icon2.tooltip"));
    enumerationBox.add(enumerationMode2Label);

    enumerationBox.add(Box.createHorizontalGlue());

    Component verticalStrut_101 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_101);

    JSeparator bottomSeparator = new JSeparator();
    bottomSeparator.setMaximumSize(new Dimension(320, 10));
    bottomSeparator.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    bottomSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
    verticalBox_7.add(bottomSeparator);

    verticalStrut_16 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_16);

    breakTracksTitlePanel = new JPanel();
    breakTracksTitlePanel.setMaximumSize(new Dimension(320, 40));
    breakTracksTitlePanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    breakTracksTitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    verticalBox_7.add(breakTracksTitlePanel);

    JLabel breakTracksTitle = new JLabel(bundle.getString("ui.form.break_list.label"));
    breakTracksTitlePanel.add(breakTracksTitle);
    breakTracksTitle.setHorizontalAlignment(SwingConstants.CENTER);

    verticalStrut_15 = Box.createVerticalStrut(10);
    verticalBox_7.add(verticalStrut_15);

    breakTracks = new DraggableAudioFileList(this, AudioFileListType.BREAK, musicListControl, messageProducer);
    breakTracks.getModel().addListDataListener(new ListDataListener() {

      @Override
      public void intervalRemoved(ListDataEvent e) {
        updateTrackListExtractLengthInformation();
      }

      @Override
      public void intervalAdded(ListDataEvent e) {
        updateTrackListExtractLengthInformation();
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        updateTrackListExtractLengthInformation();
      }
    });

    breakTracks.setMinimumSize(new Dimension(320, 120));
    breakTracks.setPreferredSize(new Dimension(320, 120));
    breakTracks.setMaximumSize(new Dimension(320, Short.MAX_VALUE));

    breakTracks.addKeyListener(new KeyListener() {

      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getComponent().hasFocus() && e.getKeyCode() == KeyEvent.VK_DELETE) {
          DraggableAudioFileList src = (DraggableAudioFileList) e.getSource();

          int[] selected = src.getSelectedIndices();

          musicListControl.removeBreakTracks(selected);
          if (0 < selected.length) {
            src.setSelectedIndex(selected[0] - 1);
          }
        }
      }

      @Override
      public void keyPressed(KeyEvent e) {
      }
    });

    JScrollPane breakScrollPane = new JScrollPane(breakTracks);
    breakScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    breakScrollPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    verticalBox_7.add(breakScrollPane);
    breakScrollPane.setMinimumSize(new Dimension(320, 120));
    breakScrollPane.setPreferredSize(new Dimension(320, 120));
    breakScrollPane.setMaximumSize(new Dimension(320, Short.MAX_VALUE));
    breakScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    breakScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    breakTracks.setVisibleRowCount(1);
    breakScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

    JPanel right_panel = new JPanel();

    right_panel.setPreferredSize(new Dimension(620, 720));
    right_panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    pack();

    splitPane.setRightComponent(right_panel);

    ButtonGroup blendModeGroup = new ButtonGroup();
    right_panel.setLayout(new BoxLayout(right_panel, BoxLayout.X_AXIS));

    Box verticalBox_1 = Box.createVerticalBox();
    verticalBox_1.setAlignmentX(Component.CENTER_ALIGNMENT);
    verticalBox_1.setSize(620, 70);

    right_panel.add(verticalBox_1);

    JLabel instructions = new JLabel();
    instructions.setMaximumSize(new Dimension(620, 120));
    instructions.setAlignmentY(Component.TOP_ALIGNMENT);
    verticalBox_1.add(instructions);
    instructions.setText("<html><b>" + applicationData.getProgramName() + " " + applicationData.getProgramVersion() + "</b><br/><br/>"
        + bundle.getString("ui.instructions") + "</html>");

    verticalStrut = Box.createVerticalStrut(20);
    verticalBox_1.add(verticalStrut);
    this.image = new JLabel(titleImage);
    image.setAlignmentY(Component.TOP_ALIGNMENT);
    verticalBox_1.add(image);
    this.image.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    verticalStrut_1 = Box.createVerticalStrut(20);
    verticalBox_1.add(verticalStrut_1);

    progressBar = new JProgressBar(0, 100);
    progressBar.setMaximumSize(new Dimension(620, 120));
    progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    progressBar.setAlignmentY(Component.TOP_ALIGNMENT);
    verticalBox_1.add(progressBar);
    progressBar.setStringPainted(true);
    progressBar.setEnabled(false);

    verticalStrut_2 = Box.createVerticalStrut(20);
    verticalBox_1.add(verticalStrut_2);

    settingsPanel = new JPanel();
    settingsPanel.setMaximumSize(new Dimension(620, 620));
    settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    settingsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    verticalBox_1.add(settingsPanel);
    settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));

    horizontalBox = Box.createHorizontalBox();
    settingsPanel.add(horizontalBox);

    numberSpacePanel = new JPanel();
    horizontalBox.add(numberSpacePanel);
    numberSpacePanel.setLayout(new BoxLayout(numberSpacePanel, BoxLayout.X_AXIS));

    verticalBox_6 = Box.createVerticalBox();
    numberSpacePanel.add(verticalBox_6);

    JPanel numbersPanel = new JPanel();
    verticalBox_6.add(numbersPanel);
    numbersPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    numbersPanel.setLayout(new BoxLayout(numbersPanel, BoxLayout.X_AXIS));

    Box verticalBox = Box.createVerticalBox();
    numbersPanel.add(verticalBox);

    iterationsPanel = new JPanel();
    verticalBox.add(iterationsPanel);
    iterationsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    JLabel iterationsFieldLabel = new JLabel(bundle.getString("ui.form.iterations.label"));
    iterationsPanel.add(iterationsFieldLabel);
    iterationsFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    iterationsField = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
    iterationsPanel.add(iterationsField);

    iterationsField.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        addDebugMessage("Iterations: " + iterationsField.getValue());
        refresh();
      }
    });

    verticalStrut_5 = Box.createVerticalStrut(10);
    verticalBox.add(verticalStrut_5);

    simpleAdvancedPane = new JTabbedPane(JTabbedPane.TOP);
    verticalBox.add(simpleAdvancedPane);

    simplePanel = new JPanel();
    simplePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    simpleAdvancedPane.addTab(bundle.getString("ui.form.simple_pane.title"), null, simplePanel, null);

    simplePanel.setLayout(new BoxLayout(simplePanel, BoxLayout.X_AXIS));

    verticalBox_2 = Box.createVerticalBox();
    simplePanel.add(verticalBox_2);

    JLabel periodFieldLabel = new JLabel(bundle.getString("ui.form.sound_period.label"));
    verticalBox_2.add(periodFieldLabel);
    periodFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    verticalStrut_17 = Box.createVerticalStrut(5);
    verticalBox_2.add(verticalStrut_17);

    periodField = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
    periodField.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        addDebugMessage("Sound period: " + periodField.getValue());
        refresh();
      }
    });
    verticalBox_2.add(periodField);

    verticalStrut_18 = Box.createVerticalStrut(10);
    verticalBox_2.add(verticalStrut_18);

    JLabel breakFieldLabel = new JLabel(bundle.getString("ui.form.break_duration.label"));
    verticalBox_2.add(breakFieldLabel);
    breakFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    verticalStrut_19 = Box.createVerticalStrut(5);
    verticalBox_2.add(verticalStrut_19);

    breakField = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
    breakField.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        addDebugMessage("Break period: " + breakField.getValue());
        refresh();
      }
    });
    verticalBox_2.add(breakField);

    verticalGlue_1 = Box.createVerticalGlue();
    verticalBox_2.add(verticalGlue_1);

    advancedPanel = new JPanel();
    advancedPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    simpleAdvancedPane.addTab(bundle.getString("ui.form.advanced_pane.title"), null, advancedPanel, null);

    simpleAdvancedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent evt) {
        if (evt.getSource() instanceof JTabbedPane) {
          // update break times in other tab when changing.
          compositionMode = ((JTabbedPane) evt.getSource()).getSelectedIndex() == 1 ? CompositionMode.ADVANCED : CompositionMode.SIMPLE;

          if (compositionMode == CompositionMode.ADVANCED) {

            if (soundPatternField.getText().isEmpty()) {
              soundPatternField.setText(periodField.getValue().toString());
            }

            if (breakPatternField.getText().isEmpty()) {
              breakPatternField.setText(breakField.getValue().toString());
            }

          }
        }

        refresh();
      }
    });

    advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.X_AXIS));

    verticalBox_4 = Box.createVerticalBox();
    advancedPanel.add(verticalBox_4);

    soundPatternLabel = new JLabel(bundle.getString("ui.form.sound_pattern.label"));
    verticalBox_4.add(soundPatternLabel);

    verticalStrut_10 = Box.createVerticalStrut(5);
    verticalBox_4.add(verticalStrut_10);

    soundPatternField = new JTextField();

    soundPatternField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent evt) {
        char c = evt.getKeyChar();
        // allow only 0-9, comma and backspace.
        if (((c < KeyEvent.VK_0) || (c > KeyEvent.VK_9)) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_COMMA) {
          evt.consume();
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        addDebugMessage("Sound pattern: " + soundPatternField.getText());
        refresh();
      }
    });

    verticalBox_4.add(soundPatternField);
    // soundPattern.setColumns(10);

    verticalStrut_9 = Box.createVerticalStrut(10);
    verticalBox_4.add(verticalStrut_9);

    breakPatternLabel = new JLabel(bundle.getString("ui.form.break_pattern.label"));
    verticalBox_4.add(breakPatternLabel);

    verticalStrut_11 = Box.createVerticalStrut(5);
    verticalBox_4.add(verticalStrut_11);

    breakPatternField = new JTextField();

    breakPatternField.addKeyListener(new KeyAdapter() {

      @Override
      public void keyTyped(KeyEvent evt) {
        char c = evt.getKeyChar();
        // allow only 0-9, comma and backspace.
        if (((c < KeyEvent.VK_0) || (c > KeyEvent.VK_9)) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_COMMA) {
          evt.consume();
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        addDebugMessage("Break pattern: " + breakPatternField.getText());
        refresh();
      }
    });

    verticalBox_4.add(breakPatternField);
    // breakPattern.setColumns(10);

    verticalGlue_5 = Box.createVerticalGlue();
    verticalBox_4.add(verticalGlue_5);

    verticalGlue_2 = Box.createVerticalGlue();
    verticalBox.add(verticalGlue_2);

    verticalStrut_8 = Box.createVerticalStrut(20);
    verticalBox.add(verticalStrut_8);

    durationPanel = new JPanel();
    verticalBox.add(durationPanel);

    durationEstimation = new JLabel(bundle.getString("ui.form.duration_estimation.label.init"));
    durationEstimation.setHorizontalAlignment(SwingConstants.CENTER);

    durationPanel.add(durationEstimation);

    verticalGlue_4 = Box.createVerticalGlue();
    verticalBox_6.add(verticalGlue_4);

    horizontalStrut_3 = Box.createHorizontalStrut(20);
    horizontalBox.add(horizontalStrut_3);

    blendSpacePanel = new JPanel();
    horizontalBox.add(blendSpacePanel);
    blendSpacePanel.setLayout(new BoxLayout(blendSpacePanel, BoxLayout.X_AXIS));

    verticalBox_5 = Box.createVerticalBox();
    blendSpacePanel.add(verticalBox_5);

    blendBorderPanel = new JPanel();
    verticalBox_5.add(blendBorderPanel);
    blendBorderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    blendBorderPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    blendBorderPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    blendBorderPanel.setLayout(new BoxLayout(blendBorderPanel, BoxLayout.X_AXIS));

    blendPanel = new JPanel();
    blendBorderPanel.add(blendPanel);

    blendPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    blendPanel.setLayout(new BoxLayout(blendPanel, BoxLayout.X_AXIS));

    verticalBox_3 = Box.createVerticalBox();
    blendPanel.add(verticalBox_3);

    sliderLabelPanel = new JPanel();
    verticalBox_3.add(sliderLabelPanel);

    JLabel blendSliderLabel = new JLabel(bundle.getString("ui.form.blend_duration.label"));
    sliderLabelPanel.add(blendSliderLabel);
    blendSliderLabel.setHorizontalAlignment(SwingConstants.CENTER);

    blendSlider = new JSlider(0, 10, Integer.parseInt(bundle.getString("ui.form.blend_duration.initial_value")));
    verticalBox_3.add(blendSlider);
    blendSlider.setMajorTickSpacing(5);
    blendSlider.setMinorTickSpacing(1);
    blendSlider.setPaintTicks(true);
    blendSlider.setPaintLabels(true);
    blendSlider.setSnapToTicks(true);

    blendSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        addDebugMessage("Blend duration: " + blendSlider.getValue());
        refresh();
      }
    });

    blendModeLabelPanel = new JPanel();
    verticalBox_3.add(blendModeLabelPanel);

    blendModeLabel = new JLabel(bundle.getString("ui.form.blend_mode.label"));
    blendModeLabelPanel.add(blendModeLabel);

    blendModeLabel.setHorizontalAlignment(SwingConstants.CENTER);

    blendModeButtonsPanel = new JPanel();
    verticalBox_3.add(blendModeButtonsPanel);
    blendModeButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 5));

    blendModeRadiobutton1 = new JRadioButton("");
    blendModeRadiobutton1.setToolTipText(bundle.getString("ui.form.blend_mode.icon1.tooltip"));

    blendModeButtonsPanel.add(blendModeRadiobutton1);

    blendModeRadiobutton2 = new JRadioButton("");
    blendModeRadiobutton2.setToolTipText(bundle.getString("ui.form.blend_mode.icon2.tooltip"));

    blendMode1Label = new JLabel();
    blendMode1Label.setIcon(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.form.blend_mode.icon1"))));
    blendMode1Label.setToolTipText(bundle.getString("ui.form.blend_mode.icon1.tooltip"));
    blendModeButtonsPanel.add(blendMode1Label);
    blendModeButtonsPanel.add(blendModeRadiobutton2);

    blendMode2Label = new JLabel();
    blendMode2Label.setIcon(new ImageIcon(getClass().getClassLoader().getResource(bundle.getString("ui.form.blend_mode.icon2"))));
    blendMode2Label.setToolTipText(bundle.getString("ui.form.blend_mode.icon2.tooltip"));
    blendModeButtonsPanel.add(blendMode2Label);
    blendModeGroup.add(blendModeRadiobutton1);
    blendModeGroup.add(blendModeRadiobutton2);

    blendModeGroup.setSelected(blendModeRadiobutton1.getModel(), true);

    blendModeRadiobutton1.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        addDebugMessage("Blend mode: " + BlendMode.SEPARATE);
        refresh();
      }
    });

    blendModeRadiobutton2.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        addDebugMessage("Blend mode: " + BlendMode.CROSS);
        refresh();
      }
    });

    verticalGlue_3 = Box.createVerticalGlue();
    verticalBox_5.add(verticalGlue_3);

    verticalStrut_3 = Box.createVerticalStrut(20);
    verticalBox_1.add(verticalStrut_3);

    outfileBorderPanel = new JPanel();
    outfileBorderPanel.setMaximumSize(new Dimension(620, 120));
    outfileBorderPanel.setAlignmentY(Component.TOP_ALIGNMENT);
    outfileBorderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    outfileBorderPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

    verticalBox_1.add(outfileBorderPanel);
    outfileBorderPanel.setLayout(new BoxLayout(outfileBorderPanel, BoxLayout.X_AXIS));

    JPanel outfilePanel = new JPanel();
    outfileBorderPanel.add(outfilePanel);
    outfilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    outfilePanel.setAlignmentY(Component.TOP_ALIGNMENT);
    outfilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    outfilePanel.setLayout(new BoxLayout(outfilePanel, BoxLayout.X_AXIS));

    horizontalBox_5 = Box.createHorizontalBox();
    horizontalBox_5.setAlignmentX(Component.LEFT_ALIGNMENT);
    outfilePanel.add(horizontalBox_5);

    outfileChooserButton = new JButton(bundle.getString("ui.form.outfile_button_text"));
    horizontalBox_5.add(outfileChooserButton);

    horizontalStrut_6 = Box.createHorizontalStrut(20);
    horizontalBox_5.add(horizontalStrut_6);
    final JLabel outfileLabel = new JLabel(bundle.getString("ui.form.outfile_label"));
    horizontalBox_5.add(outfileLabel);

    horizontalGlue_3 = Box.createHorizontalGlue();
    horizontalBox_5.add(horizontalGlue_3);

    outfileChooserButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        String dialogTitle = bundle.getString("ui.form.outfile_dialog_title");
        int returnVal = 0;
        File selectedFile;

        if (platform.isMac()) {
          System.setProperty("apple.awt.fileDialogForDirectories", "true");
          FileDialog fd = new FileDialog((Frame) getParent(), dialogTitle, FileDialog.SAVE);
          SwingUtilities.updateComponentTreeUI(fd);
          fd.setVisible(true);
          String dir = fd.getDirectory();
          selectedFile = new File(dir);
        }
        else {
          JFileChooser fc = new JFileChooser(outputDirectory);
          fc.setDialogTitle(dialogTitle);
          fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          returnVal = fc.showSaveDialog(getParent());
          selectedFile = fc.getSelectedFile();
        }

        if (returnVal == JFileChooser.APPROVE_OPTION || (platform.isMac() && selectedFile != null)) {
          outputDirectory = selectedFile.getAbsolutePath();
          String shortPath = outputDirectory;

          if (50 < outputDirectory.length()) {
            shortPath = "..." + outputDirectory.substring(outputDirectory.length() - 47, outputDirectory.length());
          }

          outfileLabel.setToolTipText(outputDirectory);
          outfileLabel.setText(shortPath);

          addDebugMessage("Changed output directory to: " + outputDirectory);
        }
      }
    });

    verticalStrut_4 = Box.createVerticalStrut(20);
    verticalBox_1.add(verticalStrut_4);

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setMaximumSize(new Dimension(620, 120));
    buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    verticalBox_1.add(buttonsPanel);
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

    horizontalBox_4 = Box.createHorizontalBox();
    horizontalBox_4.setAlignmentX(Component.LEFT_ALIGNMENT);
    buttonsPanel.add(horizontalBox_4);

    process = new IMCButton(bundle.getString("ui.form.process_button_text"));
    process.doLayout();
    horizontalBox_4.add(process);

    horizontalStrut_4 = Box.createHorizontalStrut(20);
    horizontalBox_4.add(horizontalStrut_4);

    about = new IMCButton(bundle.getString("ui.form.about_button_text"));
    horizontalBox_4.add(about);

    horizontalStrut_5 = Box.createHorizontalStrut(20);
    horizontalBox_4.add(horizontalStrut_5);

    quit = new IMCButton(bundle.getString("ui.form.quit_button_text"));
    horizontalBox_4.add(quit);

    horizontalGlue_4 = Box.createHorizontalGlue();
    horizontalBox_4.add(horizontalGlue_4);

    verticalGlue = Box.createVerticalGlue();
    verticalBox_1.add(verticalGlue);

    verticalStrut_7 = Box.createVerticalStrut(20);
    verticalBox_1.add(verticalStrut_7);

    quit.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        quit();
      }

    });

    about.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        adc.startGui();
      }
    });

    process.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {

        int soundPeriod = 0;
        for (int p : soundPattern) {
          soundPeriod += p;
        }

        if (0 <= soundPeriod && 0 < iterations && musicListControl.isTrackListReady()) {

          // Reset to start image
          ((SwingUserInterface) ((JButton) arg0.getSource()).getTopLevelAncestor()).image.setIcon(titleImage);

          // Transfer values to control
          musicCompilationController.startCompilation(CompilationParameters.buildFrom(soundPattern, breakPattern, iterations, blendMode, blendTime,
              listSortMode, outputDirectory, enumerationMode));
        }

      }
    });
    contentPane.setLayout(new BorderLayout(0, 0));
    contentPane.add(splitPane);

    setMinimumSize(new Dimension(1010, 760));

    pack();
  }

  private void setProgramIcons() {
    List<Image> programIcons = newArrayList();
    programIcons.add(getDefaultToolkit().createImage(getClass().getClassLoader().getResource(bundle.getString("ui.program_icon.small"))));
    programIcons.add(getDefaultToolkit().createImage(getClass().getClassLoader().getResource(bundle.getString("ui.program_icon.large"))));
    setIconImages(programIcons);
  }

  private void setDefaultClosingAction() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        quit();
      }
    });
  }

  private void setTitleImage() {
    URL startImageUrl = getClass().getClassLoader().getResource(bundle.getString("ui.start_image"));
    titleImage = new ImageIcon(startImageUrl);
  }

  private void setTitle() {
    this.setTitle(applicationData.getProgramName());
  }

  private void createAboutDialogControl(UpdateAvailabilityChecker updateAvailabilityChecker) {
    adc = new AboutDialogControl(this, updateAvailabilityChecker, applicationData, messageProducer);
  }

  private void addProgressMessageSubscriber(MessageSubscriber messageSubscriber) {
    messageSubscriber.addHandler(new MessageHandler<ProgressMessage>() {

      @Override
      public void handle(ProgressMessage message) {
        updateProgressBar(message.getProgressInPercent(), message.getCurrentActivity());
      }

    });
  }

  private void setUIComponentsEnablement(boolean enabled) {
    Component[] statusChangingComponents = new Component[] { process, about, quit, sortButton, shuffleButton, outfileChooserButton, iterationsField,
        periodField, breakField, soundPatternField, breakPatternField, simpleAdvancedPane, blendSlider, musicTracks, breakTracks, blendModeRadiobutton1,
        blendModeRadiobutton2, enumerationModeRadiobutton1, enumerationModeRadiobutton2 };

    for (Component c : statusChangingComponents) {
      c.setEnabled(enabled);
    }
  }

  public void setInactive() {
    setWaitCursor();

    // Deactivate all control items
    setUIComponentsEnablement(false);

    // Activate progress bar
    progressBar.setVisible(true);
    progressBar.setEnabled(true);
    progressBar.setIndeterminate(true);
  }

  private void setWaitCursor() {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
  }

  public void setActive() {
    setDefaultCursor();
    setUIComponentsEnablement(true);
  }

  private void setDefaultCursor() {
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  public void updateProgressBar(int n, String s) {
    progressBar.setIndeterminate(false);
    progressBar.setValue(n);
    if (s == null) {
      s = bundle.getString("compilation.status.default");
    }
    progressBar.setString(s);
  }

  public synchronized void refresh() {
    
    messageProducer.send(new DebugMessage(this, "refresh"));
    
    fetchSortOrder();
    fetchSoundLists();

    updateMusicListSortModeIndicator();
    updateUsableTracks();
    parseFields();
    updateTrackListExtractLengthInformation();
    updateDuration();

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        musicTracks.repaint();
        breakTracks.repaint();
      }
    });

  }

  private void fetchSortOrder() {
    listSortMode = musicListControl.getSortMode();
  }

  private void fetchSoundLists() {
    ((DefaultListModel<IAudioFile>) musicTracks.getModel()).clear();

    for (IAudioFile audioFile : musicListControl.getMusicList()) {
      ((DefaultListModel<IAudioFile>) musicTracks.getModel()).addElement(audioFile);
    }

    ((DefaultListModel<IAudioFile>) breakTracks.getModel()).clear();

    for (IAudioFile audioFile : musicListControl.getBreakList()) {
      ((DefaultListModel<IAudioFile>) breakTracks.getModel()).addElement(audioFile);
    }
  }

  public void updateDuration() {
    String prefix = bundle.getString("ui.form.duration_estimation.label.prefix");

    int singleDuration = 0;

    for (int i = 0; i < soundPattern.size(); i++) {

      // Add current pattern item
      singleDuration += soundPattern.get(i);

      // Add current break item (or restart if there are not enough of them)
      singleDuration += breakPattern.get(i % breakPattern.size());
    }

    int durationEstimationValue = singleDuration * iterations;

    // If crossfading is activated, add fade interval once (= a half at start
    // and one at the end)
    if (blendMode.equals(BlendMode.CROSS) && 0 < singleDuration && 0 < iterations) {
      durationEstimationValue += blendTime;
    }

    durationEstimation.setText(prefix + " " + getFormattedTime(durationEstimationValue));

    if (0 < durationEstimationValue) {
      BarChart bar = new BarChart(620, 140);
      bar.generate(soundPattern, breakPattern, iterations, (0 < breakTracks.getModel().getSize()));
      image.setIcon(new ImageIcon(bar.getBufferedImage()));
    }
    else {
      image.setIcon(titleImage);
    }
  }

  private String getFormattedTime(int seconds) {
    return new FormatTime().getStrictFormattedTime(seconds);
  }

  public void parseFields() {

    iterations = (Integer) iterationsField.getValue();
    blendTime = blendSlider.getValue();

    if (blendModeRadiobutton1.getModel().isSelected()) {
      blendMode = BlendMode.SEPARATE;
    }
    else if (blendModeRadiobutton2.getModel().isSelected()) {
      blendMode = BlendMode.CROSS;
    }

    soundPattern.clear();
    breakPattern.clear();

    if (compositionMode.equals(CompositionMode.ADVANCED)) {

      for (String s : soundPatternField.getText().split(",")) {
        try {
          soundPattern.add(Math.abs(Integer.parseInt(s)));
        }
        catch (NumberFormatException e) {
          // We just ignore non-numeric input.
        }
      }

      for (String s : breakPatternField.getText().split(",")) {
        try {
          breakPattern.add(Math.abs(Integer.parseInt(s)));
        }
        catch (NumberFormatException e) {
          // We just ignore non-numeric input.
        }
      }

    }
    else {
      soundPattern.add((Integer) periodField.getValue());
      breakPattern.add((Integer) breakField.getValue());
    }

    // We want at least one element in the lists

    if (soundPattern.size() == 0) {
      soundPattern.add(0);
    }

    if (breakPattern.size() == 0) {
      breakPattern.add(0);
    }

  }

  @Override
  public void quit() {
    this.setEnabled(false);
    this.setVisible(false);
    this.dispose();
    programControl.quit();
  }


  public synchronized void updateUsableTracks() {
    String originalText = bundle.getString("ui.form.music_list.label");
    String tracksTextPl = bundle.getString("ui.form.music_list.tracks_label_pl");
    String tracksTextSg = bundle.getString("ui.form.music_list.tracks_label_sg");

    String tracksText = tracksTextPl;

    int usableTracks = musicListControl.getUsableTracks(soundPattern);

    // If there is only one track, we're using the singular
    if (usableTracks == 1) {
      tracksText = tracksTextSg;
    }

    final String outString = originalText + " (" + usableTracks + " " + tracksText + ")";

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        musicTracksTitle.setText(outString);
      }
    });

  }

  public void setEnvelopeImage(BufferedImage envelopeImage) {
    if (envelopeImage != null) {
      ImageIcon img = new ImageIcon(envelopeImage);
      image.setIcon(img);
      repaint();
    }
  }

  public void launch() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setVisible(true);
      }
    });
  }

  public void updateTrackListExtractLengthInformation() {
   ((AudioFileCellRenderer) musicTracks.getCellRenderer()).setDurationPatterns(soundPattern, listSortMode, blendMode, blendTime);
   ((AudioFileCellRenderer) breakTracks.getCellRenderer()).setDurationPatterns(soundPattern, listSortMode, blendMode, blendTime);
  }

  public synchronized void updateMusicListSortModeIndicator() {
    String prefix = bundle.getString("ui.form.music_list.list_mode.prefix");
    String sort = bundle.getString("ui.form.music_list.list_mode.sort");
    String sort_rev = bundle.getString("ui.form.music_list.list_mode.sort_rev");
    String shuffle = bundle.getString("ui.form.music_list.list_mode.shuffle");
    String manual = bundle.getString("ui.form.music_list.list_mode.manual");
    String labelText = "";

    listSortMode = musicListControl.getSortMode();

    if (listSortMode.equals(ListSortMode.SORT)) {
      labelText = prefix + " " + sort;
    }
    else if (listSortMode.equals(ListSortMode.SORT_REV)) {
      labelText = prefix + " " + sort_rev;
    }
    else if (listSortMode.equals(ListSortMode.SHUFFLE)) {
      labelText = prefix + " " + shuffle;
    }
    else if (listSortMode.equals(ListSortMode.MANUAL)) {
      labelText = prefix + " " + manual;
    }

    final String outText = labelText;

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        listSortModeLabel.setText(outText);
      }
    });
  }


  @Override
  public void showErrorMessage(String message) {
    JOptionPane.showMessageDialog(this, message, bundle.getString("ui.error.title"), JOptionPane.WARNING_MESSAGE);
  }

  public void startBpmDeterminationFor(AudioFileListType type, int selectedIndex) {
    new DetermineBpmDialogControl(type, selectedIndex, musicListControl, messageProducer).startGui();
  }

  private void setUiProperties() {
    try {
      /* Set system look and feel to standard. If there happens an exception; this just falls back to the standard Java ui style. */
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      addDebugMessage("Using look and feel: " + UIManager.getLookAndFeel().toString());
    }
    catch (Exception e) {
      addDebugMessage("Set UI properties: " + e.getMessage());
    }
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }
}
