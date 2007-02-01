/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.language.LanguageCf;

/**
 * @author Tobias Langner <!-- tobias[DOT]langner[AT]nightlabs[DOT]de -->
 */
public class ImageDataFieldEditor extends AbstractDataFieldEditor<ImageDataField> {
	
	public static class Factory extends AbstractDataFieldEditorFactory<ImageDataField> {

		@Override
		public String getEditorType() {
			return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
		}

		@Override
		public Class<? extends DataFieldEditor<ImageDataField>> getDataFieldEditorClass() {
			return ImageDataFieldEditor.class;
		}

		@Override
		public Class<ImageDataField> getPropDataFieldType() {
			return ImageDataField.class;
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(ImageDataFieldEditor.class);
	
	private LanguageCf language;
	
	private Text filenameTextbox;
	private Button openFileChooserButton;
	private FileDialog fileDialog;
	private XComposite comp;
	private Label imageLabel;
	private Label titleLabel;
	private Label sizeLabel;
	
	public ImageDataFieldEditor() {
		super();
		language = new LanguageCf(Locale.getDefault().getLanguage());
		
		fileDialog = new FileDialog(RCPUtil.getActiveWorkbenchShell());
	}
	
	@Override
	protected void setDataField(ImageDataField dataField) {
		super.setDataField(dataField);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(Composite parent) {
		comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 3);
		comp.getGridLayout().verticalSpacing = 0;
		
		GridData gd = new GridData();
		
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.LEFT;
		titleLabel = new Label(comp, SWT.NONE);
		titleLabel.setLayoutData(gd);
		
		filenameTextbox = new Text(comp, SWT.BORDER);
		filenameTextbox.setEditable(false);
		filenameTextbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		openFileChooserButton = new Button(comp, SWT.PUSH);		
		sizeLabel = new Label(comp, SWT.NONE);
		
		imageLabel = new Label(comp, SWT.NONE);
		imageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageLabel.getImage() != null)
					imageLabel.getImage().dispose();
			}
		});
		
		gd = new GridData();
		gd.heightHint = 200;
		gd.horizontalAlignment = SWT.CENTER;
		imageLabel.setLayoutData(gd);
		
		gd = new GridData();
		gd.widthHint = 40;
		openFileChooserButton.setText("...");
		openFileChooserButton.setLayoutData(gd);
		openFileChooserButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				File file; 
				
				String path  = fileDialog.open();				
				if (path != null) {
					file = new File(path);
					
					// check if the image fulfills the size requirements
					ImageStructField imageStructField = (ImageStructField) getStructField();
					if (!imageStructField.validateSize(file.length()/1024)) {
						MessageBox mb = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.OK | SWT.ICON_ERROR);
						mb.setText("Error");
						String msg = "The maximum image size is " + imageStructField.getMaxSizeKB() + "KB.\n";
						msg += "\nThe selected image's size is "+ file.length()/1024 + "KB. Please choose a smaller one.";
						mb.setMessage(msg);
						mb.open();
						return;
					}
					ImageData data = new ImageData(path);
					setChanged(true);
					filenameTextbox.setText(path);
					fileDialog.setFilterPath(path);
					
					displayImage(data);					
					comp.layout();
				}
			}
		});
		
		return comp;
	}
	
	private void displayImage(ImageData id) {
		if (imageLabel.getImage() != null)
			imageLabel.getImage().dispose();
		
		int width = id.width;
		int height = id.height;
		double factor = height > width ? 200.0/height : 200.0/width;
		id = id.scaledTo((int) (factor*width), (int) (factor*height));
		Image image = new Image(Display.getDefault(), id);
		imageLabel.setImage(image);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		ImageStructField imageStructField = (ImageStructField) getStructField();
		
		titleLabel.setText(imageStructField.getName().getText(language.getLanguageID()));
		List<String> extList = imageStructField.getImageFormats();
		
		String[] extensions = new String[extList.size()];		
		int i = 0;
		for (String ext : extList)		
			extensions[i++] = "*." + ext;
		
		Arrays.sort(extensions);		
		fileDialog.setFilterExtensions(extensions);
		if (getDataField().getImageData() != null) {
			ImageData id = new ImageData(new ByteArrayInputStream(getDataField().getImageData()));
			displayImage(id);			
		}
		
		filenameTextbox.setText(getDataField().getFileName());
		
		sizeLabel.setText("(max " + imageStructField.getMaxSizeKB() + " KB)");
		sizeLabel.pack();
		sizeLabel.getParent().layout(true, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return comp;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#updateProp()
	 */
	public void updateProperty() {
		if (!isChanged())
			return;
		
		ImageDataField dataField = getDataField();
		String path = filenameTextbox.getText();		
		if (path == null || "".equals(path))
			throw new RuntimeException("Path must not be empty or null!");
		
		// store the image as in the data field.
		File imageFile = new File(path);
		
		try {
			dataField.loadFile(imageFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public LanguageCf getLanguage() {
		return language;
	}
}


