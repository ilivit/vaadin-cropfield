package com.davengo.web.vaadin.crop.widgetset.client.ui;

import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropImage.SelectionFinishEvent;
import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropImage.SelectionFinishHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

/**
 * Client side widget of the {@link com.davengo.web.vaadin.crop.CropField
 * CropField} component. All in all, just a wrapper for the CropImage GWT
 * widget.
 * 
 * @author Eric Seckler
 */
public class VCropField extends SimplePanel implements Paintable {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-cropfield";

	public static final String ATTR_HEIGHT = "height";
	public static final String ATTR_WIDTH = "width";
	public static final String ATTR_READONLY = "readonly";
	public static final String ATTR_DISABLED = "disabled";
	public static final String ATTR_IMMEDIATE = "immediate";
	public static final String ATTR_IMAGE_RESOURCE = "imageSrc";
	public static final String ATTR_TRUE_IMAGE_WIDTH = "trueImageWidth";
	public static final String ATTR_TRUE_IMAGE_HEIGHT = "trueImageHeight";
	public static final String ATTR_MIN_SELECTION_WIDTH = "minSelectionWidth";
	public static final String ATTR_MIN_SELECTION_HEIGHT = "minSelectionHeight";
	public static final String ATTR_MAX_SELECTION_WIDTH = "maxSelectionWidth";
	public static final String ATTR_MAX_SELECTION_HEIGHT = "maxSelectionHeight";
	public static final String ATTR_SELECTION_ASPECT_RATIO = "selectionAspectRatio";
	public static final String ATTR_ANIMATE_TO_SELECTION = "animateToSelection";
	public static final String ATTR_ANIMATE_TO_DURATION = "animateToDuration";

	public static final String VAR_SELECTION = "value";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private VCropImage cropImage;
	private VCropSelection selection;

	private boolean immediate;

	public VCropField() {
		super();

		setWidget(cropImage = new VCropImage());

		cropImage.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				Util.notifyParentOfSizeChange(VCropField.this, false);
			}
		});

		cropImage.addSelectionFinishHandler(new SelectionFinishHandler() {
			@Override
			public void onSelectionFinished(SelectionFinishEvent event) {
				valueChange();
			}
		});

		setStyleName(CLASSNAME);
	}

	/**
	 * Called whenever an update is received from the server
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// This call should be made first.
		// It handles sizes, captions, tooltips, etc. automatically.
		if (client.updateComponent(this, uidl, true)) {
			// If client.updateComponent returns true there has been no changes
			// and we
			// do not need to update anything.
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();

		// update the size of the cropImage element as well
		if (uidl.hasAttribute(ATTR_WIDTH)) {
			setWidth(uidl.getStringAttribute("width"));
		}
		if (uidl.hasAttribute(ATTR_HEIGHT)) {
			setHeight(uidl.getStringAttribute("height"));
		}

		// update AbstractField attributes from uidl
		if (uidl.getBooleanAttribute(ATTR_READONLY)
				|| uidl.getBooleanAttribute(ATTR_DISABLED)) {
			cropImage.setReadOnly(true);
		} else {
			cropImage.setReadOnly(false);
		}

		immediate = uidl.getBooleanAttribute(ATTR_IMMEDIATE);

		// save away old selection before applying attribute changes to
		// VCropImage
		VCropSelection oldSelection = this.selection;

		// update cropImage attributes from uidl
		if (uidl.hasAttribute(ATTR_TRUE_IMAGE_WIDTH)) {
			cropImage.setTrueImageWidth(uidl
					.getIntAttribute(ATTR_TRUE_IMAGE_WIDTH));
		} else {
			cropImage.setTrueImageWidth(0);
		}

		if (uidl.hasAttribute(ATTR_TRUE_IMAGE_HEIGHT)) {
			cropImage.setTrueImageHeight(uidl
					.getIntAttribute(ATTR_TRUE_IMAGE_HEIGHT));
		} else {
			cropImage.setTrueImageHeight(0);
		}

		if (uidl.hasAttribute(ATTR_MIN_SELECTION_WIDTH)) {
			cropImage.setMinSelectionWidth(uidl
					.getIntAttribute(ATTR_MIN_SELECTION_WIDTH));
		} else {
			cropImage.setMinSelectionWidth(0);
		}

		if (uidl.hasAttribute(ATTR_MIN_SELECTION_HEIGHT)) {
			cropImage.setMinSelectionHeight(uidl
					.getIntAttribute(ATTR_MIN_SELECTION_HEIGHT));
		} else {
			cropImage.setMinSelectionHeight(0);
		}

		if (uidl.hasAttribute(ATTR_MAX_SELECTION_WIDTH)) {
			cropImage.setMaxSelectionWidth(uidl
					.getIntAttribute(ATTR_MAX_SELECTION_WIDTH));
		} else {
			cropImage.setMaxSelectionWidth(0);
		}

		if (uidl.hasAttribute(ATTR_MAX_SELECTION_HEIGHT)) {
			cropImage.setMaxSelectionHeight(uidl
					.getIntAttribute(ATTR_MAX_SELECTION_HEIGHT));
		} else {
			cropImage.setMaxSelectionHeight(0);
		}

		if (uidl.hasAttribute(ATTR_SELECTION_ASPECT_RATIO)) {
			cropImage.setSelectionAspectRatio(uidl
					.getFloatAttribute(ATTR_SELECTION_ASPECT_RATIO));
		} else {
			cropImage.setSelectionAspectRatio(0);
		}

		// update the value (selection) from uidl
		VCropSelection selection = new VCropSelection(
				uidl.getStringArrayVariable(VAR_SELECTION));
		// only update the value if it is different to the value before
		// attribute changes (so that selection changes because of attribute
		// changes won't be reversed)
		if (!selection.equals(oldSelection)) {
			this.selection = selection;
			cropImage.setSelection(selection, false);
		}

		// start an animation according to uidl
		if (uidl.hasAttribute(ATTR_ANIMATE_TO_SELECTION)) {
			VCropSelection animateToSelection = new VCropSelection(
					uidl.getStringArrayAttribute(ATTR_ANIMATE_TO_SELECTION));
			if (uidl.hasAttribute(ATTR_ANIMATE_TO_DURATION)) {
				cropImage.animateTo(animateToSelection,
						uidl.getIntAttribute(ATTR_ANIMATE_TO_DURATION));
			} else {
				cropImage.animateTo(animateToSelection);
			}
		}

		// update the image url from uidl
		cropImage.setImageUrl(getSrc(uidl, client));
	}

	/**
	 * Called when the field value might have changed
	 */
	public void valueChange() {
		if (client != null && paintableId != null) {
			VCropSelection newSelection = cropImage.getSelection();
			if (!newSelection.equals(selection)) {
				selection = newSelection;
				client.updateVariable(paintableId, VAR_SELECTION,
						selection.toStringArray(), immediate);
			}
		}
	}

	@Override
	public void setHeight(String height) {
		super.setHeight(height);
		// same height for the wrapped cropImage widget
		cropImage.setHeight(height);
	}

	@Override
	public void setWidth(String width) {
		super.setWidth(width);
		// same width for the wrapped cropImage widget
		cropImage.setWidth(width);
	}

	/**
	 * Helper to return translated src-attribute from UIDL
	 * 
	 * @param uidl
	 *            the uidl of the component
	 * @param client
	 *            the client connection
	 * @return the translated src-attribute from UIDL
	 */
	private String getSrc(UIDL uidl, ApplicationConnection client) {
		String url = client.translateVaadinUri(uidl
				.getStringAttribute(ATTR_IMAGE_RESOURCE));
		if (url == null) {
			return "";
		}
		return url;
	}
}
