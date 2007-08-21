/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.language.LanguageCf;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class ImageDataFieldEditor 
extends AbstractDataFieldEditor<ImageDataField> 
{
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
	private Group group;
	private Label imageLabel;
	private Label sizeLabel;
	
	private static final int maxThumbnailWidth = 200;
	private static final int maxThumbnailHeight = 200;
	
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
		// Border looks ugly and is unecessary as group already has a border
//		group = new Group(parent, SWT.BORDER);
		group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(3, false));
//		XComposite.configureLayout(LayoutMode.TIGHT_WRAPPER, (GridLayout)group.getLayout());
//		((GridLayout)group.getLayout()).verticalSpacing = 0;
//		((GridLayout)group.getLayout()).m= 0;
		
		XComposite.setLayoutDataMode(LayoutDataMode.GRID_DATA, group);
				
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.LEFT;
		
		filenameTextbox = new Text(group, XComposite.getBorderStyle(parent));
		filenameTextbox.setEditable(false);
		filenameTextbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		openFileChooserButton = new Button(group, SWT.NONE);		
		sizeLabel = new Label(group, SWT.NONE);
		
		imageLabel = new Label(group, SWT.NONE);
		imageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (imageLabel.getImage() != null)
					imageLabel.getImage().dispose();
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalAlignment = SWT.CENTER;
		gd.verticalIndent = 10;
		imageLabel.setLayoutData(gd);
		
		
		gd = new GridData();
		gd.widthHint = 40;
		openFileChooserButton.setText(Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ImageDataFieldEditor.openFileChooserButton.text")); //$NON-NLS-1$
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
						MessageBox messageBoxImageExceedsMaxSizeKB = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.OK | SWT.ICON_ERROR);
						messageBoxImageExceedsMaxSizeKB.setText(Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ImageDataFieldEditor.messageBoxImageExceedsMaxSizeKB.text")); //$NON-NLS-1$
						messageBoxImageExceedsMaxSizeKB.setMessage(
								String.format(
										Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ImageDataFieldEditor.messageBoxImageExceedsMaxSizeKB.message"), //$NON-NLS-1$
										new Object[] { new Long(imageStructField.getMaxSizeKB()), new Long((long)(file.length() / 1024)) })
						);
						messageBoxImageExceedsMaxSizeKB.open();
						return;
					}
					
					try {
						ImageData data = new ImageData(path);
						setChanged(true);
						filenameTextbox.setText(path);					
						fileDialog.setFilterPath(file.getParent());
						
						displayImage(data);
					} catch(SWTException swtex) {
						MessageBox messageBoxInvalidImageFile = new MessageBox(RCPUtil.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
						messageBoxInvalidImageFile.setText(Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ImageDataFieldEditor.messageBoxInvalidImageFile.text")); //$NON-NLS-1$
						messageBoxInvalidImageFile.setMessage(Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ImageDataFieldEditor.messageBoxInvalidImageFile.message")); //$NON-NLS-1$
						messageBoxInvalidImageFile.open();
					}										
				}				
			}
		});
		
		return group;
	}
	
	private void displayImage(ImageData id) {
		if (imageLabel.getImage() != null)
			imageLabel.getImage().dispose();
		
		int width = id.width;
		int height = id.height;
		double factor = 1.0;
		if (width > maxThumbnailWidth || height > maxThumbnailHeight)
			factor *= height > width ? 1.0*maxThumbnailHeight/height : 1.0*maxThumbnailHeight/width;
			
		id = id.scaledTo((int) (factor*width), (int) (factor*height));
		Image image = new Image(Display.getDefault(), id);
		imageLabel.setImage(image);
		imageLabel.getParent().layout(true, true);
		imageLabel.getParent().getParent().layout(true, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh()
	 */
	@Override
	public void doRefresh() {
		ImageStructField imageStructField = (ImageStructField) getStructField();
		
		group.setText(imageStructField.getName().getText(language.getLanguageID()));
		List<String> extList = imageStructField.getImageFormats();
		
		String[] extensions = new String[extList.size()];		
		int i = 0;
		for (String ext : extList)		
			extensions[i++] = "*." + ext; //$NON-NLS-1$
		
		Arrays.sort(extensions);		
		fileDialog.setFilterExtensions(extensions);
		
		if (!getDataField().isEmpty()) {
			filenameTextbox.setText(getDataField().getFileName());
			ImageData id = null;
			InputStream in = new InflaterInputStream(new ByteArrayInputStream(getDataField().getContent()));
			try {
				id = new ImageData(in);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						LOGGER.error(e);
					}
			}
			displayImage(id);	
		}

		sizeLabel.setText(
				String.format(Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ImageDataFieldEditor.sizeMaxKBLabel"), //$NON-NLS-1$
						new Object[] { new Long(imageStructField.getMaxSizeKB()) }));
		sizeLabel.pack();
		sizeLabel.getParent().layout(true, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return group;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#updateProperty()
	 */
	public void updatePropertySet() {
		if (!isChanged())
			return;
		
//		Display.getDefault().syncExec(new Runnable(){
//			public void run() {
				ImageDataField dataField = getDataField();
				String path = filenameTextbox.getText();		
				if (path == null || "".equals(path)) //$NON-NLS-1$
					throw new RuntimeException("Path must not be empty or null!"); //$NON-NLS-1$
				
				//FIXME: get content type somehow!
				String contentType = "application/unknown"; //$NON-NLS-1$
				String lowerPath = path.toLowerCase();
				if(lowerPath.endsWith(".png")) //$NON-NLS-1$
					contentType = "image/png"; //$NON-NLS-1$
				else if(lowerPath.endsWith(".jpg")) //$NON-NLS-1$
					contentType = "image/jpeg"; //$NON-NLS-1$
				else if(lowerPath.endsWith(".gif")) //$NON-NLS-1$
					contentType = "image/gif"; //$NON-NLS-1$

				// store the image as in the data field.
				File imageFile = new File(path);
				
				try {
					dataField.loadFile(imageFile, contentType);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}				
//			}
//		});		
	}
	
	public LanguageCf getLanguage() {
		return language;
	}
}


