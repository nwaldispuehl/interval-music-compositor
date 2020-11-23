package ch.retorte.intervalmusiccompositor.ui.list;

import static ch.retorte.intervalmusiccompositor.commons.Utils.newArrayList;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import ch.retorte.intervalmusiccompositor.audiofile.IAudioFile;
import ch.retorte.intervalmusiccompositor.list.AudioFileListType;
import ch.retorte.intervalmusiccompositor.messagebus.DebugMessage;
import ch.retorte.intervalmusiccompositor.spi.MusicListControl;
import ch.retorte.intervalmusiccompositor.spi.messagebus.MessageProducer;
import ch.retorte.intervalmusiccompositor.ui.SwingUserInterface;

/**
 * @author nw
 */
public class DraggableAudioFileList extends JList<IAudioFile> implements DropTargetListener, DragSourceListener, DragGestureListener {

  private static DraggableAudioFileList staticDragList = null;
  private static Integer staticDragIndex = null;

  private static final long serialVersionUID = 1L;
  private DropTarget dropTarget;
  private DragSource dragSource;
  private AudioFileListType type;

  private MusicListControl musicListControl;
  private SwingUserInterface ui;
  private MessageProducer messageProducer;

  public DraggableAudioFileList(SwingUserInterface ui, AudioFileListType type, MusicListControl musicListControl, MessageProducer messageProducer) {

    this.ui = ui;
    this.type = type;
    this.musicListControl = musicListControl;
    this.messageProducer = messageProducer;

    // Do some configuration
    setModel(new DefaultListModel<IAudioFile>());

    setDropMode(DropMode.INSERT);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    setLayoutOrientation(JList.VERTICAL);
    setAutoscrolls(true);
    setFixedCellWidth(320);

    dragSource = new DragSource();

    setCellRenderer(new AudioFileCellRenderer());
    dropTarget = new DropTarget(this, this);
    setDropTarget(dropTarget);

    dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);

    addMouseListener(new DragAndDropMouseListener());

  }

  public int getElementCount() {
    return getModel().getSize();
  }

  public AudioFileListType getType() {
    return type;
  }

  public MusicListControl getMusicListControl() {
    return musicListControl;
  }

  // DragGestureListener
  public void dragGestureRecognized(DragGestureEvent dge) {

    DraggableAudioFileList list = (DraggableAudioFileList) dge.getComponent();

    // find object at this x,y
    Point clickPoint = dge.getDragOrigin();
    int index = locationToIndex(clickPoint);
    if (index == -1) {
      return;
    }
    IAudioFile audioFile = getModel().getElementAt(index);

    // If there is some work in progress, we don't want to let the mess around
    if (audioFile.isLoading()) {
      return;
    }

    Transferable transferable = new AudioFileTransferable(audioFile);

    staticDragList = list;
    staticDragIndex = index;

    dragSource.startDrag(dge, new Cursor(Cursor.MOVE_CURSOR), transferable, this);

    addDebugMessage("Start drag of item in " + type + ": " + staticDragIndex);
  }

  // DragSourceListener events
  public void dragDropEnd(DragSourceDropEvent dsde) {
    repaint();
  }

  @Override
  public void dragExit(DropTargetEvent dte) {
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
  }

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {

    Boolean dropped = false;
    try {

      // Get the object to be transferred
      Transferable tr = dtde.getTransferable();
      DataFlavor[] flavors = tr.getTransferDataFlavors();

      // If flavors is empty get flavor list from DropTarget
      flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

      // Select best data flavor
      DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

      // Flavor will be null on Windows
      // In which case use the 1st available flavor
      flavor = (flavor == null) ? flavors[0] : flavor;

      // Flavors to check
      DataFlavor unix = new DataFlavor("text/uri-list;class=java.io.Reader");
      DataFlavor windows = DataFlavor.javaFileListFlavor;
      DataFlavor local = new DataFlavor(IAudioFile.class, "AudioFile");

      // Determine the drop point
      Point dropPoint = dtde.getLocation();
      int targetIndex = locationToIndex(dropPoint);

      addDebugMessage("Attempting to drop item in " + type + " at position: " + targetIndex);

      // Initialize output array
      ArrayList<File> files = new ArrayList<File>();

      // On Linux (and OS X) file DnD is a reader
      if (flavor.equals(unix)) {

        dtde.acceptDrop(DnDConstants.ACTION_MOVE);

        BufferedReader read = new BufferedReader(flavor.getReaderForText(tr));

        String rawLine;
        while ((rawLine = read.readLine()) != null) {

          // Remove 'file://' from file name
          String fileName = rawLine.substring(7).replace("%20", " ");
          fileName = URLDecoder.decode(fileName, "UTF-8");

          // Remove 'localhost' from OS X file names
          if (fileName.substring(0, 9).equals("localhost")) {
            fileName = fileName.substring(9);
          }

          File f = new File(fileName);
          if (f.isFile()) {
            files.add(f);
          }
        }

        read.close();
        dropped = true;
        dtde.dropComplete(true);
      }
      // On Windows file DnD is a file list
      else if (flavor.equals(windows)) {

        dtde.acceptDrop(DnDConstants.ACTION_MOVE);

        @SuppressWarnings("unchecked")
        List<File> list = (List<File>) tr.getTransferData(flavor);
        dtde.dropComplete(true);

        for (File f : list) {
          if (f.isFile()) {
            files.add(f);
          }
        }
        dropped = true;
      }
      else if (flavor.equals(local)) {

        // Since we're only supporting the internal dragging of one element, we pick the first item in the list
        @SuppressWarnings("unchecked")
        List<IAudioFile> list = (List<IAudioFile>) tr.getTransferData(flavor);

        if (getElementCount() <= targetIndex) {
          targetIndex = getElementCount() - 1;
        }

        if (targetIndex < 0) {
          targetIndex = 0;
        }

        Collections.reverse(list);

        if ((type.equals(AudioFileListType.BREAK) && getElementCount() < 1 && 0 < list.size() && list.size() <= 1) || type.equals(AudioFileListType.MUSIC)) {
          int sourceIndex = staticDragIndex;

          // If we're trying to fill in the last element, we increment by one
          if (targetIndex == getElementCount() - 1) {
            targetIndex++;
          }

          // If we're in the same list ...
          if (staticDragList.equals(this)) {

            boolean sourceBeforeTarget = (sourceIndex < targetIndex);
            musicListControl.moveTrack(sourceIndex, (sourceBeforeTarget ? targetIndex - 1 : targetIndex));

          }
          else {
            if (type.equals(AudioFileListType.BREAK)) {
              musicListControl.moveMusicToBreak(sourceIndex, 0);
            }
            else {
              musicListControl.moveBreakToMusic(sourceIndex, targetIndex);
            }
          }

          dropped = true;
        }
        else {
          dtde.rejectDrop();
        }
      }
      else {
        dtde.rejectDrop();
      }

      if (0 < files.size()) {
        if (targetIndex < 0) {
          targetIndex = 0;
        }

        for (File f : files) {
          if (type.equals(AudioFileListType.MUSIC)) {
            musicListControl.addMusicTrack(targetIndex, f);
          }
          else if (type.equals(AudioFileListType.BREAK)) {
            musicListControl.addBreakTrack(targetIndex, f);
          }

          targetIndex++;
        }
      }
    }
    catch (UnsupportedFlavorException e) {
      addDebugMessage("Flavor-related problems with drag and drop in " + type + ": " + e.getMessage());
    }
    catch (Exception e) {
      addDebugMessage("General problems with drag and drop in " + type + ": " + e.getMessage());
    }

    if (dropped) {
      addDebugMessage("Drop succeeded.");
    }
    else {
      addDebugMessage("Drop failed.");
    }

    dtde.dropComplete(dropped);
    ui.refresh();
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  @Override
  public void dragEnter(DragSourceDragEvent dsde) {
  }

  @Override
  public void dragOver(DragSourceDragEvent dsde) {
  }

  @Override
  public void dropActionChanged(DragSourceDragEvent dsde) {
  }

  @Override
  public void dragExit(DragSourceEvent dse) {
  }

  private void addDebugMessage(String message) {
    messageProducer.send(new DebugMessage(this, message));
  }

  private class DragAndDropMouseListener implements MouseListener {

    @Override
    public void mouseReleased(MouseEvent e) {
      checkIfEntryIsSelected(e);
      showContextMenu(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      showContextMenu(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      checkIfEntryIsSelected(e);
      showContextMenu(e);
    }

    public void checkIfEntryIsSelected(MouseEvent e) {

      if (e.isPopupTrigger()) {
        // Check if the entry which is pointed to is in the set of selected
        // items. If not, select it.

        DraggableAudioFileList list = (DraggableAudioFileList) e.getComponent();

        // Determine current click index
        int chosenIndex = list.locationToIndex(e.getPoint());

        // Check if index is in the list of the selected items
        if (!newArrayList(list.getSelectedIndices()).contains(chosenIndex)) {

          // If it is not in the list, we clear the selection and choose it as new selection
          list.clearSelection();
          list.setSelectionInterval(chosenIndex, chosenIndex);
        }
      }
    }

    public void showContextMenu(MouseEvent e) {
      if (e.isPopupTrigger()) {
        AudioFileListContextMenu cm = new AudioFileListContextMenu((DraggableAudioFileList) e.getComponent(), ui);
        cm.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

}
