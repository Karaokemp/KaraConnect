package uk.co.caprica.vlcjplayer.view.playlist;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import lombok.NonNull;
import lombok.val;
import uk.co.caprica.vlcjplayer.Data;
import uk.co.caprica.vlcjplayer.view.playlist.PlaylistFrame.GapTable;

public class PlaylistDND extends TransferHandler {

	static final DataFlavor[] flavors = {new DataFlavor(Integer.class, "Integer"), DataFlavor.javaFileListFlavor};
	
	public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_MOVE;
    }

    public Transferable createTransferable(JComponent comp)
    {
        JTable table=(JTable)comp;
//        val model = (DefaultTableModel) table.getModel();
        int row=table.getSelectedRow();
        val transferable = new TransferableRow(row);
//        model.removeRow(row);
        return transferable;
    }
    public boolean canImport(TransferHandler.TransferSupport info){
        return (info.isDataFlavorSupported(flavors[0]) || info.isDataFlavorSupported(flavors[1]));
    }
    
    @SuppressWarnings("unchecked")
    public boolean importData(TransferSupport support) {

        if (!support.isDrop()) {
            return false;
        }

        if (!canImport(support)) {
            return false;
        }

        JTable table=(JTable)support.getComponent();
        DefaultTableModel tableModel=(DefaultTableModel)table.getModel();
         
        JTable.DropLocation dl = (JTable.DropLocation)support.getDropLocation();

        int target = Math.max(0, dl.getRow());
        
        try {
			try {
			    int origin = (int)support.getTransferable().getTransferData(flavors[0]);
			    tableModel.moveRow(origin, origin, target);
			} catch (UnsupportedFlavorException e) {
			    List<File> list = (List<File>)support.getTransferable().getTransferData(flavors[1]);
			    if (support.getComponent() instanceof GapTable)
			    	for (File file : list)
			    		PlaylistFrame.addGap(file, target);
			    else
			    	return false;
			}
			
			PlaylistFrame.savePlaylist(support.getComponent() instanceof GapTable ? Data.gaplistFileName : Data.playlistFileName);
		} catch (Exception e) {
			return false;
		}
        return true;
    }
    
    public static class TransferableRow implements Transferable{

    	int rowData;
    	
    	
    	public TransferableRow(int row) {
			rowData = row;
		}
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavors[0].equals(flavor) || flavors[1].equals(flavor);
		}

		@Override
		public Object getTransferData(@NonNull DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return rowData;
		}
    	
    }
}
