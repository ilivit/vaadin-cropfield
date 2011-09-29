package com.davengo.web.vaadin.crop.widgetset.client.ui;

import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropSelection.Edge;
import com.davengo.web.vaadin.crop.widgetset.client.ui.VCropSelection.Length;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * GWT widget that shows an image and overlays it with a cropping (selection)
 * functionality. The current selection can be get and set via
 * get/setSelection(). You can also listen for selection change or finish
 * events.
 * 
 * @author Eric Seckler
 */
public class VCropImage extends AbsolutePanel implements HasResizeHandlers {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-cropimage";
	protected static final int DEFAULT_ANIMATION_DURATION = 200;

	protected VCropSelection selection = new VCropSelection(0, 0, 0, 0);

	protected DragMode dragMode;
	protected int dragStartX;
	protected int dragStartY;
	protected int dragStartClientX;
	protected int dragStartClientY;
	protected KeyHandlerFocusHandler keyHandlerFocusHandler;
	protected DragKeyHandler keyHandler;
	protected TransformTrackerHandler transformTrackerHandler;
	protected DragMouseHandler dragMouseHandler;
	protected HandlerRegistration dragMouseMoveHandlerRegistration;
	protected HandlerRegistration dragMouseUpHandlerRegistration;
	protected MoveTrackerHandler moveTrackerHandler;
	protected SelectionBoxAnimation selectionBoxAnimation;

	protected boolean imgLoaded = false;
	protected String imgUrl;
	protected VCropSelection selectionToBeSetAfterLoad;
	protected boolean selectionToBeSetAfterLoadUpdateVisibility;
	protected boolean selectionToBeSetAfterLoadEnforeExtents;
	protected boolean selectionToBeSetAfterLoadFireEvent;
	protected boolean selectionToBeSetAfterLoadCancelAnimation;

	protected int trueImageWidth;
	protected int trueImageHeight;
	protected int minSelectionWidth;
	protected int minSelectionHeight;
	protected int maxSelectionWidth;
	protected int maxSelectionHeight;
	protected float selectionAspectRatio;
	protected boolean readOnly;

	protected FocusPanel keyHandlerPanel;
	protected Image img;
	protected FocusPanel tracker;
	protected AbsolutePanel selectionBox;
	protected AbsolutePanel selectionBoxContents;
	protected Image selectionBoxImage;
	protected SimplePanel selectionBoxLineNorth;
	protected SimplePanel selectionBoxLineSouth;
	protected SimplePanel selectionBoxLineWest;
	protected SimplePanel selectionBoxLineEast;
	protected FocusPanel selectionBoxTracker;
	protected AbsolutePanel selectionBoxResizers;
	protected FocusPanel selectionBoxResizeBorderNorth;
	protected FocusPanel selectionBoxResizeBorderSouth;
	protected FocusPanel selectionBoxResizeBorderWest;
	protected FocusPanel selectionBoxResizeBorderEast;
	protected FocusPanel selectionBoxResizeHandleNorth;
	protected FocusPanel selectionBoxResizeHandleSouth;
	protected FocusPanel selectionBoxResizeHandleWest;
	protected FocusPanel selectionBoxResizeHandleEast;
	protected FocusPanel selectionBoxResizeHandleSouthWest;
	protected FocusPanel selectionBoxResizeHandleNorthWest;
	protected FocusPanel selectionBoxResizeHandleNorthEast;
	protected FocusPanel selectionBoxResizeHandleSouthEast;

	/**
	 * Creates a new VCropImage. You should set the image URL afterwards.
	 */
	public VCropImage() {
		super();

		setStyleName(CLASSNAME);

		createContents();

		setVisibilities(false);
	}

	/**
	 * Update the widget size from the image and fire a resize event.
	 */
	protected void updateSizeFromImage() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				updateChildrenSizesFromImageSize();
				ResizeEvent.fire(VCropImage.this, getOffsetWidth(),
						getOffsetHeight());
			}
		});
	}

	/**
	 * Update the sizes of all elements of the widget according to the new image
	 * size.
	 */
	private void updateChildrenSizesFromImageSize() {
		int imgHeight = img.getOffsetHeight();
		int imgWidth = img.getOffsetWidth();
		getElement().getStyle().setWidth(imgWidth, Unit.PX);
		getElement().getStyle().setHeight(imgHeight, Unit.PX);
		selectionBoxImage.setPixelSize(imgWidth, imgHeight);
		updateSelectionBoxLocation();
	}

	@Override
	public void setHeight(String height) {
		int oldWidth = img.getOffsetWidth();

		super.setHeight(height);
		img.setHeight(height);

		updateChildrenSizesFromImageSize();

		int newWidth = img.getOffsetWidth();

		// if the width has changed by applying the new height, we need to fire
		// a resize event
		if (oldWidth != newWidth) {
			ResizeEvent.fire(VCropImage.this, getOffsetWidth(),
					getOffsetHeight());
		}
	}

	@Override
	public void setWidth(String width) {
		int oldHeight = img.getOffsetHeight();

		super.setWidth(width);
		img.setWidth(width);

		updateChildrenSizesFromImageSize();

		int newHeight = img.getOffsetHeight();

		// if the height has changed by applying the new width, we need to fire
		// a resize event
		if (oldHeight != newHeight) {
			ResizeEvent.fire(VCropImage.this, getOffsetWidth(),
					getOffsetHeight());
		}
	}

	/**
	 * set the URL of the image to be cropped
	 * 
	 * @param url
	 *            the URL of the image (a <i>null</i> value is NOT allowed)
	 */
	public void setImageUrl(String url) {
		if (!url.equals(this.imgUrl)) {
			this.imgUrl = url;
			imgLoaded = false;
			img.setUrl(url);
			selectionBoxImage.setUrl(url);
			// resizing will be handled in onLoad of img
		}
	}

	/**
	 * @return the URL of the image that is currently shown
	 */
	public String getImageUrl() {
		return img.getUrl();
	}

	/**
	 * @return the current selection, never <i>null</i> (in such cases, it is an
	 *         empty selection)
	 */
	public VCropSelection getSelection() {
		return selection;
	}

	/**
	 * Set the selection to the given value, updating the visibility of the
	 * selection frame, canceling a possibly ongoing animation and firing a
	 * selection change event.
	 * 
	 * @param selection
	 *            the new selection (a <i>null</i> value is NOT allowed). If the
	 *            selection is non-empty, the extents will be forced to comply
	 *            to the limitations set on this {@link VCropImage}.
	 */
	public void setSelection(VCropSelection selection) {
		setSelection(selection, true);
	}

	/**
	 * Set the selection to the given value, updating the visibility of the
	 * selection frame, canceling a possibly ongoing animation and firing a
	 * selection change event.
	 * 
	 * @param selection
	 *            the new selection (a <i>null</i> value is NOT allowed)
	 * @param enforceExtents
	 *            if <i>true</i>, change the selection to match the extent
	 *            limitations set onto this {@link VCropImage}
	 */
	public void setSelection(VCropSelection selection, boolean enforceExtents) {
		setSelection(selection, true, enforceExtents, true);
	}

	/**
	 * Set the selection to the given value, canceling a possibly ongoing
	 * animation.
	 * 
	 * @param selection
	 *            the new selection (a <i>null</i> value is NOT allowed)
	 * @param updateVisibility
	 *            if <i>true</i>, change the visibility of the selection frame
	 *            (to visible in case of a non-empty selection, to hidden in
	 *            case of an empty selection)
	 * @param enforceExtents
	 *            if <i>true</i>, change the selection to match the extent
	 *            limitations set onto this {@link VCropImage}
	 * @param fireEvent
	 *            if <i>true</i>, a {@link SelectionChangeEvent} will be fired
	 */
	protected void setSelection(VCropSelection selection,
			boolean updateVisibility, boolean enforceExtents, boolean fireEvent) {
		setSelection(selection, updateVisibility, enforceExtents, fireEvent,
				true);
	}

	/**
	 * Set the selection to the given value.
	 * 
	 * @param selection
	 *            the new selection (a <i>null</i> value is NOT allowed)
	 * @param updateVisibility
	 *            if <i>true</i>, change the visibility of the selection frame
	 *            (to visible in case of a non-empty selection, to hidden in
	 *            case of an empty selection)
	 * @param enforceExtents
	 *            if <i>true</i>, change the selection to match the extent
	 *            limitations set onto this {@link VCropImage}
	 * @param fireEvent
	 *            if <i>true</i>, a {@link SelectionChangeEvent} will be fired
	 * @param cancelAnimation
	 *            if <i>true</i>, a possibly ongoing animation will be cancelled
	 */
	protected void setSelection(VCropSelection selection,
			boolean updateVisibility, boolean enforceExtents,
			boolean fireEvent, boolean cancelAnimation) {
		if (!imgLoaded) {
			selectionToBeSetAfterLoad = selection;
			selectionToBeSetAfterLoadUpdateVisibility = updateVisibility;
			selectionToBeSetAfterLoadEnforeExtents = enforceExtents;
			selectionToBeSetAfterLoadFireEvent = fireEvent;
			selectionToBeSetAfterLoadCancelAnimation = cancelAnimation;
		} else {
			selectionToBeSetAfterLoad = null;

			if (enforceExtents) {
				selection.enforceExtents(getMinSelectionWidth(),
						getMinSelectionHeight(), getMaxSelectionWidth(),
						getMaxSelectionHeight(), getTrueImageWidth(),
						getTrueImageHeight(), getSelectionAspectRatio(),
						Length.ANY, Edge.TOP_LEFT);
			}

			if (cancelAnimation) {
				selectionBoxAnimation.cancel();
			}

			this.selection = selection;
			updateSelectionBoxLocation();

			if (updateVisibility) {
				setVisibilities(!selection.isEmpty());
			}

			if (fireEvent) {
				fireEvent(new SelectionChangeEvent(getSelection()));
			}
		}
	}

	/**
	 * Set the selection to the true-size coordinates corresponding the
	 * coordinates of the scaled selection given as a parameter.<br>
	 * For parameter information, see {@link #setSelection(VCropSelection)}.
	 * 
	 * @see #setSelection(VCropSelection)
	 */
	public void setScaledSelection(VCropSelection selection) {
		setScaledSelection(selection, true);
	}

	/**
	 * Set the selection to the true-size coordinates corresponding the
	 * coordinates of the scaled selection given as a parameter.<br>
	 * For parameter information, see
	 * {@link #setSelection(VCropSelection, boolean)}.
	 * 
	 * @see #setSelection(VCropSelection, boolean)
	 */
	public void setScaledSelection(VCropSelection selection,
			boolean enforceExtents) {
		setScaledSelection(selection, true, enforceExtents, true);
	}

	/**
	 * Set the selection to the true-size coordinates corresponding the
	 * coordinates of the scaled selection given as a parameter.<br>
	 * For parameter information, see
	 * {@link #setSelection(VCropSelection, boolean, boolean, boolean)}.
	 * 
	 * @see #setSelection(VCropSelection, boolean, boolean, boolean)
	 */
	protected void setScaledSelection(VCropSelection selection,
			boolean updateVisibility, boolean enforceExtents, boolean fireEvent) {
		setScaledSelection(selection, updateVisibility, enforceExtents,
				fireEvent, true);
	}

	/**
	 * Set the selection to the true-size coordinates corresponding the
	 * coordinates of the scaled selection given as a parameter.<br>
	 * For parameter information, see
	 * {@link #setSelection(VCropSelection, boolean, boolean, boolean, boolean)}
	 * .
	 * 
	 * @see #setSelection(VCropSelection, boolean, boolean, boolean, boolean)
	 */
	protected void setScaledSelection(VCropSelection selection,
			boolean updateVisibility, boolean enforceExtents,
			boolean fireEvent, boolean cancelAnimation) {
		selection.setXTopLeft(getTrueWidth(selection.getXTopLeft()));
		selection.setYTopLeft(getTrueHeight(selection.getYTopLeft()));
		selection.setXBottomRight(getTrueWidth(selection.getXBottomRight()));
		selection.setYBottomRight(getTrueHeight(selection.getYBottomRight()));
		setSelection(selection, updateVisibility, enforceExtents, fireEvent,
				cancelAnimation);
	}

	/**
	 * Set the aspect ratio (width / height) the selection has to conform to.
	 * Can be used in combination with min/max width/height.
	 * 
	 * @param selectionAspectRatio
	 *            the aspect ratio (width / height) of any selection
	 */
	public void setSelectionAspectRatio(float aspectRatio) {
		this.selectionAspectRatio = aspectRatio;
		reenforceSelectionExtents();
	}

	/**
	 * @return the aspect ratio (width / height) of any selection
	 */
	public float getSelectionAspectRatio() {
		return selectionAspectRatio;
	}

	/**
	 * Set the minimum width of a non-empty selection.
	 * 
	 * @param minWidth
	 *            the minimum width of a non-empty selection
	 */
	public void setMinSelectionWidth(int minWidth) {
		this.minSelectionWidth = minWidth;
		reenforceSelectionExtents();
	}

	/**
	 * @return the minimum height of a non-empty selection
	 */
	public int getMinSelectionWidth() {
		return minSelectionWidth;
	}

	/**
	 * Set the maximum width of a selection.
	 * 
	 * @param maxWidth
	 *            the maximum width of a selection
	 */
	public void setMaxSelectionWidth(int maxWidth) {
		this.maxSelectionWidth = maxWidth;
		reenforceSelectionExtents();
	}

	/**
	 * @return the maximum width of a selection
	 */
	public int getMaxSelectionWidth() {
		return maxSelectionWidth;
	}

	/**
	 * <p>
	 * If you choose to present the image in a scaled mode (i.e. by setting the
	 * extent of this widget), you can set the true width of the image
	 * (non-scaled width), so that the selection the user performs on the scaled
	 * image is rescaled to fit the true image width.
	 * </p>
	 * <p>
	 * All other selection coordinates/extents will always correlate to the true
	 * image coordinate system.
	 * </p>
	 * <p>
	 * If no true image width is set (i.e. set to 0), the width of the scaled
	 * image is used to form the true image height.
	 * </p>
	 * 
	 * @param trueImageWidth
	 *            the width that should be used for calculation of the selection
	 */
	public void setTrueImageWidth(int trueWidth) {
		this.trueImageWidth = trueWidth;
	}

	/**
	 * @return the width that should be used for calculation of the selection
	 */
	public int getTrueImageWidth() {
		return (trueImageWidth > 0) ? trueImageWidth : img.getWidth();
	}

	/**
	 * Set the minimum height of a non-empty selection.
	 * 
	 * @param minHeight
	 *            the minimum height of a non-empty selection
	 */
	public void setMinSelectionHeight(int minHeight) {
		this.minSelectionHeight = minHeight;
		reenforceSelectionExtents();
	}

	/**
	 * @return the minimum height of a non-empty selection
	 */
	public int getMinSelectionHeight() {
		return minSelectionHeight;
	}

	/**
	 * Set the maximum height of a selection.
	 * 
	 * @param maxHeight
	 *            the maximum height of a selection
	 */
	public void setMaxSelectionHeight(int maxHeight) {
		this.maxSelectionHeight = maxHeight;
		reenforceSelectionExtents();
	}

	/**
	 * @return the maximum height of a selection
	 */
	public int getMaxSelectionHeight() {
		return maxSelectionHeight;
	}

	/**
	 * <p>
	 * If you choose to present the image in a scaled mode (i.e. by setting the
	 * extent of this widget), you can set the true height of the image
	 * (non-scaled height), so that the selection the user performs on the
	 * scaled image is rescaled to fit the true image height.
	 * </p>
	 * <p>
	 * All other selection coordinates/extents will always correlate to the true
	 * image coordinate system.
	 * </p>
	 * <p>
	 * If no true image height is set (i.e. set to 0), the height of the scaled
	 * image is used to form the true image height.
	 * </p>
	 * 
	 * @param trueImageHeight
	 *            the height that should be used for calculation of the
	 *            selection
	 */
	public void setTrueImageHeight(int trueHeight) {
		this.trueImageHeight = trueHeight;
	}

	/**
	 * @return the height that should be used for calculation of the selection
	 */
	public int getTrueImageHeight() {
		return (trueImageHeight > 0) ? trueImageHeight : img.getHeight();
	}

	/**
	 * Set the {@link VCropImage} to readOnly mode or back to edit mode.<br>
	 * In readOnly mode, the selection is only shown, but cannot be changed by
	 * the user.
	 * 
	 * @param readOnly
	 *            <i>true</i> for readOnly mode, <i>false</i> for edit mode.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		selectionBoxResizers.setVisible(!readOnly);

		removeStyleName("readOnly");
		if (readOnly) {
			addStyleName("readOnly");
		}
		
		updateSelectionBoxLocation();
	}

	/**
	 * @return <i>true</i> if in readOnly mode, <i>false</i> if in edit mode.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return the width of the scaled image as shown in the browser
	 */
	public int getScaledImageWidth() {
		return img.getWidth();
	}

	/**
	 * @return the height of the scaled image as shown in the browser
	 */
	public int getScaledImageHeight() {
		return img.getHeight();
	}

	/**
	 * Convert a true width value to a scaled width value.
	 * 
	 * @return the scaled width value
	 */
	public int getScaledWidth(int trueWidth) {
		if (getTrueImageWidth() == 0)
			return 0;
		return Math.round(trueWidth
				* (getScaledImageWidth() / (float) getTrueImageWidth()));
	}

	/**
	 * Convert a true height value to a scaled height value.
	 * 
	 * @return the scaled height value
	 */
	public int getScaledHeight(int trueHeight) {
		if (getTrueImageHeight() == 0)
			return 0;
		return Math.round(trueHeight
				* (getScaledImageHeight() / (float) getTrueImageHeight()));
	}

	/**
	 * Convert a scaled width value to a true width value.
	 * 
	 * @return the true width value
	 */
	public int getTrueWidth(int scaledWidth) {
		if (getScaledImageWidth() == 0)
			return 0;
		return Math.round(scaledWidth
				* (getTrueImageWidth() / (float) getScaledImageWidth()));
	}

	/**
	 * Convert a scaled height value to a true height value.
	 * 
	 * @return the true height value
	 */
	public int getTrueHeight(int scaledHeight) {
		if (getScaledImageHeight() == 0)
			return 0;
		return Math.round(scaledHeight
				* (getTrueImageHeight() / (float) getScaledImageHeight()));
	}

	/*
	 * ------------------------------------------------------------------------
	 * contents and visualization
	 * ------------------------------------------------------------------------
	 */

	/**
	 * Create the contents of the {@link VCropImage}. This includes the Image,
	 * the tracking panels and the selection box.
	 */
	protected void createContents() {
		keyHandler = new DragKeyHandler();
		keyHandlerPanel = new FocusPanel();
		add(keyHandlerPanel, -150, 0);
		keyHandlerPanel.addKeyDownHandler(keyHandler);
		keyHandlerPanel.addKeyUpHandler(keyHandler);
		keyHandlerPanel.setWidget(new SimplePanel());

		keyHandlerFocusHandler = new KeyHandlerFocusHandler();

		img = new Image();
		add(img, 0, 0);
		img.addStyleName("full-background-image");
		img.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				updateSizeFromImage();
				imgLoaded = true;
				if (selectionToBeSetAfterLoad != null) {
					setSelection(selectionToBeSetAfterLoad,
							selectionToBeSetAfterLoadUpdateVisibility,
							selectionToBeSetAfterLoadEnforeExtents,
							selectionToBeSetAfterLoadFireEvent,
							selectionToBeSetAfterLoadCancelAnimation);
				}
			}
		});

		tracker = new FocusPanel();
		add(tracker, 0, 0);
		tracker.setStyleName("tracker");

		transformTrackerHandler = new TransformTrackerHandler();
		tracker.addMouseDownHandler(transformTrackerHandler);

		tracker.addFocusHandler(keyHandlerFocusHandler);

		createSelectionBox();

		selectionBoxAnimation = new SelectionBoxAnimation();

		dragMouseHandler = new DragMouseHandler();
	}

	/**
	 * Create the selection box and its contents (mainly visual representation
	 * and resize handles).
	 */
	protected void createSelectionBox() {
		selectionBox = new AbsolutePanel();
		add(selectionBox, 0, 0);
		selectionBox.setStyleName("selection");
		selectionBox.getElement().getStyle().setOverflow(Overflow.VISIBLE);

		selectionBoxContents = new AbsolutePanel();
		selectionBox.add(selectionBoxContents, 0, 0);
		selectionBoxContents.setStyleName("contents");

		selectionBoxImage = new Image();
		selectionBoxContents.add(selectionBoxImage, 0, 0);

		selectionBoxLineNorth = new SimplePanel();
		selectionBoxContents.add(selectionBoxLineNorth);
		selectionBoxLineNorth.setStyleName("h-line");
		selectionBoxLineNorth.addStyleName("north");

		selectionBoxLineSouth = new SimplePanel();
		selectionBoxContents.add(selectionBoxLineSouth);
		selectionBoxLineSouth.setStyleName("h-line");
		selectionBoxLineSouth.addStyleName("south");

		selectionBoxLineWest = new SimplePanel();
		selectionBoxContents.add(selectionBoxLineWest);
		selectionBoxLineWest.setStyleName("v-line");
		selectionBoxLineWest.addStyleName("west");

		selectionBoxLineEast = new SimplePanel();
		selectionBoxContents.add(selectionBoxLineEast);
		selectionBoxLineEast.setStyleName("v-line");
		selectionBoxLineEast.addStyleName("east");

		selectionBoxTracker = new FocusPanel();
		selectionBoxContents.add(selectionBoxTracker, 0, 0);
		selectionBoxTracker.setStyleName("tracker");
		selectionBoxTracker.addStyleName("move");
		moveTrackerHandler = new MoveTrackerHandler();
		selectionBoxTracker.addMouseDownHandler(moveTrackerHandler);
		selectionBoxTracker.addFocusHandler(keyHandlerFocusHandler);

		/* create resize borders and handles */

		selectionBoxResizers = new AbsolutePanel();
		selectionBox.add(selectionBoxResizers, -1, -1);
		selectionBoxResizers.setStyleName("resizers");
		selectionBoxResizers.getElement().getStyle()
				.setOverflow(Overflow.VISIBLE);
		// positioning has to be static, for IE7 compatibility (z-index problems
		// otherwise). this won't affect children positioning as we are 100%
		// size to the parent absolute panel.
		selectionBoxResizers.getElement().getStyle()
				.setPosition(Position.STATIC);

		TransformHandleMouseHandler transformNorthHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_NORTH);
		TransformHandleMouseHandler transformSouthHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_SOUTH);
		TransformHandleMouseHandler transformWestHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_WEST);
		TransformHandleMouseHandler transformEastHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_EAST);
		TransformHandleMouseHandler transformNorthWestHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_NORTHWEST);
		TransformHandleMouseHandler transformNorthEastHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_NORTHEAST);
		TransformHandleMouseHandler transformSouthWestHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_SOUTHWEST);
		TransformHandleMouseHandler transformSouthEastHandler = new TransformHandleMouseHandler(
				DragMode.TRANSFORM_SOUTHEAST);

		selectionBoxResizeBorderNorth = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeBorderNorth);
		selectionBoxResizeBorderNorth.setStyleName("resize-border");
		selectionBoxResizeBorderNorth.addStyleName("north");
		selectionBoxResizeBorderNorth
				.addMouseDownHandler(transformNorthHandler);
		selectionBoxResizeBorderNorth.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeBorderSouth = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeBorderSouth);
		selectionBoxResizeBorderSouth.setStyleName("resize-border");
		selectionBoxResizeBorderSouth.addStyleName("south");
		selectionBoxResizeBorderSouth
				.addMouseDownHandler(transformSouthHandler);
		selectionBoxResizeBorderSouth.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeBorderWest = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeBorderWest);
		selectionBoxResizeBorderWest.setStyleName("resize-border");
		selectionBoxResizeBorderWest.addStyleName("west");
		selectionBoxResizeBorderWest.addMouseDownHandler(transformWestHandler);
		selectionBoxResizeBorderWest.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeBorderEast = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeBorderEast);
		selectionBoxResizeBorderEast.setStyleName("resize-border");
		selectionBoxResizeBorderEast.addStyleName("east");
		selectionBoxResizeBorderEast.addMouseDownHandler(transformEastHandler);
		selectionBoxResizeBorderEast.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleNorth = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleNorth);
		selectionBoxResizeHandleNorth.setStyleName("resize-handle");
		selectionBoxResizeHandleNorth.addStyleName("north");
		selectionBoxResizeHandleNorth
				.addMouseDownHandler(transformNorthHandler);
		selectionBoxResizeHandleNorth.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleSouth = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleSouth);
		selectionBoxResizeHandleSouth.setStyleName("resize-handle");
		selectionBoxResizeHandleSouth.addStyleName("south");
		selectionBoxResizeHandleSouth
				.addMouseDownHandler(transformSouthHandler);
		selectionBoxResizeHandleSouth.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleWest = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleWest);
		selectionBoxResizeHandleWest.setStyleName("resize-handle");
		selectionBoxResizeHandleWest.addStyleName("west");
		selectionBoxResizeHandleWest.addMouseDownHandler(transformWestHandler);
		selectionBoxResizeHandleWest.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleEast = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleEast);
		selectionBoxResizeHandleEast.setStyleName("resize-handle");
		selectionBoxResizeHandleEast.addStyleName("east");
		selectionBoxResizeHandleEast.addMouseDownHandler(transformEastHandler);
		selectionBoxResizeHandleEast.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleSouthWest = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleSouthWest);
		selectionBoxResizeHandleSouthWest.setStyleName("resize-handle");
		selectionBoxResizeHandleSouthWest.addStyleName("southwest");
		selectionBoxResizeHandleSouthWest
				.addMouseDownHandler(transformSouthWestHandler);
		selectionBoxResizeHandleSouthWest
				.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleNorthWest = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleNorthWest);
		selectionBoxResizeHandleNorthWest.setStyleName("resize-handle");
		selectionBoxResizeHandleNorthWest.addStyleName("northwest");
		selectionBoxResizeHandleNorthWest
				.addMouseDownHandler(transformNorthWestHandler);
		selectionBoxResizeHandleNorthWest
				.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleNorthEast = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleNorthEast);
		selectionBoxResizeHandleNorthEast.setStyleName("resize-handle");
		selectionBoxResizeHandleNorthEast.addStyleName("northeast");
		selectionBoxResizeHandleNorthEast
				.addMouseDownHandler(transformNorthEastHandler);
		selectionBoxResizeHandleNorthEast
				.addFocusHandler(keyHandlerFocusHandler);

		selectionBoxResizeHandleSouthEast = new FocusPanel();
		selectionBoxResizers.add(selectionBoxResizeHandleSouthEast);
		selectionBoxResizeHandleSouthEast.setStyleName("resize-handle");
		selectionBoxResizeHandleSouthEast.addStyleName("southeast");
		selectionBoxResizeHandleSouthEast
				.addMouseDownHandler(transformSouthEastHandler);
		selectionBoxResizeHandleSouthEast
				.addFocusHandler(keyHandlerFocusHandler);
	}

	/**
	 * update the selection box visualization according to the current
	 * selection.
	 */
	protected void updateSelectionBoxLocation() {
		// as real image size is used in selection => use scaling here
		setWidgetPosition(selectionBox, getScaledWidth(getSelection()
				.getXTopLeft()), getScaledHeight(getSelection().getYTopLeft()));
		selectionBox.setPixelSize(getScaledWidth(getSelection().getWidth()),
				getScaledHeight(getSelection().getHeight()));

		int imgLeft = -getScaledWidth(getSelection().getXTopLeft());
		int imgTop = -getScaledHeight(getSelection().getYTopLeft());
		selectionBoxContents.setWidgetPosition(selectionBoxImage, imgLeft,
				imgTop);

		// reposition the resize-handles, using the offset size of the box
		int width = selectionBoxResizers.getOffsetWidth();
		int height = selectionBoxResizers.getOffsetHeight();
		float centeringOffset = selectionBoxResizeHandleNorth.getOffsetWidth()
				/ (float) 2;
		int flooredCenteringOffset = (int) Math.floor(centeringOffset);
		int ceiledCenteringOffset = (int) Math.ceil(centeringOffset);
		selectionBoxResizers.setWidgetPosition(selectionBoxResizeHandleNorth,
				width / 2 - flooredCenteringOffset, -flooredCenteringOffset);
		selectionBoxResizers.setWidgetPosition(selectionBoxResizeHandleSouth,
				width / 2 - flooredCenteringOffset, height
						- ceiledCenteringOffset);
		selectionBoxResizers.setWidgetPosition(selectionBoxResizeHandleWest,
				-flooredCenteringOffset, height / 2 - flooredCenteringOffset);
		selectionBoxResizers.setWidgetPosition(selectionBoxResizeHandleEast,
				width - ceiledCenteringOffset, height / 2
						- flooredCenteringOffset);
	}

	/**
	 * Ensure that the current selection conforms to the extent limitations set
	 * on this {@link VCropImage}. Change the selection accordingly if
	 * necessary.
	 */
	protected void reenforceSelectionExtents() {
		VCropSelection selection = new VCropSelection(getSelection());

		if (!selection.isEmpty()) {
			selection.enforceExtents(getMinSelectionWidth(),
					getMinSelectionHeight(), getMaxSelectionWidth(),
					getMaxSelectionHeight(), getTrueImageWidth(),
					getTrueImageHeight(), getSelectionAspectRatio(),
					Length.ANY, Edge.TOP_LEFT);

			if (!selection.equals(getSelection())) {
				setSelection(selection, false);
				fireEvent(new SelectionFinishEvent(getSelection()));
			}
		}
	}

	/*
	 * ------------------------------------------------------------------------
	 * dragging
	 * ------------------------------------------------------------------------
	 */

	/**
	 * Convert a horizontal client coordinate into a coordinate of the tracker
	 * coordinate system.
	 * 
	 * @param clientX
	 *            the client coordinate
	 * @return the corresponding coordinate in the tracker coordinate system
	 */
	protected int getTrackerX(int clientX) {
		return clientX - tracker.getAbsoluteLeft();
	}

	/**
	 * Convert a vertical client coordinate into a coordinate of the tracker
	 * coordinate system.
	 * 
	 * @param clienty
	 *            the client coordinate
	 * @return the corresponding coordinate in the tracker coordinate system
	 */
	protected int getTrackerY(int clientY) {
		return clientY - tracker.getAbsoluteTop();
	}

	/**
	 * @return the horizontal coordinate of the starting point of the drag in
	 *         the tracker coordinate system.
	 */
	protected int getDragStartX() {
		return dragStartX;
	}

	/**
	 * @return the vertical coordinate of the starting point of the drag in the
	 *         tracker coordinate system.
	 */
	protected int getDragStartY() {
		return dragStartY;
	}

	/**
	 * @return the horizontal coordinate of the starting point of the drag in
	 *         the client coordinate system.
	 */
	protected int getDragStartClientX() {
		return dragStartClientX;
	}

	/**
	 * @return the vertical coordinate of the starting point of the drag in the
	 *         client coordinate system.
	 */
	protected int getDragStartClientY() {
		return dragStartClientY;
	}

	/**
	 * Initiate a drag of the given mode at the given starting point.
	 * 
	 * @param mode
	 *            the mode of the drag (<i>MOVE</i> or one of the transform
	 *            directions)
	 * @param startX
	 *            the horizontal starting point coordinate in the tracker
	 *            coordinate system
	 * @param startY
	 *            the vertical starting point coordinate in the tracker
	 *            coordinate system
	 * @param startClientX
	 *            the horizontal starting point coordinate in the client
	 *            coordinate system
	 * @param startClientY
	 *            the vertical starting point coordinate in the client
	 *            coordinate system
	 */
	protected void startDrag(DragMode mode, int startX, int startY,
			int startClientX, int startClientY) {
		setVisibilities(true);
		setDragMode(mode);
		dragStartX = startX;
		dragStartY = startY;
		dragStartClientX = startClientX;
		dragStartClientY = startClientY;

		dragMouseMoveHandlerRegistration = RootPanel.get().addDomHandler(
				dragMouseHandler, MouseMoveEvent.getType());
		dragMouseUpHandlerRegistration = RootPanel.get().addDomHandler(
				dragMouseHandler, MouseUpEvent.getType());
	}

	/**
	 * Complete the previously initiated drag.
	 */
	protected void finishDrag() {
		dragMouseMoveHandlerRegistration.removeHandler();
		dragMouseUpHandlerRegistration.removeHandler();
		setDragMode(null);
		setVisibilities(!selection.isEmpty());

		if (!selectionBoxResizers.isVisible()) {
			selectionBoxResizers.setVisible(true);
			updateSelectionBoxLocation();
		}

		fireEvent(new SelectionFinishEvent(getSelection()));
		keyHandlerPanel.setFocus(true);
	}

	/**
	 * Handle a mouse movement during a drag.
	 * 
	 * @param event
	 *            the {@link MouseMoveEvent} of the mouse movement
	 */
	protected void handleDragMouseMove(MouseMoveEvent event) {
		if (getDragMode() != null) {
			getDragMode().getHandler().handleMouseMove(this,
					event.getClientX(), event.getClientY());
		}
	}

	/**
	 * @return the mode of the currently ongoing drag, or <i>null</i> if no drag
	 *         is currently ongoing
	 */
	protected DragMode getDragMode() {
		return dragMode;
	}

	/**
	 * Change the mode of the current drag to the given value.
	 * 
	 * @param mode
	 *            the new {@link DragMode} (a <i>null</i> value is not allowed)
	 */
	protected void setDragMode(DragMode mode) {
		if (dragMode != null) {
			removeStyleName("drag");
			removeStyleName("drag-" + dragMode.getClassName());
		}

		dragMode = mode;

		if (dragMode != null) {
			addStyleName("drag");
			addStyleName("drag-" + dragMode.getClassName());
		}
	}

	/**
	 * Change the visibility of the selection box.
	 * 
	 * @param selectionVisible
	 *            show the selection if <i>true</i>, hide it if <i>false</i>.
	 */
	protected void setVisibilities(boolean selectionVisible) {
		selectionBox.setVisible(selectionVisible);
		if (selectionVisible) {
			addStyleName("selection-visible");
		} else {
			removeStyleName("selection-visible");
		}
	}

	/*
	 * ------------------------------------------------------------------------
	 * drag modes and handlers
	 * ------------------------------------------------------------------------
	 */

	/**
	 * Enum containing the possible drag modes and their corresponding
	 * {@link DragModeHandler}s.
	 * 
	 * @author Eric Seckler
	 */
	protected enum DragMode {
		MOVE(new MoveDragModeHandler()), //
		TRANSFORM_NORTH(new TransformNorthDragModeHandler()), //
		TRANSFORM_SOUTH(new TransformSouthDragModeHandler()), //
		TRANSFORM_WEST(new TransformWestDragModeHandler()), //
		TRANSFORM_EAST(new TransformEastDragModeHandler()), //
		TRANSFORM_NORTHWEST(new TransformNorthWestDragModeHandler()), //
		TRANSFORM_NORTHEAST(new TransformNorthEastDragModeHandler()), //
		TRANSFORM_SOUTHWEST(new TransformSouthWestDragModeHandler()), //
		TRANSFORM_SOUTHEAST(new TransformSouthEastDragModeHandler());

		private DragModeHandler handler;

		DragMode(DragModeHandler handler) {
			this.handler = handler;
		}

		public String getClassName() {
			return this.name().toLowerCase().replaceAll("_", "-");
		}

		/**
		 * @return the {@link DragModeHandler} corresponding to this
		 *         {@link DragMode}
		 */
		public DragModeHandler getHandler() {
			return handler;
		}
	}

	/**
	 * Interface of a handler associated to a {@link DragMode}.
	 * 
	 * @author Eric Seckler
	 */
	protected interface DragModeHandler {
		/**
		 * Called when a mouse movement has been captured during a drag that is
		 * associated with this {@link DragModeHandler}.
		 * 
		 * @param image
		 *            the {@link VCropImage} that the drag takes place in
		 * @param x
		 *            the horizontal coordinate of the new mouse position (in
		 *            client coordinate system)
		 * @param y
		 *            the vertical coordinate of the new mouse position (in
		 *            client coordinate system)
		 */
		public void handleMouseMove(VCropImage image, int x, int y);
	}

	/**
	 * {@link DragModeHandler} for moving the selection box.
	 * 
	 * @author Eric Seckler
	 */
	protected static class MoveDragModeHandler implements DragModeHandler {
		@Override
		public void handleMouseMove(VCropImage image, int x, int y) {
			VCropSelection selection = new VCropSelection(image.getSelection());

			x = x - image.getDragStartClientX() - image.getDragStartX()
					+ image.getTrackerX(image.getDragStartClientX());
			y = y - image.getDragStartClientY() - image.getDragStartY()
					+ image.getTrackerY(image.getDragStartClientY());

			x = image.getTrueWidth(x);
			y = image.getTrueHeight(y);

			x = Math.min(image.getTrueImageWidth() - selection.getWidth(),
					Math.max(0, x));
			y = Math.min(image.getTrueImageHeight() - selection.getHeight(),
					Math.max(0, y));

			selection.setBottomRight(x + selection.getWidth(),
					y + selection.getHeight());
			selection.setTopLeft(x, y);
			image.setSelection(selection, false, false, true);
		}
	}

	/**
	 * Abstract superclass for all {@link DragModeHandler} for selection extent
	 * transforming {@link DragMode}s.
	 * 
	 * @author Eric Seckler
	 */
	protected static abstract class TransformDragModeHandler implements
			DragModeHandler {
		@Override
		public void handleMouseMove(VCropImage image, int x, int y) {
			VCropSelection selection = new VCropSelection(image.getSelection());

			x = image.getTrueWidth(image.getTrackerX(x));
			y = image.getTrueHeight(image.getTrackerY(y));

			transformSelection(selection, x, y);
			selection.enforceExtents(image.getMinSelectionWidth(),
					image.getMinSelectionHeight(),
					image.getMaxSelectionWidth(),
					image.getMaxSelectionHeight(), image.getTrueImageWidth(),
					image.getTrueImageHeight(),
					image.getSelectionAspectRatio(), getAspectDecisiveLength(),
					getFixedEdge());

			DragMode newMode = getNewTransformMode(selection);
			if (newMode != image.getDragMode()) {
				image.setDragMode(newMode);
			}

			selection.fixOrientation();
			selection.fixPosition(image.getTrueImageWidth(),
					image.getTrueImageHeight());
			image.setSelection(selection, false, false, true);
		}

		/**
		 * Transform the given selection according to the given coordinates of
		 * the new mouse position (in tracker coordinate system).
		 * 
		 * @param selection
		 *            the current selection to be transformed
		 * @param x
		 *            the horizontal mouse coordinate (in tracker coordinate
		 *            system)
		 * @param y
		 *            the vertical mouse coordinate (in tracker coordinate
		 *            system)
		 */
		protected abstract void transformSelection(VCropSelection selection,
				int x, int y);

		/**
		 * If the tranforming changed the selection in a way that the
		 * orientation of the {@link VCropSelection} has to be corrected, the
		 * direction in which the selection will be transformed on the next
		 * mouse movement will most likely change. This method will return the
		 * DragMode that is associated with that new direction.
		 * 
		 * @param selection
		 *            the new {@link VCropSelection} (orientation is not fixed
		 *            yet)
		 * @return the new {@link DragMode} for the new transform direction
		 */
		protected abstract DragMode getNewTransformMode(VCropSelection selection);

		/**
		 * @return the edge of the {@link VCropSelection} that will be fixed
		 *         (remain unchanged) during extent correction
		 */
		protected abstract VCropSelection.Edge getFixedEdge();

		/**
		 * @return the {@link Length} that should be decisive for a possible
		 *         aspect ratio correction
		 */
		protected abstract VCropSelection.Length getAspectDecisiveLength();
	}

	/**
	 * The {@link DragModeHandler} for a transform into north direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformNorthDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setYTopLeft(y);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			return (selection.getHeight() >= 0) ? DragMode.TRANSFORM_NORTH
					: DragMode.TRANSFORM_SOUTH;
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.BOTTOM_LEFT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.HEIGHT;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into south direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformSouthDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setYBottomRight(y);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			return (selection.getHeight() >= 0) ? DragMode.TRANSFORM_SOUTH
					: DragMode.TRANSFORM_NORTH;
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.TOP_LEFT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.HEIGHT;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into west direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformWestDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setXTopLeft(x);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			return (selection.getWidth() >= 0) ? DragMode.TRANSFORM_WEST
					: DragMode.TRANSFORM_EAST;
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.TOP_RIGHT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.WIDTH;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into east direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformEastDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setXBottomRight(x);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			return (selection.getWidth() >= 0) ? DragMode.TRANSFORM_EAST
					: DragMode.TRANSFORM_WEST;
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.TOP_LEFT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.WIDTH;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into northwest direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformNorthWestDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setXTopLeft(x);
			selection.setYTopLeft(y);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			if (selection.getWidth() >= 0) {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_NORTHWEST;
				} else {
					return DragMode.TRANSFORM_SOUTHWEST;
				}
			} else {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_NORTHEAST;
				} else {
					return DragMode.TRANSFORM_SOUTHEAST;
				}
			}
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.BOTTOM_RIGHT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.ANY;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into northeast direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformNorthEastDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setXBottomRight(x);
			selection.setYTopLeft(y);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			if (selection.getWidth() >= 0) {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_NORTHEAST;
				} else {
					return DragMode.TRANSFORM_SOUTHEAST;
				}
			} else {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_NORTHWEST;
				} else {
					return DragMode.TRANSFORM_SOUTHWEST;
				}
			}
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.BOTTOM_LEFT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.ANY;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into southwest direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformSouthWestDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setXTopLeft(x);
			selection.setYBottomRight(y);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			if (selection.getWidth() >= 0) {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_SOUTHWEST;
				} else {
					return DragMode.TRANSFORM_NORTHWEST;
				}
			} else {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_SOUTHEAST;
				} else {
					return DragMode.TRANSFORM_NORTHEAST;
				}
			}
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.TOP_RIGHT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.ANY;
		}
	}

	/**
	 * The {@link DragModeHandler} for a transform into southeast direction.
	 * 
	 * @author Eric Seckler
	 */
	protected static class TransformSouthEastDragModeHandler extends
			TransformDragModeHandler {
		@Override
		protected void transformSelection(VCropSelection selection, int x, int y) {
			selection.setXBottomRight(x);
			selection.setYBottomRight(y);
		}

		@Override
		protected DragMode getNewTransformMode(VCropSelection selection) {
			if (selection.getWidth() >= 0) {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_SOUTHEAST;
				} else {
					return DragMode.TRANSFORM_NORTHEAST;
				}
			} else {
				if (selection.getHeight() >= 0) {
					return DragMode.TRANSFORM_SOUTHWEST;
				} else {
					return DragMode.TRANSFORM_NORTHWEST;
				}
			}
		}

		@Override
		protected Edge getFixedEdge() {
			return Edge.TOP_LEFT;
		}

		@Override
		protected Length getAspectDecisiveLength() {
			return Length.ANY;
		}
	}

	/*
	 * ------------------------------------------------------------------------
	 * event handlers
	 * ------------------------------------------------------------------------
	 */

	/**
	 * A {@link FocusHandler} that redirects the focus to the keyHandlerPanel.
	 * 
	 * @author Eric Seckler
	 */
	protected class KeyHandlerFocusHandler implements FocusHandler {
		@Override
		public void onFocus(FocusEvent event) {
			((Focusable) event.getSource()).setFocus(false);
			keyHandlerPanel.setFocus(true);
		}
	}

	/**
	 * The key handler that handles the movement and transformation of the
	 * selection by using keys. Using the SHIFT key in combination with the
	 * arrow keys will grow the selection, using the CTRL key will shrinken it.
	 * Without modifier keys, the selection can be moved with the arrow keys.
	 * 
	 * @author Eric Seckler
	 */
	protected class DragKeyHandler implements KeyDownHandler, KeyUpHandler {
		boolean hasToFinish = false;

		@Override
		public void onKeyUp(KeyUpEvent event) {
			if (hasToFinish) {
				fireEvent(new SelectionFinishEvent(getSelection()));
				hasToFinish = false;
			}
		}

		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (!readOnly) {
				VCropSelection selection = new VCropSelection(getSelection());

				Edge fixedEdge = Edge.TOP_LEFT;

				if (event.isShiftKeyDown()) {
					/* scaling (additive) */
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_UP:
						selection.setYTopLeft(selection.getYTopLeft() - 1);
						fixedEdge = Edge.BOTTOM_LEFT;
						break;
					case KeyCodes.KEY_DOWN:
						selection
								.setYBottomRight(selection.getYBottomRight() + 1);
						fixedEdge = Edge.TOP_LEFT;
						break;
					case KeyCodes.KEY_LEFT:
						selection.setXTopLeft(selection.getXTopLeft() - 1);
						fixedEdge = Edge.TOP_RIGHT;
						break;
					case KeyCodes.KEY_RIGHT:
						selection
								.setXBottomRight(selection.getXBottomRight() + 1);
						fixedEdge = Edge.TOP_LEFT;
						break;
					}
				} else if (event.isControlKeyDown()) {
					/* scaling (subtractive) */
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_UP:
						selection
								.setYBottomRight(selection.getYBottomRight() - 1);
						fixedEdge = Edge.BOTTOM_LEFT;
						break;
					case KeyCodes.KEY_DOWN:
						selection.setYTopLeft(selection.getYTopLeft() + 1);
						fixedEdge = Edge.TOP_LEFT;
						break;
					case KeyCodes.KEY_LEFT:
						selection
								.setXBottomRight(selection.getXBottomRight() - 1);
						fixedEdge = Edge.TOP_RIGHT;
						break;
					case KeyCodes.KEY_RIGHT:
						selection.setXTopLeft(selection.getXTopLeft() + 1);
						fixedEdge = Edge.TOP_LEFT;
						break;
					}
				} else {
					/* moving */
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_UP:
						selection.move(0, -1);
						break;
					case KeyCodes.KEY_DOWN:
						selection.move(0, 1);
						break;
					case KeyCodes.KEY_LEFT:
						selection.move(-1, 0);
						break;
					case KeyCodes.KEY_RIGHT:
						selection.move(1, 0);
						break;
					}
				}

				selection.enforceExtents(getMinSelectionWidth(),
						getMinSelectionHeight(), getMaxSelectionWidth(),
						getMaxSelectionHeight(), getTrueImageWidth(),
						getTrueImageHeight(), getSelectionAspectRatio(),
						Length.ANY, fixedEdge);
				selection.fixOrientation();
				selection
						.fixPosition(getTrueImageWidth(), getTrueImageHeight());

				setSelection(selection, false);
				hasToFinish = true;
			}
			event.stopPropagation();
			event.preventDefault();
		}
	}

	/**
	 * The {@link MouseDownHandler} for the tracker. Initiates a new drag with
	 * an empty selection on mouseDown.
	 * 
	 * @author Eric Seckler
	 */
	protected class TransformTrackerHandler implements MouseDownHandler {
		@Override
		public void onMouseDown(MouseDownEvent event) {
			if (!readOnly) {
				if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
					selectionBoxResizers.setVisible(false);
					setScaledSelection(
							new VCropSelection(event.getX(), event.getY(),
									event.getX(), event.getY()), false, false,
							true);
					startDrag(DragMode.TRANSFORM_SOUTHEAST, event.getX(),
							event.getY(), event.getClientX(),
							event.getClientY());
					event.preventDefault();
					event.stopPropagation();
				}
			}
		}
	}

	/**
	 * The {@link MouseDownHandler} for the move tracker. Initiates a new move
	 * drag on mouseDown.
	 * 
	 * @author Eric Seckler
	 */
	protected class MoveTrackerHandler implements MouseDownHandler {
		@Override
		public void onMouseDown(MouseDownEvent event) {
			if (!readOnly) {
				if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
					startDrag(DragMode.MOVE, event.getX(), event.getY(),
							event.getClientX(), event.getClientY());
					event.preventDefault();
					event.stopPropagation();
					keyHandlerPanel.setFocus(true);
				}
			}
		}
	}

	/**
	 * The {@link MouseDownHandler} for a transform handle. Initiates a new
	 * transform drag into the corresponding direction on mouseDown.
	 * 
	 * @author Eric Seckler
	 */
	protected class TransformHandleMouseHandler implements MouseDownHandler {
		private DragMode mode;

		public TransformHandleMouseHandler(DragMode mode) {
			this.mode = mode;
		}

		@Override
		public void onMouseDown(MouseDownEvent event) {
			if (!readOnly) {
				if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
					startDrag(mode, 0, 0, event.getClientX(),
							event.getClientY());
					event.preventDefault();
					event.stopPropagation();
				}
			}
		}
	}

	/**
	 * The global drag mouse handler (will be attached to the window). Will pass
	 * on mouseMove events to the
	 * {@link VCropImage#handleDragMouseMove(MouseMoveEvent)} method and finish
	 * the drag on mouseUp.
	 * 
	 * @author Eric Seckler
	 * 
	 */
	protected class DragMouseHandler implements MouseMoveHandler,
			MouseUpHandler {
		@Override
		public void onMouseMove(MouseMoveEvent event) {
			handleDragMouseMove(event);
			event.preventDefault();
			event.stopPropagation();
		}

		@Override
		public void onMouseUp(MouseUpEvent event) {
			finishDrag();
			event.preventDefault();
			event.stopPropagation();
		}
	}

	/*
	 * ------------------------------------------------------------------------
	 * own event types and handlers
	 * ------------------------------------------------------------------------
	 */

	/**
	 * An {@link EventHandler} for {@link SelectionFinishEvent}s.
	 * 
	 * @author Eric Seckler
	 */
	public static interface SelectionFinishHandler extends EventHandler {
		/**
		 * Called when the selection change has been completed.
		 * 
		 * @param event
		 *            the {@link SelectionFinishEvent} that was fired
		 */
		void onSelectionFinished(SelectionFinishEvent event);
	}

	/**
	 * An {@link EventHandler} for {@link SelectionChangeEvent}s.
	 * 
	 * @author Eric Seckler
	 */
	public static interface SelectionChangeHandler extends EventHandler {
		/**
		 * Called when the selection changes, e.g. during a transform drag.
		 * 
		 * @param event
		 *            the {@link SelectionChangeEvent} that was fired
		 */
		void onSelectionChanged(SelectionChangeEvent event);
	}

	/**
	 * The abstract superclass for SelectionEvents.
	 * 
	 * @author Eric Seckler
	 */
	public static abstract class SelectionEvent<H extends EventHandler> extends
			GwtEvent<H> {
		private VCropSelection selection;

		public SelectionEvent(VCropSelection selection) {
			this.selection = selection;
		}

		/**
		 * @return the new {@link VCropSelection} of the {@link VCropImage}
		 */
		public VCropSelection getSelection() {
			return selection;
		}

		/**
		 * Set the new selection
		 * 
		 * @param selection
		 *            the new {@link VCropSelection} of the {@link VCropImage}
		 */
		public void setSelection(VCropSelection selection) {
			this.selection = selection;
		}
	}

	/**
	 * Events of this type will be sent whenever a selection process completes.
	 * 
	 * @author Eric Seckler
	 */
	public static class SelectionFinishEvent extends
			SelectionEvent<SelectionFinishHandler> {
		/**
		 * Event type for selection finish events. Represents the meta-data
		 * associated with this event.
		 */
		private static final Type<SelectionFinishHandler> TYPE = new Type<SelectionFinishHandler>();

		public SelectionFinishEvent(VCropSelection selection) {
			super(selection);
		}

		@Override
		public Type<SelectionFinishHandler> getAssociatedType() {
			return TYPE;
		}

		public static Type<SelectionFinishHandler> getType() {
			return TYPE;
		}

		@Override
		protected void dispatch(SelectionFinishHandler handler) {
			handler.onSelectionFinished(this);
		}
	}

	/**
	 * Events of this type will be sent whenever the selection changes during a
	 * selection process (i.e. a drag).
	 * 
	 * @author Eric Seckler
	 */
	public static class SelectionChangeEvent extends
			SelectionEvent<SelectionChangeHandler> {
		/**
		 * Event type for selection change events. Represents the meta-data
		 * associated with this event.
		 */
		private static final Type<SelectionChangeHandler> TYPE = new Type<SelectionChangeHandler>();

		public SelectionChangeEvent(VCropSelection selection) {
			super(selection);
		}

		@Override
		public Type<SelectionChangeHandler> getAssociatedType() {
			return TYPE;
		}

		public static Type<SelectionChangeHandler> getType() {
			return TYPE;
		}

		@Override
		protected void dispatch(SelectionChangeHandler handler) {
			handler.onSelectionChanged(this);
		}
	}

	/**
	 * Add a {@link SelectionChangeHandler}.
	 * 
	 * @param handler
	 *            the handler
	 * @return the handler registration
	 */
	public HandlerRegistration addSelectionChangeHandler(
			SelectionChangeHandler handler) {
		return addHandler(handler, SelectionChangeEvent.getType());
	}

	/**
	 * Add a {@link SelectionFinishHandler}.
	 * 
	 * @param handler
	 *            the handler
	 * @return the handler registration
	 */
	public HandlerRegistration addSelectionFinishHandler(
			SelectionFinishHandler handler) {
		return addHandler(handler, SelectionFinishEvent.getType());
	}

	@Override
	public HandlerRegistration addResizeHandler(ResizeHandler handler) {
		return addHandler(handler, ResizeEvent.getType());
	}

	/*
	 * ------------------------------------------------------------------------
	 * animating
	 * ------------------------------------------------------------------------
	 */

	/**
	 * The {@link Animation} class that handles a selection animation.
	 * 
	 * @author Eric Seckler
	 */
	protected class SelectionBoxAnimation extends Animation {
		protected VCropSelection initialSelection;
		protected VCropSelection finalSelection;

		/**
		 * Animate the current selection to transform into the given final
		 * selection.
		 * 
		 * @param finalSelection
		 *            the final {@link VCropSelection}
		 * @param duration
		 *            the duration of the animation
		 */
		public void animateTo(VCropSelection finalSelection, int duration) {
			this.initialSelection = new VCropSelection(getSelection());
			this.finalSelection = new VCropSelection(finalSelection);

			this.finalSelection.enforceExtents(getMinSelectionWidth(),
					getMinSelectionHeight(), getMaxSelectionWidth(),
					getMaxSelectionHeight(), getTrueImageWidth(),
					getTrueImageHeight(), getSelectionAspectRatio(),
					Length.ANY, Edge.TOP_LEFT);

			run(duration);
		}

		@Override
		protected void onUpdate(double progress) {
			VCropSelection interpolatedSelection = new VCropSelection();
			interpolatedSelection.setXTopLeft((int) (initialSelection
					.getXTopLeft() + Math.round(progress
					* (finalSelection.getXTopLeft() - initialSelection
							.getXTopLeft()))));
			interpolatedSelection.setYTopLeft((int) (initialSelection
					.getYTopLeft() + Math.round(progress
					* (finalSelection.getYTopLeft() - initialSelection
							.getYTopLeft()))));
			interpolatedSelection.setXBottomRight((int) (initialSelection
					.getXBottomRight() + Math.round(progress
					* (finalSelection.getXBottomRight() - initialSelection
							.getXBottomRight()))));
			interpolatedSelection.setYBottomRight((int) (initialSelection
					.getYBottomRight() + Math.round(progress
					* (finalSelection.getYBottomRight() - initialSelection
							.getYBottomRight()))));
			setSelection(interpolatedSelection, false, false, false, false);
		}

		@Override
		protected void onComplete() {
			super.onComplete();
			setSelection(finalSelection, true, false, true, false);
			fireEvent(new SelectionFinishEvent(finalSelection));
		}

	}

	/**
	 * Animate the current selection to the given new selection with a default
	 * animation duration.
	 * 
	 * @param selection
	 *            the final selection after the animation
	 */
	public void animateTo(VCropSelection selection) {
		animateTo(selection, DEFAULT_ANIMATION_DURATION);
	}

	/**
	 * Animate the current selection to the given new selection with a default
	 * animation duration.
	 * 
	 * @param selection
	 *            the final selection after the animation
	 */
	public void animateTo(VCropSelection selection, int duration) {
		setVisibilities(true);
		selectionBoxAnimation.animateTo(selection, duration);
	}
}
